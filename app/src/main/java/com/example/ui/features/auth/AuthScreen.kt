package com.example.ui.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.model.StaffRole
import com.example.ui.components.HebrDropdownSelector
import com.example.ui.components.HebrErrorBanner

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
  val staffList by viewModel.staffList.collectAsStateWithLifecycle()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showAddStaffDialog by remember { mutableStateOf(false) }

  val selectedStaff =
    staffList.firstOrNull { it.id == uiState.selectedStaffId } ?: staffList.firstOrNull()

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background,
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .verticalScroll(rememberScrollState())
          .padding(20.dp),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Brand Hero Header
        Card(
          shape = MaterialTheme.shapes.large,
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
          Column {
            Image(
              painter = painterResource(id = R.drawable.img_hebr_hero_banner),
              contentDescription = stringResource(R.string.cd_hero_banner),
              modifier = Modifier.fillMaxWidth().height(150.dp),
              contentScale = ContentScale.Crop,
            )
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = stringResource(R.string.app_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        HebrErrorBanner(
          errorMessage = uiState.errorMessage,
          onDismiss = viewModel::clearMessages,
        )

        if (!uiState.successMessage.isNullOrBlank()) {
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = uiState.successMessage.orEmpty(),
              modifier = Modifier.padding(12.dp),
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }

        // Login Card
        Card(
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.VerifiedUser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
              Text(
                text = stringResource(R.string.auth_portal_title),
                style = MaterialTheme.typography.titleLarge,
              )
            }

            val staffOptionPattern = stringResource(R.string.auth_staff_option_format)
            HebrDropdownSelector(
              label = stringResource(R.string.auth_select_staff_label),
              options = staffList,
              selectedOption = selectedStaff,
              optionLabel = { String.format(staffOptionPattern, it.fullName, it.role.labelAr) },
              onOptionSelected = { viewModel.selectStaff(it.id) },
            )

            OutlinedTextField(
              value = uiState.pinInput,
              onValueChange = viewModel::onPinChange,
              label = { Text(stringResource(R.string.auth_pin_label)) },
              leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
              visualTransformation = PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("login_pin_input"),
              shape = MaterialTheme.shapes.small,
            )

            Surface(
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
              shape = MaterialTheme.shapes.small,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = stringResource(R.string.auth_default_admin_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              )
            }

            Button(
              onClick = { viewModel.signIn(selectedStaff?.id) },
              enabled = !uiState.isLoading && selectedStaff != null,
              modifier = Modifier.fillMaxWidth().height(50.dp).testTag("login_submit_button"),
            ) {
              if (uiState.isLoading) {
                CircularProgressIndicator(
                  color = MaterialTheme.colorScheme.onPrimary,
                  strokeWidth = 2.dp,
                )
              } else {
                Text(
                  stringResource(R.string.auth_sign_in_button),
                  style = MaterialTheme.typography.labelLarge,
                )
              }
            }

            OutlinedButton(
              onClick = { showAddStaffDialog = true },
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("open_add_staff_button"),
            ) {
              Icon(Icons.Filled.PersonAdd, contentDescription = null)
              Spacer(modifier = Modifier.padding(4.dp))
              Text(stringResource(R.string.auth_add_staff_button))
            }
          }
        }
      }
    }
  }

  if (showAddStaffDialog) {
    AddStaffDialog(
      onDismiss = { showAddStaffDialog = false },
      onConfirm = { name, role, phone, specialty, pin ->
        viewModel.registerStaff(name, role, phone, specialty, pin) {
          showAddStaffDialog = false
        }
      },
    )
  }
}

@Composable
fun AddStaffDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, StaffRole, String, String, String) -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var specialty by remember { mutableStateOf("") }
  var pin by remember { mutableStateOf("") }
  var selectedRole by remember { mutableStateOf(StaffRole.CASE_WORKER) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        stringResource(R.string.auth_add_staff_dialog_title),
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(stringResource(R.string.auth_field_full_name)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("staff_name_input"),
        )
        HebrDropdownSelector(
          label = stringResource(R.string.auth_field_role),
          options = StaffRole.entries,
          selectedOption = selectedRole,
          optionLabel = { it.labelAr },
          onOptionSelected = { selectedRole = it },
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text(stringResource(R.string.auth_field_phone)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("staff_phone_input"),
        )
        OutlinedTextField(
          value = specialty,
          onValueChange = { specialty = it },
          label = { Text(stringResource(R.string.auth_field_specialty)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = pin,
          onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) pin = it },
          label = { Text(stringResource(R.string.auth_field_pin_range)) },
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("staff_pin_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(name, selectedRole, phone, specialty, pin) },
        modifier = Modifier.testTag("save_staff_button"),
      ) {
        Text(stringResource(R.string.action_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
    },
  )
}
