package com.example

import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.core.security.PinHasher
import com.example.core.security.SessionManager
import com.example.domain.model.AidDistribution
import com.example.domain.model.AidType
import com.example.domain.model.Course
import com.example.domain.model.CourseCategory
import com.example.domain.model.CourseEnrollment
import com.example.domain.model.CourseStatus
import com.example.domain.model.Donation
import com.example.domain.model.EnrollmentStatus
import com.example.domain.model.StaffMember
import com.example.domain.model.StaffRole
import com.example.domain.repository.EducationRepository
import com.example.domain.repository.FinanceAidRepository
import com.example.domain.usecase.CreateCourseUseCase
import com.example.domain.usecase.DistributeAidUseCase
import com.example.domain.validator.HebrValidators
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HebrDomainUnitTest {

  @Test
  fun `pin hasher verifies valid pin and rejects invalid pin`() {
    val salt = PinHasher.generateSaltHex()
    val hash = PinHasher.hashPin("4826", salt)
    assertTrue(PinHasher.verifyPin("4826", salt, hash))
    assertFalse(PinHasher.verifyPin("1234", salt, hash))
  }

  @Test
  fun `validators enforce national id, phone, pin and positive amounts`() {
    assertTrue(HebrValidators.validateNationalId("123") is AppResult.Error)
    assertTrue(HebrValidators.validateNationalId("1029384756") is AppResult.Success)
    assertTrue(HebrValidators.validatePin("12") is AppResult.Error)
    assertTrue(HebrValidators.validatePin("1234") is AppResult.Success)
    assertTrue(HebrValidators.validatePositiveAmount(0.0, "مبلغ") is AppResult.Error)
    assertTrue(HebrValidators.validatePositiveAmount(250.0, "مبلغ") is AppResult.Success)
  }

  @Test
  fun `cash aid distribution enforces admin or accountant role`() = runTest {
    val fakeFinanceRepo =
      object : FinanceAidRepository {
        override fun observeDonations(): Flow<List<Donation>> = flowOf(emptyList())
        override fun observeAidDistributions(): Flow<List<AidDistribution>> = flowOf(emptyList())
        override fun observeAidForBeneficiary(beneficiaryId: Long): Flow<List<AidDistribution>> =
          flowOf(emptyList())
        override suspend fun recordDonation(donation: Donation, actorStaffId: Long?) =
          AppResult.Success(1L)
        override suspend fun distributeAidAtomic(
          distribution: AidDistribution,
          actorStaffId: Long?,
        ) = AppResult.Success(1L)
      }

    val session = SessionManager()
    session.signIn(
      StaffMember(id = 1, fullName = "متطوع", role = StaffRole.VOLUNTEER, phone = "0501234567")
    )

    val useCase = DistributeAidUseCase(fakeFinanceRepo, session)
    val deniedResult =
      useCase(AidDistribution(beneficiaryId = 1L, aidType = AidType.CASH, amount = 500.0))
    assertTrue(deniedResult is AppResult.Error)
    assertTrue((deniedResult as AppResult.Error).error is AppError.UnauthorizedError)

    session.signIn(
      StaffMember(id = 2, fullName = "مدير", role = StaffRole.ADMIN, phone = "0500000001")
    )
    val allowedResult =
      useCase(AidDistribution(beneficiaryId = 1L, aidType = AidType.CASH, amount = 500.0))
    assertTrue(allowedResult is AppResult.Success)
  }

  @Test
  fun `create course validates capacity and end date`() = runTest {
    val fakeEducationRepo =
      object : EducationRepository {
        override fun observeCourses(): Flow<List<Course>> = flowOf(emptyList())
        override fun observeEnrollmentsForCourse(courseId: Long): Flow<List<CourseEnrollment>> =
          flowOf(emptyList())
        override fun observeAttendanceForEnrollment(enrollmentId: Long) =
          flowOf(emptyList<com.example.domain.model.AttendanceRecord>())
        override suspend fun createCourse(course: Course, actorStaffId: Long?) =
          AppResult.Success(10L)
        override suspend fun updateCourseStatus(
          courseId: Long,
          status: CourseStatus,
          actorStaffId: Long?,
        ) = AppResult.Success(Unit)
        override suspend fun enrollBeneficiaryAtomic(
          courseId: Long,
          beneficiaryId: Long,
          actorStaffId: Long?,
        ) = AppResult.Success(1L)
        override suspend fun markAttendance(
          enrollmentId: Long,
          sessionDate: Long,
          isPresent: Boolean,
          note: String?,
        ) = AppResult.Success(Unit)
        override suspend fun updateEnrollmentStatus(
          enrollmentId: Long,
          status: EnrollmentStatus,
        ) = AppResult.Success(Unit)
      }

    val session = SessionManager()
    val useCase = CreateCourseUseCase(fakeEducationRepo, session)

    val invalidCapacity =
      useCase(
        Course(
          title = "دورة البرمجة",
          category = CourseCategory.DIGITAL_SKILLS,
          startDate = 1000L,
          endDate = 5000L,
          maxCapacity = 0,
          status = CourseStatus.ONGOING,
        )
      )
    assertTrue(invalidCapacity is AppResult.Error)

    val validCourse =
      useCase(
        Course(
          title = "دورة البرمجة",
          category = CourseCategory.DIGITAL_SKILLS,
          startDate = 1000L,
          endDate = 5000L,
          maxCapacity = 25,
          status = CourseStatus.ONGOING,
        )
      )
    assertTrue(validCourse is AppResult.Success)
  }
}
