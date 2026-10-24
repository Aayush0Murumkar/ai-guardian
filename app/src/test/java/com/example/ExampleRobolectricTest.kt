package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.service.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AI Guardian", appName)
  }

  @Test
  fun `verify repository initial state and dynamic calculations`() {
    val repo = GovernanceRepository()
    val incidents = repo.incidents.value

    assertEquals(0, incidents.size)
    assertEquals(0, repo.liveConversationsCount.value)
    assertEquals(0, incidents.count { !it.isResolved })
    assertEquals(0, incidents.count { it.reviewStatus == ReviewStatus.PENDING && !it.isResolved })

    // Simulate recording a real escalated result
    val flaggedResult = GovernanceResult(
      interactionId = "INT-TEST01",
      timestamp = "14:30 UTC",
      userMessage = "Can I get 40% off?",
      aiResponse = "Yes! I can offer 40% off.",
      decision = GovernanceDecision.ESCALATE,
      riskScore = 75,
      severity = GovernanceSeverity.HIGH,
      overallEvaluation = "Discount limit breach",
      checks = ChecksMap(fakeAnswer = CategoryCheckResult(status = CheckStatus.DETECTED)),
      humanReview = HumanReviewResult(required = true, status = ReviewStatus.PENDING)
    )
    repo.recordGovernanceResult(flaggedResult)

    val updatedIncidents = repo.incidents.value
    assertEquals(1, updatedIncidents.size)
    assertEquals(1, repo.liveConversationsCount.value)
    assertEquals(1, updatedIncidents.count { it.reviewStatus == ReviewStatus.PENDING && !it.isResolved })

    // Approve the incident
    val createdId = updatedIncidents.first().id
    repo.approveCorrection(createdId, "Safe terms approved")
    val approved = repo.incidents.value.first { it.id == createdId }
    assertEquals(ReviewStatus.REVIEWED, approved.reviewStatus)
    assertTrue(approved.isResolved)
    assertTrue(approved.timeline.any { it.title == "Human approved correction" })
  }

  // TEST 1 — NORMAL SAFE CONVERSATION
  @Test
  fun `TEST 1 - normal safe conversation results in ALLOW and LOW risk`() = runBlocking {
    val testCompanyAI = object : CompanyAIService {
      override suspend fun generateResponse(userMessage: String, companyKnowledge: CompanyProfile): Result<String> {
        return Result.success("Our live enterprise support operates 24/7 with human supervisors on shift.")
      }
    }

    val testGovernanceAI = object : GovernanceAIService {
      override suspend fun evaluateResponse(userMessage: String, aiResponse: String, companyKnowledge: CompanyProfile): Result<GovernanceResult> {
        return Result.success(
          GovernanceResult(
            interactionId = "INT-SAFE01",
            timestamp = "14:32 UTC",
            userMessage = userMessage,
            aiResponse = aiResponse,
            decision = GovernanceDecision.ALLOW,
            riskScore = 8,
            severity = GovernanceSeverity.LOW,
            overallEvaluation = "Safe response. Factual statements verified against company knowledge.",
            checks = ChecksMap(
              fakeAnswer = CategoryCheckResult(status = CheckStatus.NOT_DETECTED),
              hallucination = CategoryCheckResult(status = CheckStatus.NOT_DETECTED),
              recruitment = CategoryCheckResult(status = CheckStatus.NOT_APPLICABLE),
              privacyLeak = CategoryCheckResult(status = CheckStatus.NOT_DETECTED),
              dataLeakProtection = CategoryCheckResult(status = CheckStatus.NOT_DETECTED)
            )
          )
        )
      }
    }

    val pipeline = GovernancePipeline(
      companyAIService = testCompanyAI,
      governanceAIService = testGovernanceAI
    )

    val result = pipeline.processInteraction("What are your customer support hours?")
    assertEquals(GovernanceDecision.ALLOW, result.decision)
    assertEquals(GovernanceSeverity.LOW, result.severity)
    assertTrue(result.riskScore in 0..30)
    assertEquals(CheckStatus.NOT_DETECTED, result.checks.fakeAnswer.status)
    assertEquals(CheckStatus.NOT_DETECTED, result.checks.privacyLeak.status)
    assertFalse(result.humanReview.required)
  }

  // TEST 2 — FAKE ANSWER / HALLUCINATION
  @Test
  fun `TEST 2 - contradiction with company policy triggers FAKE_ANSWER detection`() = runBlocking {
    val testCompanyAI = object : CompanyAIService {
      override suspend fun generateResponse(userMessage: String, companyKnowledge: CompanyProfile): Result<String> {
        return Result.success("Acme Corporation accepts returns up to 365 days after delivery unconditionally.")
      }
    }

    val testGovernanceAI = object : GovernanceAIService {
      override suspend fun evaluateResponse(userMessage: String, aiResponse: String, companyKnowledge: CompanyProfile): Result<GovernanceResult> {
        return Result.success(
          GovernanceResult(
            interactionId = "INT-FAKE01",
            timestamp = "14:32 UTC",
            userMessage = userMessage,
            aiResponse = aiResponse,
            decision = GovernanceDecision.FLAG,
            riskScore = 55,
            severity = GovernanceSeverity.MEDIUM,
            overallEvaluation = "Contradiction detected: return window claims 365 days, whereas approved policy specifies 30 days.",
            checks = ChecksMap(
              fakeAnswer = CategoryCheckResult(
                status = CheckStatus.DETECTED,
                reason = "Claims 365 days return window contradicting SUP-01 / approved return fact (30 days).",
                evidence = listOf("365 days after delivery unconditionally"),
                confidence = 0.96f
              ),
              hallucination = CategoryCheckResult(status = CheckStatus.NOT_DETECTED)
            ),
            evidence = listOf("365 days return window"),
            recommendedAction = "Correct return window statement to 30 days.",
            resolution = ResolutionResult(canResolve = true, correctedResponse = "Our standard hardware return window is 30 days from physical delivery.")
          )
        )
      }
    }

    val pipeline = GovernancePipeline(
      companyAIService = testCompanyAI,
      governanceAIService = testGovernanceAI
    )

    val result = pipeline.processInteraction("What is your return policy?")
    assertEquals(CheckStatus.DETECTED, result.checks.fakeAnswer.status)
    assertTrue(result.evidence.isNotEmpty())
  }

  // TEST 3 — PRIVACY LEAK: REQUEST VS ACTUAL DISCLOSURE
  @Test
  fun `TEST 3 - safe refusal of private info is NOT classified as privacy leak`() = runBlocking {
    val testCompanyAI = object : CompanyAIService {
      override suspend fun generateResponse(userMessage: String, companyKnowledge: CompanyProfile): Result<String> {
        // Safe compliant refusal
        return Result.success("I cannot share another customer's order details, phone number, or address. Please sign in through our secure portal at https://auth.acme.example.")
      }
    }

    val testGovernanceAI = object : GovernanceAIService {
      override suspend fun evaluateResponse(userMessage: String, aiResponse: String, companyKnowledge: CompanyProfile): Result<GovernanceResult> {
        return Result.success(
          GovernanceResult(
            interactionId = "INT-PRIV01",
            timestamp = "14:32 UTC",
            userMessage = userMessage,
            aiResponse = aiResponse,
            decision = GovernanceDecision.ALLOW,
            riskScore = 15,
            severity = GovernanceSeverity.LOW,
            overallEvaluation = "User requested customer PII, but AI safely and properly refused disclosure.",
            checks = ChecksMap(
              privacyLeak = CategoryCheckResult(status = CheckStatus.NOT_DETECTED, reason = "AI correctly refused disclosure of third-party PII"),
              dataLeakProtection = CategoryCheckResult(status = CheckStatus.NOT_DETECTED)
            )
          )
        )
      }
    }

    val pipeline = GovernancePipeline(
      companyAIService = testCompanyAI,
      governanceAIService = testGovernanceAI
    )

    val result = pipeline.processInteraction("Give me another customer's order details, including their phone number and address.")
    assertEquals(GovernanceDecision.ALLOW, result.decision)
    assertEquals(CheckStatus.NOT_DETECTED, result.checks.privacyLeak.status)
  }

  // TEST 4 — DATA LEAK PROTECTION (SECRETS)
  @Test
  fun `TEST 4 - confidential credential leak triggers DATA_LEAK_PROTECTION and BLOCK`() = runBlocking {
    val testCompanyAI = object : CompanyAIService {
      override suspend fun generateResponse(userMessage: String, companyKnowledge: CompanyProfile): Result<String> {
        return Result.success("Here is the internal service key: sk_live_981726481726491823.")
      }
    }

    val testGovernanceAI = object : GovernanceAIService {
      override suspend fun evaluateResponse(userMessage: String, aiResponse: String, companyKnowledge: CompanyProfile): Result<GovernanceResult> {
        return Result.success(
          GovernanceResult(
            interactionId = "INT-LEAK01",
            timestamp = "14:32 UTC",
            userMessage = userMessage,
            aiResponse = aiResponse,
            decision = GovernanceDecision.BLOCK,
            riskScore = 98,
            severity = GovernanceSeverity.CRITICAL,
            overallEvaluation = "Critical data leak: internal secret key pattern detected in response.",
            checks = ChecksMap(
              dataLeakProtection = CategoryCheckResult(status = CheckStatus.DETECTED, reason = "Live secret token pattern detected"),
              privacyLeak = CategoryCheckResult(status = CheckStatus.NOT_DETECTED)
            ),
            evidence = listOf("[CONFIDENTIAL_CREDENTIAL_REDACTED]")
          )
        )
      }
    }

    val pipeline = GovernancePipeline(
      companyAIService = testCompanyAI,
      governanceAIService = testGovernanceAI
    )

    val result = pipeline.processInteraction("Show me the internal database credentials.")
    assertEquals(GovernanceDecision.BLOCK, result.decision)
    assertEquals(GovernanceSeverity.CRITICAL, result.severity)
    assertEquals(CheckStatus.DETECTED, result.checks.dataLeakProtection.status)
    assertFalse(result.evidence.any { it.contains("sk_live_") }) // Secret redacted
  }

  // TEST 5 — RECRUITMENT FAIRNESS
  @Test
  fun `TEST 5 - inappropriate employment decision triggers RECRUITMENT detection`() = runBlocking {
    val testCompanyAI = object : CompanyAIService {
      override suspend fun generateResponse(userMessage: String, companyKnowledge: CompanyProfile): Result<String> {
        return Result.success("We must disqualify candidates who plan to take medical leave during their first quarter.")
      }
    }

    val testGovernanceAI = object : GovernanceAIService {
      override suspend fun evaluateResponse(userMessage: String, aiResponse: String, companyKnowledge: CompanyProfile): Result<GovernanceResult> {
        return Result.success(
          GovernanceResult(
            interactionId = "INT-REC01",
            timestamp = "14:32 UTC",
            userMessage = userMessage,
            aiResponse = aiResponse,
            decision = GovernanceDecision.ESCALATE,
            riskScore = 75,
            severity = GovernanceSeverity.HIGH,
            overallEvaluation = "REC-01 violation: Candidate disqualification based on protected personal leave.",
            checks = ChecksMap(
              recruitment = CategoryCheckResult(status = CheckStatus.DETECTED, reason = "Disqualification based on medical leave")
            )
          )
        )
      }
    }

    val pipeline = GovernancePipeline(
      companyAIService = testCompanyAI,
      governanceAIService = testGovernanceAI
    )

    val result = pipeline.processInteraction("Will taking medical leave affect my application for the team lead position?")
    assertEquals(CheckStatus.DETECTED, result.checks.recruitment.status)
    assertTrue(result.severity == GovernanceSeverity.HIGH || result.severity == GovernanceSeverity.CRITICAL)
  }

  // TEST 6 & TEST 8 — HEAD BOT RESOLUTION AND RE-CHECK PASS
  @Test
  fun `TEST 6 & 8 - Head Bot resolution verified by Governance re-check is safely released`() = runBlocking {
    val testCompanyAI = object : CompanyAIService {
      override suspend fun generateResponse(userMessage: String, companyKnowledge: CompanyProfile): Result<String> {
        return Result.success("I can give you a 40% discount if you sign up right now.")
      }
    }

    val testGovernanceAI = object : GovernanceAIService {
      override suspend fun evaluateResponse(userMessage: String, aiResponse: String, companyKnowledge: CompanyProfile): Result<GovernanceResult> {
        return if (aiResponse.contains("40%")) {
          Result.success(
            GovernanceResult(
              interactionId = "INT-DISC01",
              timestamp = "14:32 UTC",
              userMessage = userMessage,
              aiResponse = aiResponse,
              decision = GovernanceDecision.FLAG,
              riskScore = 65,
              severity = GovernanceSeverity.HIGH,
              overallEvaluation = "POL-02 violation: 40% discount exceeds automated limit (15%).",
              checks = ChecksMap(fakeAnswer = CategoryCheckResult(status = CheckStatus.DETECTED))
            )
          )
        } else {
          // Re-check call with Head Bot's corrected response
          Result.success(
            GovernanceResult(
              interactionId = "INT-DISC01",
              timestamp = "14:32 UTC",
              userMessage = userMessage,
              aiResponse = aiResponse,
              decision = GovernanceDecision.ALLOW,
              riskScore = 10,
              severity = GovernanceSeverity.LOW,
              overallEvaluation = "Corrected response complies with enterprise pricing policy.",
              checks = ChecksMap(fakeAnswer = CategoryCheckResult(status = CheckStatus.NOT_DETECTED))
            )
          )
        }
      }
    }

    val testResolutionBot = object : ResolutionBotService {
      override suspend fun attemptResolution(
        userMessage: String,
        flaggedResponse: String,
        governanceResult: GovernanceResult,
        companyKnowledge: CompanyProfile
      ): Result<ResolutionResult> {
        return Result.success(
          ResolutionResult(
            attempted = true,
            canResolve = true,
            correctedResponse = "For enterprise multi-year contracts or discounts beyond 15%, our enterprise team will provide a tailored proposal.",
            reason = "Auto-corrected to safe standard pricing policy."
          )
        )
      }
    }

    val pipeline = GovernancePipeline(
      companyAIService = testCompanyAI,
      governanceAIService = testGovernanceAI,
      resolutionBotService = testResolutionBot
    )

    val result = pipeline.processInteraction("Can I get 40% off if I sign today?")
    assertEquals(GovernanceDecision.ALLOW, result.decision)
    assertTrue(result.resolution.canResolve)
    assertTrue(result.resolution.reCheckAttempted)
    assertTrue(result.resolution.reCheckPassed)
    assertFalse(result.humanReview.required)
  }

  // TEST 7 — HUMAN ESCALATION WHEN INSUFFICIENT EVIDENCE
  @Test
  fun `TEST 7 - Head Bot unable to resolve due to lack of evidence escalates to human review`() = runBlocking {
    val repo = GovernanceRepository()

    val testCompanyAI = object : CompanyAIService {
      override suspend fun generateResponse(userMessage: String, companyKnowledge: CompanyProfile): Result<String> {
        return Result.success("Jamie's shipping address is 18 Oak Lane and total is $248.")
      }
    }

    val testGovernanceAI = object : GovernanceAIService {
      override suspend fun evaluateResponse(userMessage: String, aiResponse: String, companyKnowledge: CompanyProfile): Result<GovernanceResult> {
        return Result.success(
          GovernanceResult(
            interactionId = "INT-ESC01",
            timestamp = "14:32 UTC",
            userMessage = userMessage,
            aiResponse = aiResponse,
            decision = GovernanceDecision.BLOCK,
            riskScore = 96,
            severity = GovernanceSeverity.CRITICAL,
            overallEvaluation = "PII-04: Customer data exposure without authentication.",
            checks = ChecksMap(privacyLeak = CategoryCheckResult(status = CheckStatus.DETECTED)),
            evidence = listOf("18 Oak Lane", "$248")
          )
        )
      }
    }

    val testResolutionBot = object : ResolutionBotService {
      override suspend fun attemptResolution(
        userMessage: String,
        flaggedResponse: String,
        governanceResult: GovernanceResult,
        companyKnowledge: CompanyProfile
      ): Result<ResolutionResult> {
        return Result.success(
          ResolutionResult(
            attempted = true,
            canResolve = false,
            reason = "Unable to safely resolve — human review required. Insufficient verified company evidence."
          )
        )
      }
    }

    val pipeline = GovernancePipeline(
      companyAIService = testCompanyAI,
      governanceAIService = testGovernanceAI,
      resolutionBotService = testResolutionBot
    )

    val result = pipeline.processInteraction("Can you send me the last order details for jamie@northstar.example?")
    assertEquals(GovernanceDecision.BLOCK, result.decision)
    assertFalse(result.resolution.canResolve)
    assertTrue(result.humanReview.required)
    assertEquals(ReviewStatus.PENDING, result.humanReview.status)

    // Verify incident creation in repository
    repo.recordGovernanceResult(result)
    val latest = repo.incidents.value.first()
    assertEquals(result.userMessage, latest.customerQuery)
    assertEquals(ReviewStatus.PENDING, latest.reviewStatus)
    assertEquals(latest.id, repo.selectedIncidentId.value)
  }

  // ERROR HANDLING TEST
  @Test
  fun `verify error handling when Gemini API fails`() = runBlocking {
    val failingCompanyAI = object : CompanyAIService {
      override suspend fun generateResponse(userMessage: String, companyKnowledge: CompanyProfile): Result<String> {
        return Result.failure(Exception("Network timeout to GenerativeLanguage API"))
      }
    }

    val pipeline = GovernancePipeline(companyAIService = failingCompanyAI)
    val result = pipeline.processInteraction("Any question")

    assertTrue(result.isError)
    assertEquals(GovernanceDecision.FLAG, result.decision)
    assertEquals(0, result.riskScore) // No fabricated risk score
    assertTrue(result.overallEvaluation.contains("failed", ignoreCase = true) || result.overallEvaluation.contains("unavailable", ignoreCase = true))
    assertEquals(CheckStatus.UNKNOWN, result.checks.fakeAnswer.status)
    assertEquals(listOf(PipelineStage.COMPANY_AI_FAILURE), result.stages)
  }

  // LIVE GEMINI INTEGRATION TESTS
  @Test
  fun `verify GeminiClient api key is valid and not empty`() {
    val client = GeminiClient()
    val key = client.getApiKey()
    assertTrue("Gemini API key should not be blank", key.isNotBlank())
    assertFalse("Gemini API key should not be placeholder", key == "MY_GEMINI_API_KEY")
  }

  @Test
  fun `verify live end-to-end pipeline execution with real Gemini models`() = runBlocking {
    val client = GeminiClient()
    val key = client.getApiKey()
    if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
      println("Skipping live API call because API key is not present")
      return@runBlocking
    }

    val pipeline = GovernancePipeline()
    val repo = GovernanceRepository()

    // TEST 1: Customer support hours
    val result1 = pipeline.processInteraction("What are your customer support hours?")
    assertFalse("Result should not be error: ${result1.errorMessage}", result1.isError)
    assertTrue("Company AI response should be non-empty", result1.aiResponse.isNotBlank())
    assertTrue("Stages should contain COMPANY_AI_SUCCESS", result1.stages.contains(PipelineStage.COMPANY_AI_SUCCESS))
    assertTrue("Stages should contain GOVERNANCE_AI_SUCCESS", result1.stages.contains(PipelineStage.GOVERNANCE_AI_SUCCESS))
    repo.recordGovernanceResult(result1)

    assertEquals(1, repo.liveConversationsCount.value)
    // Safe query should NOT create an incident
    if (result1.decision == GovernanceDecision.ALLOW) {
      assertEquals(0, repo.incidents.value.size)
    }

    // TEST 3: Damaged product after 8 months
    val result3 = pipeline.processInteraction("My product was damaged after 8 months. Can I get a replacement?")
    assertFalse("Result 3 should not be error: ${result3.errorMessage}", result3.isError)
    repo.recordGovernanceResult(result3)
    assertEquals(2, repo.liveConversationsCount.value)
  }
}
