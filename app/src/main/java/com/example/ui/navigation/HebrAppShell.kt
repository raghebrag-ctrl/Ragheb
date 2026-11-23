package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.core.di.AppContainer
import com.example.ui.features.aid.AidAndDonationsScreen
import com.example.ui.features.aid.AidAndDonationsViewModel
import com.example.ui.features.beneficiaries.BeneficiariesScreen
import com.example.ui.features.beneficiaries.BeneficiariesViewModel
import com.example.ui.features.dashboard.DashboardScreen
import com.example.ui.features.dashboard.DashboardViewModel
import com.example.ui.features.education.EducationAndEmpowermentScreen
import com.example.ui.features.education.EducationAndEmpowermentViewModel
import com.example.ui.features.volunteers.VolunteersAndReportsScreen
import com.example.ui.features.volunteers.VolunteersAndReportsViewModel

enum class HebrDestination(
  val route: String,
  val labelResId: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
) {
  DASHBOARD(
    route = "dashboard",
    labelResId = R.string.nav_dashboard,
    selectedIcon = Icons.Filled.Home,
    unselectedIcon = Icons.Outlined.Home,
    testTag = "nav_dashboard",
  ),
  BENEFICIARIES(
    route = "beneficiaries",
    labelResId = R.string.nav_beneficiaries,
    selectedIcon = Icons.Filled.Groups,
    unselectedIcon = Icons.Outlined.Groups,
    testTag = "nav_beneficiaries",
  ),
  EDUCATION(
    route = "education",
    labelResId = R.string.nav_education,
    selectedIcon = Icons.Filled.School,
    unselectedIcon = Icons.Outlined.School,
    testTag = "nav_education",
  ),
  FINANCE(
    route = "finance",
    labelResId = R.string.nav_aid,
    selectedIcon = Icons.Filled.AccountBalanceWallet,
    unselectedIcon = Icons.Outlined.AccountBalanceWallet,
    testTag = "nav_finance",
  ),
  VOLUNTEERS(
    route = "volunteers",
    labelResId = R.string.nav_volunteers,
    selectedIcon = Icons.AutoMirrored.Filled.Assignment,
    unselectedIcon = Icons.AutoMirrored.Outlined.Assignment,
    testTag = "nav_volunteers",
  ),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HebrAppShell(container: AppContainer) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: HebrDestination.DASHBOARD.route
  val currentDest =
    HebrDestination.entries.firstOrNull { it.route == currentRoute } ?: HebrDestination.DASHBOARD

  val navigateToTab: (String) -> Unit = { route ->
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  val dashboardVm: DashboardViewModel =
    viewModel(
      factory =
        DashboardViewModel.Factory(
          container.sessionManager,
          container.beneficiaryRepository,
          container.volunteerAndReportsRepository,
        )
    )
  val beneficiariesVm: BeneficiariesViewModel =
    viewModel(
      factory =
        BeneficiariesViewModel.Factory(
          container.beneficiaryRepository,
          container.financeAidRepository,
          container.sessionManager,
          container.registerBeneficiaryUseCase,
          container.createSocialCaseUseCase,
        )
    )
  val educationVm: EducationAndEmpowermentViewModel =
    viewModel(
      factory =
        EducationAndEmpowermentViewModel.Factory(
          container.educationRepository,
          container.empowermentRepository,
          container.beneficiaryRepository,
          container.staffRepository,
          container.sessionManager,
          container.createCourseUseCase,
          container.enrollBeneficiaryUseCase,
          container.createEmpowermentProgramUseCase,
        )
    )
  val aidVm: AidAndDonationsViewModel =
    viewModel(
      factory =
        AidAndDonationsViewModel.Factory(
          container.financeAidRepository,
          container.beneficiaryRepository,
          container.volunteerAndReportsRepository,
          container.recordDonationUseCase,
          container.distributeAidUseCase,
        )
    )
  val volunteersVm: VolunteersAndReportsViewModel =
    viewModel(
      factory =
        VolunteersAndReportsViewModel.Factory(
          container.volunteerAndReportsRepository,
          container.staffRepository,
          container.sessionManager,
          container.createVolunteerTaskUseCase,
        )
    )

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useRail = maxWidth >= 600.dp

    Scaffold(
      contentWindowInsets = WindowInsets.safeDrawing,
      topBar = {
        CenterAlignedTopAppBar(
          title = {
            Text(
              text =
                if (currentDest == HebrDestination.DASHBOARD) stringResource(R.string.app_name)
                else stringResource(currentDest.labelResId),
              fontWeight = FontWeight.Bold,
            )
          },
          actions = {
            IconButton(
              onClick = { container.sessionManager.signOut() },
              modifier = Modifier.testTag("topbar_lock_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = stringResource(R.string.action_logout),
              )
            }
          },
          colors =
            TopAppBarDefaults.centerAlignedTopAppBarColors(
              containerColor = MaterialTheme.colorScheme.surface,
              titleContentColor = MaterialTheme.colorScheme.primary,
            ),
        )
      },
      bottomBar = {
        if (!useRail) {
          NavigationBar(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
            HebrDestination.entries.forEach { dest ->
              val selected = currentRoute == dest.route
              val label = stringResource(dest.labelResId)
              NavigationBarItem(
                selected = selected,
                onClick = { navigateToTab(dest.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                    contentDescription = label,
                  )
                },
                label = { Text(label, maxLines = 1) },
                modifier = Modifier.testTag(dest.testTag),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (useRail) {
          NavigationRail {
            HebrDestination.entries.forEach { dest ->
              val selected = currentRoute == dest.route
              val label = stringResource(dest.labelResId)
              NavigationRailItem(
                selected = selected,
                onClick = { navigateToTab(dest.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                    contentDescription = label,
                  )
                },
                label = { Text(label) },
                modifier = Modifier.testTag(dest.testTag),
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
          NavHost(
            navController = navController,
            startDestination = HebrDestination.DASHBOARD.route,
          ) {
            composable(HebrDestination.DASHBOARD.route) {
              DashboardScreen(
                viewModel = dashboardVm,
                onNavigateToBeneficiaries = { navigateToTab(HebrDestination.BENEFICIARIES.route) },
                onNavigateToEducation = { navigateToTab(HebrDestination.EDUCATION.route) },
                onNavigateToFinance = { navigateToTab(HebrDestination.FINANCE.route) },
                onNavigateToVolunteers = { navigateToTab(HebrDestination.VOLUNTEERS.route) },
              )
            }
            composable(HebrDestination.BENEFICIARIES.route) {
              BeneficiariesScreen(viewModel = beneficiariesVm)
            }
            composable(HebrDestination.EDUCATION.route) {
              EducationAndEmpowermentScreen(viewModel = educationVm)
            }
            composable(HebrDestination.FINANCE.route) {
              AidAndDonationsScreen(viewModel = aidVm)
            }
            composable(HebrDestination.VOLUNTEERS.route) {
              VolunteersAndReportsScreen(viewModel = volunteersVm)
            }
          }
        }
      }
    }
  }
}
