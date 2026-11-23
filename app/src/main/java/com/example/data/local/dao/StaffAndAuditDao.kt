package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.AuditLogRow
import com.example.data.local.entity.StaffMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
  @Query("SELECT * FROM staff_members ORDER BY isActive DESC, joinedAt ASC")
  fun observeAllStaff(): Flow<List<StaffMemberEntity>>

  @Query("SELECT * FROM staff_members WHERE id = :id LIMIT 1")
  suspend fun getById(id: Long): StaffMemberEntity?

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(staff: StaffMemberEntity): Long

  @Query("UPDATE staff_members SET isActive = :isActive WHERE id = :id")
  suspend fun setActive(id: Long, isActive: Boolean)
}

@Dao
interface AuditLogDao {
  @Query(
    """
    SELECT a.id, a.actorStaffId, s.fullName AS actorName, a.action, a.entityName,
           a.entityId, a.details, a.timestamp
    FROM audit_logs a
    LEFT JOIN staff_members s ON a.actorStaffId = s.id
    ORDER BY a.timestamp DESC
    LIMIT 150
    """
  )
  fun observeAuditLogs(): Flow<List<AuditLogRow>>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(log: AuditLogEntity): Long
}
