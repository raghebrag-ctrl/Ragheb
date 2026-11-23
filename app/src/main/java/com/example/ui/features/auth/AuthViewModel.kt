package com.example.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.result.AppResult
import com.example.core.security.SessionManager
import com.example.domain.model.StaffMember
import com.example.domain.model.StaffRole
import com.example.domain.repository.StaffRepository
import com.example.domain.usecase.AuthenticateStaffUseCase
import com.example.domain.usecase.CreateStaffMemberUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
  val selectedStaffId: Long? = null,
  val pinInput: String = "",
  val errorMessage: String? = null,
  val successMessage: String? = null,
  val isLoading: Boolean = false,
)

class AuthViewModel(
  staffRepository: StaffRepository,
  val sessionManager: SessionManager,
  private val authenticateStaffUseCase: AuthenticateStaffUseCase,
  private val createStaffMemberUseCase: CreateStaffMemberUseCase,
) : ViewModel() {

  val staffList: StateFlow<List<StaffMember>> =
    staffRepository.observeAllStaff().stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val currentStaff: StateFlow<StaffMember?> = sessionManager.currentStaff

  private val _uiState = MutableStateFlow(AuthUiState())
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  fun selectStaff(staffId: Long) {
    _uiState.update { it.copy(selectedStaffId = staffId, errorMessage = null) }
  }

  fun onPinChange(pin: String) {
    if (pin.length <= 8 && pin.all { it.isDigit() }) {
      _uiState.update { it.copy(pinInput = pin, errorMessage = null) }
    }
  }

  fun clearMessages() {
    _uiState.update { it.copy(errorMessage = null, successMessage = null) }
  }

  fun signIn(defaultFirstStaffId: Long?) {
    val targetId = _uiState.value.selectedStaffId ?: defaultFirstStaffId
    if (targetId == null) {
      _uiState.update { it.copy(errorMessage = "يرجى اختيار حساب الموظف أولاً.") }
      return
    }
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMessage = null) }
      when (val result = authenticateStaffUseCase(targetId, _uiState.value.pinInput)) {
        is AppResult.Success ->
          _uiState.update { it.copy(isLoading = false, pinInput = "", errorMessage = null) }
        is AppResult.Error ->
          _uiState.update { it.copy(isLoading = false, errorMessage = result.error.messageAr) }
      }
    }
  }

  fun registerStaff(
    fullName: String,
    role: StaffRole,
    phone: String,
    specialty: String,
    pin: String,
    onDone: () -> Unit,
  ) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMessage = null) }
      // Allow registering initial team members from login only if currently signed in as ADMIN,
      // or allow adding non-ADMIN field accounts when bootstrapping team.
      val requireAdmin = sessionManager.currentStaff.value != null
      when (
        val result =
          createStaffMemberUseCase(
            fullName = fullName,
            role = role,
            phone = phone,
            email = null,
            specialty = specialty,
            rawPin = pin,
            requireAdminSession = requireAdmin,
          )
      ) {
        is AppResult.Success -> {
          _uiState.update {
            it.copy(
              isLoading = false,
              selectedStaffId = result.data,
              successMessage = "تم إنشاء حساب ($fullName) بنجاح.",
            )
          }
          onDone()
        }
        is AppResult.Error ->
          _uiState.update { it.copy(isLoading = false, errorMessage = result.error.messageAr) }
      }
    }
  }

  class Factory(
    private val staffRepository: StaffRepository,
    private val sessionManager: SessionManager,
    private val authenticateStaffUseCase: AuthenticateStaffUseCase,
    private val createStaffMemberUseCase: CreateStaffMemberUseCase,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return AuthViewModel(
        staffRepository,
        sessionManager,
        authenticateStaffUseCase,
        createStaffMemberUseCase,
      )
        as T
    }
  }
}
