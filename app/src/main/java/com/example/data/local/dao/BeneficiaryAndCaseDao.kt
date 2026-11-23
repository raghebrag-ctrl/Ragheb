package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BeneficiaryEntity
import com.example.data.local.entity.SocialCaseEntity
import com.example.data.local.entity.SocialCaseRow
import com.example.domain.model.BeneficiaryStatus
import com.example.domain.model.CaseStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BeneficiaryDao {
  @Query(
    """
    SELECT * FROM beneficiaries
    WHERE (:query = '' OR fullName LIKE '%' || :query || '%'
           OR nationalId LIKE '%' || :query || '%'
           OR fileNumber LIKE '%' || :query || '%'
           OR phone LIKE '%' || :query || '%')
    ORDER BY createdAt DESC
    """
  )
  fun observeBeneficiaries(query: String): Flow<List<BeneficiaryEntity>>

  @Query("SELECT * FROM beneficiaries WHERE id = :id LIMIT 1")
  fun observeById(id: Long): Flow<BeneficiaryEntity?>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(entity: BeneficiaryEntity): Long

  @Query("UPDATE beneficiaries SET status = :status, updatedAt = :updatedAt WHERE id = :id")
  suspend fun updateStatus(id: Long, status: BeneficiaryStatus, updatedAt: Long)

  @Query("DELETE FROM beneficiaries WHERE id = :id")
  suspend fun deleteById(id: Long)
}

@Dao
interface SocialCaseDao {
  @Query(
    """
    SELECT c.id, c.beneficiaryId, b.fullName AS beneficiaryName,
           c.assignedStaffId, s.fullName AS assignedStaffName,
           c.caseType, c.priority, c.assessmentSummary, c.recommendedAction,
           c.status, c.openedAt, c.closedAt
    FROM social_cases c
    INNER JOIN beneficiaries b ON c.beneficiaryId = b.id
    LEFT JOIN staff_members s ON c.assignedStaffId = s.id
    ORDER BY c.openedAt DESC
    """
  )
  fun observeAllCases(): Flow<List<SocialCaseRow>>

  @Query(
    """
    SELECT c.id, c.beneficiaryId, b.fullName AS beneficiaryName,
           c.assignedStaffId, s.fullName AS assignedStaffName,
           c.caseType, c.priority, c.assessmentSummary, c.recommendedAction,
           c.status, c.openedAt, c.closedAt
    FROM social_cases c
    INNER JOIN beneficiaries b ON c.beneficiaryId = b.id
    LEFT JOIN staff_members s ON c.assignedStaffId = s.id
    WHERE c.beneficiaryId = :beneficiaryId
    ORDER BY c.openedAt DESC
    """
  )
  fun observeCasesByBeneficiary(beneficiaryId: Long): Flow<List<SocialCaseRow>>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(entity: SocialCaseEntity): Long

  @Query("UPDATE social_cases SET status = :status, closedAt = :closedAt WHERE id = :caseId")
  suspend fun updateStatus(caseId: Long, status: CaseStatus, closedAt: Long?)
}
