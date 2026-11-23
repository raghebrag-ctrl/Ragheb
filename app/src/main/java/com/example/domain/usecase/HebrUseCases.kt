package com.example.domain.usecase

import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.core.security.SessionManager
import com.example.domain.model.AidDistribution
import com.example.domain.model.AidType
import com.example.domain.model.Beneficiary
import com.example.domain.model.BeneficiaryStatus
import com.example.domain.model.Course
import com.example.domain.model.CourseStatus
import com.example.domain.model.Donation
import com.example.domain.model.DonationType
import com.example.domain.model.EmpowermentProgram
import com.example.domain.model.EmpowermentStage
import com.example.domain.model.SocialCase
import com.example.domain.model.StaffMember
import com.example.domain.model.StaffRole
import com.example.domain.model.VolunteerTask
import com.example.domain.repository.BeneficiaryRepository
import com.example.domain.repository.EducationRepository
import com.example.domain.repository.EmpowermentRepository
import com.example.domain.repository.FinanceAidRepository
import com.example.domain.repository.StaffRepository
import com.example.domain.repository.VolunteerAndReportsRepository
import com.example.domain.validator.HebrValidators
import java.util.Calendar

class AuthenticateStaffUseCase(
  private val staffRepository: StaffRepository,
  private val sessionManager: SessionManager,
) {
  suspend operator fun invoke(staffId: Long, rawPin: String): AppResult<StaffMember> {
    when (val pinCheck = HebrValidators.validatePin(rawPin)) {
      is AppResult.Error -> return pinCheck
      is AppResult.Success -> Unit
    }
    val result = staffRepository.authenticateWithPin(staffId, rawPin.trim())
    if (result is AppResult.Success) {
      sessionManager.signIn(result.data)
    }
    return result
  }
}

class CreateStaffMemberUseCase(
  private val staffRepository: StaffRepository,
  private val sessionManager: SessionManager,
) {
  suspend operator fun invoke(
    fullName: String,
    role: StaffRole,
    phone: String,
    email: String?,
    specialty: String?,
    rawPin: String,
    requireAdminSession: Boolean = true,
  ): AppResult<Long> {
    if (requireAdminSession && !sessionManager.hasAnyRole(StaffRole.ADMIN)) {
      return AppResult.Error(
        AppError.UnauthorizedError("إضافة موظفين جدد متاحة فقط لمدير الجمعية.")
      )
    }
    val cleanName =
      when (val r = HebrValidators.validateRequiredText(fullName, "الاسم الكامل")) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    val cleanPhone =
      when (val r = HebrValidators.validatePhone(phone)) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    val cleanPin =
      when (val r = HebrValidators.validatePin(rawPin)) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    return staffRepository.createStaffMember(
      StaffMember(
        fullName = cleanName,
        role = role,
        phone = cleanPhone,
        email = email?.trim()?.ifBlank { null },
        specialty = specialty?.trim()?.ifBlank { null },
      ),
      cleanPin,
    )
  }
}

class RegisterBeneficiaryUseCase(
  private val repository: BeneficiaryRepository,
  private val sessionManager: SessionManager,
) {
  suspend operator fun invoke(beneficiary: Beneficiary): AppResult<Long> {
    val cleanName =
      when (val r = HebrValidators.validateRequiredText(beneficiary.fullName, "اسم المستفيد")) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    val cleanNationalId =
      when (val r = HebrValidators.validateNationalId(beneficiary.nationalId)) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    val cleanPhone =
      when (val r = HebrValidators.validatePhone(beneficiary.phone)) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    val cleanAddress =
      when (val r = HebrValidators.validateRequiredText(beneficiary.address, "العنوان")) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    when (val r = HebrValidators.validatePositiveInt(beneficiary.familySize, "عدد أفراد الأسرة")) {
      is AppResult.Error -> return r
      is AppResult.Success -> Unit
    }
    when (val r = HebrValidators.validateNonNegativeAmount(beneficiary.monthlyIncome, "الدخل الشهري")) {
      is AppResult.Error -> return r
      is AppResult.Success -> Unit
    }
    val year = Calendar.getInstance().get(Calendar.YEAR)
    val suffix = (System.currentTimeMillis() % 10000).toString().padStart(4, '0')
    val generatedFileNumber =
      beneficiary.fileNumber.trim().ifBlank { "HEBR-$year-$suffix" }

    return repository.addBeneficiary(
      beneficiary.copy(
        fileNumber = generatedFileNumber,
        fullName = cleanName,
        nationalId = cleanNationalId,
        phone = cleanPhone,
        address = cleanAddress,
        status = BeneficiaryStatus.ACTIVE,
      ),
      actorStaffId = sessionManager.currentStaff.value?.id,
    )
  }
}

class CreateSocialCaseUseCase(
  private val repository: BeneficiaryRepository,
  private val sessionManager: SessionManager,
) {
  suspend operator fun invoke(socialCase: SocialCase): AppResult<Long> {
    val cleanSummary =
      when (
        val r =
          HebrValidators.validateRequiredText(socialCase.assessmentSummary, "ملخص تقييم الحالة")
      ) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    return repository.createSocialCase(
      socialCase.copy(
        assessmentSummary = cleanSummary,
        assignedStaffId = socialCase.assignedStaffId ?: sessionManager.currentStaff.value?.id,
      ),
      actorStaffId = sessionManager.currentStaff.value?.id,
    )
  }
}

class CreateCourseUseCase(
  private val repository: EducationRepository,
  private val sessionManager: SessionManager,
) {
  suspend operator fun invoke(course: Course): AppResult<Long> {
    val cleanTitle =
      when (val r = HebrValidators.validateRequiredText(course.title, "عنوان الدورة")) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    when (val r = HebrValidators.validatePositiveInt(course.maxCapacity, "السعة القصوى")) {
      is AppResult.Error -> return r
      is AppResult.Success -> Unit
    }
    if (course.endDate < course.startDate) {
      return AppResult.Error(
        AppError.ValidationError("تاريخ انتهاء الدورة يجب أن يكون بعد تاريخ البدء.")
      )
    }
    return repository.createCourse(
      course.copy(title = cleanTitle, status = CourseStatus.ONGOING),
      actorStaffId = sessionManager.currentStaff.value?.id,
    )
  }
}

class EnrollBeneficiaryUseCase(
  private val repository: EducationRepository,
  private val sessionManager: SessionManager,
) {
  suspend operator fun invoke(courseId: Long, beneficiaryId: Long): AppResult<Long> {
    return repository.enrollBeneficiaryAtomic(
      courseId = courseId,
      beneficiaryId = beneficiaryId,
      actorStaffId = sessionManager.currentStaff.value?.id,
    )
  }
}

class CreateEmpowermentProgramUseCase(
  private val repository: EmpowermentRepository,
  private val sessionManager: SessionManager,
) {
  suspend operator fun invoke(program: EmpowermentProgram): AppResult<Long> {
    val cleanTitle =
      when (val r = HebrValidators.validateRequiredText(program.projectTitle, "اسم المشروع")) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    when (val r = HebrValidators.validateNonNegativeAmount(program.grantAmount, "قيمة التمويل")) {
      is AppResult.Error -> return r
      is AppResult.Success -> Unit
    }
    return repository.createProgram(
      program.copy(
        projectTitle = cleanTitle,
        supervisorId = program.supervisorId ?: sessionManager.currentStaff.value?.id,
        stage = EmpowermentStage.PROPOSAL,
        progressPercent = 15,
      ),
      actorStaffId = sessionManager.currentStaff.value?.id,
    )
  }
}

class RecordDonationUseCase(
  private val repository: FinanceAidRepository,
  private val sessionManager: SessionManager,
) {
  suspend operator fun invoke(donation: Donation): AppResult<Long> {
    val cleanDonor =
      when (val r = HebrValidators.validateRequiredText(donation.donorName, "اسم المتبرع")) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    if (donation.donationType == DonationType.IN_KIND) {
      val desc = donation.inKindDescription.orEmpty().trim()
      if (desc.isBlank()) {
        return AppResult.Error(
          AppError.ValidationError("يرجى وصف المواد العينية المتبرع بها.", "inKindDescription")
        )
      }
    } else {
      when (val r = HebrValidators.validatePositiveAmount(donation.amount, "مبلغ التبرع")) {
        is AppResult.Error -> return r
        is AppResult.Success -> Unit
      }
    }
    val receipt =
      donation.receiptNumber.trim().ifBlank {
        "RCP-${System.currentTimeMillis() % 1000000}"
      }
    return repository.recordDonation(
      donation.copy(
        donorName = cleanDonor,
        receiptNumber = receipt,
        receivedById = sessionManager.currentStaff.value?.id,
      ),
      actorStaffId = sessionManager.currentStaff.value?.id,
    )
  }
}

class DistributeAidUseCase(
  private val repository: FinanceAidRepository,
  private val sessionManager: SessionManager,
) {
  suspend operator fun invoke(distribution: AidDistribution): AppResult<Long> {
    val actor =
      try {
        sessionManager.requireRole(StaffRole.ADMIN, StaffRole.ACCOUNTANT, StaffRole.CASE_WORKER)
      } catch (e: SecurityException) {
        return AppResult.Error(AppError.UnauthorizedError(e.message ?: "صلاحية غير كافية."))
      }

    if (distribution.aidType == AidType.CASH) {
      when (val r = HebrValidators.validatePositiveAmount(distribution.amount, "قيمة المساعدة")) {
        is AppResult.Error -> return r
        is AppResult.Success -> Unit
      }
      if (!sessionManager.hasAnyRole(StaffRole.ADMIN, StaffRole.ACCOUNTANT)) {
        return AppResult.Error(
          AppError.UnauthorizedError("صرف المساعدات النقدية يتطلب صلاحية مدير الجمعية أو أمين الصندوق.")
        )
      }
    } else {
      when (val r = HebrValidators.validateNonNegativeAmount(distribution.amount, "القيمة التقديرية")) {
        is AppResult.Error -> return r
        is AppResult.Success -> Unit
      }
      if (distribution.itemDetails.isNullOrBlank()) {
        return AppResult.Error(
          AppError.ValidationError("يرجى توضيح تفاصيل المساعدة العينية المصروفة.")
        )
      }
    }

    return repository.distributeAidAtomic(
      distribution.copy(approvedById = actor.id),
      actorStaffId = actor.id,
    )
  }
}

class CreateVolunteerTaskUseCase(
  private val repository: VolunteerAndReportsRepository,
  private val sessionManager: SessionManager,
) {
  suspend operator fun invoke(task: VolunteerTask): AppResult<Long> {
    val cleanTitle =
      when (val r = HebrValidators.validateRequiredText(task.title, "عنوان المهمة")) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    val cleanDesc =
      when (val r = HebrValidators.validateRequiredText(task.description, "وصف المهمة")) {
        is AppResult.Error -> return r
        is AppResult.Success -> r.data
      }
    return repository.createTask(
      task.copy(title = cleanTitle, description = cleanDesc),
      actorStaffId = sessionManager.currentStaff.value?.id,
    )
  }
}
