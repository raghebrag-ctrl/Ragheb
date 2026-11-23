package com.example.ui.features.volunteers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.result.AppResult
import com.example.core.security.SessionManager
import com.example.domain.model.AssociationReportSummary
import com.example.domain.model.AuditLog
import com.example.domain.model.Priority
import com.example.domain.model.StaffMember
import com.example.domain.model.TaskStatus
import com.example.domain.model.VolunteerTask
import com.example.domain.repository.StaffRepository
import com.example.domain.repository.VolunteerAndReportsRepository
import com.example.domain.usecase.CreateVolunteerTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VolunteersUiState(
  val selectedTab: Int = 0,
  val errorMessage: String? = null,
  val feedbackMessage: String? = null,
)

class VolunteersAndReportsViewModel(
  private val volunteerAndReportsRepository: VolunteerAndReportsRepository,
  staffRepository: StaffRepository,
  private val sessionManager: SessionManager,
  private val createVolunteerTaskUseCase: CreateVolunteerTaskUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(VolunteersUiState())
  val uiState: StateFlow<VolunteersUiState> = _uiState.asStateFlow()

  val tasks: StateFlow<List<VolunteerTask>> =
    volunteerAndReportsRepository
      .observeTasks()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val staffList: StateFlow<List<StaffMember>> =
    staffRepository
      .observeAllStaff()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val reportSummary: StateFlow<AssociationReportSummary> =
    volunteerAndReportsRepository
      .observeReportSummary()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AssociationReportSummary())

  val auditLogs: StateFlow<List<AuditLog>> =
    staffRepository
      .observeAuditLogs()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun selectTab(index: Int) {
    _uiState.update { it.copy(selectedTab = index, errorMessage = null) }
  }

  fun clearMessages() {
    _uiState.update { it.copy(errorMessage = null, feedbackMessage = null) }
  }

  fun createTask(
    assigneeId: Long,
    title: String,
    description: String,
    dueDaysText: String,
    priority: Priority,
    onSuccess: () -> Unit,
  ) {
    val dueDays = (dueDaysText.toIntOrNull() ?: 3).coerceAtLeast(0)
    val dueDate = System.currentTimeMillis() + dueDays * 24L * 60L * 60L * 1000L
    viewModelScope.launch {
      when (
        val result =
          createVolunteerTaskUseCase(
            VolunteerTask(
              assigneeId = assigneeId,
              title = title,
              description = description,
              dueDate = dueDate,
              priority = priority,
              status = TaskStatus.TODO,
            )
          )
      ) {
        is AppResult.Success -> {
          _uiState.update { it.copy(feedbackMessage = "تم إسناد المهمة بنجاح.", errorMessage = null) }
          onSuccess()
        }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  fun updateTask(taskId: Long, status: TaskStatus, hoursLogged: Double) {
    viewModelScope.launch {
      when (
        val result =
          volunteerAndReportsRepository.updateTaskStatus(
            taskId = taskId,
            status = status,
            hoursLogged = hoursLogged,
            actorStaffId = sessionManager.currentStaff.value?.id,
          )
      ) {
        is AppResult.Success ->
          _uiState.update { it.copy(feedbackMessage = "تم تحديث حالة المهمة وساعات العمل.") }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  class Factory(
    private val volunteerAndReportsRepository: VolunteerAndReportsRepository,
    private val staffRepository: StaffRepository,
    private val sessionManager: SessionManager,
    private val createVolunteerTaskUseCase: CreateVolunteerTaskUseCase,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return VolunteersAndReportsViewModel(
        volunteerAndReportsRepository,
        staffRepository,
        sessionManager,
        createVolunteerTaskUseCase,
      )
        as T
    }
  }
}
