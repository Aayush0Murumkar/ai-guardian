package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.service.GovernancePipeline
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class DashboardScreen {
  OVERVIEW,
  REVIEW,
  LIVE_TEST,
  POLICIES
}

class GovernanceViewModel(
  val repository: GovernanceRepository = GovernanceRepository(),
  val pipeline: GovernancePipeline = GovernancePipeline()
) : ViewModel() {

  private val _currentScreen = MutableStateFlow(DashboardScreen.OVERVIEW)
  val currentScreen: StateFlow<DashboardScreen> = _currentScreen.asStateFlow()

  val incidents = repository.incidents
  val selectedIncidentId = repository.selectedIncidentId
  val liveInteractions = repository.liveInteractions
  val liveConversationsCount = repository.liveConversationsCount

  val activeIncident: StateFlow<IncidentRecord?> = combine(incidents, selectedIncidentId) { list, id ->
    list.firstOrNull { it.id == id } ?: list.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

  val openIncidentsCount = incidents.map { list -> list.count { !it.isResolved } }
    .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

  val awaitingReviewCount = incidents.map { list -> list.count { it.reviewStatus == ReviewStatus.PENDING && !it.isResolved } }
    .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

  val resolvedTodayCount = incidents.map { list -> list.count { it.isResolved } }
    .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

  val criticalCount = incidents.map { list -> list.count { it.severity == GovernanceSeverity.CRITICAL && !it.isResolved } }
    .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

  private val _isProcessing = MutableStateFlow(false)
  val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

  private val _latestResult = MutableStateFlow<GovernanceResult?>(null)
  val latestResult: StateFlow<GovernanceResult?> = _latestResult.asStateFlow()

  fun navigateTo(screen: DashboardScreen) {
    _currentScreen.value = screen
  }

  fun selectIncident(id: String) {
    repository.selectIncident(id)
  }

  fun approveActiveCorrection(note: String = "") {
    val current = activeIncident.value ?: return
    repository.approveCorrection(current.id, note)
  }

  fun requestActiveChanges(note: String = "") {
    val current = activeIncident.value ?: return
    repository.requestChanges(current.id, note)
  }

  fun resolveActiveIncident() {
    val current = activeIncident.value ?: return
    repository.resolveIncident(current.id)
  }

  fun runLiveInteraction(prompt: String) {
    if (prompt.isBlank()) return
    viewModelScope.launch {
      _isProcessing.value = true
      try {
        val result = pipeline.processInteraction(prompt)
        _latestResult.value = result
        repository.recordGovernanceResult(result)
      } finally {
        _isProcessing.value = false
      }
    }
  }
}
