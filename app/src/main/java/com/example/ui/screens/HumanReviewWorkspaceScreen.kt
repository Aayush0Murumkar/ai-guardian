package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Sort
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
import com.example.ui.GovernanceViewModel
import com.example.ui.theme.*

@Composable
fun HumanReviewWorkspaceScreen(
  viewModel: GovernanceViewModel,
  modifier: Modifier = Modifier
) {
  val incidents by viewModel.incidents.collectAsState()
  val activeIncident by viewModel.activeIncident.collectAsState()
  val awaitingReviewCount by viewModel.awaitingReviewCount.collectAsState()
  val resolvedTodayCount by viewModel.resolvedTodayCount.collectAsState()

  var reviewNoteText by remember { mutableStateOf("") }
  var actionStatusMessage by remember { mutableStateOf<String?>(null) }

  val pendingQueue = remember(incidents) {
    incidents.filter { it.reviewStatus == ReviewStatus.PENDING && !it.isResolved }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
  ) {
    // Top Header Section
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "ACME CORPORATION / PRODUCTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandMuted,
            letterSpacing = 0.8.sp
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(BrandMintDark)
            )
            Text(
              text = "Live · 14:32 UTC",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Medium,
              color = BrandDark
            )
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Human review workspace",
          fontSize = 28.sp,
          fontWeight = FontWeight.ExtraBold,
          color = BrandDark,
          letterSpacing = (-0.5).sp
        )
        Text(
          text = "Review escalations. Make the final call.",
          fontSize = 13.5.sp,
          color = BrandSecondary
        )
      }
    }

    // Metrics Overview (3-columns with dividers)
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Awaiting review", fontSize = 11.sp, color = BrandSecondary, fontWeight = FontWeight.Medium)
            Text("$awaitingReviewCount", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = BrandDark)
          }
          VerticalDivider(
            modifier = Modifier
              .height(44.dp)
              .padding(horizontal = 8.dp),
            color = BrandBorder
          )
          Column(modifier = Modifier.weight(1f)) {
            Text("Oldest waiting", fontSize = 11.sp, color = BrandSecondary, fontWeight = FontWeight.Medium)
            val oldest = pendingQueue.maxOfOrNull { it.slaRemainingMinutes } ?: 0
            Text("${oldest}m", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = BrandDark)
          }
          VerticalDivider(
            modifier = Modifier
              .height(44.dp)
              .padding(horizontal = 8.dp),
            color = BrandBorder
          )
          Column(modifier = Modifier.weight(1f)) {
            Text("Resolved today", fontSize = 11.sp, color = BrandSecondary, fontWeight = FontWeight.Medium)
            Text("$resolvedTodayCount", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = BrandDark)
          }
        }
      }
    }

    // Sub-bar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (pendingQueue.isEmpty()) "Review queue clear · 0 pending" else "Priority review · 1 of ${pendingQueue.size}",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = BrandSecondary
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BrandBlueLight)
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = "Assigned to you",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandPrimary
          )
        }
      }
    }

    // Active Incident Details Card
    item {
      if (activeIncident != null) {
        val inc = activeIncident!!
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBorder)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("active_incident_card")
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Header: Ticket & Actions
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = inc.id,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = BrandDark
                )
                if (inc.timestampUtc == "Just now") {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(BrandMintBg)
                      .border(1.dp, BrandMintBorder, RoundedCornerShape(4.dp))
                      .padding(horizontal = 8.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "LIVE INCIDENT",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = BrandMintDark
                    )
                  }
                }
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(BrandCriticalBg)
                    .border(1.dp, BrandCriticalBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = inc.severity.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandCriticalText
                  )
                }
              }
              Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = null,
                tint = BrandSecondary
              )
            }

            // Title & Meta
            Column {
              Text(
                text = inc.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDark
              )
              Spacer(modifier = Modifier.height(2.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.SmartToy,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = BrandSecondary
                )
                Text(
                  text = "${inc.botName} · ${inc.timestampUtc}",
                  fontSize = 12.sp,
                  color = BrandSecondary
                )
              }
            }

            // Status & SLA Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(BrandAmberPillBg)
                  .border(1.dp, BrandAmberBorder, RoundedCornerShape(16.dp))
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(BrandWarningDark)
                  )
                  Text(
                    text = if (inc.isResolved) "Resolved · Delivered" else "Awaiting human review",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandAmberPillText
                  )
                }
              }
              Text(
                text = if (inc.isResolved) "SLA Met" else "${inc.slaRemainingMinutes}m to SLA",
                fontSize = 12.sp,
                color = BrandSecondary
              )
            }

            // Yellow Warning Violation Callout
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BrandWarningBg)
                .border(1.dp, BrandWarningBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = BrandWarningDark,
                    modifier = Modifier.size(16.dp)
                  )
                  Text(
                    text = "${inc.piiCode} · ${inc.piiTitle}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandWarningText
                  )
                }
                Text(
                  text = inc.piiDescription,
                  fontSize = 12.sp,
                  color = BrandWarningText,
                  lineHeight = 16.sp
                )
                Text(
                  text = "Detection confidence ${inc.confidencePercent}% · ${inc.policyVersion}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = BrandWarningDark
                )
              }
            }

            // Chat Messages Preview
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              // Customer message
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "CUSTOMER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSecondary
                  )
                  Text(
                    text = "14:27:05",
                    fontSize = 11.sp,
                    color = BrandSubtle
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = inc.customerQuery,
                  fontSize = 12.sp,
                  color = BrandDark
                )
              }

              // Atlas support blocked message
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "${inc.botName.uppercase()} · BLOCKED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandCriticalText
                  )
                  Text(
                    text = "14:27:08",
                    fontSize = 11.sp,
                    color = BrandCriticalRed
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                    .background(BrandCriticalBg)
                    .border(
                      width = 2.dp,
                      color = BrandCriticalRed,
                      shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(10.dp)
                ) {
                  Text(
                    text = inc.blockedResponse,
                    fontSize = 12.sp,
                    color = BrandDark,
                    lineHeight = 16.sp
                  )
                }
              }
            }

            // Auto-correction Failed Notice
            if (inc.autoCorrectionFailedReason != null) {
              HorizontalDivider(color = BrandBorder)
              Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ErrorOutline,
                  contentDescription = null,
                  tint = BrandHighText,
                  modifier = Modifier.size(16.dp)
                )
                Column {
                  Text(
                    text = "Auto-correction failed validation",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDark
                  )
                  Text(
                    text = "14:28 · ${inc.autoCorrectionFailedReason}",
                    fontSize = 11.5.sp,
                    color = BrandSecondary,
                    lineHeight = 15.sp
                  )
                }
              }
            }
          }
        }
      } else {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBorder)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("active_incident_card")
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = BrandMintDark,
              modifier = Modifier.size(36.dp)
            )
            Text(
              text = "Review Queue Clear",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = BrandDark
            )
            Text(
              text = "No incidents currently require human review. When the Governance Engine detects unresolvable queries, they will appear here.",
              fontSize = 12.5.sp,
              color = BrandSecondary,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      }
    }

    // Your Decision Section
    if (activeIncident != null) {
      item {
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
            Text(
              text = "Your decision",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = BrandDark
            )
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(BrandBlueLight)
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = activeIncident?.assignedTo ?: "Maya Patel",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandPrimary
              )
            }
          }

          // Recommended Correction Sub-heading
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = null,
              tint = BrandSkyBlue,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Recommended correction",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = BrandDark
            )
          }

          // Suggested Message Bubble
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(BrandBlueLight)
              .border(1.dp, BrandBlueBorder, RoundedCornerShape(12.dp))
              .padding(14.dp)
          ) {
            Text(
              text = activeIncident?.recommendedCorrection ?: "",
              fontSize = 13.sp,
              color = BrandDark,
              lineHeight = 18.sp
            )
          }

          Text(
            text = "Replace the blocked reply and require verified sign-in. No customer data will be sent.",
            fontSize = 12.sp,
            color = BrandSecondary
          )

          // Review Note Textarea
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Review note (optional)",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BrandDark
            )
            OutlinedTextField(
              value = reviewNoteText,
              onValueChange = { reviewNoteText = it },
              placeholder = { Text("Add context for the audit trail...", fontSize = 12.sp, color = BrandSubtle) },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("review_note_input"),
              shape = RoundedCornerShape(12.dp),
              maxLines = 3,
              colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = BrandBorder,
                focusedBorderColor = BrandPrimary
              )
            )
          }

          // Feedback Status message if action clicked
          actionStatusMessage?.let { msg ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(BrandMintBg)
                .padding(8.dp)
            ) {
              Text(text = msg, fontSize = 12.sp, color = BrandMintDark, fontWeight = FontWeight.Medium)
            }
          }

          // Action Buttons
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Primary Approve Button
            Button(
              onClick = {
                viewModel.approveActiveCorrection(reviewNoteText)
                actionStatusMessage = "Correction approved and released to user session."
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("approve_correction_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = "Approve correction",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            // Secondary Action Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  viewModel.requestActiveChanges(reviewNoteText)
                  actionStatusMessage = "Changes requested. Returned to queue for review."
                },
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBorder)),
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .testTag("request_changes_button")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = BrandSecondary
                  )
                  Text(
                    text = "Request changes",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandDark
                  )
                }
              }

              val isResolved = activeIncident?.isResolved == true
              Button(
                onClick = {
                  if (isResolved) {
                    viewModel.resolveActiveIncident()
                    actionStatusMessage = "Incident marked fully resolved."
                  }
                },
                enabled = isResolved,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = BrandMintDark,
                  disabledContainerColor = BrandBg
                ),
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .testTag("resolve_button")
              ) {
                Text(
                  text = if (isResolved) "Resolved" else "Resolve",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isResolved) Color.White else BrandSubtle
                )
              }
            }
          }

          Text(
            text = "Approval sends the safe reply. Resolve becomes available after delivery is verified.",
            fontSize = 11.sp,
            color = BrandSubtle
          )
        }
      }
    }
  }

    // Incident Timeline Section
    item {
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
            Text(
              text = "Incident timeline",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = BrandDark
            )
            Text(
              text = "UTC · Audit trail",
              fontSize = 11.sp,
              color = BrandSubtle
            )
          }

          val timelineItems = activeIncident?.timeline ?: emptyList()
          if (timelineItems.isEmpty()) {
            Text(
              text = "No timeline events recorded yet.",
              fontSize = 12.sp,
              color = BrandSubtle
            )
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
              timelineItems.forEach { item ->
                TimelineRow(item)
              }
            }
          }
        }
      }
    }

    // Review Queue Section
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Review queue",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = BrandDark
            )
            Text(
              text = "${pendingQueue.size} pending",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = BrandSecondary
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Priority, then oldest first",
              fontSize = 12.sp,
              color = BrandSubtle
            )
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Sort,
              contentDescription = "Sort",
              tint = BrandSubtle,
              modifier = Modifier.size(16.dp)
            )
          }

          if (pendingQueue.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Queue is clear. No incidents currently waiting for review.",
                fontSize = 12.sp,
                color = BrandSubtle
              )
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              pendingQueue.forEach { inc ->
              val isSelected = inc.id == activeIncident?.id
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isSelected) BrandBlueSelectBg else Color.White)
                  .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) BrandBlueSelectBorder else BrandBorder,
                    shape = RoundedCornerShape(12.dp)
                  )
                  .clickable { viewModel.selectIncident(inc.id) }
                  .padding(14.dp)
                  .testTag("queue_card_${inc.id}")
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(text = inc.id, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                    Row(
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      if (inc.timestampUtc == "Just now") {
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandMintBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                          Text(
                            text = "LIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandMintDark
                          )
                        }
                      }
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(4.dp))
                          .background(if (inc.severity == GovernanceSeverity.CRITICAL) BrandCriticalBg else BrandHighBg)
                          .padding(horizontal = 6.dp, vertical = 2.dp)
                      ) {
                        Text(
                          text = inc.severity.name.lowercase().replaceFirstChar { it.uppercase() },
                          fontSize = 11.sp,
                          fontWeight = FontWeight.SemiBold,
                          color = if (inc.severity == GovernanceSeverity.CRITICAL) BrandCriticalText else BrandHighText
                        )
                      }
                    }
                  }
                  Text(text = inc.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(text = inc.botName, fontSize = 12.sp, color = BrandSecondary)
                    Text(text = "${inc.slaRemainingMinutes}m waiting", fontSize = 12.sp, color = BrandSecondary)
                  }
                  if (isSelected) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                      modifier = Modifier.padding(top = 4.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = BrandPrimary
                      )
                      Text(
                        text = "Assigned to you · ${inc.assignedTo}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandPrimary
                      )
                    }
                  }
                }
              }
            }
          }
        }
        }
      }
    }
  }
}
