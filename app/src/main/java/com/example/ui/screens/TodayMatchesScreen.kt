package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.WinGreen
import com.example.ui.theme.WinGreenBg
import com.example.ui.theme.WinGreenLight

enum class TodayFilter {
    ALL,
    FREE_3_ONLY,
    VIP_ONLY
}

@Composable
fun TodayMatchesScreen(
    predictions: List<PredictionWithFixture>,
    isVipActive: Boolean,
    onUnlockVipClick: () -> Unit,
    onQuickActivateVip: (String) -> Unit,
    onRefreshClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf(TodayFilter.ALL) }
    var selectedLeagueFilter by remember { mutableStateOf<String?>(null) }
    var showVipQuickDialog by remember { mutableStateOf(false) }
    var vipEmailInput by remember { mutableStateOf("") }

    // Today's matches (scheduled / pending fixtures for today)
    val todayMatches = remember(predictions) {
        predictions.filter {
            it.prediction.result == "pending" || it.fixture.kickoff.contains("Aujourd'hui", ignoreCase = true)
        }
    }

    // Extract available leagues for filtering
    val availableLeagues = remember(todayMatches) {
        todayMatches.map { it.fixture.league }.distinct()
    }

    // Filtered list based on category and league
    val filteredMatches = remember(todayMatches, selectedCategoryFilter, selectedLeagueFilter) {
        todayMatches.filter { item ->
            val matchesCategory = when (selectedCategoryFilter) {
                TodayFilter.ALL -> true
                TodayFilter.FREE_3_ONLY -> item.prediction.isFreePick
                TodayFilter.VIP_ONLY -> !item.prediction.isFreePick
            }
            val matchesLeague = if (selectedLeagueFilter == null) true else item.fixture.league == selectedLeagueFilter
            matchesCategory && matchesLeague
        }
    }

    val freePicksCount = remember(todayMatches) { todayMatches.count { it.prediction.isFreePick } }
    val vipPicksCount = remember(todayMatches) { todayMatches.count { !it.prediction.isFreePick } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .testTag("today_matches_screen"),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Hero Header: Matchs du Jour (Today's Matches)
        item {
            TodayHeroBanner(
                totalMatchesCount = todayMatches.size,
                freeCount = freePicksCount,
                vipCount = vipPicksCount,
                isVipActive = isVipActive,
                onUnlockVipClick = { showVipQuickDialog = true },
                onRefreshClick = onRefreshClick,
                isLoading = isLoading
            )
        }

        // VIP Status / Quick Promo Banner
        item {
            TodayVipStatusBanner(
                isVipActive = isVipActive,
                freeCount = freePicksCount,
                vipCount = vipPicksCount,
                onUnlockClick = { showVipQuickDialog = true }
            )
        }

        // Category Filter Tabs (Tous / 3 Gratuits / VIP)
        item {
            TodayCategoryTabs(
                selectedFilter = selectedCategoryFilter,
                onFilterSelected = { selectedCategoryFilter = it },
                totalCount = todayMatches.size,
                freeCount = freePicksCount,
                vipCount = vipPicksCount
            )
        }

        // League Filter Chips
        if (availableLeagues.isNotEmpty()) {
            item {
                TodayLeagueChips(
                    leagues = availableLeagues,
                    selectedLeague = selectedLeagueFilter,
                    onLeagueSelected = { selectedLeagueFilter = it }
                )
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PROGRAMME DES MATCHS (${filteredMatches.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                if (isVipActive) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = WinGreenBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WinGreen)
                    ) {
                        Text(
                            text = "TOUT DÉBLOQUÉ",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = WinGreenLight,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GoldAccent.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "3 OUVERTS • ${vipPicksCount} VIP",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Matches List
        if (filteredMatches.isEmpty()) {
            item {
                EmptyTodayMatchesCard(onRefreshClick = onRefreshClick)
            }
        } else {
            items(filteredMatches, key = { it.prediction.id }) { item ->
                TicketCard(
                    item = item,
                    isVipUnlocked = isVipActive,
                    onUnlockClick = { showVipQuickDialog = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Responsible Gaming Disclaimer
        item {
            DisclaimerBanner()
        }
    }

    // Quick VIP Activation Dialog
    if (showVipQuickDialog) {
        AlertDialog(
            onDismissRequest = { showVipQuickDialog = false },
            containerColor = NavyCard,
            titleContentColor = GoldLight,
            textContentColor = TextPrimary,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = GoldAccent.copy(alpha = 0.2f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Accès VIP - Matchs du Jour",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Débloquez l'intégralité des 10 matchs du jour et toutes les analyses détaillées de l'algorithme.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NavySurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = WinGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("100% des pronostics du jour débloqués", fontSize = 12.sp, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = WinGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Accès complet aux grosses cotes & analyses xG", fontSize = 12.sp, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = WinGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Seulement 99 MAD/mois • Sans engagement", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = vipEmailInput,
                        onValueChange = { vipEmailInput = it },
                        label = { Text("Votre adresse email") },
                        placeholder = { Text("exemple@gmail.com") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = GoldAccent,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vip_quick_email_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val email = if (vipEmailInput.isBlank()) "vip.user@pickslip.ma" else vipEmailInput
                        onQuickActivateVip(email)
                        showVipQuickDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_quick_vip_button")
                ) {
                    Text(
                        text = "Activer le VIP (99 MAD)",
                        fontWeight = FontWeight.Bold,
                        color = NavyBackground
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showVipQuickDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun TodayHeroBanner(
    totalMatchesCount: Int,
    freeCount: Int,
    vipCount: Int,
    isVipActive: Boolean,
    onUnlockVipClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(18.dp),
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
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GoldAccent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "CALENDRIER DU JOUR • TODAY MATCHES",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Matchs d'Aujourd'hui",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tous les matchs du jour avec pronostics algorithmiques : 3 matchs ouverts à tous et sélection VIP.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }

                    IconButton(
                        onClick = onRefreshClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(NavySurface, CircleShape)
                            .testTag("refresh_today_matches_button")
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

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Bar on Hero
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TodayStatBox(
                        label = "Total Matchs",
                        value = "$totalMatchesCount",
                        accentColor = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    TodayStatBox(
                        label = "3 Gratuits",
                        value = "$freeCount",
                        accentColor = WinGreenLight,
                        modifier = Modifier.weight(1f)
                    )
                    TodayStatBox(
                        label = "VIP Exclusifs",
                        value = "$vipCount",
                        accentColor = GoldAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun TodayStatBox(
    label: String,
    value: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = NavySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TodayVipStatusBanner(
    isVipActive: Boolean,
    freeCount: Int,
    vipCount: Int,
    onUnlockClick: () -> Unit
) {
    if (isVipActive) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = WinGreenBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, WinGreen),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = WinGreenLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "MEMBRE VIP ACTIF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = WinGreenLight,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Tous les $vipCount matchs VIP de la journée sont débloqués et accessibles sans restriction.",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = GoldAccent.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onUnlockClick() }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "3 Matchs Gratuits Débloqués",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Débloquez les $vipCount autres matchs VIP du jour (99 MAD)",
                            fontSize = 11.sp,
                            color = GoldLight
                        )
                    }
                }
                Button(
                    onClick = onUnlockClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Débloquer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBackground
                    )
                }
            }
        }
    }
}

@Composable
fun TodayCategoryTabs(
    selectedFilter: TodayFilter,
    onFilterSelected: (TodayFilter) -> Unit,
    totalCount: Int,
    freeCount: Int,
    vipCount: Int
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TodayTabChip(
            title = "Tous les Matchs ($totalCount)",
            selected = selectedFilter == TodayFilter.ALL,
            onClick = { onFilterSelected(TodayFilter.ALL) }
        )
        TodayTabChip(
            title = "⭐ 3 Pronos Gratuits ($freeCount)",
            selected = selectedFilter == TodayFilter.FREE_3_ONLY,
            onClick = { onFilterSelected(TodayFilter.FREE_3_ONLY) }
        )
        TodayTabChip(
            title = "🔒 Pronos VIP ($vipCount)",
            selected = selectedFilter == TodayFilter.VIP_ONLY,
            onClick = { onFilterSelected(TodayFilter.VIP_ONLY) }
        )
    }
}

@Composable
fun TodayTabChip(
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
            .testTag("today_tab_$title")
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
fun TodayLeagueChips(
    leagues: List<String>,
    selectedLeague: String?,
    onLeagueSelected: (String?) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (selectedLeague == null) NavyBorder else NavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedLeague == null) GoldAccent else NavyBorder),
            modifier = Modifier.clickable { onLeagueSelected(null) }
        ) {
            Text(
                text = "Toutes les ligues",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 11.sp,
                fontWeight = if (selectedLeague == null) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedLeague == null) TextPrimary else TextSecondary
            )
        }

        leagues.forEach { league ->
            val isSelected = selectedLeague == league
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) NavyBorder else NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldAccent else NavyBorder),
                modifier = Modifier.clickable { onLeagueSelected(league) }
            ) {
                Text(
                    text = league,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) TextPrimary else TextSecondary
                )
            }
        }
    }
}

@Composable
fun EmptyTodayMatchesCard(onRefreshClick: () -> Unit) {
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
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Aucun match trouvé pour ce filtre",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Rechargez les données pour obtenir la grille des matchs du jour.",
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
                    text = "Générer les Matchs",
                    fontWeight = FontWeight.Bold,
                    color = NavyBackground
                )
            }
        }
    }
}
