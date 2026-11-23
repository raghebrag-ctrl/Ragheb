package com.example.ui.features.beneficiaries

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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.util.HebrFormatters
import com.example.domain.model.Beneficiary
import com.example.domain.model.BeneficiaryStatus
import com.example.domain.model.CaseStatus
import com.example.domain.model.CaseType
import com.example.domain.model.Priority
import com.example.domain.model.VulnerabilityLevel
import com.example.ui.components.HebrConfirmDialog
import com.example.ui.components.HebrDropdownSelector
import com.example.ui.components.HebrEmptyState
import com.example.ui.components.HebrErrorBanner
import com.example.ui.components.HebrSearchBar
import com.example.ui.components.HebrStatusBadge

@Composable
fun BeneficiariesScreen(viewModel: BeneficiariesViewModel) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val beneficiaries by viewModel.beneficiaries.collectAsStateWithLifecycle()
  val socialCases by viewModel.socialCases.collectAsStateWithLifecycle()
  val selectedBeneficiary by viewModel.selectedBeneficiary.collectAsStateWithLifecycle()
  val selectedBeneficiaryAid by viewModel.selectedBeneficiaryAid.collectAsStateWithLifecycle()

  var showAddBeneficiaryDialog by remember { mutableStateOf(false) }
  var showAddCaseDialog by remember { mutableStateOf(false) }
  var deleteCandidateId by remember { mutableStateOf<Long?>(null) }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          if (state.selectedTab == 0) showAddBeneficiaryDialog = true else showAddCaseDialog = true
        },
        modifier = Modifier.testTag("beneficiaries_fab"),
      ) {
        Icon(
          imageVector = if (state.selectedTab == 0) Icons.Filled.PersonAdd else Icons.Filled.Add,
          contentDescription = stringResource(R.string.action_add),
        )
      }
    }
  ) { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      TabRow(selectedTabIndex = state.selectedTab) {
        Tab(
          selected = state.selectedTab == 0,
          onClick = { viewModel.selectTab(0) },
          text = { Text(stringResource(R.string.beneficiaries_tab_title, beneficiaries.size)) },
          modifier = Modifier.testTag("tab_beneficiaries"),
        )
        Tab(
          selected = state.selectedTab == 1,
          onClick = { viewModel.selectTab(1) },
          text = { Text(stringResource(R.string.cases_tab_title, socialCases.size)) },
          modifier = Modifier.testTag("tab_social_cases"),
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
        HebrSearchBar(
          query = state.searchQuery,
          onQueryChange = viewModel::onSearchChange,
          placeholder = stringResource(R.string.beneficiaries_search_placeholder),
          modifier = Modifier.padding(horizontal = 16.dp),
          testTag = "beneficiaries_search_input",
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (beneficiaries.isEmpty()) {
          HebrEmptyState(
            title = stringResource(R.string.beneficiaries_empty_title),
            subtitle = stringResource(R.string.beneficiaries_empty_subtitle),
            actionLabel = stringResource(R.string.beneficiaries_empty_action),
            onAction = { showAddBeneficiaryDialog = true },
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            items(beneficiaries, key = { it.id }) { item ->
              Card(
                onClick = { viewModel.openBeneficiaryDetail(item.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().testTag("beneficiary_card_${item.id}"),
              ) {
                Column(
                  modifier = Modifier.padding(14.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Text(
                      text = item.fullName,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                    )
                    HebrStatusBadge(
                      text = item.vulnerabilityLevel.labelAr,
                      containerColor =
                        if (item.vulnerabilityLevel == VulnerabilityLevel.CRITICAL ||
                          item.vulnerabilityLevel == VulnerabilityLevel.HIGH
                        ) {
                          MaterialTheme.colorScheme.errorContainer
                        } else {
                          MaterialTheme.colorScheme.secondaryContainer
                        },
                      contentColor =
                        if (item.vulnerabilityLevel == VulnerabilityLevel.CRITICAL ||
                          item.vulnerabilityLevel == VulnerabilityLevel.HIGH
                        ) {
                          MaterialTheme.colorScheme.onErrorContainer
                        } else {
                          MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                  }
                  Text(
                    text = stringResource(
                      R.string.beneficiaries_card_ids_format,
                      item.fileNumber,
                      item.nationalId,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Text(
                      text = stringResource(
                        R.string.beneficiaries_card_family_format,
                        item.familySize,
                        HebrFormatters.formatAmount(item.monthlyIncome),
                      ),
                      style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                      text = item.status.labelAr,
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.primary,
                    )
                  }
                }
              }
            }
          }
        }
      } else {
        if (socialCases.isEmpty()) {
          HebrEmptyState(
            title = stringResource(R.string.cases_empty_title),
            subtitle =
              if (beneficiaries.isEmpty()) stringResource(R.string.cases_empty_need_beneficiary)
              else stringResource(R.string.cases_empty_subtitle),
            actionLabel =
              if (beneficiaries.isNotEmpty()) stringResource(R.string.cases_empty_action) else null,
            onAction = if (beneficiaries.isNotEmpty()) ({ showAddCaseDialog = true }) else null,
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            items(socialCases, key = { it.id }) { socialCase ->
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
                        text = socialCase.beneficiaryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                      )
                      Text(
                        text = "${socialCase.caseType.labelAr} • ${HebrFormatters.formatDate(socialCase.openedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                    HebrStatusBadge(
                      text = socialCase.priority.labelAr,
                      containerColor =
                        if (socialCase.priority == Priority.NORMAL) {
                          MaterialTheme.colorScheme.primaryContainer
                        } else {
                          MaterialTheme.colorScheme.errorContainer
                        },
                      contentColor =
                        if (socialCase.priority == Priority.NORMAL) {
                          MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                          MaterialTheme.colorScheme.onErrorContainer
                        },
                    )
                  }
                  Text(
                    text = socialCase.assessmentSummary,
                    style = MaterialTheme.typography.bodyMedium,
                  )
                  if (!socialCase.recommendedAction.isNullOrBlank()) {
                    Text(
                      text = stringResource(
                        R.string.cases_recommendation_format,
                        socialCase.recommendedAction,
                      ),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.secondary,
                    )
                  }
                  LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CaseStatus.entries) { statusOption ->
                      FilterChip(
                        selected = socialCase.status == statusOption,
                        onClick = { viewModel.updateCaseStatus(socialCase.id, statusOption) },
                        label = { Text(statusOption.labelAr) },
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

  if (showAddBeneficiaryDialog) {
    AddBeneficiaryDialog(
      onDismiss = { showAddBeneficiaryDialog = false },
      onSave = { name, nid, phone, address, familySize, income, level, notes ->
        viewModel.registerBeneficiary(
          name,
          nid,
          phone,
          address,
          familySize,
          income,
          level,
          notes,
        ) {
          showAddBeneficiaryDialog = false
        }
      },
    )
  }

  if (showAddCaseDialog) {
    if (beneficiaries.isEmpty()) {
      AlertDialog(
        onDismissRequest = { showAddCaseDialog = false },
        title = { Text(stringResource(R.string.label_notice)) },
        text = { Text(stringResource(R.string.cases_require_beneficiary_message)) },
        confirmButton = {
          Button(onClick = {
            showAddCaseDialog = false
            viewModel.selectTab(0)
            showAddBeneficiaryDialog = true
          }) {
            Text(stringResource(R.string.cases_register_beneficiary_now))
          }
        },
        dismissButton = {
          TextButton(onClick = { showAddCaseDialog = false }) {
            Text(stringResource(R.string.action_close))
          }
        },
      )
    } else {
      AddSocialCaseDialog(
        beneficiaries = beneficiaries,
        onDismiss = { showAddCaseDialog = false },
        onSave = { beneficiaryId, type, priority, summary, action ->
          viewModel.createSocialCase(beneficiaryId, type, priority, summary, action) {
            showAddCaseDialog = false
          }
        },
      )
    }
  }

  selectedBeneficiary?.let { detail ->
    AlertDialog(
      onDismissRequest = { viewModel.openBeneficiaryDetail(null) },
      icon = { Icon(Icons.Filled.FolderShared, contentDescription = null) },
      title = { Text(detail.fullName, style = MaterialTheme.typography.titleLarge) },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(stringResource(R.string.beneficiary_detail_file_format, detail.fileNumber))
          Text(stringResource(R.string.beneficiary_detail_nid_format, detail.nationalId))
          Text(stringResource(R.string.beneficiary_detail_phone_format, detail.phone))
          Text(stringResource(R.string.beneficiary_detail_address_format, detail.address))
          Text(
            stringResource(
              R.string.beneficiary_detail_family_format,
              detail.familySize,
              HebrFormatters.formatAmount(detail.monthlyIncome),
            )
          )
          if (!detail.notes.isNullOrBlank()) {
            Text(
              stringResource(R.string.beneficiary_detail_notes_format, detail.notes),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          HorizontalDivider()
          Text(
            stringResource(R.string.beneficiary_detail_update_status),
            style = MaterialTheme.typography.labelLarge,
          )
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(BeneficiaryStatus.entries) { st ->
              FilterChip(
                selected = detail.status == st,
                onClick = { viewModel.updateBeneficiaryStatus(detail.id, st) },
                label = { Text(st.labelAr) },
              )
            }
          }
          HorizontalDivider()
          Text(
            text = stringResource(
              R.string.beneficiary_detail_aid_history_title,
              selectedBeneficiaryAid.size,
            ),
            style = MaterialTheme.typography.labelLarge,
          )
          if (selectedBeneficiaryAid.isEmpty()) {
            Text(
              stringResource(R.string.beneficiary_detail_no_aid),
              style = MaterialTheme.typography.bodySmall,
            )
          } else {
            selectedBeneficiaryAid.forEach { aid ->
              Text(
                text = "• ${aid.aidType.labelAr} (${HebrFormatters.formatAmount(aid.amount)}) - ${HebrFormatters.formatDate(aid.distributedAt)}",
                style = MaterialTheme.typography.bodySmall,
              )
            }
          }
          OutlinedButton(
            onClick = { deleteCandidateId = detail.id },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(Icons.Filled.DeleteOutline, contentDescription = null)
            Text(stringResource(R.string.beneficiary_detail_delete_button))
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { viewModel.openBeneficiaryDetail(null) }) {
          Text(stringResource(R.string.action_close))
        }
      },
    )
  }

  deleteCandidateId?.let { id ->
    HebrConfirmDialog(
      title = stringResource(R.string.beneficiary_delete_confirm_title),
      message = stringResource(R.string.beneficiary_delete_confirm_message),
      confirmLabel = stringResource(R.string.action_delete),
      onConfirm = {
        deleteCandidateId = null
        viewModel.deleteBeneficiary(id)
      },
      onDismiss = { deleteCandidateId = null },
    )
  }
}

@Composable
private fun AddBeneficiaryDialog(
  onDismiss: () -> Unit,
  onSave: (String, String, String, String, String, String, VulnerabilityLevel, String) -> Unit,
) {
  var fullName by remember { mutableStateOf("") }
  var nationalId by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }
  var familySize by remember { mutableStateOf("4") }
  var income by remember { mutableStateOf("0") }
  var level by remember { mutableStateOf(VulnerabilityLevel.MEDIUM) }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        stringResource(R.string.beneficiary_add_dialog_title),
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = { Text(stringResource(R.string.auth_field_full_name)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("ben_name_input"),
        )
        OutlinedTextField(
          value = nationalId,
          onValueChange = { if (it.all(Char::isDigit)) nationalId = it },
          label = { Text(stringResource(R.string.beneficiary_field_nid)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("ben_nid_input"),
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text(stringResource(R.string.auth_field_phone)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("ben_phone_input"),
        )
        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text(stringResource(R.string.beneficiary_field_address)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("ben_address_input"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = familySize,
            onValueChange = { if (it.all(Char::isDigit)) familySize = it },
            label = { Text(stringResource(R.string.beneficiary_field_family_size)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = income,
            onValueChange = { income = it },
            label = { Text(stringResource(R.string.beneficiary_field_monthly_income)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }
        HebrDropdownSelector(
          label = stringResource(R.string.beneficiary_field_vulnerability),
          options = VulnerabilityLevel.entries,
          selectedOption = level,
          optionLabel = { it.labelAr },
          onOptionSelected = { level = it },
        )
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(stringResource(R.string.beneficiary_field_notes)) },
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(fullName, nationalId, phone, address, familySize, income, level, notes)
        },
        modifier = Modifier.testTag("save_beneficiary_button"),
      ) {
        Text(stringResource(R.string.action_save))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
  )
}

@Composable
private fun AddSocialCaseDialog(
  beneficiaries: List<Beneficiary>,
  onDismiss: () -> Unit,
  onSave: (Long, CaseType, Priority, String, String) -> Unit,
) {
  var selectedBeneficiary by remember { mutableStateOf(beneficiaries.first()) }
  var caseType by remember { mutableStateOf(CaseType.FINANCIAL) }
  var priority by remember { mutableStateOf(Priority.NORMAL) }
  var summary by remember { mutableStateOf("") }
  var recommendedAction by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        stringResource(R.string.case_add_dialog_title),
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        HebrDropdownSelector(
          label = stringResource(R.string.case_field_beneficiary),
          options = beneficiaries,
          selectedOption = selectedBeneficiary,
          optionLabel = { "${it.fullName} (${it.fileNumber})" },
          onOptionSelected = { selectedBeneficiary = it },
        )
        HebrDropdownSelector(
          label = stringResource(R.string.case_field_type),
          options = CaseType.entries,
          selectedOption = caseType,
          optionLabel = { it.labelAr },
          onOptionSelected = { caseType = it },
        )
        HebrDropdownSelector(
          label = stringResource(R.string.case_field_priority),
          options = Priority.entries,
          selectedOption = priority,
          optionLabel = { it.labelAr },
          onOptionSelected = { priority = it },
        )
        OutlinedTextField(
          value = summary,
          onValueChange = { summary = it },
          label = { Text(stringResource(R.string.case_field_summary)) },
          minLines = 2,
          modifier = Modifier.fillMaxWidth().testTag("case_summary_input"),
        )
        OutlinedTextField(
          value = recommendedAction,
          onValueChange = { recommendedAction = it },
          label = { Text(stringResource(R.string.case_field_recommendation)) },
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(selectedBeneficiary.id, caseType, priority, summary, recommendedAction)
        },
        modifier = Modifier.testTag("save_case_button"),
      ) {
        Text(stringResource(R.string.action_save))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
  )
}
