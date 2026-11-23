package com.example.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HebrFormatters {
  private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
  private val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale("ar"))
  private val numberFormat =
    DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))

  fun formatDate(epochMillis: Long): String = dateFormat.format(Date(epochMillis))

  fun formatDateTime(epochMillis: Long): String = dateTimeFormat.format(Date(epochMillis))

  fun formatAmount(amount: Double): String = "${numberFormat.format(amount)} ر.س"

  fun formatNumber(value: Number): String = numberFormat.format(value)
}
