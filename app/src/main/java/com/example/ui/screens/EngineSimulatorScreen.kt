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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.algorithm.StatisticalAlgorithm
import com.example.ui.components.DisclaimerBanner
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PendingBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WinGreen
import com.example.ui.theme.WinGreenLight

@Composable
fun EngineSimulatorScreen(
    onRunGenerateCron: () -> Unit,
    onRunCheckResultsCron: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    // Interactive algorithm sandbox state
    var homeTeamName by remember { mutableStateOf("Paris SG") }
    var awayTeamName by remember { mutableStateOf("Marseille") }
    var homeForm by remember { mutableStateOf("WWWDW") }
    var awayForm by remember { mutableStateOf("WDLWW") }
    var homeGoalsFor by remember { mutableDoubleStateOf(2.4) }
    var awayGoalsFor by remember { mutableDoubleStateOf(1.6) }
    var h2hBttsRate by remember { mutableDoubleStateOf(0.70) }
    var h2hOver25Rate by remember { mutableDoubleStateOf(0.65) }

    val algoOutput = remember(
        homeTeamName,
        awayTeamName,
        homeForm,
        awayForm,
        homeGoalsFor,
        awayGoalsFor,
        h2hBttsRate,
        h2hOver25Rate
    ) {
        StatisticalAlgorithm.evaluate(
            homeTeam = homeTeamName.ifBlank { "Equipe A" },
            awayTeam = awayTeamName.ifBlank { "Equipe B" },
            homeForm = homeForm,
            awayForm = awayForm,
            homeGoalsForAvg = homeGoalsFor,
            homeGoalsAgainstAvg = 0.9,
            awayGoalsForAvg = awayGoalsFor,
            awayGoalsAgainstAvg = 1.3,
            h2hBttsRate = h2hBttsRate,
            h2hOver25Rate = h2hOver25Rate
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .testTag("engine_simulator_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = GoldAccent.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "MOTEUR & CRON JOBS VERCEL",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Autopilot & Simulateur Mathématique",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "Testez manuellement les tâches planifiées et découvrez le calcul pas-à-pas de l'algorithme.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        item {
            DisclaimerBanner()
        }

        // Cron Job Triggers
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Tâches Automatiques Planifiées",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Les 2 cron jobs configurés dans vercel.json qui tournent 24h/24 sans intervention humaine :",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Job 1: 06:00 Generate Predictions
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NavySurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "06:00 UTC • Générer les Pronostics",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "/api/cron/generate-predictions",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Récupère les matchs du jour sur API-Football, calcule les métriques et publie les tickets. Le 1er ticket est gratuit, les autres VIP.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onRunGenerateCron,
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cron_generate_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = NavyBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Exécuter Cron 06:00 (Simuler)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NavyBackground
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Job 2: 23:00 Check Results
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NavySurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = WinGreenLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "23:00 UTC • Vérifier les Résultats",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "/api/cron/check-results",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Récupère les scores finaux, évalue les paris (Gagné / Perdu) selon l'algorithme et met à jour l'agrégat stats_daily.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onRunCheckResultsCron,
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = WinGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cron_check_results_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Exécuter Cron 23:00 (Simuler)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive Statistical Algorithm Sandbox
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bac à Sable de l'Arbre de Décision",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Ajustez les variables statistiques pour voir la décision mathématique déterministe en direct :",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Inputs: Team names
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = homeTeamName,
                            onValueChange = { homeTeamName = it },
                            label = { Text("Domicile", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NavySurface,
                                unfocusedContainerColor = NavySurface,
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = NavyBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = awayTeamName,
                            onValueChange = { awayTeamName = it },
                            label = { Text("Extérieur", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NavySurface,
                                unfocusedContainerColor = NavySurface,
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = NavyBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Form inputs (W/D/L)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = homeForm,
                            onValueChange = { if (it.length <= 5) homeForm = it.uppercase() },
                            label = { Text("Forme Domicile (5 m)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NavySurface,
                                unfocusedContainerColor = NavySurface,
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = NavyBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = awayForm,
                            onValueChange = { if (it.length <= 5) awayForm = it.uppercase() },
                            label = { Text("Forme Extérieur (5 m)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NavySurface,
                                unfocusedContainerColor = NavySurface,
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = NavyBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sliders for Goals and H2H
                    Text(
                        text = "Moyenne buts Domicile : ${String.format("%.1f", homeGoalsFor)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Slider(
                        value = homeGoalsFor.toFloat(),
                        onValueChange = { homeGoalsFor = it.toDouble() },
                        valueRange = 0.5f..4.0f,
                        colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                    )

                    Text(
                        text = "Taux BTTS Confrontations Directes : ${(h2hBttsRate * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Slider(
                        value = h2hBttsRate.toFloat(),
                        onValueChange = { h2hBttsRate = it.toDouble() },
                        valueRange = 0.1f..0.95f,
                        colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Output Result Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NavySurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RÉSULTAT DE L'ALGORITHME",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Cote est. ${String.format("%.2f", algoOutput.estimatedOdds)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = algoOutput.pick,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Confiance : ${String.format("%.1f", algoOutput.confidence)}% • xG Total : ${String.format("%.2f", algoOutput.metrics.totalExpectedGoals)} (Dom: ${String.format("%.2f", algoOutput.metrics.expectedGoalsHome)} / Ext: ${String.format("%.2f", algoOutput.metrics.expectedGoalsAway)})",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "« ${algoOutput.reasoning} »",
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
