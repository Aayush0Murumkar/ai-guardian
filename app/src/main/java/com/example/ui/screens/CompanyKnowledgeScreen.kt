package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VerifiedCompanyKnowledge
import com.example.ui.theme.*

@Composable
fun CompanyKnowledgeScreen(
  modifier: Modifier = Modifier
) {
  val knowledge = VerifiedCompanyKnowledge

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BrandBg)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
  ) {
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "VERIFIED REFERENCE SOURCE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandMuted,
            letterSpacing = 0.8.sp
          )
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(BrandMintBg)
              .border(1.dp, BrandMintBorder, RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = "REFERENCE DATA",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = BrandMintDark
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Company Policy Store",
          fontSize = 28.sp,
          fontWeight = FontWeight.ExtraBold,
          color = BrandNavy
        )
        Text(
          text = "The Governance Engine verifies all AI statements strictly against this reference knowledge.",
          fontSize = 13.5.sp,
          color = BrandSecondary
        )
      }
    }

    // Company Profile Info
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = knowledge.companyName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = BrandNavy
          )
          Text(
            text = "Industry: ${knowledge.industry}",
            fontSize = 12.sp,
            color = BrandSecondary
          )
        }
      }
    }

    // Privacy Rules Section
    item {
      KnowledgeSectionCard(
        title = "Privacy & PII Protection Rules",
        icon = Icons.Default.Policy,
        items = knowledge.privacyRules
      )
    }

    // Policies
    item {
      KnowledgeSectionCard(
        title = "Enterprise Governance Policies",
        icon = Icons.Default.Bookmark,
        items = knowledge.policies
      )
    }

    // Data Protection Rules
    item {
      KnowledgeSectionCard(
        title = "Data Protection & Export Boundaries",
        icon = Icons.Default.Policy,
        items = knowledge.dataProtectionRules
      )
    }

    // Support Rules
    item {
      KnowledgeSectionCard(
        title = "Customer Support & SLA Guardrails",
        icon = Icons.Default.Policy,
        items = knowledge.supportRules
      )
    }

    // Recruitment Rules
    item {
      KnowledgeSectionCard(
        title = "Recruitment & Candidate Ethics Rules",
        icon = Icons.Default.Policy,
        items = knowledge.recruitmentRules
      )
    }

    // Approved Facts
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandMintDark, modifier = Modifier.size(18.dp))
            Text("Approved Facts & Canonical Statements", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
          }

          knowledge.approvedFacts.forEach { fact ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(BrandBg)
                .padding(10.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(fact.topic, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                  Text("Source: ${fact.sourcePolicy}", fontSize = 10.sp, color = BrandSecondary)
                }
                Text(fact.statement, fontSize = 11.5.sp, color = BrandDark)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun KnowledgeSectionCard(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  items: List<String>
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBorder)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(icon, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
      }

      items.forEach { rule ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BrandBg)
            .padding(10.dp)
        ) {
          Text(rule, fontSize = 12.sp, color = BrandDark, lineHeight = 16.sp)
        }
      }
    }
  }
}
