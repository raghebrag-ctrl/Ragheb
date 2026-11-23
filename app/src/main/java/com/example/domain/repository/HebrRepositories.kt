package com.example.domain.repository

import com.example.core.result.AppResult
import com.example.domain.model.AidDistribution
import com.example.domain.model.AssociationReportSummary
import com.example.domain.model.AttendanceRecord
import com.example.domain.model.AuditLog
import com.example.domain.model.Beneficiary
import com.example.domain.model.BeneficiaryStatus
import com.example.domain.model.CaseStatus
import com.example.domain.model.Course
import com.example.domain.model.CourseEnrollment
import com.example.domain.model.CourseStatus
import com.example.domain.model.Donation
import com.example.domain.model.EmpowermentProgram
import com.example.domain.model.EmpowermentStage
import com.example.domain.model.EnrollmentStatus
import com.example.domain.model.SocialCase
import com.example.domain.model.StaffMember
import com.example.domain.model.TaskStatus
import com.example.domain.model.VolunteerTask
import kotlinx.coroutines.flow.Flow

interface StaffRepository {
  fun observeAllStaff(): Flow<List<StaffMember>>

  fun observeAuditLogs(): Flow<List<AuditLog>>

  suspend fun createStaffMember(staff: StaffMember, rawPin: String): AppResult<Long>

  suspend fun authenticateWithPin(staffId: Long, rawPin: String): AppResult<StaffMember>

  suspend fun toggleStaffActive(staffId: Long, isActive: Boolean): AppResult<Unit>

  suspend fun logAction(
    actorStaffId: Long?,
    action: String,
    entityName: String,
    entityId: Long,
    details: String,
  )
}

interface BeneficiaryRepository {
  fun observeBeneficiaries(query: String = ""): Flow<List<Beneficiary>>

  fun observeBeneficiaryById(id: Long): Flow<Beneficiary?>

  fun observeSocialCases(): Flow<List<SocialCase>>

  fun observeCasesByBeneficiary(beneficiaryId: Long): Flow<List<SocialCase>>

  suspend fun addBeneficiary(beneficiary: Beneficiary, actorStaffId: Long?): AppResult<Long>

  suspend fun updateBeneficiaryStatus(
    beneficiaryId: Long,
    status: BeneficiaryStatus,
    actorStaffId: Long?,
  ): AppResult<Unit>

  suspend fun deleteBeneficiary(beneficiaryId: Long, actorStaffId: Long?): AppResult<Unit>

  suspend fun createSocialCase(socialCase: SocialCase, actorStaffId: Long?): AppResult<Long>

  suspend fun updateSocialCaseStatus(
    caseId: Long,
    status: CaseStatus,
    actorStaffId: Long?,
  ): AppResult<Unit>
}

interface EducationRepository {
  fun observeCourses(): Flow<List<Course>>

  fun observeEnrollmentsForCourse(courseId: Long): Flow<List<CourseEnrollment>>

  fun observeAttendanceForEnrollment(enrollmentId: Long): Flow<List<AttendanceRecord>>

  suspend fun createCourse(course: Course, actorStaffId: Long?): AppResult<Long>

  suspend fun updateCourseStatus(
    courseId: Long,
    status: CourseStatus,
    actorStaffId: Long?,
  ): AppResult<Unit>

  suspend fun enrollBeneficiaryAtomic(
    courseId: Long,
    beneficiaryId: Long,
    actorStaffId: Long?,
  ): AppResult<Long>

  suspend fun markAttendance(
    enrollmentId: Long,
    sessionDate: Long,
    isPresent: Boolean,
    note: String?,
  ): AppResult<Unit>

  suspend fun updateEnrollmentStatus(
    enrollmentId: Long,
    status: EnrollmentStatus,
  ): AppResult<Unit>
}

interface EmpowermentRepository {
  fun observePrograms(): Flow<List<EmpowermentProgram>>

  suspend fun createProgram(program: EmpowermentProgram, actorStaffId: Long?): AppResult<Long>

  suspend fun updateStageAndProgress(
    programId: Long,
    stage: EmpowermentStage,
    progressPercent: Int,
    actorStaffId: Long?,
  ): AppResult<Unit>
}

interface FinanceAidRepository {
  fun observeDonations(): Flow<List<Donation>>

  fun observeAidDistributions(): Flow<List<AidDistribution>>

  fun observeAidForBeneficiary(beneficiaryId: Long): Flow<List<AidDistribution>>

  suspend fun recordDonation(donation: Donation, actorStaffId: Long?): AppResult<Long>

  suspend fun distributeAidAtomic(
    distribution: AidDistribution,
    actorStaffId: Long?,
  ): AppResult<Long>
}

interface VolunteerAndReportsRepository {
  fun observeTasks(): Flow<List<VolunteerTask>>

  fun observeReportSummary(): Flow<AssociationReportSummary>

  suspend fun createTask(task: VolunteerTask, actorStaffId: Long?): AppResult<Long>

  suspend fun updateTaskStatus(
    taskId: Long,
    status: TaskStatus,
    hoursLogged: Double,
    actorStaffId: Long?,
  ): AppResult<Unit>
}
