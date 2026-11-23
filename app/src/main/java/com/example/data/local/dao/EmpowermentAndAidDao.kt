package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.AidDistributionEntity
import com.example.data.local.entity.AidDistributionRow
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.DonationEntity
import com.example.data.local.entity.DonationRow
import com.example.data.local.entity.EmpowermentProgramEntity
import com.example.data.local.entity.EmpowermentProgramRow
import com.example.data.local.entity.VolunteerTaskEntity
import com.example.data.local.entity.VolunteerTaskRow
import com.example.domain.model.AssociationReportSummary
import com.example.domain.model.CaseStatus
import com.example.domain.model.EmpowermentStage
import com.example.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpowermentDao {
  @Query(
    """
    SELECT p.id, p.beneficiaryId, b.fullName AS beneficiaryName,
           p.supervisorId, s.fullName AS supervisorName,
           p.projectTitle, p.sector, p.grantAmount, p.fundingType,
           p.stage, p.progressPercent, p.startedAt
    FROM empowerment_programs p
    INNER JOIN beneficiaries b ON p.beneficiaryId = b.id
    LEFT JOIN staff_members s ON p.supervisorId = s.id
    ORDER BY p.startedAt DESC
    """
  )
  fun observeAll(): Flow<List<EmpowermentProgramRow>>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(entity: EmpowermentProgramEntity): Long

  @Query(
    "UPDATE empowerment_programs SET stage = :stage, progressPercent = :progressPercent WHERE id = :id"
  )
  suspend fun updateStageAndProgress(id: Long, stage: EmpowermentStage, progressPercent: Int)
}

@Dao
interface DonationDao {
  @Query(
    """
    SELECT d.id, d.donorName, d.donorPhone, d.donationType, d.amount,
           d.inKindDescription, d.purpose, d.receiptNumber,
           d.receivedById, s.fullName AS receivedByName, d.receivedAt
    FROM donations d
    LEFT JOIN staff_members s ON d.receivedById = s.id
    ORDER BY d.receivedAt DESC
    """
  )
  fun observeAll(): Flow<List<DonationRow>>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(entity: DonationEntity): Long
}

@Dao
interface AidDistributionDao {
  @Query(
    """
    SELECT a.id, a.beneficiaryId, b.fullName AS beneficiaryName, a.caseId,
           a.approvedById, s.fullName AS approvedByName, a.aidType, a.amount,
           a.itemDetails, a.distributedAt
    FROM aid_distributions a
    INNER JOIN beneficiaries b ON a.beneficiaryId = b.id
    LEFT JOIN staff_members s ON a.approvedById = s.id
    ORDER BY a.distributedAt DESC
    """
  )
  fun observeAll(): Flow<List<AidDistributionRow>>

  @Query(
    """
    SELECT a.id, a.beneficiaryId, b.fullName AS beneficiaryName, a.caseId,
           a.approvedById, s.fullName AS approvedByName, a.aidType, a.amount,
           a.itemDetails, a.distributedAt
    FROM aid_distributions a
    INNER JOIN beneficiaries b ON a.beneficiaryId = b.id
    LEFT JOIN staff_members s ON a.approvedById = s.id
    WHERE a.beneficiaryId = :beneficiaryId
    ORDER BY a.distributedAt DESC
    """
  )
  fun observeForBeneficiary(beneficiaryId: Long): Flow<List<AidDistributionRow>>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(entity: AidDistributionEntity): Long

  @Query("UPDATE social_cases SET status = :status, closedAt = :closedAt WHERE id = :caseId")
  suspend fun updateCaseStatus(caseId: Long, status: CaseStatus, closedAt: Long?)

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertAuditLog(log: AuditLogEntity): Long

  @Transaction
  suspend fun distributeAidWithAuditAndCaseUpdate(
    entity: AidDistributionEntity,
    actorStaffId: Long?,
  ): Long {
    val aidId = insert(entity)
    entity.caseId?.let { caseId ->
      updateCaseStatus(caseId, CaseStatus.APPROVED, null)
    }
    insertAuditLog(
      AuditLogEntity(
        actorStaffId = actorStaffId,
        action = "DISTRIBUTE_AID",
        entityName = "aid_distributions",
        entityId = aidId,
        details =
          "صرف مساعدة (${entity.aidType.labelAr}) بقيمة ${entity.amount} للمستفيد #${entity.beneficiaryId}",
      )
    )
    return aidId
  }
}

@Dao
interface VolunteerTaskDao {
  @Query(
    """
    SELECT t.id, t.assigneeId, s.fullName AS assigneeName, t.title, t.description,
           t.dueDate, t.priority, t.status, t.hoursLogged
    FROM volunteer_tasks t
    INNER JOIN staff_members s ON t.assigneeId = s.id
    ORDER BY t.dueDate ASC
    """
  )
  fun observeAll(): Flow<List<VolunteerTaskRow>>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(entity: VolunteerTaskEntity): Long

  @Query("UPDATE volunteer_tasks SET status = :status, hoursLogged = :hoursLogged WHERE id = :taskId")
  suspend fun updateStatus(taskId: Long, status: TaskStatus, hoursLogged: Double)
}

@Dao
interface ReportsDao {
  @Query(
    """
    SELECT
      (SELECT COUNT(*) FROM beneficiaries) AS totalBeneficiaries,
      (SELECT COUNT(*) FROM social_cases WHERE status IN ('OPEN', 'IN_PROGRESS')) AS activeCasesCount,
      (SELECT COUNT(*) FROM social_cases WHERE priority IN ('URGENT', 'EMERGENCY') AND status IN ('OPEN', 'IN_PROGRESS')) AS urgentCasesCount,
      (SELECT COUNT(*) FROM courses WHERE status IN ('UPCOMING', 'ONGOING')) AS activeCoursesCount,
      (SELECT COUNT(*) FROM course_enrollments) AS totalEnrollments,
      (SELECT COUNT(*) FROM empowerment_programs) AS empowermentProjectsCount,
      (SELECT COUNT(*) FROM empowerment_programs WHERE stage = 'SELF_RELIANT') AS selfReliantProjectsCount,
      (SELECT COALESCE(SUM(amount), 0.0) FROM donations) AS totalCashDonations,
      (SELECT COALESCE(SUM(amount), 0.0) FROM aid_distributions) AS totalCashAidDistributed,
      (SELECT COUNT(*) FROM aid_distributions) AS totalAidRecordsCount,
      (SELECT COALESCE(SUM(hoursLogged), 0.0) FROM volunteer_tasks) AS totalVolunteerHours,
      (SELECT COUNT(*) FROM volunteer_tasks WHERE status = 'DONE') AS completedTasksCount,
      (SELECT COUNT(*) FROM volunteer_tasks WHERE status != 'DONE') AS openTasksCount
    """
  )
  fun observeSummary(): Flow<AssociationReportSummary>
}
