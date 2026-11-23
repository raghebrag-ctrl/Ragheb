package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.features.auth.AuthScreen
import com.example.ui.features.auth.AuthViewModel
import com.example.ui.navigation.HebrAppShell
import com.example.ui.theme.HebrAssociationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val container = (application as HebrApplication).container
    setContent {
      CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HebrAssociationTheme {
          val currentStaff by container.sessionManager.currentStaff.collectAsStateWithLifecycle()
          if (currentStaff == null) {
            val authViewModel: AuthViewModel =
              viewModel(
                factory =
                  AuthViewModel.Factory(
                    container.staffRepository,
                    container.sessionManager,
                    container.authenticateStaffUseCase,
                    container.createStaffMemberUseCase,
                  )
              )
            AuthScreen(viewModel = authViewModel)
          } else {
            HebrAppShell(container = container)
          }
        }
      }
    }
  }
}
