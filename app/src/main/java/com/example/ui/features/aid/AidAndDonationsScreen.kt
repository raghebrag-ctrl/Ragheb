package com.example.ui.features.aid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.example.domain.model.AidType
import com.example.domain.model.Beneficiary
import com.example.domain.model.DonationType
import com.example.domain.model.SocialCase
import com.example.ui.components.HebrDropdownSelector
import com.example.ui.components.HebrEmptyState
import com.example.ui.components.HebrErrorBanner
import com.example.ui.components.HebrStatusBadge

@Composable
fun AidAndDonationsScreen(viewModel: AidAndDonationsViewModel) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val donations by viewModel.donations.collectAsStateWithLifecycle()
  val aidList by viewModel.aidDistributions.collectAsStateWithLifecycle()
  val beneficiaries by viewModel.beneficiaries.collectAsStateWithLifecycle()
  val socialCases by viewModel.socialCases.collectAsStateWithLifecycle()
  val summary by viewModel.reportSummary.collectAsStateWithLifecycle()

  var showAddDonationDialog by remember { mutableStateOf(false) }
  var showDistributeAidDialog by remember { mutableStateOf(false) }

  val balance = (summary.totalCashDonations - summary.totalCashAidDistributed).coerceAtLeast(0.0)

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          if (state.selectedTab == 0) showAddDonationDialog = true else showDistributeAidDialog = true
        },
        modifier = Modifier.testTag("finance_fab"),
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
          text = { Text(stringResource(R.string.finance_tab_donations, donations.size)) },
          modifier = Modifier.testTag("tab_donations"),
        )
        Tab(
          selected = state.selectedTab == 1,
          onClick = { viewModel.selectTab(1) },
          text = { Text(stringResource(R.string.finance_tab_aid, aidList.size)) },
          modifier = Modifier.testTag("tab_aid_distributions"),
        )
      }

      // Financial Balance Strip
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.medium,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column {
            Text(
              stringResource(R.string.finance_strip_incoming),
              style = MaterialTheme.typography.labelSmall,
            )
            Text(
              HebrFormatters.formatAmount(summary.totalCashDonations),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
          }
          Column {
            Text(
              stringResource(R.string.finance_strip_outgoing),
              style = MaterialTheme.typography.labelSmall,
            )
            Text(
              HebrFormatters.formatAmount(summary.totalCashAidDistributed),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
          }
          Column {
            Text(
              stringResource(R.string.finance_strip_balance),
              style = MaterialTheme.typography.labelSmall,
            )
            Text(
              HebrFormatters.formatAmount(balance),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        HebrErrorBanner(
          errorMessage = state.errorMessage,
          onDismiss = viewModel::clearMessages,
        )
        if (!state.feedbackMessage.isNullOrBlank()) {
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
        if (donations.isEmpty()) {
          HebrEmptyState(
            title = stringResource(R.string.donations_empty_title),
            subtitle = stringResource(R.string.donations_empty_subtitle),
            actionLabel = stringResource(R.string.donations_empty_action),
            onAction = { showAddDonationDialog = true },
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            items(donations, key = { it.id }) { donation ->
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.medium,
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
                      text = donation.donorName,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                    )
                    HebrStatusBadge(
                      text = donation.donationType.labelAr,
                      containerColor = MaterialTheme.colorScheme.secondaryContainer,
                      contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                  }
                  Text(
                    text =
                      if (donation.amount > 0) {
                        stringResource(
                          R.string.donation_amount_receipt_format,
                          HebrFormatters.formatAmount(donation.amount),
                          donation.receiptNumber,
                        )
                      } else {
                        stringResource(
                          R.string.donation_inkind_receipt_format,
                          donation.inKindDescription.orEmpty(),
                          donation.receiptNumber,
                        )
                      },
                    style = MaterialTheme.typography.bodyMedium,
                  )
                  Text(
                    text = stringResource(
                      R.string.donation_meta_format,
                      HebrFormatters.formatDate(donation.receivedAt),
                      donation.receivedByName ?: stringResource(R.string.donation_default_receiver),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      } else {
        if (aidList.isEmpty()) {
          HebrEmptyState(
            title = stringResource(R.string.aid_empty_title),
            subtitle =
              if (beneficiaries.isEmpty()) stringResource(R.string.aid_empty_need_beneficiary)
              else stringResource(R.string.aid_empty_subtitle),
            actionLabel =
              if (beneficiaries.isNotEmpty()) stringResource(R.string.aid_empty_action) else null,
            onAction = if (beneficiaries.isNotEmpty()) ({ showDistributeAidDialog = true }) else null,
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            items(aidList, key = { it.id }) { aid ->
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.medium,
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
                      text = aid.beneficiaryName,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                    )
                    HebrStatusBadge(
                      text = aid.aidType.labelAr,
                      containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                      contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                  }
                  if (aid.amount > 0) {
                    Text(
                      text = stringResource(
                        R.string.aid_amount_format,
                        HebrFormatters.formatAmount(aid.amount),
                      ),
                      style = MaterialTheme.typography.bodyMedium,
                    )
                  }
                  if (!aid.itemDetails.isNullOrBlank()) {
                    Text(
                      text = stringResource(R.string.aid_details_format, aid.itemDetails),
                      style = MaterialTheme.typography.bodySmall,
                    )
                  }
                  Text(
                    text = stringResource(
                      R.string.aid_approved_by_format,
                      aid.approvedByName ?: stringResource(R.string.donation_default_receiver),
                      HebrFormatters.formatDate(aid.distributedAt),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  if (showAddDonationDialog) {
    AddDonationDialog(
      onDismiss = { showAddDonationDialog = false },
      onSave = { donor, phone, type, amount, inKind, purpose, receipt ->
        viewModel.recordDonation(donor, phone, type, amount, inKind, purpose, receipt) {
          showAddDonationDialog = false
        }
      },
    )
  }

  if (showDistributeAidDialog) {
    if (beneficiaries.isEmpty()) {
      AlertDialog(
        onDismissRequest = { showDistributeAidDialog = false },
        title = { Text(stringResource(R.string.label_notice)) },
        text = { Text(stringResource(R.string.aid_require_beneficiary)) },
        confirmButton = {
          Button(onClick = { showDistributeAidDialog = false }) {
            Text(stringResource(R.string.action_ok))
          }
        },
      )
    } else {
      DistributeAidDialog(
        beneficiaries = beneficiaries,
        socialCases = socialCases,
        onDismiss = { showDistributeAidDialog = false },
        onSave = { benId, caseId, type, amount, details ->
          viewModel.distributeAid(benId, caseId, type, amount, details) {
            showDistributeAidDialog = false
          }
        },
      )
    }
  }
}

@Composable
private fun AddDonationDialog(
  onDismiss: () -> Unit,
  onSave: (String, String, DonationType, String, String, String, String) -> Unit,
) {
  var donorName by remember { mutableStateOf("") }
  var donorPhone by remember { mutableStateOf("") }
  var type by remember { mutableStateOf(DonationType.CASH) }
  var amount by remember { mutableStateOf("") }
  var inKindDesc by remember { mutableStateOf("") }
  var purpose by remember { mutableStateOf("") }
  var receipt by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        stringResource(R.string.donation_add_dialog_title),
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = donorName,
          onValueChange = { donorName = it },
          label = { Text(stringResource(R.string.donation_field_donor)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("donor_name_input"),
        )
        OutlinedTextField(
          value = donorPhone,
          onValueChange = { donorPhone = it },
          label = { Text(stringResource(R.string.donation_field_phone)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        HebrDropdownSelector(
          label = stringResource(R.string.donation_field_type),
          options = DonationType.entries,
          selectedOption = type,
          optionLabel = { it.labelAr },
          onOptionSelected = { type = it },
        )
        if (type == DonationType.IN_KIND) {
          OutlinedTextField(
            value = inKindDesc,
            onValueChange = { inKindDesc = it },
            label = { Text(stringResource(R.string.donation_field_inkind)) },
            modifier = Modifier.fillMaxWidth().testTag("donation_inkind_input"),
          )
        } else {
          OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text(stringResource(R.string.donation_field_amount)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("donation_amount_input"),
          )
        }
        OutlinedTextField(
          value = purpose,
          onValueChange = { purpose = it },
          label = { Text(stringResource(R.string.donation_field_purpose)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = receipt,
          onValueChange = { receipt = it },
          label = { Text(stringResource(R.string.donation_field_receipt)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(donorName, donorPhone, type, amount, inKindDesc, purpose, receipt) },
        modifier = Modifier.testTag("save_donation_button"),
      ) {
        Text(stringResource(R.string.action_save))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
  )
}

@Composable
private fun DistributeAidDialog(
  beneficiaries: List<Beneficiary>,
  socialCases: List<SocialCase>,
  onDismiss: () -> Unit,
  onSave: (Long, Long?, AidType, String, String) -> Unit,
) {
  var selectedBen by remember { mutableStateOf(beneficiaries.first()) }
  val matchingCases = remember(selectedBen, socialCases) {
    socialCases.filter { it.beneficiaryId == selectedBen.id }
  }
  var selectedCase by remember(matchingCases) { mutableStateOf(matchingCases.firstOrNull()) }
  var aidType by remember { mutableStateOf(AidType.CASH) }
  var amount by remember { mutableStateOf("") }
  var details by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        stringResource(R.string.aid_add_dialog_title),
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
          selectedOption = selectedBen,
          optionLabel = { "${it.fullName} (${it.fileNumber})" },
          onOptionSelected = { selectedBen = it },
        )
        if (matchingCases.isNotEmpty()) {
          HebrDropdownSelector(
            label = stringResource(R.string.aid_field_case),
            options = matchingCases,
            selectedOption = selectedCase,
            optionLabel = { "${it.caseType.labelAr} (${it.status.labelAr})" },
            onOptionSelected = { selectedCase = it },
          )
        }
        HebrDropdownSelector(
          label = stringResource(R.string.aid_field_type),
          options = AidType.entries,
          selectedOption = aidType,
          optionLabel = { it.labelAr },
          onOptionSelected = { aidType = it },
        )
        OutlinedTextField(
          value = amount,
          onValueChange = { amount = it },
          label = {
            Text(
              if (aidType == AidType.CASH) stringResource(R.string.aid_field_amount_cash)
              else stringResource(R.string.aid_field_amount_estimate)
            )
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("aid_amount_input"),
        )
        OutlinedTextField(
          value = details,
          onValueChange = { details = it },
          label = { Text(stringResource(R.string.aid_field_details)) },
          modifier = Modifier.fillMaxWidth().testTag("aid_details_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(selectedBen.id, selectedCase?.id, aidType, amount, details) },
        modifier = Modifier.testTag("save_aid_button"),
      ) {
        Text(stringResource(R.string.aid_confirm_button))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
  )
}
