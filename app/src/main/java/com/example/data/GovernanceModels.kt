package com.example.data

enum class GovernanceDecision {
  ALLOW,
  FLAG,
  BLOCK,
  ESCALATE
}

enum class GovernanceSeverity {
  LOW,
  MEDIUM,
  HIGH,
  CRITICAL
}

enum class CheckStatus {
  DETECTED,
  NOT_DETECTED,
  UNKNOWN,
  NOT_APPLICABLE,
  INSUFFICIENT_EVIDENCE
}

enum class ReviewStatus {
  NOT_REQUIRED,
  PENDING,
  REVIEWED
}

data class CategoryCheckResult(
  val status: CheckStatus = CheckStatus.NOT_APPLICABLE,
  val reason: String = "",
  val evidence: List<String> = emptyList(),
  val confidence: Float = 0f
)

data class ChecksMap(
  val fakeAnswer: CategoryCheckResult = CategoryCheckResult(status = CheckStatus.NOT_DETECTED),
  val hallucination: CategoryCheckResult = CategoryCheckResult(status = CheckStatus.NOT_DETECTED),
  val recruitment: CategoryCheckResult = CategoryCheckResult(status = CheckStatus.NOT_APPLICABLE),
  val privacyLeak: CategoryCheckResult = CategoryCheckResult(status = CheckStatus.NOT_DETECTED),
  val dataLeakProtection: CategoryCheckResult = CategoryCheckResult(status = CheckStatus.NOT_DETECTED)
)

data class ResolutionResult(
  val attempted: Boolean = false,
  val canResolve: Boolean = false,
  val correctedResponse: String = "",
  val reason: String = "",
  val confidence: Float = 0f,
  val reCheckAttempted: Boolean = false,
  val reCheckPassed: Boolean = false,
  val reCheckNotes: String = ""
)

data class HumanReviewResult(
  val required: Boolean = false,
  val reason: String = "",
  val status: ReviewStatus = ReviewStatus.NOT_REQUIRED,
  val reviewerName: String? = null,
  val reviewNote: String? = null
)

data class IncidentResult(
  val created: Boolean = false,
  val incidentId: String = "",
  val severity: String = "",
  val status: String = ""
)

data class ReportResult(
  val generated: Boolean = false,
  val reportId: String = "",
  val generatedAt: String = ""
)

enum class PipelineStage {
  COMPANY_AI_SUCCESS,
  COMPANY_AI_FAILURE,
  GOVERNANCE_AI_SUCCESS,
  GOVERNANCE_AI_FAILURE,
  RESOLUTION_SUCCESS,
  RESOLUTION_FAILURE,
  RECHECK_SUCCESS,
  RECHECK_FAILURE
}

data class GovernanceResult(
  val interactionId: String,
  val timestamp: String,
  val userMessage: String,
  val aiResponse: String,
  val decision: GovernanceDecision,
  val riskScore: Int,
  val severity: GovernanceSeverity,
  val overallEvaluation: String,
  val checks: ChecksMap,
  val evidence: List<String> = emptyList(),
  val recommendedAction: String = "",
  val resolution: ResolutionResult = ResolutionResult(),
  val humanReview: HumanReviewResult = HumanReviewResult(),
  val incident: IncidentResult = IncidentResult(),
  val report: ReportResult = ReportResult(),
  val isError: Boolean = false,
  val errorMessage: String? = null,
  val stages: List<PipelineStage> = emptyList(),
  val stageDetails: String? = null,
  val isDemoScenario: Boolean = false
)

data class TimelineItem(
  val time: String,
  val title: String,
  val description: String,
  val dotColorType: String // "red", "amber", "blue", "gray", "green"
)

data class IncidentRecord(
  val id: String,
  val title: String,
  val botName: String,
  val timestampUtc: String,
  val severity: GovernanceSeverity,
  val reviewStatus: ReviewStatus,
  val slaRemainingMinutes: Int,
  val piiCode: String,
  val piiTitle: String,
  val piiDescription: String,
  val confidencePercent: Float,
  val policyVersion: String,
  val customerQuery: String,
  val blockedResponse: String,
  val recommendedCorrection: String,
  val autoCorrectionFailedReason: String?,
  val retryCount: Int = 1,
  val assignedTo: String = "Maya Patel",
  val reviewNote: String = "",
  val isResolved: Boolean = false,
  val isEscalated: Boolean = true,
  val timeline: List<TimelineItem> = emptyList()
)
