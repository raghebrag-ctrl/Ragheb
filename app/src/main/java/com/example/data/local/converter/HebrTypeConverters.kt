package com.example.data.local.converter

import androidx.room.TypeConverter
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

class HebrTypeConverters {
  @TypeConverter fun fromStaffRole(v: StaffRole): String = v.name
  @TypeConverter fun toStaffRole(v: String): StaffRole = StaffRole.valueOf(v)

  @TypeConverter fun fromVulnerabilityLevel(v: VulnerabilityLevel): String = v.name
  @TypeConverter fun toVulnerabilityLevel(v: String): VulnerabilityLevel = VulnerabilityLevel.valueOf(v)

  @TypeConverter fun fromBeneficiaryStatus(v: BeneficiaryStatus): String = v.name
  @TypeConverter fun toBeneficiaryStatus(v: String): BeneficiaryStatus = BeneficiaryStatus.valueOf(v)

  @TypeConverter fun fromCaseType(v: CaseType): String = v.name
  @TypeConverter fun toCaseType(v: String): CaseType = CaseType.valueOf(v)

  @TypeConverter fun fromPriority(v: Priority): String = v.name
  @TypeConverter fun toPriority(v: String): Priority = Priority.valueOf(v)

  @TypeConverter fun fromCaseStatus(v: CaseStatus): String = v.name
  @TypeConverter fun toCaseStatus(v: String): CaseStatus = CaseStatus.valueOf(v)

  @TypeConverter fun fromCourseCategory(v: CourseCategory): String = v.name
  @TypeConverter fun toCourseCategory(v: String): CourseCategory = CourseCategory.valueOf(v)

  @TypeConverter fun fromCourseStatus(v: CourseStatus): String = v.name
  @TypeConverter fun toCourseStatus(v: String): CourseStatus = CourseStatus.valueOf(v)

  @TypeConverter fun fromEnrollmentStatus(v: EnrollmentStatus): String = v.name
  @TypeConverter fun toEnrollmentStatus(v: String): EnrollmentStatus = EnrollmentStatus.valueOf(v)

  @TypeConverter fun fromEmpowermentSector(v: EmpowermentSector): String = v.name
  @TypeConverter fun toEmpowermentSector(v: String): EmpowermentSector = EmpowermentSector.valueOf(v)

  @TypeConverter fun fromFundingType(v: FundingType): String = v.name
  @TypeConverter fun toFundingType(v: String): FundingType = FundingType.valueOf(v)

  @TypeConverter fun fromEmpowermentStage(v: EmpowermentStage): String = v.name
  @TypeConverter fun toEmpowermentStage(v: String): EmpowermentStage = EmpowermentStage.valueOf(v)

  @TypeConverter fun fromDonationType(v: DonationType): String = v.name
  @TypeConverter fun toDonationType(v: String): DonationType = DonationType.valueOf(v)

  @TypeConverter fun fromAidType(v: AidType): String = v.name
  @TypeConverter fun toAidType(v: String): AidType = AidType.valueOf(v)

  @TypeConverter fun fromTaskStatus(v: TaskStatus): String = v.name
  @TypeConverter fun toTaskStatus(v: String): TaskStatus = TaskStatus.valueOf(v)
}
