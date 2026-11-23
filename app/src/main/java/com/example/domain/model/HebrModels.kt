package com.example.domain.model

enum class StaffRole(val labelAr: String) {
  ADMIN("مدير الجمعية"),
  CASE_WORKER("باحث اجتماعي"),
  TRAINER("مدرب تعليمي"),
  ACCOUNTANT("أمين صندوق"),
  VOLUNTEER("متطوع ميداني"),
}

enum class VulnerabilityLevel(val labelAr: String) {
  LOW("منخفض"),
  MEDIUM("متوسط"),
  HIGH("مرتفع"),
  CRITICAL("حرج للغاية"),
}

enum class BeneficiaryStatus(val labelAr: String) {
  ACTIVE("نشط"),
  PENDING_REVIEW("قيد المراجعة"),
  GRADUATED("مكتفٍ ذاتياً"),
  SUSPENDED("موقوف"),
}

enum class CaseType(val labelAr: String) {
  FINANCIAL("دعم مالي"),
  MEDICAL("رعاية صحية"),
  HOUSING("إسكان وترميم"),
  EDUCATIONAL("دعم تعليمي"),
  ORPHAN_CARE("كفالة أيتام"),
}

enum class Priority(val labelAr: String) {
  NORMAL("عادي"),
  URGENT("عاجل"),
  EMERGENCY("طارئ"),
}

enum class CaseStatus(val labelAr: String) {
  OPEN("مفتوحة"),
  IN_PROGRESS("قيد الدراسة"),
  APPROVED("معتمدة"),
  CLOSED("مغلقة"),
  REJECTED("مرفوضة"),
}

enum class CourseCategory(val labelAr: String) {
  LITERACY("محو الأمية"),
  SCHOOL_SUPPORT("دعم دراسي"),
  DIGITAL_SKILLS("مهارات رقمية"),
  LANGUAGES("لغات"),
  VOCATIONAL("تأهيل مهني"),
}

enum class CourseStatus(val labelAr: String) {
  UPCOMING("قادمة"),
  ONGOING("جارية"),
  COMPLETED("مكتملة"),
  CANCELLED("ملغاة"),
}

enum class EnrollmentStatus(val labelAr: String) {
  ENROLLED("مسجل"),
  COMPLETED("أتم الدورة"),
  DROPPED("منسحب"),
}

enum class EmpowermentSector(val labelAr: String) {
  CRAFT("حرف يدوية"),
  TECH("خدمات تقنية"),
  RETAIL("تجارة مصغرة"),
  AGRICULTURE("زراعة وإنتاج"),
  FOOD_SERVICES("مطبخ وأسر منتجة"),
}

enum class FundingType(val labelAr: String) {
  NON_REFUNDABLE_GRANT("منحة تأسيسية"),
  REVOLVING_LOAN("قرض حسن دوّار"),
  EQUIPMENT_SUPPORT("تجهيز معدات"),
}

enum class EmpowermentStage(val labelAr: String) {
  PROPOSAL("دراسة الفكرة"),
  TRAINING("تأهيل وتدريب"),
  FUNDED("تم التمويل"),
  ACTIVE_MONITORING("متابعة التشغيل"),
  SELF_RELIANT("مشروع مستدام"),
}

enum class DonationType(val labelAr: String) {
  CASH("تبرع نقدي"),
  IN_KIND("تبرع عيني"),
  SPONSORSHIP("كفالة شهرية"),
  ZAKAT("زكاة"),
  ENDOWMENT("وقف خيري"),
}

enum class AidType(val labelAr: String) {
  CASH("مساعدة نقدية"),
  FOOD_BASKET("سلة غذائية"),
  MEDICAL_AID("علاج ودواء"),
  TUITION_SUPPORT("حقيبة ورسوم دراسية"),
  CLOTHING("كسوة"),
}

enum class TaskStatus(val labelAr: String) {
  TODO("قيد الانتظار"),
  IN_PROGRESS("قيد التنفيذ"),
  DONE("منجزة"),
}

data class StaffMember(
  val id: Long = 0,
  val fullName: String,
  val role: StaffRole,
  val phone: String,
  val email: String? = null,
  val specialty: String? = null,
  val isActive: Boolean = true,
  val joinedAt: Long = System.currentTimeMillis(),
)

data class Beneficiary(
  val id: Long = 0,
  val fileNumber: String,
  val fullName: String,
  val nationalId: String,
  val phone: String,
  val address: String,
  val familySize: Int,
  val monthlyIncome: Double,
  val vulnerabilityLevel: VulnerabilityLevel,
  val status: BeneficiaryStatus,
  val notes: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)

data class SocialCase(
  val id: Long = 0,
  val beneficiaryId: Long,
  val beneficiaryName: String = "",
  val assignedStaffId: Long? = null,
  val assignedStaffName: String? = null,
  val caseType: CaseType,
  val priority: Priority,
  val assessmentSummary: String,
  val recommendedAction: String? = null,
  val status: CaseStatus,
  val openedAt: Long = System.currentTimeMillis(),
  val closedAt: Long? = null,
)

data class Course(
  val id: Long = 0,
  val title: String,
  val category: CourseCategory,
  val instructorId: Long? = null,
  val instructorName: String? = null,
  val startDate: Long,
  val endDate: Long,
  val maxCapacity: Int,
  val enrolledCount: Int = 0,
  val status: CourseStatus,
)

data class CourseEnrollment(
  val id: Long = 0,
  val courseId: Long,
  val beneficiaryId: Long,
  val beneficiaryName: String = "",
  val fileNumber: String = "",
  val enrolledAt: Long = System.currentTimeMillis(),
  val completionStatus: EnrollmentStatus = EnrollmentStatus.ENROLLED,
  val finalGrade: Double? = null,
  val presentSessions: Int = 0,
  val totalSessions: Int = 0,
)

data class AttendanceRecord(
  val id: Long = 0,
  val enrollmentId: Long,
  val sessionDate: Long,
  val isPresent: Boolean,
  val note: String? = null,
)

data class EmpowermentProgram(
  val id: Long = 0,
  val beneficiaryId: Long,
  val beneficiaryName: String = "",
  val supervisorId: Long? = null,
  val supervisorName: String? = null,
  val projectTitle: String,
  val sector: EmpowermentSector,
  val grantAmount: Double,
  val fundingType: FundingType,
  val stage: EmpowermentStage,
  val progressPercent: Int,
  val startedAt: Long = System.currentTimeMillis(),
)

data class Donation(
  val id: Long = 0,
  val donorName: String,
  val donorPhone: String? = null,
  val donationType: DonationType,
  val amount: Double,
  val inKindDescription: String? = null,
  val purpose: String? = null,
  val receiptNumber: String,
  val receivedById: Long? = null,
  val receivedByName: String? = null,
  val receivedAt: Long = System.currentTimeMillis(),
)

data class AidDistribution(
  val id: Long = 0,
  val beneficiaryId: Long,
  val beneficiaryName: String = "",
  val caseId: Long? = null,
  val approvedById: Long? = null,
  val approvedByName: String? = null,
  val aidType: AidType,
  val amount: Double,
  val itemDetails: String? = null,
  val distributedAt: Long = System.currentTimeMillis(),
)

data class VolunteerTask(
  val id: Long = 0,
  val assigneeId: Long,
  val assigneeName: String = "",
  val title: String,
  val description: String,
  val dueDate: Long,
  val priority: Priority,
  val status: TaskStatus,
  val hoursLogged: Double = 0.0,
)

data class AuditLog(
  val id: Long = 0,
  val actorStaffId: Long? = null,
  val actorName: String = "النظام",
  val action: String,
  val entityName: String,
  val entityId: Long,
  val details: String,
  val timestamp: Long = System.currentTimeMillis(),
)

data class AssociationReportSummary(
  val totalBeneficiaries: Int = 0,
  val activeCasesCount: Int = 0,
  val urgentCasesCount: Int = 0,
  val activeCoursesCount: Int = 0,
  val totalEnrollments: Int = 0,
  val empowermentProjectsCount: Int = 0,
  val selfReliantProjectsCount: Int = 0,
  val totalCashDonations: Double = 0.0,
  val totalCashAidDistributed: Double = 0.0,
  val totalAidRecordsCount: Int = 0,
  val totalVolunteerHours: Double = 0.0,
  val completedTasksCount: Int = 0,
  val openTasksCount: Int = 0,
)
