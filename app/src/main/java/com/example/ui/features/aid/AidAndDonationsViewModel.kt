package com.example.ui.features.aid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.result.AppResult
import com.example.domain.model.AidDistribution
import com.example.domain.model.AidType
import com.example.domain.model.AssociationReportSummary
import com.example.domain.model.Beneficiary
import com.example.domain.model.Donation
import com.example.domain.model.DonationType
import com.example.domain.model.SocialCase
import com.example.domain.repository.BeneficiaryRepository
import com.example.domain.repository.FinanceAidRepository
import com.example.domain.repository.VolunteerAndReportsRepository
import com.example.domain.usecase.DistributeAidUseCase
import com.example.domain.usecase.RecordDonationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AidAndDonationsUiState(
  val selectedTab: Int = 0,
  val errorMessage: String? = null,
  val feedbackMessage: String? = null,
)

class AidAndDonationsViewModel(
  financeAidRepository: FinanceAidRepository,
  beneficiaryRepository: BeneficiaryRepository,
  volunteerAndReportsRepository: VolunteerAndReportsRepository,
  private val recordDonationUseCase: RecordDonationUseCase,
  private val distributeAidUseCase: DistributeAidUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(AidAndDonationsUiState())
  val uiState: StateFlow<AidAndDonationsUiState> = _uiState.asStateFlow()

  val donations: StateFlow<List<Donation>> =
    financeAidRepository
      .observeDonations()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val aidDistributions: StateFlow<List<AidDistribution>> =
    financeAidRepository
      .observeAidDistributions()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val beneficiaries: StateFlow<List<Beneficiary>> =
    beneficiaryRepository
      .observeBeneficiaries("")
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val socialCases: StateFlow<List<SocialCase>> =
    beneficiaryRepository
      .observeSocialCases()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val reportSummary: StateFlow<AssociationReportSummary> =
    volunteerAndReportsRepository
      .observeReportSummary()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AssociationReportSummary())

  fun selectTab(index: Int) {
    _uiState.update { it.copy(selectedTab = index, errorMessage = null) }
  }

  fun clearMessages() {
    _uiState.update { it.copy(errorMessage = null, feedbackMessage = null) }
  }

  fun recordDonation(
    donorName: String,
    donorPhone: String,
    donationType: DonationType,
    amountText: String,
    inKindDescription: String,
    purpose: String,
    receiptNumber: String,
    onSuccess: () -> Unit,
  ) {
    val amount =
      if (donationType == DonationType.IN_KIND) {
        amountText.toDoubleOrNull() ?: 0.0
      } else {
        amountText.toDoubleOrNull() ?: -1.0
      }
    viewModelScope.launch {
      when (
        val result =
          recordDonationUseCase(
            Donation(
              donorName = donorName,
              donorPhone = donorPhone.trim().ifBlank { null },
              donationType = donationType,
              amount = amount,
              inKindDescription = inKindDescription.trim().ifBlank { null },
              purpose = purpose.trim().ifBlank { null },
              receiptNumber = receiptNumber,
            )
          )
      ) {
        is AppResult.Success -> {
          _uiState.update {
            it.copy(feedbackMessage = "تم توثيق التبرع وإصدار الإيصال بنجاح.", errorMessage = null)
          }
          onSuccess()
        }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  fun distributeAid(
    beneficiaryId: Long,
    caseId: Long?,
    aidType: AidType,
    amountText: String,
    itemDetails: String,
    onSuccess: () -> Unit,
  ) {
    val amount = amountText.toDoubleOrNull() ?: if (aidType == AidType.CASH) -1.0 else 0.0
    viewModelScope.launch {
      when (
        val result =
          distributeAidUseCase(
            AidDistribution(
              beneficiaryId = beneficiaryId,
              caseId = caseId,
              aidType = aidType,
              amount = amount,
              itemDetails = itemDetails.trim().ifBlank { null },
            )
          )
      ) {
        is AppResult.Success -> {
          _uiState.update {
            it.copy(
              feedbackMessage = "تم اعتماد وصرف المساعدة وتوثيقها في سجل التدقيق.",
              errorMessage = null,
            )
          }
          onSuccess()
        }
        is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.messageAr) }
      }
    }
  }

  class Factory(
    private val financeAidRepository: FinanceAidRepository,
    private val beneficiaryRepository: BeneficiaryRepository,
    private val volunteerAndReportsRepository: VolunteerAndReportsRepository,
    private val recordDonationUseCase: RecordDonationUseCase,
    private val distributeAidUseCase: DistributeAidUseCase,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return AidAndDonationsViewModel(
        financeAidRepository,
        beneficiaryRepository,
        volunteerAndReportsRepository,
        recordDonationUseCase,
        distributeAidUseCase,
      )
        as T
    }
  }
}
