package com.example.core.di

import android.content.Context
import com.example.core.security.SessionManager
import com.example.data.local.database.HebrDatabase
import com.example.data.repository.BeneficiaryRepositoryImpl
import com.example.data.repository.EducationRepositoryImpl
import com.example.data.repository.EmpowermentRepositoryImpl
import com.example.data.repository.FinanceAidRepositoryImpl
import com.example.data.repository.StaffRepositoryImpl
import com.example.data.repository.VolunteerAndReportsRepositoryImpl
import com.example.domain.repository.BeneficiaryRepository
import com.example.domain.repository.EducationRepository
import com.example.domain.repository.EmpowermentRepository
import com.example.domain.repository.FinanceAidRepository
import com.example.domain.repository.StaffRepository
import com.example.domain.repository.VolunteerAndReportsRepository
import com.example.domain.usecase.AuthenticateStaffUseCase
import com.example.domain.usecase.CreateCourseUseCase
import com.example.domain.usecase.CreateEmpowermentProgramUseCase
import com.example.domain.usecase.CreateSocialCaseUseCase
import com.example.domain.usecase.CreateStaffMemberUseCase
import com.example.domain.usecase.CreateVolunteerTaskUseCase
import com.example.domain.usecase.DistributeAidUseCase
import com.example.domain.usecase.EnrollBeneficiaryUseCase
import com.example.domain.usecase.RecordDonationUseCase
import com.example.domain.usecase.RegisterBeneficiaryUseCase

class AppContainer(context: Context) {
  private val database: HebrDatabase = HebrDatabase.getInstance(context)

  val sessionManager: SessionManager = SessionManager()

  val staffRepository: StaffRepository =
    StaffRepositoryImpl(database.staffDao(), database.auditLogDao())

  val beneficiaryRepository: BeneficiaryRepository =
    BeneficiaryRepositoryImpl(
      database.beneficiaryDao(),
      database.socialCaseDao(),
      database.auditLogDao(),
    )

  val educationRepository: EducationRepository =
    EducationRepositoryImpl(
      database.courseDao(),
      database.enrollmentDao(),
      database.attendanceDao(),
      database.auditLogDao(),
    )

  val empowermentRepository: EmpowermentRepository =
    EmpowermentRepositoryImpl(database.empowermentDao(), database.auditLogDao())

  val financeAidRepository: FinanceAidRepository =
    FinanceAidRepositoryImpl(
      database.donationDao(),
      database.aidDistributionDao(),
      database.auditLogDao(),
    )

  val volunteerAndReportsRepository: VolunteerAndReportsRepository =
    VolunteerAndReportsRepositoryImpl(
      database.volunteerTaskDao(),
      database.reportsDao(),
      database.auditLogDao(),
    )

  // UseCases
  val authenticateStaffUseCase = AuthenticateStaffUseCase(staffRepository, sessionManager)
  val createStaffMemberUseCase = CreateStaffMemberUseCase(staffRepository, sessionManager)
  val registerBeneficiaryUseCase = RegisterBeneficiaryUseCase(beneficiaryRepository, sessionManager)
  val createSocialCaseUseCase = CreateSocialCaseUseCase(beneficiaryRepository, sessionManager)
  val createCourseUseCase = CreateCourseUseCase(educationRepository, sessionManager)
  val enrollBeneficiaryUseCase = EnrollBeneficiaryUseCase(educationRepository, sessionManager)
  val createEmpowermentProgramUseCase =
    CreateEmpowermentProgramUseCase(empowermentRepository, sessionManager)
  val recordDonationUseCase = RecordDonationUseCase(financeAidRepository, sessionManager)
  val distributeAidUseCase = DistributeAidUseCase(financeAidRepository, sessionManager)
  val createVolunteerTaskUseCase =
    CreateVolunteerTaskUseCase(volunteerAndReportsRepository, sessionManager)
}
