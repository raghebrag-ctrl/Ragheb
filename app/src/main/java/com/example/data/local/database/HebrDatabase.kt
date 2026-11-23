package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.security.PinHasher
import com.example.data.local.converter.HebrTypeConverters
import com.example.data.local.dao.AidDistributionDao
import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.BeneficiaryDao
import com.example.data.local.dao.CourseDao
import com.example.data.local.dao.DonationDao
import com.example.data.local.dao.EmpowermentDao
import com.example.data.local.dao.EnrollmentDao
import com.example.data.local.dao.ReportsDao
import com.example.data.local.dao.SocialCaseDao
import com.example.data.local.dao.StaffDao
import com.example.data.local.dao.VolunteerTaskDao
import com.example.data.local.entity.AidDistributionEntity
import com.example.data.local.entity.AttendanceRecordEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.BeneficiaryEntity
import com.example.data.local.entity.CourseEnrollmentEntity
import com.example.data.local.entity.CourseEntity
import com.example.data.local.entity.DonationEntity
import com.example.data.local.entity.EmpowermentProgramEntity
import com.example.data.local.entity.SocialCaseEntity
import com.example.data.local.entity.StaffMemberEntity
import com.example.data.local.entity.VolunteerTaskEntity
import com.example.domain.model.StaffRole

@Database(
  entities = [
    StaffMemberEntity::class,
    BeneficiaryEntity::class,
    SocialCaseEntity::class,
    CourseEntity::class,
    CourseEnrollmentEntity::class,
    AttendanceRecordEntity::class,
    EmpowermentProgramEntity::class,
    DonationEntity::class,
    AidDistributionEntity::class,
    VolunteerTaskEntity::class,
    AuditLogEntity::class,
  ],
  version = 1,
  exportSchema = false,
)
@TypeConverters(HebrTypeConverters::class)
abstract class HebrDatabase : RoomDatabase() {
  abstract fun staffDao(): StaffDao

  abstract fun auditLogDao(): AuditLogDao

  abstract fun beneficiaryDao(): BeneficiaryDao

  abstract fun socialCaseDao(): SocialCaseDao

  abstract fun courseDao(): CourseDao

  abstract fun enrollmentDao(): EnrollmentDao

  abstract fun attendanceDao(): AttendanceDao

  abstract fun empowermentDao(): EmpowermentDao

  abstract fun donationDao(): DonationDao

  abstract fun aidDistributionDao(): AidDistributionDao

  abstract fun volunteerTaskDao(): VolunteerTaskDao

  abstract fun reportsDao(): ReportsDao

  companion object {
    private const val DB_NAME = "hebr_association.db"

    @Volatile private var INSTANCE: HebrDatabase? = null

    fun getInstance(context: Context): HebrDatabase {
      return INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                HebrDatabase::class.java,
                DB_NAME,
              )
              .addCallback(
                object : Callback() {
                  override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seedInitialAdminAccount(db)
                  }
                }
              )
              .build()
              .also { INSTANCE = it }
        }
    }

    private fun seedInitialAdminAccount(db: SupportSQLiteDatabase) {
      val salt = PinHasher.generateSaltHex()
      val hash = PinHasher.hashPin("1234", salt)
      val now = System.currentTimeMillis()
      db.execSQL(
        """
        INSERT INTO staff_members (fullName, role, phone, email, specialty, pinHash, pinSalt, isActive, joinedAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent(),
        arrayOf<Any?>(
          "مدير الجمعية",
          StaffRole.ADMIN.name,
          "0500000001",
          "admin@hebr.org",
          "الإدارة العامة",
          hash,
          salt,
          1,
          now,
        ),
      )
    }
  }
}
