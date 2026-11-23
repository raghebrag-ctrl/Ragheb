package com.example.data.mapper

import com.example.data.local.entity.AidDistributionEntity
import com.example.data.local.entity.AidDistributionRow
import com.example.data.local.entity.AttendanceRecordEntity
import com.example.data.local.entity.AuditLogRow
import com.example.data.local.entity.BeneficiaryEntity
import com.example.data.local.entity.CourseEntity
import com.example.data.local.entity.CourseWithCountRow
import com.example.data.local.entity.DonationEntity
import com.example.data.local.entity.DonationRow
import com.example.data.local.entity.EmpowermentProgramEntity
import com.example.data.local.entity.EmpowermentProgramRow
import com.example.data.local.entity.EnrollmentRow
import com.example.data.local.entity.SocialCaseEntity
import com.example.data.local.entity.SocialCaseRow
import com.example.data.local.entity.StaffMemberEntity
import com.example.data.local.entity.VolunteerTaskEntity
import com.example.data.local.entity.VolunteerTaskRow
import com.example.domain.model.AidDistribution
import com.example.domain.model.AttendanceRecord
import com.example.domain.model.AuditLog
import com.example.domain.model.Beneficiary
import com.example.domain.model.Course
import com.example.domain.model.CourseEnrollment
import com.example.domain.model.Donation
import com.example.domain.model.EmpowermentProgram
import com.example.domain.model.SocialCase
import com.example.domain.model.StaffMember
import com.example.domain.model.VolunteerTask

fun StaffMemberEntity.toDomain() =
  StaffMember(
    id = id,
    fullName = fullName,
    role = role,
    phone = phone,
    email = email,
    specialty = specialty,
    isActive = isActive,
    joinedAt = joinedAt,
  )

fun BeneficiaryEntity.toDomain() =
  Beneficiary(
    id = id,
    fileNumber = fileNumber,
    fullName = fullName,
    nationalId = nationalId,
    phone = phone,
    address = address,
    familySize = familySize,
    monthlyIncome = monthlyIncome,
    vulnerabilityLevel = vulnerabilityLevel,
    status = status,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
  )

fun Beneficiary.toEntity() =
  BeneficiaryEntity(
    id = id,
    fileNumber = fileNumber,
    fullName = fullName,
    nationalId = nationalId,
    phone = phone,
    address = address,
    familySize = familySize,
    monthlyIncome = monthlyIncome,
    vulnerabilityLevel = vulnerabilityLevel,
    status = status,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
  )

fun SocialCaseRow.toDomain() =
  SocialCase(
    id = id,
    beneficiaryId = beneficiaryId,
    beneficiaryName = beneficiaryName,
    assignedStaffId = assignedStaffId,
    assignedStaffName = assignedStaffName,
    caseType = caseType,
    priority = priority,
    assessmentSummary = assessmentSummary,
    recommendedAction = recommendedAction,
    status = status,
    openedAt = openedAt,
    closedAt = closedAt,
  )

fun SocialCase.toEntity() =
  SocialCaseEntity(
    id = id,
    beneficiaryId = beneficiaryId,
    assignedStaffId = assignedStaffId,
    caseType = caseType,
    priority = priority,
    assessmentSummary = assessmentSummary,
    recommendedAction = recommendedAction,
    status = status,
    openedAt = openedAt,
    closedAt = closedAt,
  )

fun CourseWithCountRow.toDomain() =
  Course(
    id = id,
    title = title,
    category = category,
    instructorId = instructorId,
    instructorName = instructorName,
    startDate = startDate,
    endDate = endDate,
    maxCapacity = maxCapacity,
    enrolledCount = enrolledCount,
    status = status,
  )

fun Course.toEntity() =
  CourseEntity(
    id = id,
    title = title,
    category = category,
    instructorId = instructorId,
    startDate = startDate,
    endDate = endDate,
    maxCapacity = maxCapacity,
    status = status,
  )

fun EnrollmentRow.toDomain() =
  CourseEnrollment(
    id = id,
    courseId = courseId,
    beneficiaryId = beneficiaryId,
    beneficiaryName = beneficiaryName,
    fileNumber = fileNumber,
    enrolledAt = enrolledAt,
    completionStatus = completionStatus,
    finalGrade = finalGrade,
    presentSessions = presentSessions,
    totalSessions = totalSessions,
  )

fun AttendanceRecordEntity.toDomain() =
  AttendanceRecord(
    id = id,
    enrollmentId = enrollmentId,
    sessionDate = sessionDate,
    isPresent = isPresent,
    note = note,
  )

fun EmpowermentProgramRow.toDomain() =
  EmpowermentProgram(
    id = id,
    beneficiaryId = beneficiaryId,
    beneficiaryName = beneficiaryName,
    supervisorId = supervisorId,
    supervisorName = supervisorName,
    projectTitle = projectTitle,
    sector = sector,
    grantAmount = grantAmount,
    fundingType = fundingType,
    stage = stage,
    progressPercent = progressPercent,
    startedAt = startedAt,
  )

fun EmpowermentProgram.toEntity() =
  EmpowermentProgramEntity(
    id = id,
    beneficiaryId = beneficiaryId,
    supervisorId = supervisorId,
    projectTitle = projectTitle,
    sector = sector,
    grantAmount = grantAmount,
    fundingType = fundingType,
    stage = stage,
    progressPercent = progressPercent,
    startedAt = startedAt,
  )

fun DonationRow.toDomain() =
  Donation(
    id = id,
    donorName = donorName,
    donorPhone = donorPhone,
    donationType = donationType,
    amount = amount,
    inKindDescription = inKindDescription,
    purpose = purpose,
    receiptNumber = receiptNumber,
    receivedById = receivedById,
    receivedByName = receivedByName,
    receivedAt = receivedAt,
  )

fun Donation.toEntity() =
  DonationEntity(
    id = id,
    donorName = donorName,
    donorPhone = donorPhone,
    donationType = donationType,
    amount = amount,
    inKindDescription = inKindDescription,
    purpose = purpose,
    receiptNumber = receiptNumber,
    receivedById = receivedById,
    receivedAt = receivedAt,
  )

fun AidDistributionRow.toDomain() =
  AidDistribution(
    id = id,
    beneficiaryId = beneficiaryId,
    beneficiaryName = beneficiaryName,
    caseId = caseId,
    approvedById = approvedById,
    approvedByName = approvedByName,
    aidType = aidType,
    amount = amount,
    itemDetails = itemDetails,
    distributedAt = distributedAt,
  )

fun AidDistribution.toEntity() =
  AidDistributionEntity(
    id = id,
    beneficiaryId = beneficiaryId,
    caseId = caseId,
    approvedById = approvedById,
    aidType = aidType,
    amount = amount,
    itemDetails = itemDetails,
    distributedAt = distributedAt,
  )

fun VolunteerTaskRow.toDomain() =
  VolunteerTask(
    id = id,
    assigneeId = assigneeId,
    assigneeName = assigneeName,
    title = title,
    description = description,
    dueDate = dueDate,
    priority = priority,
    status = status,
    hoursLogged = hoursLogged,
  )

fun VolunteerTask.toEntity() =
  VolunteerTaskEntity(
    id = id,
    assigneeId = assigneeId,
    title = title,
    description = description,
    dueDate = dueDate,
    priority = priority,
    status = status,
    hoursLogged = hoursLogged,
  )

fun AuditLogRow.toDomain() =
  AuditLog(
    id = id,
    actorStaffId = actorStaffId,
    actorName = actorName ?: "النظام",
    action = action,
    entityName = entityName,
    entityId = entityId,
    details = details,
    timestamp = timestamp,
  )
