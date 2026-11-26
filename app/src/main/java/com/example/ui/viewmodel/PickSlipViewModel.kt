package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PickSlipDatabase
import com.example.data.model.DailyStatsEntity
import com.example.data.model.PredictionWithFixture
import com.example.data.model.SubscriberEntity
import com.example.data.repository.PickSlipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class FeedFilter {
    ALL,
    FREE_ONLY,
    VIP_ONLY,
    SCHEDULED,
    RESOLVED
}

enum class NavigationTab {
    TODAY_MATCHES,
    FEED,
    DASHBOARD,
    SUBSCRIBE,
    SIMULATOR
}

data class DashboardMetrics(
    val totalPicks: Int = 0,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val pendingCount: Int = 0,
    val winRate: Double = 0.0,
    val roiPercentage: Double = 0.0,
    val averageOdds: Double = 1.65,
    val currentStreak: Int = 0
)

data class PickSlipUiState(
    val predictions: List<PredictionWithFixture> = emptyList(),
    val filteredPredictions: List<PredictionWithFixture> = emptyList(),
    val currentSubscriber: SubscriberEntity? = null,
    val isVipActive: Boolean = false,
    val selectedFilter: FeedFilter = FeedFilter.ALL,
    val currentTab: NavigationTab = NavigationTab.TODAY_MATCHES,
    val dailyStats: List<DailyStatsEntity> = emptyList(),
    val metrics: DashboardMetrics = DashboardMetrics(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val emailInput: String = ""
)

class PickSlipViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PickSlipRepository

    private val _selectedFilter = MutableStateFlow(FeedFilter.ALL)
    private val _currentTab = MutableStateFlow(NavigationTab.TODAY_MATCHES)
    private val _isLoading = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _emailInput = MutableStateFlow("")

    init {
        val db = PickSlipDatabase.getInstance(application)
        repository = PickSlipRepository(db)
        viewModelScope.launch {
            repository.initializeStarterDataIfNeeded()
        }
    }

    val uiState: StateFlow<PickSlipUiState> = combine(
        repository.allPredictions,
        repository.currentSubscriber,
        repository.isVipActive,
        repository.dailyStats,
        _selectedFilter,
        _currentTab,
        _isLoading,
        _userMessage,
        _emailInput
    ) { params ->
        val rawPredictions = params[0] as List<PredictionWithFixture>
        val subscriber = params[1] as SubscriberEntity?
        val isVip = params[2] as Boolean
        val dailyStatsList = params[3] as List<DailyStatsEntity>
        val filter = params[4] as FeedFilter
        val tab = params[5] as NavigationTab
        val loading = params[6] as Boolean
        val message = params[7] as String?
        val email = params[8] as String

        val filtered = filterPredictions(rawPredictions, filter)
        val metrics = calculateMetrics(rawPredictions)

        PickSlipUiState(
            predictions = rawPredictions,
            filteredPredictions = filtered,
            currentSubscriber = subscriber,
            isVipActive = isVip,
            selectedFilter = filter,
            currentTab = tab,
            dailyStats = dailyStatsList,
            metrics = metrics,
            isLoading = loading,
            userMessage = message,
            emailInput = email
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PickSlipUiState(isLoading = true)
    )

    fun setFilter(filter: FeedFilter) {
        _selectedFilter.value = filter
    }

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setEmailInput(email: String) {
        _emailInput.value = email
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun activateVip(email: String = _emailInput.value) {
        val targetEmail = if (email.isBlank()) "membre.vip@pickslip.com" else email.trim()
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.activateVipSubscription(targetEmail)
                _userMessage.value = "Abonnement VIP activé avec succès pour $targetEmail !"
                _emailInput.value = ""
            } catch (e: Exception) {
                _userMessage.value = "Erreur lors de l'activation : ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelVip() {
        val sub = uiState.value.currentSubscriber ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.cancelSubscription(sub.email)
                _userMessage.value = "Votre abonnement a été résilié."
            } catch (e: Exception) {
                _userMessage.value = "Erreur : ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun triggerCronGeneratePredictions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newPicks = repository.generateDailyPredictionsCron()
                _userMessage.value = "Cron 06:00 exécuté : ${newPicks.size} nouveaux pronostics générés !"
            } catch (e: Exception) {
                _userMessage.value = "Erreur Cron : ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun triggerCronCheckResults() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resolved = repository.checkResultsCron()
                _userMessage.value = "Cron 23:00 exécuté : $resolved pronostics évalués et bilan mis à jour !"
            } catch (e: Exception) {
                _userMessage.value = "Erreur Cron : ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun filterPredictions(
        list: List<PredictionWithFixture>,
        filter: FeedFilter
    ): List<PredictionWithFixture> {
        return when (filter) {
            FeedFilter.ALL -> list
            FeedFilter.FREE_ONLY -> list.filter { it.prediction.isFreePick }
            FeedFilter.VIP_ONLY -> list.filter { !it.prediction.isFreePick }
            FeedFilter.SCHEDULED -> list.filter { it.prediction.result == "pending" }
            FeedFilter.RESOLVED -> list.filter { it.prediction.result != "pending" }
        }
    }

    private fun calculateMetrics(list: List<PredictionWithFixture>): DashboardMetrics {
        val resolved = list.filter { it.prediction.result in listOf("win", "loss") }
        val wins = resolved.count { it.prediction.result == "win" }
        val losses = resolved.count { it.prediction.result == "loss" }
        val total = wins + losses
        val winRate = if (total > 0) ((wins.toDouble() / total) * 100.0) else 0.0
        val pending = list.count { it.prediction.result == "pending" }

        // Approximate ROI based on average estimated odds
        val totalStaked = total * 100.0
        var totalReturns = 0.0
        resolved.forEach {
            if (it.prediction.result == "win") {
                totalReturns += 100.0 * it.prediction.oddsEst
            }
        }
        val roi = if (totalStaked > 0) (((totalReturns - totalStaked) / totalStaked) * 100.0) else 0.0

        // Calculate current streak
        var streak = 0
        val sortedResolved = resolved.sortedByDescending { it.prediction.createdAt }
        for (item in sortedResolved) {
            if (item.prediction.result == "win") {
                streak++
            } else {
                break
            }
        }

        return DashboardMetrics(
            totalPicks = total,
            totalWins = wins,
            totalLosses = losses,
            pendingCount = pending,
            winRate = (winRate * 10.0).roundToInt() / 10.0,
            roiPercentage = (roi * 10.0).roundToInt() / 10.0,
            averageOdds = 1.68,
            currentStreak = streak
        )
    }
}
