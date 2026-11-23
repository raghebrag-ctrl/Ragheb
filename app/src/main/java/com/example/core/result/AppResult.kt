package com.example.core.result

import android.database.sqlite.SQLiteConstraintException

sealed interface AppError {
  val messageAr: String

  data class ValidationError(override val messageAr: String, val field: String? = null) : AppError

  data class DuplicateRecordError(override val messageAr: String) : AppError

  data class CapacityExceededError(override val messageAr: String) : AppError

  data class UnauthorizedError(override val messageAr: String) : AppError

  data class DatabaseConstraintError(override val messageAr: String) : AppError

  data class NotFoundError(override val messageAr: String) : AppError

  data class UnexpectedError(override val messageAr: String, val cause: Throwable? = null) :
    AppError
}

sealed interface AppResult<out T> {
  data class Success<T>(val data: T) : AppResult<T>

  data class Error(val error: AppError) : AppResult<Nothing>
}

inline fun <T> AppResult<T>.onSuccess(block: (T) -> Unit): AppResult<T> {
  if (this is AppResult.Success) block(data)
  return this
}

inline fun <T> AppResult<T>.onError(block: (AppError) -> Unit): AppResult<T> {
  if (this is AppResult.Error) block(error)
  return this
}

suspend fun <T> safeDbCall(
  duplicateMessage: String = "يوجد سجل مطابق مسجل مسبقاً في النظام.",
  constraintMessage: String = "لا يمكن إتمام العملية لوجود سجلات مرتبطة بها.",
  block: suspend () -> T,
): AppResult<T> {
  return try {
    AppResult.Success(block())
  } catch (e: SQLiteConstraintException) {
    val msg = e.message.orEmpty()
    when {
      msg.contains("UNIQUE", ignoreCase = true) ->
        AppResult.Error(AppError.DuplicateRecordError(duplicateMessage))
      msg.contains("FOREIGN KEY", ignoreCase = true) ->
        AppResult.Error(AppError.DatabaseConstraintError(constraintMessage))
      else -> AppResult.Error(AppError.DatabaseConstraintError(constraintMessage))
    }
  } catch (e: IllegalStateException) {
    AppResult.Error(AppError.ValidationError(e.message ?: "حالة غير صالحة لتنفيذ العملية."))
  } catch (e: SecurityException) {
    AppResult.Error(AppError.UnauthorizedError(e.message ?: "لا تملك الصلاحية لتنفيذ هذا الإجراء."))
  } catch (e: Exception) {
    AppResult.Error(
      AppError.UnexpectedError("تعذر إتمام العملية بسبب خطأ في قاعدة البيانات المحلية.", e)
    )
  }
}
