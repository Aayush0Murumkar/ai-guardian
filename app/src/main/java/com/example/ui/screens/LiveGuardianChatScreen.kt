package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.DashboardScreen
import com.example.ui.GovernanceViewModel
import com.example.ui.theme.*

@Composable
fun LiveGuardianChatScreen(
  viewModel: GovernanceViewModel,
  modifier: Modifier = Modifier
) {
  val isProcessing by viewModel.isProcessing.collectAsState()
  val latestResult by viewModel.latestResult.collectAsState()
  val liveInteractions by viewModel.liveInteractions.collectAsState()

  var inputPrompt by remember { mutableStateOf("") }

  val samplePrompts = listOf(
    "What is your return window, and do refunds require manager approval?",
    "What are your customer support hours?",
    "What is your return policy?",
    "My product was damaged after 8 months. Can I get a replacement?",
    "Can you send me the last order details for jamie@northstar.example?",
    "Can I get 35% off if I sign for 3 years today?"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BrandBg)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
  ) {
    // Header
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "DYNAMIC GOVERNANCE RUNNER",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = BrandMuted,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Live AI Guard",
          fontSize = 28.sp,
          fontWeight = FontWeight.ExtraBold,
          color = BrandNavy
        )
        Text(
          text = "Test actual conversations through Company AI → Governance Engine → Resolution Bot.",
          fontSize = 13.5.sp,
          color = BrandSecondary
        )
      }
    }

    // Input Box & Sample Prompt Chips
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Enter user query to evaluate:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BrandNavy
          )

          OutlinedTextField(
            value = inputPrompt,
            onValueChange = { inputPrompt = it },
            placeholder = { Text("Ask a question to test the governance pipeline...", fontSize = 12.sp, color = BrandSubtle) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("live_chat_input"),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor = BrandBorder,
              focusedBorderColor = BrandPrimary
            )
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = {
                viewModel.runLiveInteraction(inputPrompt)
              },
              enabled = inputPrompt.isNotBlank() && !isProcessing,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
              modifier = Modifier.testTag("send_interaction_button")
            ) {
              if (isProcessing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  color = Color.White,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Evaluating...")
              } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run Governance Check")
              }
            }

            if (inputPrompt.isNotBlank()) {
              TextButton(onClick = { inputPrompt = "" }) {
                Text("Clear", color = BrandSecondary, fontSize = 12.sp)
              }
            }
          }

          Text(
            text = "Try an example query:",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandSecondary
          )

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            samplePrompts.forEach { prompt ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(BrandBg)
                  .border(1.dp, BrandBorder, RoundedCornerShape(8.dp))
                  .clickable {
                    inputPrompt = prompt
                  }
                  .padding(horizontal = 10.dp, vertical = 8.dp)
              ) {
                Text(
                  text = "“$prompt”",
                  fontSize = 12.sp,
                  color = BrandDark
                )
              }
            }
          }
        }
      }
    }

    // Latest Result Display
    item {
      latestResult?.let { res ->
        GovernanceResultCard(res, onOpenReview = {
          viewModel.navigateTo(DashboardScreen.REVIEW)
        })
      }
    }

    // History Log
    if (liveInteractions.isNotEmpty()) {
      item {
        Text(
          text = "Recent Evaluated Interactions (${liveInteractions.size})",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = BrandNavy
        )
      }

      items(liveInteractions, key = { it.interactionId }) { item ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(item.interactionId, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
              DecisionBadge(item.decision)
            }
            Text("User: ${item.userMessage}", fontSize = 12.sp, color = BrandDark)
            Text("Result: ${item.overallEvaluation}", fontSize = 11.5.sp, color = BrandSecondary)
          }
        }
      }
    }
  }
}

@Composable
fun GovernanceResultCard(
  res: GovernanceResult,
  onOpenReview: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBorder)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(res.interactionId, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
          Text(res.timestamp, fontSize = 11.sp, color = BrandSecondary)
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (res.isDemoScenario) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(BrandCriticalBg)
                .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Text(
                text = "DEMO / BLOCKED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrandCriticalText
              )
            }
          }
          DecisionBadge(res.decision)
        }
      }

      if (res.stages.isNotEmpty()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Stages:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandSecondary)
          res.stages.forEach { stage ->
            val isSuccess = !stage.name.endsWith("_FAILURE")
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isSuccess) BrandMintBg else BrandCriticalBg)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = stage.name.replace("_", " "),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSuccess) BrandMintDark else BrandCriticalText
              )
            }
          }
        }
      }

      if (res.isError) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BrandCriticalBg)
            .border(1.dp, BrandCriticalBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = BrandCriticalRed)
              Text(
                text = "Pipeline Execution Failure",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = BrandCriticalText
              )
            }
            Text(
              text = res.errorMessage ?: res.overallEvaluation,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = BrandCriticalText
            )
            if (!res.stageDetails.isNullOrBlank()) {
              Text(
                text = "Stage: ${res.stages.lastOrNull()?.name ?: "UNKNOWN"} · ${res.stageDetails}",
                fontSize = 11.sp,
                color = BrandCriticalText.copy(alpha = 0.85f)
              )
            }
          }
        }
      }

      // Display Real Company AI Generated Response
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Real Company AI Response", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, BrandBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
        ) {
          Text(
            text = res.aiResponse,
            fontSize = 12.5.sp,
            color = BrandDark,
            lineHeight = 17.sp
          )
        }
      }

      // Risk score and severity
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(BrandBg)
            .padding(10.dp)
        ) {
          Column {
            Text("Risk Score", fontSize = 11.sp, color = BrandSecondary)
            Text("${res.riskScore} / 100", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
          }
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(BrandBg)
            .padding(10.dp)
        ) {
          Column {
            Text("Severity", fontSize = 11.sp, color = BrandSecondary)
            Text(res.severity.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandCriticalText)
          }
        }
      }

      // Overall Evaluation
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("AI Evaluation", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (res.decision == GovernanceDecision.ALLOW) BrandMintBg else BrandWarningBg)
            .padding(12.dp)
        ) {
          Text(
            text = res.overallEvaluation,
            fontSize = 12.5.sp,
            color = if (res.decision == GovernanceDecision.ALLOW) BrandMintDark else BrandWarningText,
            lineHeight = 17.sp
          )
        }
      }

      // Check Categories Breakdown
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Governance Checks (5 detection categories):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)

        CategoryRow("Fake Answer", res.checks.fakeAnswer)
        CategoryRow("Hallucination", res.checks.hallucination)
        CategoryRow("Recruitment", res.checks.recruitment)
        CategoryRow("Privacy Leak", res.checks.privacyLeak)
        CategoryRow("Data Leak Protection", res.checks.dataLeakProtection)
      }

      if (res.evidence.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("Detected Evidence:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
          res.evidence.forEach { ev ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(BrandWarningBg)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(text = "• $ev", fontSize = 11.5.sp, color = BrandWarningText)
            }
          }
        }
      }

      if (res.recommendedAction.isNotBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("Recommended Action:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
          Text(text = res.recommendedAction, fontSize = 11.5.sp, color = BrandSecondary)
        }
      }

      // Resolution Bot outcome & Governance Re-Check
      if (res.resolution.attempted) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Role 3: Head / Resolution Bot", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(BrandBlueLight)
              .border(1.dp, BrandBlueBorder, RoundedCornerShape(10.dp))
              .padding(12.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (res.resolution.canResolve) "Can Resolve: YES (Verified Policy Grounded)" else "Can Resolve: FALSE (Insufficient Evidence)",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (res.resolution.canResolve) BrandAccentBlue else BrandHighText
                )
              }

              if (res.resolution.correctedResponse.isNotBlank()) {
                Text("HEAD BOT CORRECTION:", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BrandSecondary)
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .border(1.dp, BrandBorder, RoundedCornerShape(6.dp))
                    .padding(8.dp)
                ) {
                  Text(
                    text = "“${res.resolution.correctedResponse}”",
                    fontSize = 12.sp,
                    color = BrandDark
                  )
                }
              }

              Text(text = "Reason: ${res.resolution.reason}", fontSize = 11.sp, color = BrandSecondary)

              // Governance Re-Check Audit
              if (res.resolution.reCheckAttempted) {
                HorizontalDivider(color = BrandBlueBorder.copy(alpha = 0.5f))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(
                      imageVector = if (res.resolution.reCheckPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                      contentDescription = null,
                      tint = if (res.resolution.reCheckPassed) BrandMintDark else BrandCriticalRed,
                      modifier = Modifier.size(14.dp)
                    )
                    Text(
                      text = if (res.resolution.reCheckPassed) "GOVERNANCE RE-CHECK: PASSED" else "GOVERNANCE RE-CHECK: FAILED",
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (res.resolution.reCheckPassed) BrandMintDark else BrandCriticalText
                    )
                  }
                  if (res.resolution.reCheckNotes.isNotBlank()) {
                    Text(
                      text = res.resolution.reCheckNotes,
                      fontSize = 11.sp,
                      color = BrandSecondary
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Banner & button to open review workspace if human review is needed
      if (res.humanReview.required) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(BrandWarningBg)
              .border(1.dp, BrandWarningBorder, RoundedCornerShape(8.dp))
              .padding(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.Warning, contentDescription = null, tint = BrandWarningDark, modifier = Modifier.size(16.dp))
              Text(
                text = "Unable to safely resolve — human review required.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BrandWarningText
              )
            }
          }

          Button(
            onClick = onOpenReview,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            modifier = Modifier.fillMaxWidth().testTag("open_human_review_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text("Open In Human Review Workspace", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
fun DecisionBadge(decision: GovernanceDecision) {
  val (bg, textColor) = when (decision) {
    GovernanceDecision.ALLOW -> Pair(BrandMintBg, BrandMintDark)
    GovernanceDecision.FLAG -> Pair(BrandAmberPillBg, BrandAmberPillText)
    GovernanceDecision.BLOCK -> Pair(BrandCriticalBg, BrandCriticalText)
    GovernanceDecision.ESCALATE -> Pair(BrandBlueLight, BrandAccentBlue)
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(bg)
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Text(
      text = decision.name,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = textColor
    )
  }
}

@Composable
fun CategoryRow(name: String, check: CategoryCheckResult) {
  val statusColor = when (check.status) {
    CheckStatus.DETECTED -> BrandCriticalRed
    CheckStatus.NOT_DETECTED -> BrandMintDark
    CheckStatus.UNKNOWN -> BrandWarningDark
    CheckStatus.NOT_APPLICABLE -> BrandSecondary
    CheckStatus.INSUFFICIENT_EVIDENCE -> BrandSubtle
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(BrandBg)
      .padding(horizontal = 10.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = BrandNavy)
      if (check.reason.isNotBlank()) {
        Text(check.reason, fontSize = 10.5.sp, color = BrandSecondary)
      }
    }
    Text(
      text = check.status.name.replace("_", " "),
      fontSize = 10.5.sp,
      fontWeight = FontWeight.Bold,
      color = statusColor
    )
  }
}
