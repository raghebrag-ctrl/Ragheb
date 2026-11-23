package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.AttendanceRecordEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CourseEnrollmentEntity
import com.example.data.local.entity.CourseEntity
import com.example.data.local.entity.CourseWithCountRow
import com.example.data.local.entity.EnrollmentRow
import com.example.domain.model.CourseStatus
import com.example.domain.model.EnrollmentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
  @Query(
    """
    SELECT c.id, c.title, c.category, c.instructorId, s.fullName AS instructorName,
           c.startDate, c.endDate, c.maxCapacity,
           (SELECT COUNT(*) FROM course_enrollments e WHERE e.courseId = c.id) AS enrolledCount,
           c.status
    FROM courses c
    LEFT JOIN staff_members s ON c.instructorId = s.id
    ORDER BY c.startDate DESC
    """
  )
  fun observeCourses(): Flow<List<CourseWithCountRow>>

  @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
  suspend fun getById(courseId: Long): CourseEntity?

  @Query("SELECT COUNT(*) FROM course_enrollments WHERE courseId = :courseId")
  suspend fun getEnrollmentCount(courseId: Long): Int

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(course: CourseEntity): Long

  @Query("UPDATE courses SET status = :status WHERE id = :courseId")
  suspend fun updateStatus(courseId: Long, status: CourseStatus)

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertEnrollment(enrollment: CourseEnrollmentEntity): Long

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertAuditLog(log: AuditLogEntity): Long

  @Transaction
  suspend fun enrollBeneficiaryCheckingCapacity(
    courseId: Long,
    beneficiaryId: Long,
    actorStaffId: Long?,
  ): Long {
    val course = getById(courseId) ?: throw IllegalStateException("الدورة غير موجودة.")
    if (course.status == CourseStatus.CANCELLED || course.status == CourseStatus.COMPLETED) {
      throw IllegalStateException("لا يمكن التسجيل في دورة مكتملة أو ملغاة.")
    }
    val currentCount = getEnrollmentCount(courseId)
    if (currentCount >= course.maxCapacity) {
      throw IllegalStateException("عذراً، اكتملت السعة القصوى للدورة (${course.maxCapacity} مقاعد).")
    }
    val enrollmentId =
      insertEnrollment(CourseEnrollmentEntity(courseId = courseId, beneficiaryId = beneficiaryId))
    insertAuditLog(
      AuditLogEntity(
        actorStaffId = actorStaffId,
        action = "ENROLL_COURSE",
        entityName = "course_enrollments",
        entityId = enrollmentId,
        details = "تسجيل مستفيد (#$beneficiaryId) في دورة (${course.title})",
      )
    )
    return enrollmentId
  }
}

@Dao
interface EnrollmentDao {
  @Query(
    """
    SELECT e.id, e.courseId, e.beneficiaryId, b.fullName AS beneficiaryName, b.fileNumber,
           e.enrolledAt, e.completionStatus, e.finalGrade,
           (SELECT COUNT(*) FROM attendance_records a WHERE a.enrollmentId = e.id AND a.isPresent = 1) AS presentSessions,
           (SELECT COUNT(*) FROM attendance_records a WHERE a.enrollmentId = e.id) AS totalSessions
    FROM course_enrollments e
    INNER JOIN beneficiaries b ON e.beneficiaryId = b.id
    WHERE e.courseId = :courseId
    ORDER BY e.enrolledAt DESC
    """
  )
  fun observeEnrollmentsForCourse(courseId: Long): Flow<List<EnrollmentRow>>

  @Query("UPDATE course_enrollments SET completionStatus = :status WHERE id = :enrollmentId")
  suspend fun updateStatus(enrollmentId: Long, status: EnrollmentStatus)
}

@Dao
interface AttendanceDao {
  @Query("SELECT * FROM attendance_records WHERE enrollmentId = :enrollmentId ORDER BY sessionDate DESC")
  fun observeForEnrollment(enrollmentId: Long): Flow<List<AttendanceRecordEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(record: AttendanceRecordEntity): Long
}
