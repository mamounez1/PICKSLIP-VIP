package com.example.data.repository

import com.example.data.algorithm.StatisticalAlgorithm
import com.example.data.local.PickSlipDatabase
import com.example.data.model.DailyStatsEntity
import com.example.data.model.FixtureEntity
import com.example.data.model.PredictionEntity
import com.example.data.model.PredictionWithFixture
import com.example.data.model.SubscriberEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

class PickSlipRepository(private val db: PickSlipDatabase) {

    private val subscriberDao = db.subscriberDao()
    private val fixtureDao = db.fixtureDao()
    private val predictionDao = db.predictionDao()
    private val dailyStatsDao = db.dailyStatsDao()

    val currentSubscriber: Flow<SubscriberEntity?> = subscriberDao.getCurrentSubscriber()

    val isVipActive: Flow<Boolean> = currentSubscriber.map { sub ->
        sub != null && sub.subscriptionStatus == "active" && (sub.currentPeriodEnd == 0L || sub.currentPeriodEnd > System.currentTimeMillis())
    }

    val allPredictions: Flow<List<PredictionWithFixture>> = predictionDao.getAllPredictionsRaw().map { rawList ->
        rawList.map { it.toDomain() }
    }

    val dailyStats: Flow<List<DailyStatsEntity>> = dailyStatsDao.getAllDailyStats()

    /**
     * Initializes default starter data if empty or refreshes today's roster.
     */
    suspend fun initializeStarterDataIfNeeded() = withContext(Dispatchers.IO) {
        val existing = predictionDao.getAllPredictionsDirect()
        val pending = predictionDao.getPendingPredictions()
        if (existing.isEmpty()) {
            seedSampleTrackRecordAndFixtures()
        } else if (pending.size < 6 || pending.count { it.isFreePick } < 3) {
            // Re-generate today's matches to ensure 3 free + VIP full matchday lineup
            generateDailyPredictionsCron()
        }
    }

    /**
     * Cron Simulation #1: Generate Daily Predictions (Scheduled daily 06:00).
     * Pulls upcoming match fixtures for tracked leagues, applies deterministic algorithm,
     * stores in Room DB. First 3 picks are marked as isFreePick = true (open), rest VIP.
     */
    suspend fun generateDailyPredictionsCron(): List<PredictionEntity> = withContext(Dispatchers.IO) {
        val newFixtures = generateFreshFixtures()
        fixtureDao.insertFixtures(newFixtures)

        val newPredictions = mutableListOf<PredictionEntity>()
        newFixtures.forEachIndexed { index, fixture ->
            val algoResult = StatisticalAlgorithm.calculatePrediction(fixture)
            val isFree = (index < 3) // Top 3 picks are 100% FREE (MAFTHOUHIN), rest require VIP
            val prediction = PredictionEntity(
                id = UUID.randomUUID().toString(),
                fixtureId = fixture.id,
                pick = algoResult.pick,
                pickType = algoResult.pickType,
                confidence = algoResult.confidence,
                reasoning = algoResult.reasoning,
                isFreePick = isFree,
                result = "pending",
                oddsEst = algoResult.estimatedOdds,
                createdAt = System.currentTimeMillis() + index
            )
            newPredictions.add(prediction)
        }

        predictionDao.insertPredictions(newPredictions)
        return@withContext newPredictions
    }

    /**
     * Cron Simulation #2: Check Results (Scheduled daily 23:00).
     * Resolves pending predictions against match scores, sets WIN/LOSS, and updates daily aggregates.
     */
    suspend fun checkResultsCron(): Int = withContext(Dispatchers.IO) {
        val pendingPredictions = predictionDao.getPendingPredictions()
        if (pendingPredictions.isEmpty()) return@withContext 0

        var resolvedCount = 0
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        pendingPredictions.forEach { pred ->
            val fixture = fixtureDao.getFixtureById(pred.fixtureId)
            if (fixture != null) {
                // Generate realistic final score based on xG & stats if score not present
                val homeScore = fixture.homeScore ?: generateSimulatedScore(fixture.homeGoalsForAvg, fixture.homeGoalsAgainstAvg)
                val awayScore = fixture.awayScore ?: generateSimulatedScore(fixture.awayGoalsForAvg, fixture.awayGoalsAgainstAvg)

                // Update fixture status
                fixtureDao.updateFixtureResult(
                    id = fixture.id,
                    status = "finished",
                    homeScore = homeScore,
                    awayScore = awayScore
                )

                // Evaluate prediction result using algorithm rules
                val outcome = StatisticalAlgorithm.evaluateResult(pred.pickType, homeScore, awayScore)
                predictionDao.updatePredictionResult(pred.id, outcome)
                resolvedCount++
            }
        }

        // Refresh stats_daily table aggregate
        refreshDailyStatsAggregate(todayStr)
        return@withContext resolvedCount
    }

    private suspend fun refreshDailyStatsAggregate(todayDate: String) {
        val allResolved = predictionDao.getResolvedPredictions()
        val totalCount = allResolved.size
        val totalWins = allResolved.count { it.result == "win" }
        val totalLosses = allResolved.count { it.result == "loss" }
        val winRate = if (totalCount > 0) ((totalWins.toDouble() / totalCount) * 100.0) else 0.0

        val todayStats = DailyStatsEntity(
            date = todayDate,
            picksCount = totalCount,
            wins = totalWins,
            losses = totalLosses,
            winRate = (winRate * 10.0).roundToInt() / 10.0
        )
        dailyStatsDao.insertOrUpdateDailyStats(todayStats)
    }

    /**
     * Handles VIP Subscription checkout / activation.
     */
    suspend fun activateVipSubscription(email: String): SubscriberEntity = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000
        val periodEnd = System.currentTimeMillis() + thirtyDaysMillis

        val subscriber = SubscriberEntity(
            userId = "usr_" + UUID.randomUUID().toString().take(8),
            email = cleanEmail,
            stripeCustomerId = "cus_" + UUID.randomUUID().toString().take(10),
            stripeSubscriptionId = "sub_" + UUID.randomUUID().toString().take(12),
            subscriptionStatus = "active",
            currentPeriodEnd = periodEnd,
            createdAt = System.currentTimeMillis()
        )
        subscriberDao.insertOrUpdate(subscriber)
        return@withContext subscriber
    }

    suspend fun cancelSubscription(email: String) = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        subscriberDao.updateStatus(cleanEmail, "canceled", System.currentTimeMillis())
    }

    suspend fun verifySubscriberEmail(email: String): Boolean = withContext(Dispatchers.IO) {
        val sub = subscriberDao.getSubscriberByEmail(email.trim().lowercase())
        return@withContext sub != null && sub.subscriptionStatus == "active"
    }

    private fun generateSimulatedScore(goalsForAvg: Double, goalsAgainstAvg: Double): Int {
        val expected = (goalsForAvg * 0.6 + goalsAgainstAvg * 0.4)
        val rand = Random.nextDouble()
        return when {
            rand < 0.25 -> (expected.toInt()).coerceAtLeast(0)
            rand < 0.65 -> (expected.roundToInt()).coerceAtLeast(0)
            rand < 0.88 -> ((expected + 1.0).roundToInt()).coerceAtLeast(1)
            else -> ((expected + 2.0).roundToInt()).coerceAtLeast(2)
        }
    }

    private fun generateFreshFixtures(): List<FixtureEntity> {
        val baseId = System.currentTimeMillis()
        return listOf(
            // MATCH 1 (FREE 1): Premier League Clash
            FixtureEntity(
                id = baseId + 1,
                league = "Premier League",
                leagueCountry = "Angleterre",
                homeTeam = "Arsenal",
                awayTeam = "Chelsea",
                kickoff = "Aujourd'hui, 17:30",
                homeTeamId = 42,
                awayTeamId = 49,
                status = "scheduled",
                homeRecentForm = "WWWDW",
                awayRecentForm = "WLDWD",
                homeGoalsForAvg = 2.4,
                homeGoalsAgainstAvg = 0.8,
                awayGoalsForAvg = 1.6,
                awayGoalsAgainstAvg = 1.3,
                h2hBttsRate = 0.70,
                h2hOver25Rate = 0.65
            ),
            // MATCH 2 (FREE 2): La Liga Big Match
            FixtureEntity(
                id = baseId + 2,
                league = "La Liga",
                leagueCountry = "Espagne",
                homeTeam = "Real Madrid",
                awayTeam = "Sevilla FC",
                kickoff = "Aujourd'hui, 19:00",
                homeTeamId = 541,
                awayTeamId = 536,
                status = "scheduled",
                homeRecentForm = "WWWWW",
                awayRecentForm = "LLDWL",
                homeGoalsForAvg = 2.6,
                homeGoalsAgainstAvg = 0.7,
                awayGoalsForAvg = 1.0,
                awayGoalsAgainstAvg = 1.8,
                h2hBttsRate = 0.50,
                h2hOver25Rate = 0.75
            ),
            // MATCH 3 (FREE 3): Ligue 1 Le Classique
            FixtureEntity(
                id = baseId + 3,
                league = "Ligue 1",
                leagueCountry = "France",
                homeTeam = "Paris SG",
                awayTeam = "Marseille",
                kickoff = "Aujourd'hui, 20:45",
                homeTeamId = 85,
                awayTeamId = 81,
                status = "scheduled",
                homeRecentForm = "WWWDW",
                awayRecentForm = "WDWWL",
                homeGoalsForAvg = 2.7,
                homeGoalsAgainstAvg = 0.9,
                awayGoalsForAvg = 1.8,
                awayGoalsAgainstAvg = 1.2,
                h2hBttsRate = 0.80,
                h2hOver25Rate = 0.70
            ),
            // MATCH 4 (VIP 1): Botola Pro Classico Marocain
            FixtureEntity(
                id = baseId + 4,
                league = "Botola Pro",
                leagueCountry = "Maroc",
                homeTeam = "Raja Casablanca",
                awayTeam = "FAR Rabat",
                kickoff = "Aujourd'hui, 20:00",
                homeTeamId = 1101,
                awayTeamId = 1102,
                status = "scheduled",
                homeRecentForm = "WWWDW",
                awayRecentForm = "WWDLW",
                homeGoalsForAvg = 1.9,
                homeGoalsAgainstAvg = 0.6,
                awayGoalsForAvg = 1.7,
                awayGoalsAgainstAvg = 0.8,
                h2hBttsRate = 0.60,
                h2hOver25Rate = 0.50
            ),
            // MATCH 5 (VIP 2): Serie A Derby d'Italia
            FixtureEntity(
                id = baseId + 5,
                league = "Serie A",
                leagueCountry = "Italie",
                homeTeam = "Inter Milan",
                awayTeam = "Juventus",
                kickoff = "Aujourd'hui, 20:45",
                homeTeamId = 505,
                awayTeamId = 496,
                status = "scheduled",
                homeRecentForm = "WWDLW",
                awayRecentForm = "DDWWD",
                homeGoalsForAvg = 2.0,
                homeGoalsAgainstAvg = 0.8,
                awayGoalsForAvg = 1.3,
                awayGoalsAgainstAvg = 0.7,
                h2hBttsRate = 0.40,
                h2hOver25Rate = 0.35
            ),
            // MATCH 6 (VIP 3): Bundesliga Der Klassiker
            FixtureEntity(
                id = baseId + 6,
                league = "Bundesliga",
                leagueCountry = "Allemagne",
                homeTeam = "Bayern Munich",
                awayTeam = "Borussia Dortmund",
                kickoff = "Aujourd'hui, 18:30",
                homeTeamId = 157,
                awayTeamId = 165,
                status = "scheduled",
                homeRecentForm = "WWLWW",
                awayRecentForm = "LWWDL",
                homeGoalsForAvg = 3.1,
                homeGoalsAgainstAvg = 1.1,
                awayGoalsForAvg = 2.0,
                awayGoalsAgainstAvg = 1.7,
                h2hBttsRate = 0.85,
                h2hOver25Rate = 0.90
            ),
            // MATCH 7 (VIP 4): UEFA Champions League
            FixtureEntity(
                id = baseId + 7,
                league = "UEFA Champions League",
                leagueCountry = "Europe",
                homeTeam = "Manchester City",
                awayTeam = "FC Barcelona",
                kickoff = "Aujourd'hui, 21:00",
                homeTeamId = 50,
                awayTeamId = 529,
                status = "scheduled",
                homeRecentForm = "WWWDW",
                awayRecentForm = "WWWWL",
                homeGoalsForAvg = 2.8,
                homeGoalsAgainstAvg = 1.0,
                awayGoalsForAvg = 2.5,
                awayGoalsAgainstAvg = 1.2,
                h2hBttsRate = 0.80,
                h2hOver25Rate = 0.75
            ),
            // MATCH 8 (VIP 5): Botola Pro Maroc Choc
            FixtureEntity(
                id = baseId + 8,
                league = "Botola Pro",
                leagueCountry = "Maroc",
                homeTeam = "Wydad Casablanca",
                awayTeam = "RS Berkane",
                kickoff = "Aujourd'hui, 18:00",
                homeTeamId = 1103,
                awayTeamId = 1104,
                status = "scheduled",
                homeRecentForm = "WDWWW",
                awayRecentForm = "WWLDD",
                homeGoalsForAvg = 1.8,
                homeGoalsAgainstAvg = 0.7,
                awayGoalsForAvg = 1.5,
                awayGoalsAgainstAvg = 0.9,
                h2hBttsRate = 0.55,
                h2hOver25Rate = 0.45
            ),
            // MATCH 9 (VIP 6): Premier League Top 4
            FixtureEntity(
                id = baseId + 9,
                league = "Premier League",
                leagueCountry = "Angleterre",
                homeTeam = "Liverpool",
                awayTeam = "Tottenham",
                kickoff = "Aujourd'hui, 15:00",
                homeTeamId = 40,
                awayTeamId = 47,
                status = "scheduled",
                homeRecentForm = "WWWWW",
                awayRecentForm = "WLDWL",
                homeGoalsForAvg = 2.5,
                homeGoalsAgainstAvg = 0.9,
                awayGoalsForAvg = 1.9,
                awayGoalsAgainstAvg = 1.6,
                h2hBttsRate = 0.85,
                h2hOver25Rate = 0.80
            ),
            // MATCH 10 (VIP 7): Serie A Big Clash
            FixtureEntity(
                id = baseId + 10,
                league = "Serie A",
                leagueCountry = "Italie",
                homeTeam = "Napoli",
                awayTeam = "AC Milan",
                kickoff = "Aujourd'hui, 20:45",
                homeTeamId = 492,
                awayTeamId = 489,
                status = "scheduled",
                homeRecentForm = "WWLWD",
                awayRecentForm = "LWDWW",
                homeGoalsForAvg = 2.1,
                homeGoalsAgainstAvg = 1.0,
                awayGoalsForAvg = 1.8,
                awayGoalsAgainstAvg = 1.3,
                h2hBttsRate = 0.65,
                h2hOver25Rate = 0.60
            )
        )
    }

    private suspend fun seedSampleTrackRecordAndFixtures() {
        // Seed historical finished fixtures & predictions to provide instant track record on dashboard
        val historicalFixtures = listOf(
            FixtureEntity(
                id = 1001L,
                league = "Premier League",
                homeTeam = "Liverpool",
                awayTeam = "Aston Villa",
                kickoff = "Hier, 17:30",
                homeTeamId = 40,
                awayTeamId = 66,
                status = "finished",
                homeScore = 2,
                awayScore = 0,
                homeRecentForm = "WWWWW",
                awayRecentForm = "LLDWD",
                homeGoalsForAvg = 2.3,
                homeGoalsAgainstAvg = 0.7,
                awayGoalsForAvg = 1.2,
                awayGoalsAgainstAvg = 1.6,
                h2hBttsRate = 0.50,
                h2hOver25Rate = 0.60
            ),
            FixtureEntity(
                id = 1002L,
                league = "La Liga",
                homeTeam = "Atlético Madrid",
                awayTeam = "Athletic Bilbao",
                kickoff = "Hier, 21:00",
                homeTeamId = 530,
                awayTeamId = 531,
                status = "finished",
                homeScore = 1,
                awayScore = 0,
                homeRecentForm = "WDDWW",
                awayRecentForm = "DWWDL",
                homeGoalsForAvg = 1.5,
                homeGoalsAgainstAvg = 0.6,
                awayGoalsForAvg = 1.1,
                awayGoalsAgainstAvg = 0.9,
                h2hBttsRate = 0.30,
                h2hOver25Rate = 0.20
            ),
            FixtureEntity(
                id = 1003L,
                league = "Ligue 1",
                homeTeam = "Monaco",
                awayTeam = "Lille",
                kickoff = "Avant-hier, 21:00",
                homeTeamId = 91,
                awayTeamId = 79,
                status = "finished",
                homeScore = 2,
                awayScore = 1,
                homeRecentForm = "WWDLW",
                awayRecentForm = "WWDWD",
                homeGoalsForAvg = 2.1,
                homeGoalsAgainstAvg = 1.2,
                awayGoalsForAvg = 1.7,
                awayGoalsAgainstAvg = 1.0,
                h2hBttsRate = 0.75,
                h2hOver25Rate = 0.65
            ),
            FixtureEntity(
                id = 1004L,
                league = "Bundesliga",
                homeTeam = "Bayer Leverkusen",
                awayTeam = "RB Leipzig",
                kickoff = "26 Août, 15:30",
                homeTeamId = 168,
                awayTeamId = 173,
                status = "finished",
                homeScore = 3,
                awayScore = 2,
                homeRecentForm = "WWWWW",
                awayRecentForm = "WWLDW",
                homeGoalsForAvg = 2.8,
                homeGoalsAgainstAvg = 1.0,
                awayGoalsForAvg = 2.2,
                awayGoalsAgainstAvg = 1.4,
                h2hBttsRate = 0.85,
                h2hOver25Rate = 0.80
            ),
            FixtureEntity(
                id = 1005L,
                league = "Serie A",
                homeTeam = "Napoli",
                awayTeam = "Atalanta",
                kickoff = "25 Août, 20:45",
                homeTeamId = 492,
                awayTeamId = 499,
                status = "finished",
                homeScore = 1,
                awayScore = 2,
                homeRecentForm = "WLDWW",
                awayRecentForm = "WWWWL",
                homeGoalsForAvg = 1.8,
                homeGoalsAgainstAvg = 1.1,
                awayGoalsForAvg = 2.1,
                awayGoalsAgainstAvg = 1.2,
                h2hBttsRate = 0.70,
                h2hOver25Rate = 0.70
            )
        )

        fixtureDao.insertFixtures(historicalFixtures)

        val historicalPredictions = listOf(
            PredictionEntity(
                id = "pred_hist_1",
                fixtureId = 1001L,
                pick = "Victoire Liverpool",
                pickType = "HOME_WIN",
                confidence = 88.4,
                reasoning = "Liverpool domine à Anfield (série de 5 victoires) avec un net différentiel xG favorable (+1.6).",
                isFreePick = true,
                result = "win",
                oddsEst = 1.48,
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            PredictionEntity(
                id = "pred_hist_2",
                fixtureId = 1002L,
                pick = "Moins de 2.5 Buts",
                pickType = "UNDER_25",
                confidence = 84.2,
                reasoning = "Blocs défensifs très étanches (xG total estimé à 1.9) avec 80% de scores sous 2.5 buts en H2H.",
                isFreePick = false,
                result = "win",
                oddsEst = 1.70,
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            PredictionEntity(
                id = "pred_hist_3",
                fixtureId = 1003L,
                pick = "Les 2 équipes marquent (BTTS)",
                pickType = "BTTS_YES",
                confidence = 81.6,
                reasoning = "Potentiel offensif des deux côtés (75% BTTS historique et minimum 1.5 but attendu par match).",
                isFreePick = false,
                result = "win",
                oddsEst = 1.75,
                createdAt = System.currentTimeMillis() - 172800000L
            ),
            PredictionEntity(
                id = "pred_hist_4",
                fixtureId = 1004L,
                pick = "Plus de 2.5 Buts",
                pickType = "OVER_25",
                confidence = 89.5,
                reasoning = "xG total combiné projeté à 3.4 buts et 85% des confrontations directes récentes dépassant 2.5 buts.",
                isFreePick = false,
                result = "win",
                oddsEst = 1.58,
                createdAt = System.currentTimeMillis() - 259200000L
            ),
            PredictionEntity(
                id = "pred_hist_5",
                fixtureId = 1005L,
                pick = "Double Chance Napoli ou Nul (1X)",
                pickType = "DC_1X",
                confidence = 74.0,
                reasoning = "Couverture statistique avec avantage du terrain pour Napoli selon la moyenne de possession.",
                isFreePick = false,
                result = "loss",
                oddsEst = 1.34,
                createdAt = System.currentTimeMillis() - 345600000L
            )
        )

        predictionDao.insertPredictions(historicalPredictions)

        // Historical daily statistics
        val dailyStatsList = listOf(
            DailyStatsEntity(date = "2026-08-29", picksCount = 2, wins = 2, losses = 0, winRate = 100.0),
            DailyStatsEntity(date = "2026-08-28", picksCount = 1, wins = 1, losses = 0, winRate = 100.0),
            DailyStatsEntity(date = "2026-08-26", picksCount = 1, wins = 1, losses = 0, winRate = 100.0),
            DailyStatsEntity(date = "2026-08-25", picksCount = 1, wins = 0, losses = 1, winRate = 0.0)
        )
        dailyStatsDao.insertAll(dailyStatsList)

        // Also generate current day's live fixtures & predictions
        generateDailyPredictionsCron()
    }
}
