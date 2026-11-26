package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyStatsEntity
import com.example.data.model.PredictionWithFixture
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.ResultBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedLight
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WinGreen
import com.example.ui.theme.WinGreenBg
import com.example.ui.theme.WinGreenLight
import com.example.ui.viewmodel.DashboardMetrics

@Composable
fun DashboardScreen(
    metrics: DashboardMetrics,
    predictions: List<PredictionWithFixture>,
    dailyStats: List<DailyStatsEntity>,
    modifier: Modifier = Modifier
) {
    val resolvedPicks = predictions.filter { it.prediction.result in listOf("win", "loss") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Track Record Header
        item {
            TrackRecordHero(metrics = metrics)
        }

        // Responsible Gambling Notice
        item {
            DisclaimerBanner()
        }

        // Quick 4-Grid Metrics
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Pronos Résolus",
                    value = "${metrics.totalPicks}",
                    subtitle = "100% archivés",
                    icon = Icons.Default.QueryStats,
                    accentColor = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Série Actuelle",
                    value = "${metrics.currentStreak} V",
                    subtitle = "Victoires d'affilée",
                    icon = Icons.Default.TrendingUp,
                    accentColor = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Victoires",
                    value = "${metrics.totalWins}",
                    subtitle = "Pari validé",
                    icon = Icons.Default.CheckCircle,
                    accentColor = WinGreenLight,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Défaites",
                    value = "${metrics.totalLosses}",
                    subtitle = "Pari non validé",
                    icon = Icons.Default.Close,
                    accentColor = LossRedLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Daily Breakdown Section
        item {
            SectionTitle(
                title = "Historique Quotidien",
                subtitle = "Agrégation certifiée des bilans quotidiens"
            )
        }

        if (dailyStats.isNotEmpty()) {
            items(dailyStats, key = { it.date }) { stats ->
                DailyStatRow(stats = stats, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
        }

        // Recent Resolved Predictions Feed
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(
                title = "Derniers Pronostics Validés",
                subtitle = "Transparence totale sur chaque billet"
            )
        }

        if (resolvedPicks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard)
                ) {
                    Text(
                        text = "Aucun pronostic résolu pour l'instant. Lancez la vérification des résultats à 23:00 dans l'onglet Moteur.",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(resolvedPicks, key = { it.prediction.id }) { item ->
                ResolvedPickItem(
                    item = item,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun TrackRecordHero(metrics: DashboardMetrics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(NavySurface, NavyCard)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GoldAccent.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "BILAN PUBLIC VÉRIFIÉ",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Performance de l'Algorithme",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sur les ${metrics.totalPicks} derniers pronostics audités sans intervention humaine.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${metrics.totalWins}V - ${metrics.totalLosses}D",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                        Text(
                            text = " • Cote moy. ${String.format("%.2f", metrics.averageOdds)}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Big Circular Win Rate Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(92.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { (metrics.winRate / 100.0).toFloat() },
                        modifier = Modifier.size(92.dp),
                        color = GoldAccent,
                        trackColor = NavySurface,
                        strokeWidth = 8.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${String.format("%.1f", metrics.winRate)}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = GoldLight
                        )
                        Text(
                            text = "WIN RATE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun DailyStatRow(stats: DailyStatsEntity, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = NavyCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stats.date,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${stats.wins}V / ${stats.losses}D",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (stats.winRate >= 60.0) WinGreenBg else NavySurface
                ) {
                    Text(
                        text = "${String.format("%.1f", stats.winRate)}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (stats.winRate >= 60.0) WinGreenLight else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ResolvedPickItem(item: PredictionWithFixture, modifier: Modifier = Modifier) {
    val prediction = item.prediction
    val fixture = item.fixture

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${fixture.league} • ${fixture.kickoff}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                ResultBadge(result = prediction.result, fixture = fixture)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Match title & score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${fixture.homeTeam} vs ${fixture.awayTeam}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (fixture.homeScore != null && fixture.awayScore != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NavySurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                    ) {
                        Text(
                            text = "${fixture.homeScore} - ${fixture.awayScore}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pick and Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pari : ${prediction.pick}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
                Text(
                    text = "Cote : ${String.format("%.2f", prediction.oddsEst)} • Confiance ${String.format("%.1f", prediction.confidence)}%",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }
        }
    }
}
