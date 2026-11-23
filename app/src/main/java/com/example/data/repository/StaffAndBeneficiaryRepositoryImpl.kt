package com.example.data.repository

import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.core.result.safeDbCall
import com.example.core.security.PinHasher
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.BeneficiaryDao
import com.example.data.local.dao.SocialCaseDao
import com.example.data.local.dao.StaffDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.StaffMemberEntity
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.AuditLog
import com.example.domain.model.Beneficiary
import com.example.domain.model.BeneficiaryStatus
import com.example.domain.model.CaseStatus
import com.example.domain.model.SocialCase
import com.example.domain.model.StaffMember
import com.example.domain.repository.BeneficiaryRepository
import com.example.domain.repository.StaffRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StaffRepositoryImpl(
  private val staffDao: StaffDao,
  private val auditLogDao: AuditLogDao,
) : StaffRepository {
  override fun observeAllStaff(): Flow<List<StaffMember>> =
    staffDao.observeAllStaff().map { list -> list.map { it.toDomain() } }

  override fun observeAuditLogs(): Flow<List<AuditLog>> =
    auditLogDao.observeAuditLogs().map { list -> list.map { it.toDomain() } }

  override suspend fun createStaffMember(staff: StaffMember, rawPin: String): AppResult<Long> =
    safeDbCall(duplicateMessage = "رقم الهاتف مسجل مسبقاً لموظف آخر.") {
      val salt = PinHasher.generateSaltHex()
      val hash = PinHasher.hashPin(rawPin, salt)
      val id =
        staffDao.insert(
          StaffMemberEntity(
            fullName = staff.fullName,
            role = staff.role,
            phone = staff.phone,
            email = staff.email,
            specialty = staff.specialty,
            pinHash = hash,
            pinSalt = salt,
            isActive = staff.isActive,
            joinedAt = staff.joinedAt,
          )
        )
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = null,
          action = "CREATE_STAFF",
          entityName = "staff_members",
          entityId = id,
          details = "إضافة عضو فريق (${staff.fullName}) بدور ${staff.role.labelAr}",
        )
      )
      id
    }

  override suspend fun authenticateWithPin(staffId: Long, rawPin: String): AppResult<StaffMember> {
    return try {
      val entity =
        staffDao.getById(staffId)
          ?: return AppResult.Error(AppError.NotFoundError("حساب الموظف غير موجود."))
      if (!entity.isActive) {
        return AppResult.Error(AppError.UnauthorizedError("تم تعطيل هذا الحساب من قبل الإدارة."))
      }
      if (!PinHasher.verifyPin(rawPin, entity.pinSalt, entity.pinHash)) {
        return AppResult.Error(AppError.UnauthorizedError("رمز التحقق (PIN) غير صحيح."))
      }
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = entity.id,
          action = "STAFF_LOGIN",
          entityName = "staff_members",
          entityId = entity.id,
          details = "تسجيل دخول الموظف ${entity.fullName}",
        )
      )
      AppResult.Success(entity.toDomain())
    } catch (e: Exception) {
      AppResult.Error(AppError.UnexpectedError("تعذر التحقق من بيانات الدخول.", e))
    }
  }

  override suspend fun toggleStaffActive(staffId: Long, isActive: Boolean): AppResult<Unit> =
    safeDbCall {
      staffDao.setActive(staffId, isActive)
    }

  override suspend fun logAction(
    actorStaffId: Long?,
    action: String,
    entityName: String,
    entityId: Long,
    details: String,
  ) {
    runCatching {
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = action,
          entityName = entityName,
          entityId = entityId,
          details = details,
        )
      )
    }
  }
}

class BeneficiaryRepositoryImpl(
  private val beneficiaryDao: BeneficiaryDao,
  private val socialCaseDao: SocialCaseDao,
  private val auditLogDao: AuditLogDao,
) : BeneficiaryRepository {
  override fun observeBeneficiaries(query: String): Flow<List<Beneficiary>> =
    beneficiaryDao.observeBeneficiaries(query.trim()).map { list -> list.map { it.toDomain() } }

  override fun observeBeneficiaryById(id: Long): Flow<Beneficiary?> =
    beneficiaryDao.observeById(id).map { it?.toDomain() }

  override fun observeSocialCases(): Flow<List<SocialCase>> =
    socialCaseDao.observeAllCases().map { list -> list.map { it.toDomain() } }

  override fun observeCasesByBeneficiary(beneficiaryId: Long): Flow<List<SocialCase>> =
    socialCaseDao.observeCasesByBeneficiary(beneficiaryId).map { list -> list.map { it.toDomain() } }

  override suspend fun addBeneficiary(
    beneficiary: Beneficiary,
    actorStaffId: Long?,
  ): AppResult<Long> =
    safeDbCall(
      duplicateMessage = "رقم الهوية الوطنية أو رقم الملف مسجل مسبقاً لمستفيد آخر."
    ) {
      val id = beneficiaryDao.insert(beneficiary.toEntity())
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = "CREATE_BENEFICIARY",
          entityName = "beneficiaries",
          entityId = id,
          details = "تسجيل مستفيد جديد: ${beneficiary.fullName} (${beneficiary.fileNumber})",
        )
      )
      id
    }

  override suspend fun updateBeneficiaryStatus(
    beneficiaryId: Long,
    status: BeneficiaryStatus,
    actorStaffId: Long?,
  ): AppResult<Unit> =
    safeDbCall {
      beneficiaryDao.updateStatus(beneficiaryId, status, System.currentTimeMillis())
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = "UPDATE_BENEFICIARY_STATUS",
          entityName = "beneficiaries",
          entityId = beneficiaryId,
          details = "تحديث حالة المستفيد #$beneficiaryId إلى ${status.labelAr}",
        )
      )
    }

  override suspend fun deleteBeneficiary(
    beneficiaryId: Long,
    actorStaffId: Long?,
  ): AppResult<Unit> =
    safeDbCall(
      constraintMessage =
        "لا يمكن حذف ملف مستفيد مرتبط بمساعدات مالية أو عينية مصروفة حفاظاً على سجل التدقيق."
    ) {
      beneficiaryDao.deleteById(beneficiaryId)
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = "DELETE_BENEFICIARY",
          entityName = "beneficiaries",
          entityId = beneficiaryId,
          details = "حذف ملف المستفيد #$beneficiaryId",
        )
      )
    }

  override suspend fun createSocialCase(
    socialCase: SocialCase,
    actorStaffId: Long?,
  ): AppResult<Long> =
    safeDbCall {
      val id = socialCaseDao.insert(socialCase.toEntity())
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = "CREATE_SOCIAL_CASE",
          entityName = "social_cases",
          entityId = id,
          details = "فتح حالة (${socialCase.caseType.labelAr}) للمستفيد #${socialCase.beneficiaryId}",
        )
      )
      id
    }

  override suspend fun updateSocialCaseStatus(
    caseId: Long,
    status: CaseStatus,
    actorStaffId: Long?,
  ): AppResult<Unit> =
    safeDbCall {
      val closedAt =
        if (status == CaseStatus.CLOSED || status == CaseStatus.REJECTED) {
          System.currentTimeMillis()
        } else {
          null
        }
      socialCaseDao.updateStatus(caseId, status, closedAt)
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = "UPDATE_CASE_STATUS",
          entityName = "social_cases",
          entityId = caseId,
          details = "تحديث الحالة الاجتماعية #$caseId إلى ${status.labelAr}",
        )
      )
    }
}
