package com.example.ui.features.education

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.util.HebrFormatters
import com.example.domain.model.Beneficiary
import com.example.domain.model.CourseCategory
import com.example.domain.model.EmpowermentSector
import com.example.domain.model.EmpowermentStage
import com.example.domain.model.FundingType
import com.example.domain.model.StaffMember
import com.example.ui.components.HebrDropdownSelector
import com.example.ui.components.HebrEmptyState
import com.example.ui.components.HebrErrorBanner
import com.example.ui.components.HebrStatusBadge

@Composable
fun EducationAndEmpowermentScreen(viewModel: EducationAndEmpowermentViewModel) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val courses by viewModel.courses.collectAsStateWithLifecycle()
  val programs by viewModel.empowermentPrograms.collectAsStateWithLifecycle()
  val beneficiaries by viewModel.beneficiaries.collectAsStateWithLifecycle()
  val staffList by viewModel.staffList.collectAsStateWithLifecycle()
  val activeEnrollments by viewModel.activeCourseEnrollments.collectAsStateWithLifecycle()

  var showAddCourseDialog by remember { mutableStateOf(false) }
  var showAddProgramDialog by remember { mutableStateOf(false) }

  val activeCourse = courses.firstOrNull { it.id == state.activeCourseId }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          if (state.selectedTab == 0) showAddCourseDialog = true else showAddProgramDialog = true
        },
        modifier = Modifier.testTag("education_fab"),
      ) {
        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_add))
      }
    }
  ) { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      TabRow(selectedTabIndex = state.selectedTab) {
        Tab(
          selected = state.selectedTab == 0,
          onClick = { viewModel.selectTab(0) },
          text = { Text(stringResource(R.string.education_tab_courses, courses.size)) },
          modifier = Modifier.testTag("tab_courses"),
        )
        Tab(
          selected = state.selectedTab == 1,
          onClick = { viewModel.selectTab(1) },
          text = { Text(stringResource(R.string.education_tab_empowerment, programs.size)) },
          modifier = Modifier.testTag("tab_empowerment"),
        )
      }

      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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

      if (state.selectedTab == 0) {
        if (courses.isEmpty()) {
          HebrEmptyState(
            title = stringResource(R.string.courses_empty_title),
            subtitle = stringResource(R.string.courses_empty_subtitle),
            actionLabel = stringResource(R.string.courses_empty_action),
            onAction = { showAddCourseDialog = true },
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            items(courses, key = { it.id }) { course ->
              val ratio =
                if (course.maxCapacity > 0) {
                  (course.enrolledCount.toFloat() / course.maxCapacity.toFloat()).coerceIn(0f, 1f)
                } else 0f
              Card(
                onClick = { viewModel.openCourseDetail(course.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().testTag("course_card_${course.id}"),
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
                    Text(
                      text = course.title,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.weight(1f),
                    )
                    HebrStatusBadge(
                      text = course.category.labelAr,
                      containerColor = MaterialTheme.colorScheme.primaryContainer,
                      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                  }
                  Text(
                    text = stringResource(
                      R.string.course_instructor_capacity_format,
                      course.instructorName ?: stringResource(R.string.course_unassigned_instructor),
                      course.enrolledCount,
                      course.maxCapacity,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier.fillMaxWidth(),
                  )
                  Text(
                    text = stringResource(R.string.course_manage_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                  )
                }
              }
            }
          }
        }
      } else {
        if (programs.isEmpty()) {
          HebrEmptyState(
            title = stringResource(R.string.empowerment_empty_title),
            subtitle =
              if (beneficiaries.isEmpty()) stringResource(R.string.empowerment_empty_need_beneficiary)
              else stringResource(R.string.empowerment_empty_subtitle),
            actionLabel =
              if (beneficiaries.isNotEmpty()) stringResource(R.string.empowerment_empty_action)
              else null,
            onAction = if (beneficiaries.isNotEmpty()) ({ showAddProgramDialog = true }) else null,
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            items(programs, key = { it.id }) { program ->
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
                        text = program.projectTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                      )
                      Text(
                        text = stringResource(
                          R.string.empowerment_card_sub_format,
                          program.beneficiaryName,
                          program.sector.labelAr,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                    HebrStatusBadge(
                      text = program.stage.labelAr,
                      containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                      contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                  }
                  Text(
                    text = stringResource(
                      R.string.empowerment_card_funding_format,
                      program.fundingType.labelAr,
                      HebrFormatters.formatAmount(program.grantAmount),
                      program.progressPercent,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                  )
                  LinearProgressIndicator(
                    progress = { program.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                  )
                  if (program.stage != EmpowermentStage.SELF_RELIANT) {
                    FilledTonalButton(
                      onClick = { viewModel.advanceEmpowermentStage(program) },
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null)
                      Spacer(modifier = Modifier.padding(4.dp))
                      Text(stringResource(R.string.empowerment_advance_button))
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

  if (showAddCourseDialog) {
    AddCourseDialog(
      staffList = staffList,
      onDismiss = { showAddCourseDialog = false },
      onSave = { title, cat, instructorId, days, capacity ->
        viewModel.createCourse(title, cat, instructorId, days, capacity) {
          showAddCourseDialog = false
        }
      },
    )
  }

  if (showAddProgramDialog) {
    if (beneficiaries.isEmpty()) {
      AlertDialog(
        onDismissRequest = { showAddProgramDialog = false },
        title = { Text(stringResource(R.string.label_notice)) },
        text = { Text(stringResource(R.string.empowerment_require_beneficiary)) },
        confirmButton = {
          Button(onClick = { showAddProgramDialog = false }) {
            Text(stringResource(R.string.action_ok))
          }
        },
      )
    } else {
      AddEmpowermentDialog(
        beneficiaries = beneficiaries,
        onDismiss = { showAddProgramDialog = false },
        onSave = { beneficiaryId, title, sector, fundingType, amount ->
          viewModel.createEmpowermentProgram(beneficiaryId, title, sector, fundingType, amount) {
            showAddProgramDialog = false
          }
        },
      )
    }
  }

  activeCourse?.let { course ->
    var selectedBen by remember(beneficiaries) { mutableStateOf(beneficiaries.firstOrNull()) }
    AlertDialog(
      onDismissRequest = { viewModel.openCourseDetail(null) },
      title = { Text(course.title, style = MaterialTheme.typography.titleLarge) },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            stringResource(
              R.string.course_detail_seats_format,
              course.enrolledCount,
              course.maxCapacity,
            )
          )
          if (beneficiaries.isNotEmpty()) {
            HebrDropdownSelector(
              label = stringResource(R.string.course_detail_select_beneficiary),
              options = beneficiaries,
              selectedOption = selectedBen,
              optionLabel = { "${it.fullName} (${it.fileNumber})" },
              onOptionSelected = { selectedBen = it },
            )
            Button(
              onClick = {
                selectedBen?.let { viewModel.enrollBeneficiary(course.id, it.id) }
              },
              modifier = Modifier.fillMaxWidth().testTag("enroll_beneficiary_button"),
            ) {
              Text(stringResource(R.string.course_detail_enroll_button))
            }
          } else {
            Text(
              text = stringResource(R.string.course_detail_no_beneficiaries),
              style = MaterialTheme.typography.bodySmall,
            )
          }
          HorizontalDivider()
          Text(
            stringResource(R.string.course_detail_attendees_title),
            style = MaterialTheme.typography.titleSmall,
          )
          if (activeEnrollments.isEmpty()) {
            Text(
              stringResource(R.string.course_detail_no_attendees),
              style = MaterialTheme.typography.bodySmall,
            )
          } else {
            activeEnrollments.forEach { enrollment ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(enrollment.beneficiaryName, style = MaterialTheme.typography.bodyMedium)
                  Text(
                    stringResource(
                      R.string.course_attendance_ratio_format,
                      enrollment.presentSessions,
                      enrollment.totalSessions,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Row {
                  IconButton(
                    onClick = { viewModel.markTodayAttendance(enrollment.id, true) }
                  ) {
                    Icon(
                      Icons.Filled.CheckCircle,
                      contentDescription = stringResource(R.string.course_attendance_present),
                      tint = MaterialTheme.colorScheme.secondary,
                    )
                  }
                  IconButton(
                    onClick = { viewModel.markTodayAttendance(enrollment.id, false) }
                  ) {
                    Icon(
                      Icons.Filled.HighlightOff,
                      contentDescription = stringResource(R.string.course_attendance_absent),
                      tint = MaterialTheme.colorScheme.error,
                    )
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { viewModel.openCourseDetail(null) }) {
          Text(stringResource(R.string.action_close))
        }
      },
    )
  }
}

@Composable
private fun AddCourseDialog(
  staffList: List<StaffMember>,
  onDismiss: () -> Unit,
  onSave: (String, CourseCategory, Long?, String, String) -> Unit,
) {
  var title by remember { mutableStateOf("") }
  var category by remember { mutableStateOf(CourseCategory.DIGITAL_SKILLS) }
  var selectedInstructor by remember { mutableStateOf(staffList.firstOrNull()) }
  var durationDays by remember { mutableStateOf("30") }
  var capacity by remember { mutableStateOf("20") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        stringResource(R.string.course_add_dialog_title),
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(stringResource(R.string.course_field_title)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("course_title_input"),
        )
        HebrDropdownSelector(
          label = stringResource(R.string.course_field_category),
          options = CourseCategory.entries,
          selectedOption = category,
          optionLabel = { it.labelAr },
          onOptionSelected = { category = it },
        )
        if (staffList.isNotEmpty()) {
          HebrDropdownSelector(
            label = stringResource(R.string.course_field_instructor),
            options = staffList,
            selectedOption = selectedInstructor,
            optionLabel = { "${it.fullName} (${it.role.labelAr})" },
            onOptionSelected = { selectedInstructor = it },
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = durationDays,
            onValueChange = { if (it.all(Char::isDigit)) durationDays = it },
            label = { Text(stringResource(R.string.course_field_duration)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = capacity,
            onValueChange = { if (it.all(Char::isDigit)) capacity = it },
            label = { Text(stringResource(R.string.course_field_capacity)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("course_capacity_input"),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(title, category, selectedInstructor?.id, durationDays, capacity) },
        modifier = Modifier.testTag("save_course_button"),
      ) {
        Text(stringResource(R.string.action_save))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
  )
}

@Composable
private fun AddEmpowermentDialog(
  beneficiaries: List<Beneficiary>,
  onDismiss: () -> Unit,
  onSave: (Long, String, EmpowermentSector, FundingType, String) -> Unit,
) {
  var selectedBen by remember { mutableStateOf(beneficiaries.first()) }
  var projectTitle by remember { mutableStateOf("") }
  var sector by remember { mutableStateOf(EmpowermentSector.CRAFT) }
  var fundingType by remember { mutableStateOf(FundingType.NON_REFUNDABLE_GRANT) }
  var amount by remember { mutableStateOf("5000") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        stringResource(R.string.empowerment_add_dialog_title),
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        HebrDropdownSelector(
          label = stringResource(R.string.empowerment_field_beneficiary),
          options = beneficiaries,
          selectedOption = selectedBen,
          optionLabel = { "${it.fullName} (${it.fileNumber})" },
          onOptionSelected = { selectedBen = it },
        )
        OutlinedTextField(
          value = projectTitle,
          onValueChange = { projectTitle = it },
          label = { Text(stringResource(R.string.empowerment_field_title)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("empowerment_title_input"),
        )
        HebrDropdownSelector(
          label = stringResource(R.string.empowerment_field_sector),
          options = EmpowermentSector.entries,
          selectedOption = sector,
          optionLabel = { it.labelAr },
          onOptionSelected = { sector = it },
        )
        HebrDropdownSelector(
          label = stringResource(R.string.empowerment_field_funding_type),
          options = FundingType.entries,
          selectedOption = fundingType,
          optionLabel = { it.labelAr },
          onOptionSelected = { fundingType = it },
        )
        OutlinedTextField(
          value = amount,
          onValueChange = { amount = it },
          label = { Text(stringResource(R.string.empowerment_field_amount)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(selectedBen.id, projectTitle, sector, fundingType, amount) },
        modifier = Modifier.testTag("save_empowerment_button"),
      ) {
        Text(stringResource(R.string.action_save))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
  )
}
