package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DashboardScreen
import com.example.ui.GovernanceViewModel
import com.example.ui.screens.CompanyKnowledgeScreen
import com.example.ui.screens.HumanReviewWorkspaceScreen
import com.example.ui.screens.LiveGuardianChatScreen
import com.example.ui.screens.MonitoringOverviewScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

  private val viewModel: GovernanceViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        GovernanceApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun GovernanceApp(viewModel: GovernanceViewModel) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val awaitingReviewCount by viewModel.awaitingReviewCount.collectAsState()

  BackHandler(enabled = currentScreen != DashboardScreen.OVERVIEW) {
    viewModel.navigateTo(DashboardScreen.OVERVIEW)
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = BrandBg,
    bottomBar = {
      NavigationBar(
        containerColor = Color.White,
        tonalElevation = 6.dp
      ) {
        NavigationBarItem(
          selected = currentScreen == DashboardScreen.OVERVIEW,
          onClick = { viewModel.navigateTo(DashboardScreen.OVERVIEW) },
          icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
          label = { Text("Overview", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = BrandPrimary,
            selectedTextColor = BrandPrimary,
            unselectedIconColor = BrandSecondary,
            unselectedTextColor = BrandSecondary,
            indicatorColor = BrandBlueLight
          ),
          modifier = Modifier.testTag("nav_overview")
        )

        NavigationBarItem(
          selected = currentScreen == DashboardScreen.REVIEW,
          onClick = { viewModel.navigateTo(DashboardScreen.REVIEW) },
          icon = {
            BadgedBox(
              badge = {
                if (awaitingReviewCount > 0) {
                  Badge(
                    containerColor = BrandCriticalRed,
                    contentColor = Color.White
                  ) {
                    Text("$awaitingReviewCount")
                  }
                }
              }
            ) {
              Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Review")
            }
          },
          label = { Text("Review", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = BrandPrimary,
            selectedTextColor = BrandPrimary,
            unselectedIconColor = BrandSecondary,
            unselectedTextColor = BrandSecondary,
            indicatorColor = BrandBlueLight
          ),
          modifier = Modifier.testTag("nav_review")
        )

        NavigationBarItem(
          selected = currentScreen == DashboardScreen.LIVE_TEST,
          onClick = { viewModel.navigateTo(DashboardScreen.LIVE_TEST) },
          icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Live Guard") },
          label = { Text("Live Guard", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = BrandPrimary,
            selectedTextColor = BrandPrimary,
            unselectedIconColor = BrandSecondary,
            unselectedTextColor = BrandSecondary,
            indicatorColor = BrandBlueLight
          ),
          modifier = Modifier.testTag("nav_live_guard")
        )

        NavigationBarItem(
          selected = currentScreen == DashboardScreen.POLICIES,
          onClick = { viewModel.navigateTo(DashboardScreen.POLICIES) },
          icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Policies") },
          label = { Text("Policies", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = BrandPrimary,
            selectedTextColor = BrandPrimary,
            unselectedIconColor = BrandSecondary,
            unselectedTextColor = BrandSecondary,
            indicatorColor = BrandBlueLight
          ),
          modifier = Modifier.testTag("nav_policies")
        )
      }
    }
  ) { innerPadding ->
    when (currentScreen) {
      DashboardScreen.OVERVIEW -> MonitoringOverviewScreen(
        viewModel = viewModel,
        modifier = Modifier.padding(innerPadding)
      )
      DashboardScreen.REVIEW -> HumanReviewWorkspaceScreen(
        viewModel = viewModel,
        modifier = Modifier.padding(innerPadding)
      )
      DashboardScreen.LIVE_TEST -> LiveGuardianChatScreen(
        viewModel = viewModel,
        modifier = Modifier.padding(innerPadding)
      )
      DashboardScreen.POLICIES -> CompanyKnowledgeScreen(
        modifier = Modifier.padding(innerPadding)
      )
    }
  }
}
