package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.DashboardScreen
import com.example.ui.GovernanceViewModel
import com.example.ui.theme.*

@Composable
fun MonitoringOverviewScreen(
  viewModel: GovernanceViewModel,
  modifier: Modifier = Modifier
) {
  val incidents by viewModel.incidents.collectAsState()
  val activeIncident by viewModel.activeIncident.collectAsState()
  val liveConversationsCount by viewModel.liveConversationsCount.collectAsState()
  val openIncidentsCount by viewModel.openIncidentsCount.collectAsState()
  val awaitingReviewCount by viewModel.awaitingReviewCount.collectAsState()
  val resolvedTodayCount by viewModel.resolvedTodayCount.collectAsState()
  val criticalCount by viewModel.criticalCount.collectAsState()

  var selectedFilter by remember { mutableStateOf("ALL") }

  val escalatedCount = remember(incidents) { incidents.count { it.isEscalated && !it.isResolved } }
  val automatedCount = remember(incidents) { incidents.count { !it.isEscalated && !it.isResolved } }

  val filteredIncidents = remember(incidents, selectedFilter) {
    if (selectedFilter == "ESCALATED") {
      incidents.filter { it.isEscalated }
    } else {
      incidents
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BrandBg)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
  ) {
    // Top Page Header
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
              color = BrandMintDark
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Monitoring overview",
          fontSize = 28.sp,
          fontWeight = FontWeight.ExtraBold,
          color = BrandNavy,
          letterSpacing = (-0.5).sp
        )
        Text(
          text = "Every conversation. Clear signals. Safer AI.",
          fontSize = 13.5.sp,
          color = BrandMuted
        )
      }
    }

    // System Status Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(BrandMintBg)
          .border(1.dp, BrandMintBorder, RoundedCornerShape(12.dp))
          .padding(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Shield status",
            tint = BrandMintDark,
            modifier = Modifier.size(28.dp)
          )
          Column {
            Text(
              text = "All 12 bots connected",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Bold,
              color = BrandMintText
            )
            Text(
              text = "212 ms detection latency · Synced 7s ago",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = BrandMintSub
            )
          }
        }
      }
    }

    // Key Metrics Grid
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricCard(
            title = "Live conversations",
            value = liveConversationsCount.toString(),
            subtitle = if (liveConversationsCount > 0) "+$liveConversationsCount evaluated" else "Awaiting live interactions",
            icon = Icons.AutoMirrored.Filled.Chat,
            iconTint = BrandAccentBlue,
            modifier = Modifier.weight(1f)
          )
          MetricCard(
            title = "Open incidents",
            value = openIncidentsCount.toString(),
            subtitle = if (openIncidentsCount > 0) "$automatedCount automated · $escalatedCount escalated" else "0 automated · 0 escalated",
            icon = Icons.Default.Warning,
            iconTint = BrandHighText,
            modifier = Modifier.weight(1f)
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricCard(
            title = "Awaiting review",
            value = awaitingReviewCount.toString(),
            subtitle = "$criticalCount critical requires attention",
            icon = Icons.Default.PersonSearch,
            iconTint = BrandCriticalRed,
            modifier = Modifier.weight(1f)
          )
          MetricCard(
            title = "Resolved today",
            value = resolvedTodayCount.toString(),
            subtitle = "$resolvedTodayCount resolved by supervisor",
            icon = Icons.Default.CheckCircle,
            iconTint = BrandMintDark,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Requires Human Oversight Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Requires human oversight",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = BrandNavy
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BrandCriticalBg)
            .border(1.dp, BrandCriticalBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = "$criticalCount critical",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BrandCriticalText
          )
        }
      }
    }

    // Featured Incident Card (INC-1042 or currently active)
    item {
      if (activeIncident != null) {
        val inc = activeIncident!!
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBorder)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("featured_incident_card")
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Top Row
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
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = BrandNavy
                )
                if (inc.timestampUtc == "Just now") {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(BrandMintBg)
                      .border(1.dp, BrandMintBorder, RoundedCornerShape(4.dp))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "LIVE INCIDENT",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = BrandMintDark
                    )
                  }
                }
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(BrandCriticalBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
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
                contentDescription = "Options",
                tint = BrandSubtle
              )
            }

            // Title & Meta
            Column {
              Text(
                text = inc.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = BrandNavy
              )
              Text(
                text = "${inc.botName} · ${inc.timestampUtc}",
                fontSize = 12.sp,
                color = BrandMuted
              )
            }

            // SLA Status Row
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
                fontWeight = FontWeight.Medium,
                color = BrandSecondary
              )
            }

            // Warning Callout
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BrandWarningBg)
                .border(1.dp, BrandWarningBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                  imageVector = Icons.Default.Shield,
                  contentDescription = "Warning",
                  tint = BrandWarningDark,
                  modifier = Modifier.size(18.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  Text(
                    text = "${inc.piiCode} · ${inc.piiTitle}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandWarningText
                  )
                  Text(
                    text = inc.piiDescription,
                    fontSize = 11.5.sp,
                    color = BrandWarningText
                  )
                }
              }
            }

            // Conversation Logs
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "CUSTOMER",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSecondary,
                    letterSpacing = 0.5.sp
                  )
                  Text(
                    text = "14:27:05",
                    fontSize = 10.5.sp,
                    color = BrandSecondary
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = inc.customerQuery,
                  fontSize = 12.5.sp,
                  color = BrandDark,
                  lineHeight = 17.sp
                )
              }

              // Atlas Support Blocked Response
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
                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "${inc.botName.uppercase()} · BLOCKED",
                      fontSize = 10.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = BrandCriticalText,
                      letterSpacing = 0.5.sp
                    )
                    Text(
                      text = "14:27:08",
                      fontSize = 10.5.sp,
                      color = BrandCriticalText
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = inc.blockedResponse,
                    fontSize = 12.sp,
                    color = BrandCriticalDark,
                    lineHeight = 16.sp
                  )
                }
              }
            }

            // Recommended Correction
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = "Correction",
                  tint = BrandSkyBlue,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "Recommended correction",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = BrandNavy
                )
              }
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(BrandBlueLight)
                  .border(1.dp, BrandBlueBorder, RoundedCornerShape(12.dp))
                  .padding(12.dp)
              ) {
                Text(
                  text = inc.recommendedCorrection,
                  fontSize = 12.sp,
                  color = BrandAccentBlue,
                  lineHeight = 16.sp
                )
              }
            }

            // Auto-correction failure note if present
            if (inc.autoCorrectionFailedReason != null) {
              Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ErrorOutline,
                  contentDescription = "Error",
                  tint = BrandWarningDark,
                  modifier = Modifier.size(16.dp)
                )
                Column {
                  Text(
                    text = "Auto-correction failed validation",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandWarningText
                  )
                  Text(
                    text = "1 retry · ${inc.autoCorrectionFailedReason}",
                    fontSize = 11.5.sp,
                    color = BrandWarningDark
                  )
                }
              }
            }

            HorizontalDivider(color = BrandBorder)

            // Escalated To Footer
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BrandBlueLight),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = BrandSkyBlue,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Column {
                  Text(
                    text = "Escalated to ${inc.assignedTo}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                  )
                  Text(
                    text = "Human supervisor · Logged ${inc.timestampUtc}",
                    fontSize = 10.5.sp,
                    color = BrandSecondary
                  )
                }
              }
              OutlinedButton(
                onClick = {
                  viewModel.selectIncident(inc.id)
                  viewModel.navigateTo(DashboardScreen.REVIEW)
                },
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandSubtle)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("view_review_button")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = BrandNavy
                  )
                  Text(
                    text = "View review",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
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
            .testTag("featured_incident_card")
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
              text = "System Compliant · 0 Open Incidents",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = BrandNavy
            )
            Text(
              text = "No policy violations detected. Execute user interactions in Live AI Guard to monitor and evaluate conversations in real-time.",
              fontSize = 12.sp,
              color = BrandSecondary,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = BrandNavy
            )
            Text(
              text = "UTC · Audit trail",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = BrandSecondary
            )
          }

          val timelineItems = activeIncident?.timeline ?: emptyList()
          if (timelineItems.isEmpty()) {
            Text(
              text = "No timeline events recorded. Run Live AI Guard interactions to populate audit trails.",
              fontSize = 12.sp,
              color = BrandSubtle
            )
          } else {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              timelineItems.forEach { item ->
                TimelineRow(item)
              }
            }
          }
        }
      }
    }

    // Monitoring Activity Chart Section
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
              text = "Monitoring activity",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = BrandNavy
            )
            Text(
              text = "Last 6 hours",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = BrandSecondary
            )
          }

          // Legend
          Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(BrandPrimary)
              )
              Text("Conversations", fontSize = 11.sp, color = BrandSecondary)
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .width(14.dp)
                  .height(2.dp)
                  .clip(RoundedCornerShape(1.dp))
                  .background(BrandHighText)
              )
              Text("Flags / hour", fontSize = 11.sp, color = BrandSecondary)
            }
          }

          // Stylized Composite Chart
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp)
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height

              // Grid lines
              drawLine(BrandBorder, Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
              drawLine(BrandBorder, Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 1f)
              drawLine(BrandBorder, Offset(0f, h), Offset(w, h), strokeWidth = 1f)

              // 6 Bar columns
              val barHeights = listOf(0.38f, 0.48f, 0.70f, 0.82f, 0.78f, 0.92f)
              val barWidth = 16.dp.toPx()
              val step = w / 6f

              barHeights.forEachIndexed { i, frac ->
                val x = i * step + (step - barWidth) / 2f
                val barH = h * frac
                val color = if (i == 5) BrandPrimary else BrandBlueBorder
                drawRoundRect(
                  color = color,
                  topLeft = Offset(x, h - barH),
                  size = androidx.compose.ui.geometry.Size(barWidth, barH),
                  cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
              }

              // Overlay trend line for flags/hour
              val trendPoints = listOf(
                Offset(step * 0.5f, h * 0.82f),
                Offset(step * 1.5f, h * 0.72f),
                Offset(step * 2.5f, h * 0.84f),
                Offset(step * 3.5f, h * 0.65f),
                Offset(step * 4.5f, h * 0.52f),
                Offset(step * 5.5f, h * 0.75f)
              )
              val path = Path().apply {
                moveTo(trendPoints.first().x, trendPoints.first().y)
                for (pt in trendPoints.drop(1)) {
                  lineTo(pt.x, pt.y)
                }
              }
              drawPath(
                path = path,
                color = BrandHighText,
                style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
              )
            }
          }

          // Time Axis
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            listOf("09:00", "10:00", "11:00", "12:00", "13:00", "14:00").forEach { time ->
              Text(text = time, fontSize = 10.sp, color = BrandSecondary, fontWeight = FontWeight.Medium)
            }
          }

          HorizontalDivider(color = BrandBorder)

          // Sub-bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = BrandMintDark,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "12 / 12 bots healthy",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = BrandMintDark
              )
            }
            Text(text = "Latency 212 ms", fontSize = 11.sp, color = BrandSecondary)
          }
        }
      }
    }

    // Recent Incidents Section
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent incidents",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = BrandNavy
          )
          Text(
            text = "$openIncidentsCount open",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = BrandSecondary
          )
        }

        // Segmented Tab Controls
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterTab(
              label = "All incidents",
              isSelected = selectedFilter == "ALL",
              onClick = { selectedFilter = "ALL" }
            )
            FilterTab(
              label = "Escalated · $escalatedCount",
              isSelected = selectedFilter == "ESCALATED",
              onClick = { selectedFilter = "ESCALATED" }
            )
          }
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Filter",
            tint = BrandSecondary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // Incident Items List
    if (filteredIncidents.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
            .padding(20.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No incidents recorded. Compliant interactions are safely delivered without creating incident tickets.",
            fontSize = 12.5.sp,
            color = BrandSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    }

    items(filteredIncidents, key = { it.id }) { incident ->
      val isSelected = incident.id == activeIncident?.id
      IncidentListItem(
        incident = incident,
        isSelected = isSelected,
        onClick = {
          viewModel.selectIncident(incident.id)
          viewModel.navigateTo(DashboardScreen.REVIEW)
        }
      )
    }

    // Footer update
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Updated 14:32 UTC",
          fontSize = 11.sp,
          color = BrandSubtle
        )
        OutlinedButton(
          onClick = {
            viewModel.navigateTo(DashboardScreen.REVIEW)
          },
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Text("View all incidents", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
        }
      }
    }
  }
}

@Composable
fun MetricCard(
  title: String,
  value: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBorder)),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = title, fontSize = 12.sp, color = BrandMuted, fontWeight = FontWeight.Medium)
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
      }
      Text(text = value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
      Text(text = subtitle, fontSize = 11.sp, color = BrandMuted)
    }
  }
}

@Composable
fun FilterTab(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) BrandBlueLight else Color.White)
      .border(
        width = 1.dp,
        color = if (isSelected) BrandBlueBorder else BrandBorder,
        shape = RoundedCornerShape(8.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) BrandAccentBlue else BrandSecondary
    )
  }
}

@Composable
fun IncidentListItem(
  incident: IncidentRecord,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(if (isSelected) BrandBlueSelectBg else Color.White)
      .border(
        width = 1.dp,
        color = if (isSelected) BrandBlueSelectBorder else BrandBorder,
        shape = RoundedCornerShape(12.dp)
      )
      .clickable(onClick = onClick)
      .padding(14.dp)
      .testTag("incident_row_${incident.id}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "${incident.id} · ${incident.title}",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = BrandNavy
        )
        Text(
          text = "${incident.botName} · ${incident.timestampUtc}",
          fontSize = 11.5.sp,
          color = BrandSecondary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (incident.severity == GovernanceSeverity.CRITICAL) BrandCriticalBg else BrandHighBg)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = incident.severity.name.lowercase().replaceFirstChar { it.uppercase() },
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (incident.severity == GovernanceSeverity.CRITICAL) BrandCriticalText else BrandHighText
            )
          }

          val (badgeBg, badgeText, badgeLabel) = when {
            incident.isResolved -> Triple(BrandMintBg, BrandMintDark, "Resolved")
            incident.reviewStatus == ReviewStatus.PENDING -> Triple(BrandAmberPillBg, BrandAmberPillText, "Human review")
            else -> Triple(BrandBlueLight, BrandAccentBlue, "Auto-correcting")
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(badgeBg)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = badgeLabel,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = badgeText
            )
          }

          if (incident.timestampUtc == "Just now") {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(BrandMintBg)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "LIVE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = BrandMintDark
              )
            }
          }
        }
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Open",
        tint = if (isSelected) BrandSkyBlue else BrandSubtle,
        modifier = Modifier.size(16.dp)
      )
    }
  }
}

@Composable
fun TimelineRow(item: TimelineItem) {
  val dotColor = when (item.dotColorType) {
    "red" -> BrandCriticalRed
    "amber" -> BrandWarningDark
    "blue" -> BrandSkyBlue
    "green" -> BrandMintDark
    else -> BrandSubtle
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .padding(top = 4.dp)
        .size(10.dp)
        .clip(CircleShape)
        .background(dotColor)
    )
    Column {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = item.time, fontSize = 11.5.sp, color = BrandSecondary, fontWeight = FontWeight.Normal)
        Text(text = item.title, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
      }
      Text(text = item.description, fontSize = 11.5.sp, color = BrandSecondary)
    }
  }
}
