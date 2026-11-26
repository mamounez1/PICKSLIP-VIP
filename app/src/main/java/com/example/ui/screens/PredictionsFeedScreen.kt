package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PredictionWithFixture
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.TicketCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FeedFilter

@Composable
fun PredictionsFeedScreen(
    predictions: List<PredictionWithFixture>,
    selectedFilter: FeedFilter,
    isVipActive: Boolean,
    onFilterSelect: (FeedFilter) -> Unit,
    onUnlockVipClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .testTag("predictions_feed_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Hero / Pitch Banner
        item {
            HeroHeaderBanner(
                isVipActive = isVipActive,
                onUnlockVipClick = onUnlockVipClick,
                onRefreshClick = onRefreshClick,
                isLoading = isLoading
            )
        }

        // Responsible Gambling Disclaimer
        item {
            DisclaimerBanner()
        }

        // Filters Bar
        item {
            FilterTabsBar(
                selectedFilter = selectedFilter,
                onFilterSelect = onFilterSelect,
                totalCount = predictions.size
            )
        }

        // Predictions List
        if (predictions.isEmpty()) {
            item {
                EmptyPredictionsCard(onRefreshClick = onRefreshClick)
            }
        } else {
            items(predictions, key = { it.prediction.id }) { item ->
                TicketCard(
                    item = item,
                    isVipUnlocked = isVipActive,
                    onUnlockClick = onUnlockVipClick,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun HeroHeaderBanner(
    isVipActive: Boolean,
    onUnlockVipClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(NavySurface, NavyCard)
                    )
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = GoldAccent.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "AUTOPILOT • ALGO STATISTIQUE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pronostics Football du Jour",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "Générés automatiquement chaque matin à 06:00 par analyse probabiliste et xG.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onRefreshClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(NavySurface, CircleShape)
                            .testTag("refresh_cron_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rafraîchir",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = GoldAccent,
                        trackColor = NavySurface
                    )
                }

                if (!isVipActive) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GoldAccent.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUnlockVipClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Débloquez tous les pronostics VIP",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Seulement 99 MAD/mois • Sans engagement",
                                        fontSize = 10.sp,
                                        color = GoldLight
                                    )
                                }
                            }
                            Button(
                                onClick = onUnlockVipClick,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "Rejoindre",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
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
fun FilterTabsBar(
    selectedFilter: FeedFilter,
    onFilterSelect: (FeedFilter) -> Unit,
    totalCount: Int
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChipItem(
            title = "Tous les slips",
            selected = selectedFilter == FeedFilter.ALL,
            onClick = { onFilterSelect(FeedFilter.ALL) }
        )
        FilterChipItem(
            title = "Prono Gratuit",
            selected = selectedFilter == FeedFilter.FREE_ONLY,
            onClick = { onFilterSelect(FeedFilter.FREE_ONLY) }
        )
        FilterChipItem(
            title = "VIP Uniquement",
            selected = selectedFilter == FeedFilter.VIP_ONLY,
            onClick = { onFilterSelect(FeedFilter.VIP_ONLY) }
        )
        FilterChipItem(
            title = "En attente",
            selected = selectedFilter == FeedFilter.SCHEDULED,
            onClick = { onFilterSelect(FeedFilter.SCHEDULED) }
        )
        FilterChipItem(
            title = "Terminés",
            selected = selectedFilter == FeedFilter.RESOLVED,
            onClick = { onFilterSelect(FeedFilter.RESOLVED) }
        )
    }
}

@Composable
fun FilterChipItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) GoldAccent else NavyCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) GoldLight else NavyBorder
        ),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("filter_chip_$title")
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) NavyBackground else TextSecondary,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun EmptyPredictionsCard(onRefreshClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Aucun pronostic dans cette catégorie",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Lancez le moteur de calcul pour générer les prochains billets.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRefreshClick,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Générer les pronostics",
                    fontWeight = FontWeight.Bold,
                    color = NavyBackground
                )
            }
        }
    }
}
