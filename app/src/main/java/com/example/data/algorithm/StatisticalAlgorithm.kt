package com.example.data.algorithm

import com.example.data.model.AlgorithmMetrics
import com.example.data.model.AlgorithmOutput
import com.example.data.model.FixtureEntity
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Deterministic Statistical Algorithm for PickSlip.
 * Pure deterministic mathematics and decision-tree logic (Zero AI / LLM).
 */
object StatisticalAlgorithm {

    data class CandidatePick(
        val pick: String,
        val pickType: String,
        val confidence: Double,
        val reasoning: String,
        val estimatedOdds: Double
    )

    fun calculatePrediction(fixture: FixtureEntity): AlgorithmOutput {
        return evaluate(
            homeTeam = fixture.homeTeam,
            awayTeam = fixture.awayTeam,
            homeForm = fixture.homeRecentForm,
            awayForm = fixture.awayRecentForm,
            homeGoalsForAvg = fixture.homeGoalsForAvg,
            homeGoalsAgainstAvg = fixture.homeGoalsAgainstAvg,
            awayGoalsForAvg = fixture.awayGoalsForAvg,
            awayGoalsAgainstAvg = fixture.awayGoalsAgainstAvg,
            h2hBttsRate = fixture.h2hBttsRate,
            h2hOver25Rate = fixture.h2hOver25Rate
        )
    }

    fun evaluate(
        homeTeam: String,
        awayTeam: String,
        homeForm: String, // e.g. "WWDLW"
        awayForm: String, // e.g. "WLDWD"
        homeGoalsForAvg: Double,
        homeGoalsAgainstAvg: Double,
        awayGoalsForAvg: Double,
        awayGoalsAgainstAvg: Double,
        h2hBttsRate: Double,
        h2hOver25Rate: Double
    ): AlgorithmOutput {
        // 1. Calculate Form Index (Points out of 15 max)
        val homeFormPoints = calculateFormPoints(homeForm)
        val awayFormPoints = calculateFormPoints(awayForm)
        val homeFormIndex = homeFormPoints / 15.0 // 0.0 to 1.0
        val awayFormIndex = awayFormPoints / 15.0 // 0.0 to 1.0
        val formGap = homeFormIndex - awayFormIndex // -1.0 to +1.0

        // 2. Calculate Expected Goals (xG) using attacking & defensive strength + Home Advantage
        val homeAdvantageFactor = 1.12
        val xGHome = max(0.4, (homeGoalsForAvg * 0.65 + awayGoalsAgainstAvg * 0.35) * homeAdvantageFactor)
        val xGAway = max(0.3, (awayGoalsForAvg * 0.55 + homeGoalsAgainstAvg * 0.45) * 0.92)
        val totalExpectedGoals = xGHome + xGAway

        // 3. Probabilities based on metrics
        val bttsProbability = min(0.95, max(0.30, (h2hBttsRate * 0.45) + ((xGHome * xGAway) / 2.6) * 0.55))
        val over25Probability = min(0.95, max(0.25, (h2hOver25Rate * 0.40) + ((totalExpectedGoals - 1.5) / 2.0) * 0.60))

        val metrics = AlgorithmMetrics(
            expectedGoalsHome = (xGHome * 100).roundToInt() / 100.0,
            expectedGoalsAway = (xGAway * 100).roundToInt() / 100.0,
            totalExpectedGoals = (totalExpectedGoals * 100).roundToInt() / 100.0,
            homeFormIndex = (homeFormIndex * 100).roundToInt() / 100.0,
            awayFormIndex = (awayFormIndex * 100).roundToInt() / 100.0,
            formGap = (formGap * 100).roundToInt() / 100.0,
            bttsProbability = (bttsProbability * 100).roundToInt() / 100.0,
            over25Probability = (over25Probability * 100).roundToInt() / 100.0
        )

        // 4. Decision-tree candidate generation
        val candidates = mutableListOf<CandidatePick>()

        // Candidate A: Over 2.5 Buts
        if (totalExpectedGoals >= 2.60 || (h2hOver25Rate >= 0.65 && totalExpectedGoals >= 2.30)) {
            val conf = clampConfidence(56.0 + (totalExpectedGoals - 2.5) * 22.0 + (h2hOver25Rate * 18.0))
            val xGFormatted = String.format("%.1f", totalExpectedGoals)
            val h2hPct = (h2hOver25Rate * 100).roundToInt()
            candidates.add(
                CandidatePick(
                    pick = "Plus de 2.5 Buts",
                    pickType = "OVER_25",
                    confidence = conf,
                    reasoning = "xG total combiné projeté à $xGFormatted buts et $h2hPct% des confrontations directes récentes dépassant 2.5 buts.",
                    estimatedOdds = roundOdds(1.60 + (95.0 - conf) * 0.015)
                )
            )
        }

        // Candidate B: Moins de 2.5 Buts
        if (totalExpectedGoals <= 2.15 || (h2hOver25Rate <= 0.35 && totalExpectedGoals <= 2.45)) {
            val conf = clampConfidence(58.0 + (2.5 - totalExpectedGoals) * 24.0 + ((1.0 - h2hOver25Rate) * 16.0))
            val xGFormatted = String.format("%.1f", totalExpectedGoals)
            val underPct = ((1.0 - h2hOver25Rate) * 100).roundToInt()
            candidates.add(
                CandidatePick(
                    pick = "Moins de 2.5 Buts",
                    pickType = "UNDER_25",
                    confidence = conf,
                    reasoning = "Blocs défensifs très étanches (xG total estimé à seulement $xGFormatted) avec $underPct% de scores sous 2.5 buts.",
                    estimatedOdds = roundOdds(1.68 + (95.0 - conf) * 0.014)
                )
            )
        }

        // Candidate C: Les deux équipes marquent (BTTS - Oui)
        if (xGHome >= 1.15 && xGAway >= 0.95 && h2hBttsRate >= 0.55) {
            val conf = clampConfidence(55.0 + (h2hBttsRate * 26.0) + (min(xGHome, xGAway) * 12.0))
            val bttsPct = (h2hBttsRate * 100).roundToInt()
            candidates.add(
                CandidatePick(
                    pick = "Les 2 équipes marquent (BTTS)",
                    pickType = "BTTS_YES",
                    confidence = conf,
                    reasoning = "Potentiel offensif des deux côtés ($bttsPct% BTTS historique et au moins 1 but attendu pour chaque équipe).",
                    estimatedOdds = roundOdds(1.72 + (95.0 - conf) * 0.012)
                )
            )
        }

        // Candidate D: Victoire Domicile (1)
        if (formGap >= 0.20 && (xGHome - xGAway) >= 0.50) {
            val conf = clampConfidence(58.0 + (formGap * 36.0) + ((xGHome - xGAway) * 14.0))
            val homePts = homeFormPoints
            candidates.add(
                CandidatePick(
                    pick = "Victoire $homeTeam",
                    pickType = "HOME_WIN",
                    confidence = conf,
                    reasoning = "$homeTeam affiche une dynamique supérieure ($homePts/15 pts) et un ascendant net à domicile.",
                    estimatedOdds = roundOdds(1.50 + (95.0 - conf) * 0.02)
                )
            )
        }

        // Candidate E: Victoire Extérieur (2)
        if (formGap <= -0.25 && (xGAway - xGHome) >= 0.35) {
            val conf = clampConfidence(56.0 + (-formGap * 38.0) + ((xGAway - xGHome) * 15.0))
            val awayPts = awayFormPoints
            candidates.add(
                CandidatePick(
                    pick = "Victoire $awayTeam",
                    pickType = "AWAY_WIN",
                    confidence = conf,
                    reasoning = "$awayTeam est en grande forme ($awayPts/15 pts) face à un adversaire en méforme défensive.",
                    estimatedOdds = roundOdds(1.85 + (95.0 - conf) * 0.025)
                )
            )
        }

        // Candidate F: Double Chance Sécurité (1X / X2)
        if (formGap >= 0.05) {
            val conf = clampConfidence(72.0 + (formGap * 20.0) + 4.0)
            candidates.add(
                CandidatePick(
                    pick = "Double Chance $homeTeam ou Nul (1X)",
                    pickType = "DC_1X",
                    confidence = conf,
                    reasoning = "Couverture statistique optimale avec l'avantage du terrain en faveur de $homeTeam.",
                    estimatedOdds = roundOdds(1.28 + (95.0 - conf) * 0.008)
                )
            )
        } else if (formGap <= -0.05) {
            val conf = clampConfidence(70.0 + (-formGap * 20.0) + 4.0)
            candidates.add(
                CandidatePick(
                    pick = "Double Chance $awayTeam ou Nul (X2)",
                    pickType = "DC_X2",
                    confidence = conf,
                    reasoning = "Sécurisation sur la forme extérieure solide de $awayTeam sans risque en cas de score nul.",
                    estimatedOdds = roundOdds(1.32 + (95.0 - conf) * 0.008)
                )
            )
        }

        // Candidate G: Match Nul (Draw)
        if (kotlin.math.abs(formGap) <= 0.10 && kotlin.math.abs(xGHome - xGAway) <= 0.25 && totalExpectedGoals <= 2.2) {
            val conf = clampConfidence(52.0 + (1.0 - kotlin.math.abs(formGap)) * 12.0)
            candidates.add(
                CandidatePick(
                    pick = "Match Nul (N)",
                    pickType = "DRAW",
                    confidence = conf,
                    reasoning = "Équilibre parfait des forces et faible différentiel attendu entre les deux formations.",
                    estimatedOdds = roundOdds(3.10)
                )
            )
        }

        // Select the candidate with highest confidence
        val bestCandidate = candidates.maxByOrNull { it.confidence } ?: CandidatePick(
            pick = "Double Chance $homeTeam ou Nul (1X)",
            pickType = "DC_1X",
            confidence = 68.0,
            reasoning = "Avantage structurel au terrain pour $homeTeam selon l'analyse comparative des séries.",
            estimatedOdds = 1.35
        )

        return AlgorithmOutput(
            pick = bestCandidate.pick,
            pickType = bestCandidate.pickType,
            confidence = (bestCandidate.confidence * 10).roundToInt() / 10.0,
            reasoning = bestCandidate.reasoning,
            estimatedOdds = bestCandidate.estimatedOdds,
            metrics = metrics
        )
    }

    private fun calculateFormPoints(form: String): Int {
        var points = 0
        form.uppercase().take(5).forEach { char ->
            when (char) {
                'W', 'V' -> points += 3
                'D', 'N' -> points += 1
                'L', 'D' -> points += 0
            }
        }
        return points
    }

    private fun clampConfidence(value: Double): Double {
        return min(95.0, max(50.0, value))
    }

    private fun roundOdds(odds: Double): Double {
        return (max(1.15, min(5.0, odds)) * 100.0).roundToInt() / 100.0
    }

    /**
     * Evaluates whether a prediction won or lost based on actual final match scores.
     */
    fun evaluateResult(pickType: String, homeScore: Int, awayScore: Int): String {
        val totalGoals = homeScore + awayScore
        val bothScored = homeScore > 0 && awayScore > 0

        val won = when (pickType) {
            "OVER_25" -> totalGoals > 2.5
            "UNDER_25" -> totalGoals < 2.5
            "BTTS_YES" -> bothScored
            "BTTS_NO" -> !bothScored
            "HOME_WIN" -> homeScore > awayScore
            "AWAY_WIN" -> awayScore > homeScore
            "DRAW" -> homeScore == awayScore
            "DC_1X" -> homeScore >= awayScore
            "DC_X2" -> awayScore >= homeScore
            "DC_12" -> homeScore != awayScore
            else -> false
        }

        return if (won) "win" else "loss"
    }
}
