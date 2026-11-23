package com.example.ui.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.security.SessionManager
import com.example.domain.model.AssociationReportSummary
import com.example.domain.model.Priority
import com.example.domain.model.SocialCase
import com.example.domain.model.StaffMember
import com.example.domain.model.TaskStatus
import com.example.domain.model.VolunteerTask
import com.example.domain.repository.BeneficiaryRepository
import com.example.domain.repository.VolunteerAndReportsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
  val currentStaff: StaffMember? = null,
  val summary: AssociationReportSummary = AssociationReportSummary(),
  val urgentCases: List<SocialCase> = emptyList(),
  val upcomingTasks: List<VolunteerTask> = emptyList(),
)

class DashboardViewModel(
  private val sessionManager: SessionManager,
  beneficiaryRepository: BeneficiaryRepository,
  volunteerAndReportsRepository: VolunteerAndReportsRepository,
) : ViewModel() {

  val uiState: StateFlow<DashboardUiState> =
    combine(
        sessionManager.currentStaff,
        volunteerAndReportsRepository.observeReportSummary(),
        beneficiaryRepository.observeSocialCases(),
        volunteerAndReportsRepository.observeTasks(),
      ) { staff, summary, cases, tasks ->
        DashboardUiState(
          currentStaff = staff,
          summary = summary,
          urgentCases =
            cases
              .filter { it.priority != Priority.NORMAL && it.status.name in listOf("OPEN", "IN_PROGRESS") }
              .take(4),
          upcomingTasks = tasks.filter { it.status != TaskStatus.DONE }.take(4),
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(),
      )

  fun lockSession() {
    sessionManager.signOut()
  }

  class Factory(
    private val sessionManager: SessionManager,
    private val beneficiaryRepository: BeneficiaryRepository,
    private val volunteerAndReportsRepository: VolunteerAndReportsRepository,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return DashboardViewModel(sessionManager, beneficiaryRepository, volunteerAndReportsRepository)
        as T
    }
  }
}
