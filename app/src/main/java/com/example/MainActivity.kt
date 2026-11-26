package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.TopHeader
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EngineSimulatorScreen
import com.example.ui.screens.PredictionsFeedScreen
import com.example.ui.screens.SubscribeScreen
import com.example.ui.screens.TodayMatchesScreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PickSlipTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.PickSlipViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PickSlipViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PickSlipTheme {
                PickSlipApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PickSlipApp(viewModel: PickSlipViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NavyBackground,
        topBar = {
            TopHeader(
                isVipActive = state.isVipActive,
                winRate = state.metrics.winRate,
                onVipClick = { viewModel.setTab(NavigationTab.SUBSCRIBE) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NavyCard,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                tonalElevation = 8.dp
            ) {
                // Tab 1: Today's Matches (Matchs du Jour - 3 Gratuits & VIP)
                NavigationBarItem(
                    selected = state.currentTab == NavigationTab.TODAY_MATCHES,
                    onClick = { viewModel.setTab(NavigationTab.TODAY_MATCHES) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = "Matchs du Jour",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Aujourd'hui",
                            fontSize = 10.sp,
                            fontWeight = if (state.currentTab == NavigationTab.TODAY_MATCHES) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyBackground,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_today_matches")
                )

                // Tab 2: All Predictions Feed
                NavigationBarItem(
                    selected = state.currentTab == NavigationTab.FEED,
                    onClick = { viewModel.setTab(NavigationTab.FEED) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = "Tous les Pronostics",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Pronos",
                            fontSize = 10.sp,
                            fontWeight = if (state.currentTab == NavigationTab.FEED) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyBackground,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_feed")
                )

                // Tab 3: Performance Dashboard / Track Record
                NavigationBarItem(
                    selected = state.currentTab == NavigationTab.DASHBOARD,
                    onClick = { viewModel.setTab(NavigationTab.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Bilan",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Bilan",
                            fontSize = 10.sp,
                            fontWeight = if (state.currentTab == NavigationTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyBackground,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                // Tab 4: VIP Subscription (99 DH)
                NavigationBarItem(
                    selected = state.currentTab == NavigationTab.SUBSCRIBE,
                    onClick = { viewModel.setTab(NavigationTab.SUBSCRIBE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "VIP",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "VIP (99 DH)",
                            fontSize = 10.sp,
                            fontWeight = if (state.currentTab == NavigationTab.SUBSCRIBE) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyBackground,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_subscribe")
                )

                // Tab 5: Cron Engine Simulator
                NavigationBarItem(
                    selected = state.currentTab == NavigationTab.SIMULATOR,
                    onClick = { viewModel.setTab(NavigationTab.SIMULATOR) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Moteur Cron",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Moteur",
                            fontSize = 10.sp,
                            fontWeight = if (state.currentTab == NavigationTab.SIMULATOR) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyBackground,
                        selectedTextColor = GoldAccent,
                        indicatorColor = GoldAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_simulator")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.currentTab) {
                NavigationTab.TODAY_MATCHES -> {
                    TodayMatchesScreen(
                        predictions = state.predictions,
                        isVipActive = state.isVipActive,
                        onUnlockVipClick = { viewModel.setTab(NavigationTab.SUBSCRIBE) },
                        onQuickActivateVip = { email -> viewModel.activateVip(email) },
                        onRefreshClick = { viewModel.triggerCronGeneratePredictions() },
                        isLoading = state.isLoading
                    )
                }

                NavigationTab.FEED -> {
                    PredictionsFeedScreen(
                        predictions = state.filteredPredictions,
                        selectedFilter = state.selectedFilter,
                        isVipActive = state.isVipActive,
                        onFilterSelect = { viewModel.setFilter(it) },
                        onUnlockVipClick = { viewModel.setTab(NavigationTab.SUBSCRIBE) },
                        onRefreshClick = { viewModel.triggerCronGeneratePredictions() },
                        isLoading = state.isLoading
                    )
                }

                NavigationTab.DASHBOARD -> {
                    DashboardScreen(
                        metrics = state.metrics,
                        predictions = state.predictions,
                        dailyStats = state.dailyStats
                    )
                }

                NavigationTab.SUBSCRIBE -> {
                    SubscribeScreen(
                        currentSubscriber = state.currentSubscriber,
                        isVipActive = state.isVipActive,
                        emailInput = state.emailInput,
                        onEmailChange = { viewModel.setEmailInput(it) },
                        onSubscribeClick = { viewModel.activateVip() },
                        onCancelSubscriptionClick = { viewModel.cancelVip() },
                        isLoading = state.isLoading
                    )
                }

                NavigationTab.SIMULATOR -> {
                    EngineSimulatorScreen(
                        onRunGenerateCron = { viewModel.triggerCronGeneratePredictions() },
                        onRunCheckResultsCron = { viewModel.triggerCronCheckResults() },
                        isLoading = state.isLoading
                    )
                }
            }
        }
    }
}

// Composable function for backwards compatibility & preview
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "PickSlip : $name",
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}
