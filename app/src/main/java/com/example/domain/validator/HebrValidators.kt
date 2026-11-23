package com.example.domain.validator

import com.example.core.result.AppError
import com.example.core.result.AppResult

object HebrValidators {
  private val NATIONAL_ID_REGEX = Regex("^[0-9]{8,14}$")
  private val PHONE_REGEX = Regex("^[+0-9][0-9\\s-]{6,15}$")

  fun validateRequiredText(value: String, fieldLabel: String): AppResult<String> {
    val clean = value.trim()
    if (clean.isBlank()) {
      return AppResult.Error(AppError.ValidationError("حقل ($fieldLabel) مطلوب.", fieldLabel))
    }
    return AppResult.Success(clean)
  }

  fun validateNationalId(value: String): AppResult<String> {
    val clean = value.trim()
    if (!NATIONAL_ID_REGEX.matches(clean)) {
      return AppResult.Error(
        AppError.ValidationError("رقم الهوية الوطنية يجب أن يتكون من 8 إلى 14 رقماً.", "nationalId")
      )
    }
    return AppResult.Success(clean)
  }

  fun validatePhone(value: String): AppResult<String> {
    val clean = value.trim()
    if (!PHONE_REGEX.matches(clean)) {
      return AppResult.Error(
        AppError.ValidationError("يرجى إدخال رقم هاتف صحيح.", "phone")
      )
    }
    return AppResult.Success(clean)
  }

  fun validatePin(pin: String): AppResult<String> {
    val clean = pin.trim()
    if (clean.length < 4 || clean.length > 8 || !clean.all { it.isDigit() }) {
      return AppResult.Error(
        AppError.ValidationError("رمز التحقق (PIN) يجب أن يكون من 4 إلى 8 أرقام.", "pin")
      )
    }
    return AppResult.Success(clean)
  }

  fun validateNonNegativeAmount(amount: Double, fieldLabel: String): AppResult<Double> {
    if (amount.isNaN() || amount < 0.0) {
      return AppResult.Error(
        AppError.ValidationError("قيمة ($fieldLabel) لا يمكن أن تكون سالبة.", fieldLabel)
      )
    }
    return AppResult.Success(amount)
  }

  fun validatePositiveAmount(amount: Double, fieldLabel: String): AppResult<Double> {
    if (amount.isNaN() || amount <= 0.0) {
      return AppResult.Error(
        AppError.ValidationError("قيمة ($fieldLabel) يجب أن تكون أكبر من صفر.", fieldLabel)
      )
    }
    return AppResult.Success(amount)
  }

  fun validatePositiveInt(value: Int, fieldLabel: String, min: Int = 1): AppResult<Int> {
    if (value < min) {
      return AppResult.Error(
        AppError.ValidationError("قيمة ($fieldLabel) يجب أن لا تقل عن $min.", fieldLabel)
      )
    }
    return AppResult.Success(value)
  }
}
