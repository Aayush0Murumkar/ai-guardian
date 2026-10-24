package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GovernanceRepository {

  private val _incidents = MutableStateFlow<List<IncidentRecord>>(emptyList())
  val incidents: StateFlow<List<IncidentRecord>> = _incidents.asStateFlow()

  private val _selectedIncidentId = MutableStateFlow("")
  val selectedIncidentId: StateFlow<String> = _selectedIncidentId.asStateFlow()

  private val _liveInteractions = MutableStateFlow<List<GovernanceResult>>(emptyList())
  val liveInteractions: StateFlow<List<GovernanceResult>> = _liveInteractions.asStateFlow()

  private val _liveConversationsCount = MutableStateFlow(0)
  val liveConversationsCount: StateFlow<Int> = _liveConversationsCount.asStateFlow()

  fun selectIncident(id: String) {
    _selectedIncidentId.value = id
  }

  fun approveCorrection(incidentId: String, note: String) {
    _incidents.update { list ->
      list.map { inc ->
        if (inc.id == incidentId) {
          val updatedTimeline = inc.timeline.toMutableList()
          updatedTimeline.add(
            TimelineItem(
              time = "14:35:10",
              title = "Human approved correction",
              description = if (note.isNotBlank()) "Approved by ${inc.assignedTo}: \"$note\"" else "Safe corrected response approved by ${inc.assignedTo}.",
              dotColorType = "green"
            )
          )
          inc.copy(
            reviewStatus = ReviewStatus.REVIEWED,
            reviewNote = note,
            isResolved = true,
            timeline = updatedTimeline
          )
        } else inc
      }
    }
  }

  fun requestChanges(incidentId: String, note: String) {
    _incidents.update { list ->
      list.map { inc ->
        if (inc.id == incidentId) {
          val updatedTimeline = inc.timeline.toMutableList()
          updatedTimeline.add(
            TimelineItem(
              time = "14:35:20",
              title = "Changes requested by supervisor",
              description = if (note.isNotBlank()) note else "Correction returned for alternate phrasing.",
              dotColorType = "amber"
            )
          )
          inc.copy(
            reviewNote = note,
            timeline = updatedTimeline
          )
        } else inc
      }
    }
  }

  fun resolveIncident(incidentId: String) {
    _incidents.update { list ->
      list.map { inc ->
        if (inc.id == incidentId) {
          inc.copy(
            isResolved = true,
            reviewStatus = ReviewStatus.REVIEWED
          )
        } else inc
      }
    }
  }

  fun recordGovernanceResult(result: GovernanceResult) {
    _liveInteractions.update { listOf(result) + it }
    _liveConversationsCount.update { it + 1 }

    // If an issue was detected and escalated, blocked, or requires human review, add it to real incident list
    if (result.decision == GovernanceDecision.BLOCK || result.decision == GovernanceDecision.ESCALATE || result.humanReview.required) {
      val newId = result.incident.incidentId.ifBlank { "INC-${1050 + _incidents.value.size}" }
      val newRecord = IncidentRecord(
        id = newId,
        title = result.overallEvaluation.ifBlank { "Governance violation detected" },
        botName = "Atlas Support · Live",
        timestampUtc = "Just now",
        severity = result.severity,
        reviewStatus = if (result.humanReview.required) ReviewStatus.PENDING else ReviewStatus.NOT_REQUIRED,
        slaRemainingMinutes = 15,
        piiCode = if (result.checks.privacyLeak.status == CheckStatus.DETECTED) "PII-04" else "GOV-01",
        piiTitle = result.overallEvaluation,
        piiDescription = result.recommendedAction,
        confidencePercent = 97.5f,
        policyVersion = "Governance Ruleset v1.0",
        customerQuery = result.userMessage,
        blockedResponse = result.aiResponse,
        recommendedCorrection = result.resolution.correctedResponse.ifBlank { "Safe response pending human supervisor review." },
        autoCorrectionFailedReason = if (result.resolution.attempted && !result.resolution.canResolve) result.resolution.reason else null,
        assignedTo = "Maya Patel",
        isResolved = false,
        isEscalated = result.decision == GovernanceDecision.ESCALATE,
        timeline = listOf(
          TimelineItem(
            time = "Now",
            title = "Risk detected; response ${result.decision.name.lowercase()}",
            description = result.overallEvaluation,
            dotColorType = "red"
          ),
          if (result.humanReview.required) {
            TimelineItem(
              time = "Now",
              title = "Escalated to human review queue",
              description = "Awaiting decision by Maya Patel.",
              dotColorType = "blue"
            )
          } else {
            TimelineItem(
              time = "Now",
              title = "Automated resolution applied",
              description = result.resolution.reason,
              dotColorType = "green"
            )
          }
        )
      )
      _incidents.update { listOf(newRecord) + it }
      _selectedIncidentId.value = newId
    }
  }
}
