package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.AidType
import com.example.domain.model.BeneficiaryStatus
import com.example.domain.model.CaseStatus
import com.example.domain.model.CaseType
import com.example.domain.model.CourseCategory
import com.example.domain.model.CourseStatus
import com.example.domain.model.DonationType
import com.example.domain.model.EmpowermentSector
import com.example.domain.model.EmpowermentStage
import com.example.domain.model.EnrollmentStatus
import com.example.domain.model.FundingType
import com.example.domain.model.Priority
import com.example.domain.model.StaffRole
import com.example.domain.model.TaskStatus
import com.example.domain.model.VulnerabilityLevel

@Entity(
  tableName = "staff_members",
  indices = [Index(value = ["phone"], unique = true)],
)
data class StaffMemberEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val fullName: String,
  val role: StaffRole,
  val phone: String,
  val email: String?,
  val specialty: String?,
  val pinHash: String,
  val pinSalt: String,
  val isActive: Boolean = true,
  val joinedAt: Long = System.currentTimeMillis(),
)

@Entity(
  tableName = "beneficiaries",
  indices = [
    Index(value = ["fileNumber"], unique = true),
    Index(value = ["nationalId"], unique = true),
    Index(value = ["fullName"]),
  ],
)
data class BeneficiaryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val fileNumber: String,
  val fullName: String,
  val nationalId: String,
  val phone: String,
  val address: String,
  val familySize: Int,
  val monthlyIncome: Double,
  val vulnerabilityLevel: VulnerabilityLevel,
  val status: BeneficiaryStatus,
  val notes: String?,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
  tableName = "social_cases",
  foreignKeys = [
    ForeignKey(
      entity = BeneficiaryEntity::class,
      parentColumns = ["id"],
      childColumns = ["beneficiaryId"],
      onDelete = ForeignKey.CASCADE,
    ),
    ForeignKey(
      entity = StaffMemberEntity::class,
      parentColumns = ["id"],
      childColumns = ["assignedStaffId"],
      onDelete = ForeignKey.SET_NULL,
    ),
  ],
  indices = [Index("beneficiaryId"), Index("assignedStaffId")],
)
data class SocialCaseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val beneficiaryId: Long,
  val assignedStaffId: Long?,
  val caseType: CaseType,
  val priority: Priority,
  val assessmentSummary: String,
  val recommendedAction: String?,
  val status: CaseStatus,
  val openedAt: Long = System.currentTimeMillis(),
  val closedAt: Long? = null,
)

@Entity(
  tableName = "courses",
  foreignKeys = [
    ForeignKey(
      entity = StaffMemberEntity::class,
      parentColumns = ["id"],
      childColumns = ["instructorId"],
      onDelete = ForeignKey.SET_NULL,
    )
  ],
  indices = [Index("instructorId"), Index("title")],
)
data class CourseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val category: CourseCategory,
  val instructorId: Long?,
  val startDate: Long,
  val endDate: Long,
  val maxCapacity: Int,
  val status: CourseStatus,
)

@Entity(
  tableName = "course_enrollments",
  foreignKeys = [
    ForeignKey(
      entity = CourseEntity::class,
      parentColumns = ["id"],
      childColumns = ["courseId"],
      onDelete = ForeignKey.CASCADE,
    ),
    ForeignKey(
      entity = BeneficiaryEntity::class,
      parentColumns = ["id"],
      childColumns = ["beneficiaryId"],
      onDelete = ForeignKey.CASCADE,
    ),
  ],
  indices = [
    Index("courseId"),
    Index("beneficiaryId"),
    Index(value = ["courseId", "beneficiaryId"], unique = true),
  ],
)
data class CourseEnrollmentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val courseId: Long,
  val beneficiaryId: Long,
  val enrolledAt: Long = System.currentTimeMillis(),
  val completionStatus: EnrollmentStatus = EnrollmentStatus.ENROLLED,
  val finalGrade: Double? = null,
)

@Entity(
  tableName = "attendance_records",
  foreignKeys = [
    ForeignKey(
      entity = CourseEnrollmentEntity::class,
      parentColumns = ["id"],
      childColumns = ["enrollmentId"],
      onDelete = ForeignKey.CASCADE,
    )
  ],
  indices = [
    Index("enrollmentId"),
    Index("sessionDate"),
    Index(value = ["enrollmentId", "sessionDate"], unique = true),
  ],
)
data class AttendanceRecordEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val enrollmentId: Long,
  val sessionDate: Long,
  val isPresent: Boolean,
  val note: String?,
)

@Entity(
  tableName = "empowerment_programs",
  foreignKeys = [
    ForeignKey(
      entity = BeneficiaryEntity::class,
      parentColumns = ["id"],
      childColumns = ["beneficiaryId"],
      onDelete = ForeignKey.CASCADE,
    ),
    ForeignKey(
      entity = StaffMemberEntity::class,
      parentColumns = ["id"],
      childColumns = ["supervisorId"],
      onDelete = ForeignKey.SET_NULL,
    ),
  ],
  indices = [Index("beneficiaryId"), Index("supervisorId")],
)
data class EmpowermentProgramEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val beneficiaryId: Long,
  val supervisorId: Long?,
  val projectTitle: String,
  val sector: EmpowermentSector,
  val grantAmount: Double,
  val fundingType: FundingType,
  val stage: EmpowermentStage,
  val progressPercent: Int,
  val startedAt: Long = System.currentTimeMillis(),
)

@Entity(
  tableName = "donations",
  foreignKeys = [
    ForeignKey(
      entity = StaffMemberEntity::class,
      parentColumns = ["id"],
      childColumns = ["receivedById"],
      onDelete = ForeignKey.SET_NULL,
    )
  ],
  indices = [Index(value = ["receiptNumber"], unique = true), Index("receivedById")],
)
data class DonationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val donorName: String,
  val donorPhone: String?,
  val donationType: DonationType,
  val amount: Double,
  val inKindDescription: String?,
  val purpose: String?,
  val receiptNumber: String,
  val receivedById: Long?,
  val receivedAt: Long = System.currentTimeMillis(),
)

@Entity(
  tableName = "aid_distributions",
  foreignKeys = [
    ForeignKey(
      entity = BeneficiaryEntity::class,
      parentColumns = ["id"],
      childColumns = ["beneficiaryId"],
      onDelete = ForeignKey.RESTRICT,
    ),
    ForeignKey(
      entity = SocialCaseEntity::class,
      parentColumns = ["id"],
      childColumns = ["caseId"],
      onDelete = ForeignKey.SET_NULL,
    ),
    ForeignKey(
      entity = StaffMemberEntity::class,
      parentColumns = ["id"],
      childColumns = ["approvedById"],
      onDelete = ForeignKey.SET_NULL,
    ),
  ],
  indices = [Index("beneficiaryId"), Index("caseId"), Index("approvedById")],
)
data class AidDistributionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val beneficiaryId: Long,
  val caseId: Long?,
  val approvedById: Long?,
  val aidType: AidType,
  val amount: Double,
  val itemDetails: String?,
  val distributedAt: Long = System.currentTimeMillis(),
)

@Entity(
  tableName = "volunteer_tasks",
  foreignKeys = [
    ForeignKey(
      entity = StaffMemberEntity::class,
      parentColumns = ["id"],
      childColumns = ["assigneeId"],
      onDelete = ForeignKey.CASCADE,
    )
  ],
  indices = [Index("assigneeId")],
)
data class VolunteerTaskEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val assigneeId: Long,
  val title: String,
  val description: String,
  val dueDate: Long,
  val priority: Priority,
  val status: TaskStatus,
  val hoursLogged: Double = 0.0,
)

@Entity(
  tableName = "audit_logs",
  foreignKeys = [
    ForeignKey(
      entity = StaffMemberEntity::class,
      parentColumns = ["id"],
      childColumns = ["actorStaffId"],
      onDelete = ForeignKey.SET_NULL,
    )
  ],
  indices = [Index("actorStaffId"), Index("timestamp")],
)
data class AuditLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val actorStaffId: Long?,
  val action: String,
  val entityName: String,
  val entityId: Long,
  val details: String,
  val timestamp: Long = System.currentTimeMillis(),
)

// Joined query projections
data class SocialCaseRow(
  val id: Long,
  val beneficiaryId: Long,
  val beneficiaryName: String,
  val assignedStaffId: Long?,
  val assignedStaffName: String?,
  val caseType: CaseType,
  val priority: Priority,
  val assessmentSummary: String,
  val recommendedAction: String?,
  val status: CaseStatus,
  val openedAt: Long,
  val closedAt: Long?,
)

data class CourseWithCountRow(
  val id: Long,
  val title: String,
  val category: CourseCategory,
  val instructorId: Long?,
  val instructorName: String?,
  val startDate: Long,
  val endDate: Long,
  val maxCapacity: Int,
  val enrolledCount: Int,
  val status: CourseStatus,
)

data class EnrollmentRow(
  val id: Long,
  val courseId: Long,
  val beneficiaryId: Long,
  val beneficiaryName: String,
  val fileNumber: String,
  val enrolledAt: Long,
  val completionStatus: EnrollmentStatus,
  val finalGrade: Double?,
  val presentSessions: Int,
  val totalSessions: Int,
)

data class EmpowermentProgramRow(
  val id: Long,
  val beneficiaryId: Long,
  val beneficiaryName: String,
  val supervisorId: Long?,
  val supervisorName: String?,
  val projectTitle: String,
  val sector: EmpowermentSector,
  val grantAmount: Double,
  val fundingType: FundingType,
  val stage: EmpowermentStage,
  val progressPercent: Int,
  val startedAt: Long,
)

data class DonationRow(
  val id: Long,
  val donorName: String,
  val donorPhone: String?,
  val donationType: DonationType,
  val amount: Double,
  val inKindDescription: String?,
  val purpose: String?,
  val receiptNumber: String,
  val receivedById: Long?,
  val receivedByName: String?,
  val receivedAt: Long,
)

data class AidDistributionRow(
  val id: Long,
  val beneficiaryId: Long,
  val beneficiaryName: String,
  val caseId: Long?,
  val approvedById: Long?,
  val approvedByName: String?,
  val aidType: AidType,
  val amount: Double,
  val itemDetails: String?,
  val distributedAt: Long,
)

data class VolunteerTaskRow(
  val id: Long,
  val assigneeId: Long,
  val assigneeName: String,
  val title: String,
  val description: String,
  val dueDate: Long,
  val priority: Priority,
  val status: TaskStatus,
  val hoursLogged: Double,
)

data class AuditLogRow(
  val id: Long,
  val actorStaffId: Long?,
  val actorName: String?,
  val action: String,
  val entityName: String,
  val entityId: Long,
  val details: String,
  val timestamp: Long,
)
