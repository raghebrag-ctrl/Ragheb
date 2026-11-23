package com.example.ui.features.beneficiaries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.result.AppResult
import com.example.core.security.SessionManager
import com.example.domain.model.AidDistribution
import com.example.domain.model.Beneficiary
import com.example.domain.model.BeneficiaryStatus
import com.example.domain.model.CaseStatus
import com.example.domain.model.CaseType
import com.example.domain.model.Priority
import com.example.domain.model.SocialCase
import com.example.domain.model.VulnerabilityLevel
import com.example.domain.repository.BeneficiaryRepository
import com.example.domain.repository.FinanceAidRepository
import com.example.domain.usecase.CreateSocialCaseUseCase
import com.example.domain.usecase.RegisterBeneficiaryUseCase
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

data class BeneficiariesUiState(
  val selectedTab: Int = 0,
  val searchQuery: String = "",
  val selectedBeneficiaryId: Long? = null,
  val errorMessage: String? = null,
  val feedbackMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class BeneficiariesViewModel(
  private val beneficiaryRepository: BeneficiaryRepository,
  private val financeAidRepository: FinanceAidRepository,
  private val sessionManager: SessionManager,
  private val registerBeneficiaryUseCase: RegisterBeneficiaryUseCase,
  private val createSocialCaseUseCase: CreateSocialCaseUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(BeneficiariesUiState())
  val uiState: StateFlow<BeneficiariesUiState> = _uiState.asStateFlow()

  private val searchQueryFlow = MutableStateFlow("")

  val beneficiaries: StateFlow<List<Beneficiary>> =
    searchQueryFlow
      .flatMapLatest { query -> beneficiaryRepository.observeBeneficiaries(query) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val socialCases: StateFlow<List<SocialCase>> =
    beneficiaryRepository
      .observeSocialCases()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val selectedIdFlow = MutableStateFlow<Long?>(null)

  val selectedBeneficiary: StateFlow<Beneficiary?> =
    selectedIdFlow
      .flatMapLatest { id ->
        if (id == null) flowOf(null) else beneficiaryRepository.observeBeneficiaryById(id)
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val selectedBeneficiaryAid: StateFlow<List<AidDistribution>> =
    selectedIdFlow
      .flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else financeAidRepository.observeAidForBeneficiary(id)
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun selectTab(index: Int) {
    _uiState.update { it.copy(selectedTab = index, errorMessage = null) }
  }

  fun onSearchChange(query: String) {
    searchQueryFlow.value = query
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun openBeneficiaryDetail(beneficiaryId: Long?) {
    selectedIdFlow.value = beneficiaryId
    _uiState.update { it.copy(selectedBeneficiaryId = beneficiaryId) }
  }

  fun clearMessages() {
    _uiState.update { it.copy(errorMessage = null, feedbackMessage = null) }
  }

  fun registerBeneficiary(
    fullName: String,
    nationalId: String,
    phone: String,
    address: String,
    familySizeText: String,
    monthlyIncomeText: String,
    vulnerabilityLevel: VulnerabilityLevel,
    notes: String,
    onSuccess: () -> Unit,
  ) {
    val familySize = familySizeText.toIntOrNull() ?: 0
    val monthlyIncome = monthlyIncomeText.toDoubleOrNull() ?: -1.0
    viewModelScope.launch {
      val result =
        registerBeneficiaryUseCase(
          Beneficiary(
            fileNumber = "",
            fullName = fullName,
            nationalId = nationalId,
            phone = phone,
            address = address,
            familySize = familySize,
            monthlyIncome = monthlyIncome,
            vulnerabilityLevel = vulnerabilityLevel,
            status = BeneficiaryStatus.ACTIVE,
            notes = notes.trim().ifBlank { null },
          )
        )
      when (result) {
        is AppResult.Success -> {
          _uiState.update { it.copy(feedbackMessage = "تم تسجيل المستفيد بنجاح.", errorMessage = null) }
          onSuccess()
        }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  fun updateBeneficiaryStatus(beneficiaryId: Long, status: BeneficiaryStatus) {
    viewModelScope.launch {
      when (
        val result =
          beneficiaryRepository.updateBeneficiaryStatus(
            beneficiaryId,
            status,
            sessionManager.currentStaff.value?.id,
          )
      ) {
        is AppResult.Success ->
          _uiState.update { it.copy(feedbackMessage = "تم تحديث حالة المستفيد إلى ${status.labelAr}.") }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  fun deleteBeneficiary(beneficiaryId: Long) {
    viewModelScope.launch {
      when (
        val result =
          beneficiaryRepository.deleteBeneficiary(
            beneficiaryId,
            sessionManager.currentStaff.value?.id,
          )
      ) {
        is AppResult.Success -> {
          openBeneficiaryDetail(null)
          _uiState.update { it.copy(feedbackMessage = "تم حذف ملف المستفيد.") }
        }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  fun createSocialCase(
    beneficiaryId: Long,
    caseType: CaseType,
    priority: Priority,
    assessmentSummary: String,
    recommendedAction: String,
    onSuccess: () -> Unit,
  ) {
    viewModelScope.launch {
      val result =
        createSocialCaseUseCase(
          SocialCase(
            beneficiaryId = beneficiaryId,
            caseType = caseType,
            priority = priority,
            assessmentSummary = assessmentSummary,
            recommendedAction = recommendedAction.trim().ifBlank { null },
            status = CaseStatus.OPEN,
          )
        )
      when (result) {
        is AppResult.Success -> {
          _uiState.update { it.copy(feedbackMessage = "تم فتح الحالة الاجتماعية بنجاح.", errorMessage = null) }
          onSuccess()
        }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  fun updateCaseStatus(caseId: Long, status: CaseStatus) {
    viewModelScope.launch {
      when (
        val result =
          beneficiaryRepository.updateSocialCaseStatus(
            caseId,
            status,
            sessionManager.currentStaff.value?.id,
          )
      ) {
        is AppResult.Success ->
          _uiState.update { it.copy(feedbackMessage = "تم تحديث الحالة إلى ${status.labelAr}.") }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  class Factory(
    private val beneficiaryRepository: BeneficiaryRepository,
    private val financeAidRepository: FinanceAidRepository,
    private val sessionManager: SessionManager,
    private val registerBeneficiaryUseCase: RegisterBeneficiaryUseCase,
    private val createSocialCaseUseCase: CreateSocialCaseUseCase,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return BeneficiariesViewModel(
        beneficiaryRepository,
        financeAidRepository,
        sessionManager,
        registerBeneficiaryUseCase,
        createSocialCaseUseCase,
      )
        as T
    }
  }
}
