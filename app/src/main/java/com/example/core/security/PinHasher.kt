package com.example.core.security

import com.example.domain.model.StaffMember
import com.example.domain.model.StaffRole
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PinHasher {
  private const val SALT_BYTES = 16

  fun generateSaltHex(): String {
    val bytes = ByteArray(SALT_BYTES)
    SecureRandom().nextBytes(bytes)
    return bytes.toHex()
  }

  fun hashPin(pin: String, saltHex: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val input = "$saltHex:$pin".toByteArray(Charsets.UTF_8)
    return digest.digest(input).toHex()
  }

  fun verifyPin(pin: String, saltHex: String, expectedHashHex: String): Boolean {
    val candidate = hashPin(pin, saltHex).toByteArray(Charsets.UTF_8)
    val expected = expectedHashHex.toByteArray(Charsets.UTF_8)
    return MessageDigest.isEqual(candidate, expected)
  }

  private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}

class SessionManager {
  private val _currentStaff = MutableStateFlow<StaffMember?>(null)
  val currentStaff: StateFlow<StaffMember?> = _currentStaff.asStateFlow()

  fun signIn(staff: StaffMember) {
    _currentStaff.value = staff
  }

  fun signOut() {
    _currentStaff.value = null
  }

  fun hasAnyRole(vararg roles: StaffRole): Boolean {
    val current = _currentStaff.value ?: return false
    return current.isActive && current.role in roles
  }

  fun requireRole(vararg roles: StaffRole): StaffMember {
    val current =
      _currentStaff.value
        ?: throw SecurityException("يجب تسجيل دخول الموظف أولاً لتنفيذ هذه العملية.")
    if (!current.isActive || current.role !in roles) {
      val allowed = roles.joinToString(" أو ") { it.labelAr }
      throw SecurityException("يتطلب هذا الإجراء صلاحية: $allowed.")
    }
    return current
  }
}
