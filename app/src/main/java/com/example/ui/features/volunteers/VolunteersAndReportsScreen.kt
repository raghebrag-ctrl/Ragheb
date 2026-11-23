package com.example.ui.features.volunteers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.util.HebrFormatters
import com.example.domain.model.Priority
import com.example.domain.model.StaffMember
import com.example.domain.model.TaskStatus
import com.example.ui.components.HebrDropdownSelector
import com.example.ui.components.HebrEmptyState
import com.example.ui.components.HebrErrorBanner
import com.example.ui.components.HebrStatusBadge

@Composable
fun VolunteersAndReportsScreen(viewModel: VolunteersAndReportsViewModel) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val tasks by viewModel.tasks.collectAsStateWithLifecycle()
  val staffList by viewModel.staffList.collectAsStateWithLifecycle()
  val summary by viewModel.reportSummary.collectAsStateWithLifecycle()
  val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

  var showAddTaskDialog by remember { mutableStateOf(false) }

  Scaffold(
    floatingActionButton = {
      if (state.selectedTab == 0) {
        FloatingActionButton(
          onClick = { showAddTaskDialog = true },
          modifier = Modifier.testTag("volunteer_task_fab"),
        ) {
          Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_add))
        }
      }
    }
  ) { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      TabRow(selectedTabIndex = state.selectedTab) {
        Tab(
          selected = state.selectedTab == 0,
          onClick = { viewModel.selectTab(0) },
          text = { Text(stringResource(R.string.volunteers_tab_tasks, tasks.size)) },
          modifier = Modifier.testTag("tab_volunteer_tasks"),
        )
        Tab(
          selected = state.selectedTab == 1,
          onClick = { viewModel.selectTab(1) },
          text = { Text(stringResource(R.string.volunteers_tab_reports)) },
          modifier = Modifier.testTag("tab_reports"),
        )
        Tab(
          selected = state.selectedTab == 2,
          onClick = { viewModel.selectTab(2) },
          text = { Text(stringResource(R.string.volunteers_tab_audit, auditLogs.size)) },
          modifier = Modifier.testTag("tab_audit_logs"),
        )
      }

      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        HebrErrorBanner(
          errorMessage = state.errorMessage,
          onDismiss = viewModel::clearMessages,
        )
        if (!state.feedbackMessage.isNullOrBlank()) {
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(state.feedbackMessage.orEmpty(), style = MaterialTheme.typography.bodySmall)
              TextButton(onClick = viewModel::clearMessages) {
                Text(stringResource(R.string.action_ok))
              }
            }
          }
        }
      }

      when (state.selectedTab) {
        0 -> {
          if (tasks.isEmpty()) {
            HebrEmptyState(
              title = stringResource(R.string.tasks_empty_title),
              subtitle = stringResource(R.string.tasks_empty_subtitle),
              actionLabel = stringResource(R.string.tasks_empty_action),
              onAction = { showAddTaskDialog = true },
            )
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              items(tasks, key = { it.id }) { task ->
                Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  shape = MaterialTheme.shapes.medium,
                ) {
                  Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = task.title,
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                        )
                        Text(
                          text = stringResource(
                            R.string.task_assignee_due_format,
                            task.assigneeName,
                            HebrFormatters.formatDate(task.dueDate),
                          ),
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                      HebrStatusBadge(
                        text = task.priority.labelAr,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                      )
                    }
                    Text(task.description, style = MaterialTheme.typography.bodyMedium)
                    Text(
                      text = stringResource(
                        R.string.task_hours_logged_format,
                        HebrFormatters.formatNumber(task.hoursLogged),
                      ),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.secondary,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                      items(TaskStatus.entries) { statusOption ->
                        FilterChip(
                          selected = task.status == statusOption,
                          onClick = {
                            val nextHours =
                              if (statusOption == TaskStatus.DONE && task.hoursLogged == 0.0) {
                                2.0
                              } else {
                                task.hoursLogged
                              }
                            viewModel.updateTask(task.id, statusOption, nextHours)
                          },
                          label = { Text(statusOption.labelAr) },
                        )
                      }
                    }
                    OutlinedButton(
                      onClick = {
                        viewModel.updateTask(task.id, task.status, task.hoursLogged + 1.0)
                      }
                    ) {
                      Text(stringResource(R.string.task_add_one_hour))
                    }
                  }
                }
              }
            }
          }
        }
        1 -> {
          val primaryColor = MaterialTheme.colorScheme.primary
          val secondaryColor = MaterialTheme.colorScheme.secondary
          val tertiaryColor = MaterialTheme.colorScheme.tertiary
          val trackColor = MaterialTheme.colorScheme.surfaceVariant
          val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            item {
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.large,
              ) {
                Column(
                  modifier = Modifier.padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  Text(
                    text = stringResource(R.string.reports_financial_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                  )
                  val maxFinance =
                    maxOf(summary.totalCashDonations, summary.totalCashAidDistributed, 1.0)
                  val donationRatio = (summary.totalCashDonations / maxFinance).toFloat()
                  val aidRatio = (summary.totalCashAidDistributed / maxFinance).toFloat()

                  Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                    val barHeight = 26.dp.toPx()
                    val radius = CornerRadius(12f, 12f)
                    val donationWidth = size.width * donationRatio
                    val aidWidth = size.width * aidRatio

                    // Donations bar (RTL-aware)
                    drawRoundRect(
                      color = trackColor,
                      topLeft = Offset(0f, 8f),
                      size = Size(size.width, barHeight),
                      cornerRadius = radius,
                    )
                    drawRoundRect(
                      color = secondaryColor,
                      topLeft = Offset(if (isRtl) size.width - donationWidth else 0f, 8f),
                      size = Size(donationWidth, barHeight),
                      cornerRadius = radius,
                    )
                    // Aid distributed bar (RTL-aware)
                    val secondY = barHeight + 28f
                    drawRoundRect(
                      color = trackColor,
                      topLeft = Offset(0f, secondY),
                      size = Size(size.width, barHeight),
                      cornerRadius = radius,
                    )
                    drawRoundRect(
                      color = tertiaryColor,
                      topLeft = Offset(if (isRtl) size.width - aidWidth else 0f, secondY),
                      size = Size(aidWidth, barHeight),
                      cornerRadius = radius,
                    )
                  }
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Text(
                      text = stringResource(
                        R.string.reports_donations_label,
                        HebrFormatters.formatAmount(summary.totalCashDonations),
                      ),
                      color = secondaryColor,
                      style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                      text = stringResource(
                        R.string.reports_aid_label,
                        HebrFormatters.formatAmount(summary.totalCashAidDistributed),
                      ),
                      color = tertiaryColor,
                      style = MaterialTheme.typography.labelLarge,
                    )
                  }
                }
              }
            }

            item {
              ReportMetricCard(
                title = stringResource(R.string.reports_section_beneficiaries),
                rows =
                  listOf(
                    stringResource(R.string.reports_metric_total_beneficiaries) to
                      "${summary.totalBeneficiaries}",
                    stringResource(R.string.reports_metric_active_cases) to
                      "${summary.activeCasesCount}",
                    stringResource(R.string.reports_metric_urgent_cases) to
                      "${summary.urgentCasesCount}",
                  ),
                accentColor = primaryColor,
              )
            }
            item {
              ReportMetricCard(
                title = stringResource(R.string.reports_section_education),
                rows =
                  listOf(
                    stringResource(R.string.reports_metric_active_courses) to
                      "${summary.activeCoursesCount}",
                    stringResource(R.string.reports_metric_total_enrollments) to
                      "${summary.totalEnrollments}",
                    stringResource(R.string.reports_metric_empowerment_projects) to
                      "${summary.empowermentProjectsCount}",
                    stringResource(R.string.reports_metric_self_reliant) to
                      "${summary.selfReliantProjectsCount}",
                  ),
                accentColor = secondaryColor,
              )
            }
            item {
              ReportMetricCard(
                title = stringResource(R.string.reports_section_volunteers),
                rows =
                  listOf(
                    stringResource(R.string.reports_metric_volunteer_hours) to
                      stringResource(
                        R.string.reports_metric_volunteer_hours_value,
                        HebrFormatters.formatNumber(summary.totalVolunteerHours),
                      ),
                    stringResource(R.string.reports_metric_completed_tasks) to
                      "${summary.completedTasksCount}",
                    stringResource(R.string.reports_metric_open_tasks) to
                      "${summary.openTasksCount}",
                    stringResource(R.string.reports_metric_aid_records) to
                      "${summary.totalAidRecordsCount}",
                  ),
                accentColor = tertiaryColor,
              )
            }
          }
        }
        else -> {
          if (auditLogs.isEmpty()) {
            HebrEmptyState(
              title = stringResource(R.string.audit_empty_title),
              subtitle = stringResource(R.string.audit_empty_subtitle),
            )
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              items(auditLogs, key = { it.id }) { log ->
                Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  shape = MaterialTheme.shapes.small,
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Security,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                      ) {
                        Text(
                          text = log.actorName,
                          style = MaterialTheme.typography.labelLarge,
                          fontWeight = FontWeight.Bold,
                        )
                        Text(
                          text = HebrFormatters.formatDateTime(log.timestamp),
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(text = log.details, style = MaterialTheme.typography.bodySmall)
                      Text(
                        text = "${log.action} • #${log.entityId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
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

  if (showAddTaskDialog && staffList.isNotEmpty()) {
    AddVolunteerTaskDialog(
      staffList = staffList,
      onDismiss = { showAddTaskDialog = false },
      onSave = { assigneeId, title, desc, dueDays, priority ->
        viewModel.createTask(assigneeId, title, desc, dueDays, priority) {
          showAddTaskDialog = false
        }
      },
    )
  }
}

@Composable
private fun ReportMetricCard(
  title: String,
  rows: List<Pair<String, String>>,
  accentColor: androidx.compose.ui.graphics.Color,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = MaterialTheme.shapes.medium,
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = accentColor,
      )
      rows.forEach { (label, value) ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(label, style = MaterialTheme.typography.bodyMedium)
          Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun AddVolunteerTaskDialog(
  staffList: List<StaffMember>,
  onDismiss: () -> Unit,
  onSave: (Long, String, String, String, Priority) -> Unit,
) {
  var selectedStaff by remember { mutableStateOf(staffList.first()) }
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var dueDays by remember { mutableStateOf("3") }
  var priority by remember { mutableStateOf(Priority.NORMAL) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        stringResource(R.string.task_add_dialog_title),
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        HebrDropdownSelector(
          label = stringResource(R.string.task_field_assignee),
          options = staffList,
          selectedOption = selectedStaff,
          optionLabel = { "${it.fullName} (${it.role.labelAr})" },
          onOptionSelected = { selectedStaff = it },
        )
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(stringResource(R.string.task_field_title)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("task_title_input"),
        )
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text(stringResource(R.string.task_field_description)) },
          minLines = 2,
          modifier = Modifier.fillMaxWidth().testTag("task_desc_input"),
        )
        OutlinedTextField(
          value = dueDays,
          onValueChange = { if (it.all(Char::isDigit)) dueDays = it },
          label = { Text(stringResource(R.string.task_field_due_days)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        HebrDropdownSelector(
          label = stringResource(R.string.case_field_priority),
          options = Priority.entries,
          selectedOption = priority,
          optionLabel = { it.labelAr },
          onOptionSelected = { priority = it },
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(selectedStaff.id, title, description, dueDays, priority) },
        modifier = Modifier.testTag("save_task_button"),
      ) {
        Text(stringResource(R.string.action_save))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
  )
}
