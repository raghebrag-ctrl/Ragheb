package com.example.ui.features.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.util.HebrFormatters
import com.example.ui.components.HebrStatCard
import com.example.ui.components.HebrStatusBadge

@Composable
fun DashboardScreen(
  viewModel: DashboardViewModel,
  onNavigateToBeneficiaries: () -> Unit,
  onNavigateToEducation: () -> Unit,
  onNavigateToFinance: () -> Unit,
  onNavigateToVolunteers: () -> Unit,
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val summary = state.summary

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Hero Banner Card
    item {
      Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      ) {
        Box(modifier = Modifier.fillMaxWidth().height(172.dp)) {
          Image(
            painter = painterResource(id = R.drawable.img_hebr_hero_banner),
            contentDescription = stringResource(R.string.cd_hero_banner),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
          Box(
            modifier =
              Modifier.fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color(0xBB0C2B48), Color(0xE00C2B48))
                  )
                )
          )
          Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                val staffName =
                  state.currentStaff?.fullName ?: stringResource(R.string.dashboard_default_staff)
                Text(
                  text = stringResource(R.string.dashboard_greeting_format, staffName),
                  style = MaterialTheme.typography.headlineSmall,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                HebrStatusBadge(
                  text = state.currentStaff?.role?.labelAr ?: "",
                  containerColor = Color(0x33FFFFFF),
                  contentColor = Color(0xFFFFE5BD),
                )
              }
              IconButton(
                onClick = viewModel::lockSession,
                modifier = Modifier.testTag("lock_session_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Lock,
                  contentDescription = stringResource(R.string.action_logout),
                  tint = Color.White,
                )
              }
            }
            Text(
              text = stringResource(R.string.dashboard_daily_subtitle),
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.88f),
            )
          }
        }
      }
    }

    // Quick Navigation Shortcuts
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
          FilledTonalButton(
            onClick = onNavigateToBeneficiaries,
            modifier = Modifier.testTag("quick_beneficiaries_button"),
          ) {
            Icon(Icons.Filled.Groups, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.dashboard_quick_beneficiaries))
          }
        }
        item {
          FilledTonalButton(
            onClick = onNavigateToEducation,
            modifier = Modifier.testTag("quick_education_button"),
          ) {
            Icon(Icons.Filled.School, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.dashboard_quick_education))
          }
        }
        item {
          FilledTonalButton(
            onClick = onNavigateToFinance,
            modifier = Modifier.testTag("quick_finance_button"),
          ) {
            Icon(Icons.Filled.VolunteerActivism, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.dashboard_quick_finance))
          }
        }
        item {
          FilledTonalButton(
            onClick = onNavigateToVolunteers,
            modifier = Modifier.testTag("quick_volunteers_button"),
          ) {
            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.dashboard_quick_volunteers))
          }
        }
      }
    }

    // KPI Grid (2 rows of 2 cards)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          HebrStatCard(
            title = stringResource(R.string.dashboard_stat_beneficiaries_title),
            value = HebrFormatters.formatNumber(summary.totalBeneficiaries),
            subtitle = stringResource(R.string.dashboard_stat_beneficiaries_sub, summary.activeCasesCount),
            icon = Icons.Filled.Groups,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f),
          )
          HebrStatCard(
            title = stringResource(R.string.dashboard_stat_education_title),
            value = HebrFormatters.formatNumber(summary.activeCoursesCount),
            subtitle = stringResource(R.string.dashboard_stat_education_sub, summary.totalEnrollments),
            icon = Icons.Filled.School,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          HebrStatCard(
            title = stringResource(R.string.dashboard_stat_empowerment_title),
            value = HebrFormatters.formatNumber(summary.empowermentProjectsCount),
            subtitle = stringResource(
              R.string.dashboard_stat_empowerment_sub,
              summary.selfReliantProjectsCount,
            ),
            icon = Icons.Filled.RocketLaunch,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f),
          )
          HebrStatCard(
            title = stringResource(R.string.dashboard_stat_finance_title),
            value = HebrFormatters.formatAmount(summary.totalCashDonations),
            subtitle = stringResource(
              R.string.dashboard_stat_finance_sub,
              HebrFormatters.formatAmount(summary.totalCashAidDistributed),
            ),
            icon = Icons.Filled.AccountBalanceWallet,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
          )
        }
      }
    }

    // Urgent Cases Section
    item {
      Text(
        text = stringResource(R.string.dashboard_urgent_cases_heading),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
    }

    if (state.urgentCases.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(
            text = stringResource(R.string.dashboard_no_urgent_cases),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
          )
        }
      }
    } else {
      items(state.urgentCases, key = { "urgent_case_${it.id}" }) { socialCase ->
        Card(
          onClick = onNavigateToBeneficiaries,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = MaterialTheme.shapes.medium,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = socialCase.beneficiaryName,
                style = MaterialTheme.typography.titleMedium,
              )
              Text(
                text = "${socialCase.caseType.labelAr} • ${socialCase.assessmentSummary}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            HebrStatusBadge(
              text = socialCase.priority.labelAr,
              containerColor = MaterialTheme.colorScheme.errorContainer,
              contentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
          }
        }
      }
    }

    // Open Volunteer Tasks Section
    item {
      Text(
        text = stringResource(R.string.dashboard_upcoming_tasks_heading),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
    }

    if (state.upcomingTasks.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(
            text = stringResource(R.string.dashboard_no_upcoming_tasks),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
          )
        }
      }
    } else {
      items(state.upcomingTasks, key = { "dash_task_${it.id}" }) { task ->
        Card(
          onClick = onNavigateToVolunteers,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = MaterialTheme.shapes.medium,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = task.title, style = MaterialTheme.typography.titleMedium)
              Text(
                text = stringResource(
                  R.string.dashboard_task_meta_format,
                  task.assigneeName,
                  HebrFormatters.formatDate(task.dueDate),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            HebrStatusBadge(
              text = task.status.labelAr,
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
              contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
        }
      }
    }
  }
}
