package com.example.data.repository

import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.core.result.safeDbCall
import com.example.data.local.dao.AidDistributionDao
import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.CourseDao
import com.example.data.local.dao.DonationDao
import com.example.data.local.dao.EmpowermentDao
import com.example.data.local.dao.EnrollmentDao
import com.example.data.local.dao.ReportsDao
import com.example.data.local.dao.VolunteerTaskDao
import com.example.data.local.entity.AttendanceRecordEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.AidDistribution
import com.example.domain.model.AssociationReportSummary
import com.example.domain.model.AttendanceRecord
import com.example.domain.model.Course
import com.example.domain.model.CourseEnrollment
import com.example.domain.model.CourseStatus
import com.example.domain.model.Donation
import com.example.domain.model.EmpowermentProgram
import com.example.domain.model.EmpowermentStage
import com.example.domain.model.EnrollmentStatus
import com.example.domain.model.TaskStatus
import com.example.domain.model.VolunteerTask
import com.example.domain.repository.EducationRepository
import com.example.domain.repository.EmpowermentRepository
import com.example.domain.repository.FinanceAidRepository
import com.example.domain.repository.VolunteerAndReportsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EducationRepositoryImpl(
  private val courseDao: CourseDao,
  private val enrollmentDao: EnrollmentDao,
  private val attendanceDao: AttendanceDao,
  private val auditLogDao: AuditLogDao,
) : EducationRepository {
  override fun observeCourses(): Flow<List<Course>> =
    courseDao.observeCourses().map { list -> list.map { it.toDomain() } }

  override fun observeEnrollmentsForCourse(courseId: Long): Flow<List<CourseEnrollment>> =
    enrollmentDao.observeEnrollmentsForCourse(courseId).map { list -> list.map { it.toDomain() } }

  override fun observeAttendanceForEnrollment(enrollmentId: Long): Flow<List<AttendanceRecord>> =
    attendanceDao.observeForEnrollment(enrollmentId).map { list -> list.map { it.toDomain() } }

  override suspend fun createCourse(course: Course, actorStaffId: Long?): AppResult<Long> =
    safeDbCall {
      val id = courseDao.insert(course.toEntity())
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = "CREATE_COURSE",
          entityName = "courses",
          entityId = id,
          details = "إنشاء دورة تدريبية: ${course.title}",
        )
      )
      id
    }

  override suspend fun updateCourseStatus(
    courseId: Long,
    status: CourseStatus,
    actorStaffId: Long?,
  ): AppResult<Unit> =
    safeDbCall {
      courseDao.updateStatus(courseId, status)
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = "UPDATE_COURSE_STATUS",
          entityName = "courses",
          entityId = courseId,
          details = "تحديث حالة الدورة #$courseId إلى ${status.labelAr}",
        )
      )
    }

  override suspend fun enrollBeneficiaryAtomic(
    courseId: Long,
    beneficiaryId: Long,
    actorStaffId: Long?,
  ): AppResult<Long> {
    val result =
      safeDbCall(duplicateMessage = "هذا المستفيد مسجل بالفعل في هذه الدورة.") {
        courseDao.enrollBeneficiaryCheckingCapacity(courseId, beneficiaryId, actorStaffId)
      }
    return if (
      result is AppResult.Error &&
        result.error is AppError.ValidationError &&
        result.error.messageAr.contains("السعة القصوى")
    ) {
      AppResult.Error(AppError.CapacityExceededError(result.error.messageAr))
    } else {
      result
    }
  }

  override suspend fun markAttendance(
    enrollmentId: Long,
    sessionDate: Long,
    isPresent: Boolean,
    note: String?,
  ): AppResult<Unit> =
    safeDbCall {
      attendanceDao.upsert(
        AttendanceRecordEntity(
          enrollmentId = enrollmentId,
          sessionDate = sessionDate,
          isPresent = isPresent,
          note = note,
        )
      )
    }

  override suspend fun updateEnrollmentStatus(
    enrollmentId: Long,
    status: EnrollmentStatus,
  ): AppResult<Unit> =
    safeDbCall {
      enrollmentDao.updateStatus(enrollmentId, status)
    }
}

class EmpowermentRepositoryImpl(
  private val empowermentDao: EmpowermentDao,
  private val auditLogDao: AuditLogDao,
) : EmpowermentRepository {
  override fun observePrograms(): Flow<List<EmpowermentProgram>> =
    empowermentDao.observeAll().map { list -> list.map { it.toDomain() } }

  override suspend fun createProgram(
    program: EmpowermentProgram,
    actorStaffId: Long?,
  ): AppResult<Long> =
    safeDbCall {
      val id = empowermentDao.insert(program.toEntity())
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = "CREATE_EMPOWERMENT",
          entityName = "empowerment_programs",
          entityId = id,
          details = "إطلاق مشروع تمكين: ${program.projectTitle}",
        )
      )
      id
    }

  override suspend fun updateStageAndProgress(
    programId: Long,
    stage: EmpowermentStage,
    progressPercent: Int,
    actorStaffId: Long?,
  ): AppResult<Unit> =
    safeDbCall {
      empowermentDao.updateStageAndProgress(
        id = programId,
        stage = stage,
        progressPercent = progressPercent.coerceIn(0, 100),
      )
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = "UPDATE_EMPOWERMENT_STAGE",
          entityName = "empowerment_programs",
          entityId = programId,
          details = "تقدم مشروع التمكين #$programId إلى مرحلة ${stage.labelAr} ($progressPercent%)",
        )
      )
    }
}

class FinanceAidRepositoryImpl(
  private val donationDao: DonationDao,
  private val aidDistributionDao: AidDistributionDao,
  private val auditLogDao: AuditLogDao,
) : FinanceAidRepository {
  override fun observeDonations(): Flow<List<Donation>> =
    donationDao.observeAll().map { list -> list.map { it.toDomain() } }

  override fun observeAidDistributions(): Flow<List<AidDistribution>> =
    aidDistributionDao.observeAll().map { list -> list.map { it.toDomain() } }

  override fun observeAidForBeneficiary(beneficiaryId: Long): Flow<List<AidDistribution>> =
    aidDistributionDao.observeForBeneficiary(beneficiaryId).map { list -> list.map { it.toDomain() } }

  override suspend fun recordDonation(donation: Donation, actorStaffId: Long?): AppResult<Long> =
    safeDbCall(duplicateMessage = "رقم الإيصال مستخدم مسبقاً.") {
      val id = donationDao.insert(donation.toEntity())
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = "RECORD_DONATION",
          entityName = "donations",
          entityId = id,
          details =
            "توثيق تبرع (${donation.donationType.labelAr}) من ${donation.donorName} - إيصال ${donation.receiptNumber}",
        )
      )
      id
    }

  override suspend fun distributeAidAtomic(
    distribution: AidDistribution,
    actorStaffId: Long?,
  ): AppResult<Long> =
    safeDbCall {
      aidDistributionDao.distributeAidWithAuditAndCaseUpdate(
        entity = distribution.toEntity(),
        actorStaffId = actorStaffId,
      )
    }
}

class VolunteerAndReportsRepositoryImpl(
  private val volunteerTaskDao: VolunteerTaskDao,
  private val reportsDao: ReportsDao,
  private val auditLogDao: AuditLogDao,
) : VolunteerAndReportsRepository {
  override fun observeTasks(): Flow<List<VolunteerTask>> =
    volunteerTaskDao.observeAll().map { list -> list.map { it.toDomain() } }

  override fun observeReportSummary(): Flow<AssociationReportSummary> = reportsDao.observeSummary()

  override suspend fun createTask(task: VolunteerTask, actorStaffId: Long?): AppResult<Long> =
    safeDbCall {
      val id = volunteerTaskDao.insert(task.toEntity())
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = "CREATE_TASK",
          entityName = "volunteer_tasks",
          entityId = id,
          details = "إسناد مهمة: ${task.title}",
        )
      )
      id
    }

  override suspend fun updateTaskStatus(
    taskId: Long,
    status: TaskStatus,
    hoursLogged: Double,
    actorStaffId: Long?,
  ): AppResult<Unit> =
    safeDbCall {
      volunteerTaskDao.updateStatus(taskId, status, hoursLogged.coerceAtLeast(0.0))
      auditLogDao.insert(
        AuditLogEntity(
          actorStaffId = actorStaffId,
          action = "UPDATE_TASK_STATUS",
          entityName = "volunteer_tasks",
          entityId = taskId,
          details = "تحديث المهمة #$taskId إلى ${status.labelAr} (${hoursLogged} ساعة)",
        )
      )
    }
}
