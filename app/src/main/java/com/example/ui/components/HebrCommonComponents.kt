package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun HebrStatCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = containerColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
        Text(
          text = title,
          style = MaterialTheme.typography.labelLarge,
          color = contentColor.copy(alpha = 0.85f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Box(
          modifier =
            Modifier.size(36.dp)
              .clip(CircleShape)
              .background(contentColor.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
          )
        }
      }
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = contentColor.copy(alpha = 0.78f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun HebrStatusBadge(
  text: String,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    color = containerColor,
    contentColor = contentColor,
    shape = RoundedCornerShape(50),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
      maxLines = 1,
    )
  }
}

@Composable
fun HebrSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
  testTag: String = "search_input",
) {
  OutlinedTextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = modifier.fillMaxWidth().testTag(testTag),
    placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
    leadingIcon = {
      Icon(
        imageVector = Icons.Filled.Search,
        contentDescription = stringResource(R.string.action_search),
      )
    },
    trailingIcon = {
      if (query.isNotEmpty()) {
        IconButton(onClick = { onQueryChange("") }) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(R.string.action_clear_search),
          )
        }
      }
    },
    singleLine = true,
    shape = MaterialTheme.shapes.medium,
  )
}

@Composable
fun HebrErrorBanner(
  errorMessage: String?,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(visible = !errorMessage.isNullOrBlank(), modifier = modifier) {
    Surface(
      color = MaterialTheme.colorScheme.errorContainer,
      contentColor = MaterialTheme.colorScheme.onErrorContainer,
      shape = MaterialTheme.shapes.medium,
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = Icons.Filled.ErrorOutline,
          contentDescription = stringResource(R.string.label_error_alert),
        )
        Text(
          text = errorMessage.orEmpty(),
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(R.string.action_dismiss_error),
          )
        }
      }
    }
  }
}

@Composable
fun HebrEmptyState(
  title: String,
  subtitle: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_hebr_empty_state),
      contentDescription = stringResource(R.string.cd_empty_illustration),
      modifier = Modifier.size(180.dp).clip(MaterialTheme.shapes.large),
      contentScale = ContentScale.Crop,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (actionLabel != null && onAction != null) {
      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = onAction, modifier = Modifier.testTag("empty_state_action_button")) {
        Text(actionLabel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> HebrDropdownSelector(
  label: String,
  options: List<T>,
  selectedOption: T?,
  optionLabel: (T) -> String,
  onOptionSelected: (T) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded },
    modifier = modifier.fillMaxWidth(),
  ) {
    OutlinedTextField(
      value = selectedOption?.let(optionLabel) ?: "",
      onValueChange = {},
      readOnly = true,
      label = { Text(label) },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      modifier =
        Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
      shape = MaterialTheme.shapes.small,
    )
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(optionLabel(option)) },
          onClick = {
            onOptionSelected(option)
            expanded = false
          },
        )
      }
    }
  }
}

@Composable
fun HebrConfirmDialog(
  title: String,
  message: String,
  confirmLabel: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title, style = MaterialTheme.typography.titleLarge) },
    text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
    confirmButton = {
      Button(onClick = onConfirm, modifier = Modifier.testTag("confirm_dialog_button")) {
        Text(confirmLabel)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.action_cancel))
      }
    },
  )
}
