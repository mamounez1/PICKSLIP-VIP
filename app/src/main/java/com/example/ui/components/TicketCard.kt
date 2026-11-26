package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PredictionWithFixture
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.LockOverlayBg
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedBg
import com.example.ui.theme.LossRedLight
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PendingBlue
import com.example.ui.theme.PendingBlueBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WinGreen
import com.example.ui.theme.WinGreenBg
import com.example.ui.theme.WinGreenLight

@Composable
fun TicketCard(
    item: PredictionWithFixture,
    isVipUnlocked: Boolean,
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prediction = item.prediction
    val fixture = item.fixture
    val isVisible = prediction.isFreePick || isVipUnlocked

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ticket_card_${prediction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (prediction.isFreePick) GoldAccent.copy(alpha = 0.5f) else NavyBorder
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Top Header: League & Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavySurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = fixture.league.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GoldAccent,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = " • ${fixture.kickoff}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Status Badge (Win / Loss / Pending)
                ResultBadge(result = prediction.result, fixture = fixture)
            }

            // Teams Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Home Team
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NavyBorder,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = fixture.homeTeam.take(1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = fixture.homeTeam,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // Score or Form
                    if (fixture.status == "finished" && fixture.homeScore != null) {
                        Text(
                            text = "${fixture.homeScore}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    } else {
                        FormBadges(form = fixture.homeRecentForm)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Away Team
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NavyBorder,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = fixture.awayTeam.take(1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = fixture.awayTeam,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    if (fixture.status == "finished" && fixture.awayScore != null) {
                        Text(
                            text = "${fixture.awayScore}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    } else {
                        FormBadges(form = fixture.awayRecentForm)
                    }
                }
            }

            // Perforated Betting Ticket Divider
            PerforatedDivider()

            // Prediction & Algorithm Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Tag: Free vs VIP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (prediction.isFreePick) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldAccent.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "⭐ PRONO GRATUIT • OUVERT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isVipUnlocked) GoldDark.copy(alpha = 0.3f) else NavySurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isVipUnlocked) GoldAccent else NavyBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isVipUnlocked) Icons.Default.Stars else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isVipUnlocked) GoldAccent else TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isVipUnlocked) "⭐ VIP DÉBLOQUÉ" else "🔒 ACCÈS VIP REQUIS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isVipUnlocked) GoldAccent else TextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Odds
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NavySurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                        ) {
                            Text(
                                text = "Cote : ${String.format("%.2f", prediction.oddsEst)}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pick Display
                    Text(
                        text = if (isVisible) prediction.pick else "••••••••••••••••••••",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isVisible) GoldLight else TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Confidence Bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Indice de confiance algorithmique",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = if (isVisible) "${String.format("%.1f", prediction.confidence)}%" else "••%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (isVisible) (prediction.confidence / 100.0).toFloat() else 0.5f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = GoldAccent,
                            trackColor = NavySurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // French Deterministic Reasoning
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavySurface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isVisible) prediction.reasoning else "Analyse statistique réservée aux membres du Club VIP.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = if (isVisible) TextSecondary else TextMuted
                            )
                        }
                    }
                }

                // Lock Overlay if VIP and not unlocked
                if (!isVisible) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(LockOverlayBg)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GoldAccent.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Verrouillé",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "PRONOSTIC VIP",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Débloquez l'algorithme complet (99 MAD/mois)",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onUnlockClick,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("unlock_vip_button_${prediction.id}")
                            ) {
                                Text(
                                    text = "Rejoindre le VIP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NavyBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultBadge(result: String, fixture: com.example.data.model.FixtureEntity) {
    when (result) {
        "win" -> {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = WinGreenBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, WinGreen)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = WinGreenLight,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GAGNÉ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WinGreenLight,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
        "loss" -> {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = LossRedBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, LossRed)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = LossRedLight,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PERDU",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LossRedLight,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
        else -> {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = PendingBlueBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, PendingBlue.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = PendingBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "EN ATTENTE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun FormBadges(form: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        form.take(5).forEach { char ->
            val (bg, textColor) = when (char.uppercaseChar()) {
                'W', 'V' -> Pair(WinGreenBg, WinGreenLight)
                'D', 'N' -> Pair(NavySurface, TextSecondary)
                else -> Pair(LossRedBg, LossRedLight)
            }
            Surface(
                shape = RoundedCornerShape(3.dp),
                color = bg,
                modifier = Modifier.size(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = char.toString().uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun PerforatedDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left notch cutout
        Box(
            modifier = Modifier
                .size(width = 10.dp, height = 20.dp)
                .background(
                    color = NavyBackground,
                    shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                )
        )

        // Perforated Dashed Line
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .padding(horizontal = 6.dp)
        ) {
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            drawLine(
                color = Color(0xFF2B3A54),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 2f,
                pathEffect = pathEffect
            )
        }

        // Right notch cutout
        Box(
            modifier = Modifier
                .size(width = 10.dp, height = 20.dp)
                .background(
                    color = NavyBackground,
                    shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                )
        )
    }
}
