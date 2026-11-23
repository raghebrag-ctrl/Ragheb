package com.example.ui.features.education

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.result.AppResult
import com.example.core.security.SessionManager
import com.example.domain.model.Beneficiary
import com.example.domain.model.Course
import com.example.domain.model.CourseCategory
import com.example.domain.model.CourseEnrollment
import com.example.domain.model.CourseStatus
import com.example.domain.model.EmpowermentProgram
import com.example.domain.model.EmpowermentSector
import com.example.domain.model.EmpowermentStage
import com.example.domain.model.FundingType
import com.example.domain.model.StaffMember
import com.example.domain.repository.BeneficiaryRepository
import com.example.domain.repository.EducationRepository
import com.example.domain.repository.EmpowermentRepository
import com.example.domain.repository.StaffRepository
import com.example.domain.usecase.CreateCourseUseCase
import com.example.domain.usecase.CreateEmpowermentProgramUseCase
import com.example.domain.usecase.EnrollBeneficiaryUseCase
import java.util.Calendar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EducationUiState(
  val selectedTab: Int = 0,
  val activeCourseId: Long? = null,
  val errorMessage: String? = null,
  val feedbackMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class EducationAndEmpowermentViewModel(
  private val educationRepository: EducationRepository,
  private val empowermentRepository: EmpowermentRepository,
  beneficiaryRepository: BeneficiaryRepository,
  staffRepository: StaffRepository,
  private val sessionManager: SessionManager,
  private val createCourseUseCase: CreateCourseUseCase,
  private val enrollBeneficiaryUseCase: EnrollBeneficiaryUseCase,
  private val createEmpowermentProgramUseCase: CreateEmpowermentProgramUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(EducationUiState())
  val uiState: StateFlow<EducationUiState> = _uiState.asStateFlow()

  val courses: StateFlow<List<Course>> =
    educationRepository
      .observeCourses()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val empowermentPrograms: StateFlow<List<EmpowermentProgram>> =
    empowermentRepository
      .observePrograms()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val beneficiaries: StateFlow<List<Beneficiary>> =
    beneficiaryRepository
      .observeBeneficiaries("")
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val staffList: StateFlow<List<StaffMember>> =
    staffRepository
      .observeAllStaff()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val activeCourseIdFlow = MutableStateFlow<Long?>(null)

  val activeCourseEnrollments: StateFlow<List<CourseEnrollment>> =
    activeCourseIdFlow
      .flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else educationRepository.observeEnrollmentsForCourse(id)
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun selectTab(index: Int) {
    _uiState.update { it.copy(selectedTab = index, errorMessage = null) }
  }

  fun openCourseDetail(courseId: Long?) {
    activeCourseIdFlow.value = courseId
    _uiState.update { it.copy(activeCourseId = courseId, errorMessage = null) }
  }

  fun clearMessages() {
    _uiState.update { it.copy(errorMessage = null, feedbackMessage = null) }
  }

  fun createCourse(
    title: String,
    category: CourseCategory,
    instructorId: Long?,
    durationDaysText: String,
    capacityText: String,
    onSuccess: () -> Unit,
  ) {
    val days = (durationDaysText.toIntOrNull() ?: 30).coerceAtLeast(1)
    val capacity = capacityText.toIntOrNull() ?: 0
    val start = System.currentTimeMillis()
    val end = start + days * 24L * 60L * 60L * 1000L

    viewModelScope.launch {
      when (
        val result =
          createCourseUseCase(
            Course(
              title = title,
              category = category,
              instructorId = instructorId,
              startDate = start,
              endDate = end,
              maxCapacity = capacity,
              status = CourseStatus.ONGOING,
            )
          )
      ) {
        is AppResult.Success -> {
          _uiState.update { it.copy(feedbackMessage = "تم إنشاء الدورة بنجاح.", errorMessage = null) }
          onSuccess()
        }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  fun enrollBeneficiary(courseId: Long, beneficiaryId: Long) {
    viewModelScope.launch {
      when (val result = enrollBeneficiaryUseCase(courseId, beneficiaryId)) {
        is AppResult.Success ->
          _uiState.update { it.copy(feedbackMessage = "تم تسجيل المستفيد في الدورة بنجاح.", errorMessage = null) }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  fun markTodayAttendance(enrollmentId: Long, isPresent: Boolean) {
    val cal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    viewModelScope.launch {
      when (
        val result =
          educationRepository.markAttendance(
            enrollmentId = enrollmentId,
            sessionDate = cal.timeInMillis,
            isPresent = isPresent,
            note = null,
          )
      ) {
        is AppResult.Success ->
          _uiState.update {
            it.copy(feedbackMessage = if (isPresent) "تم رصد الحضور لليوم." else "تم رصد الغياب لليوم.")
          }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  fun createEmpowermentProgram(
    beneficiaryId: Long,
    projectTitle: String,
    sector: EmpowermentSector,
    fundingType: FundingType,
    grantAmountText: String,
    onSuccess: () -> Unit,
  ) {
    val amount = grantAmountText.toDoubleOrNull() ?: -1.0
    viewModelScope.launch {
      when (
        val result =
          createEmpowermentProgramUseCase(
            EmpowermentProgram(
              beneficiaryId = beneficiaryId,
              projectTitle = projectTitle,
              sector = sector,
              grantAmount = amount,
              fundingType = fundingType,
              stage = EmpowermentStage.PROPOSAL,
              progressPercent = 15,
            )
          )
      ) {
        is AppResult.Success -> {
          _uiState.update { it.copy(feedbackMessage = "تم إطلاق مشروع التمكين بنجاح.", errorMessage = null) }
          onSuccess()
        }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  fun advanceEmpowermentStage(program: EmpowermentProgram) {
    val stages = EmpowermentStage.entries
    val nextIdx = (stages.indexOf(program.stage) + 1).coerceAtMost(stages.lastIndex)
    val nextStage = stages[nextIdx]
    val nextProgress = ((nextIdx + 1) * 100) / stages.size

    viewModelScope.launch {
      when (
        val result =
          empowermentRepository.updateStageAndProgress(
            programId = program.id,
            stage = nextStage,
            progressPercent = nextProgress,
            actorStaffId = sessionManager.currentStaff.value?.id,
          )
      ) {
        is AppResult.Success ->
          _uiState.update {
            it.copy(feedbackMessage = "تمت ترقية المشروع إلى مرحلة ${nextStage.labelAr}.")
          }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  class Factory(
    private val educationRepository: EducationRepository,
    private val empowermentRepository: EmpowermentRepository,
    private val beneficiaryRepository: BeneficiaryRepository,
    private val staffRepository: StaffRepository,
    private val sessionManager: SessionManager,
    private val createCourseUseCase: CreateCourseUseCase,
    private val enrollBeneficiaryUseCase: EnrollBeneficiaryUseCase,
    private val createEmpowermentProgramUseCase: CreateEmpowermentProgramUseCase,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return EducationAndEmpowermentViewModel(
        educationRepository,
        empowermentRepository,
        beneficiaryRepository,
        staffRepository,
        sessionManager,
        createCourseUseCase,
        enrollBeneficiaryUseCase,
        createEmpowermentProgramUseCase,
      )
        as T
    }
  }
}
