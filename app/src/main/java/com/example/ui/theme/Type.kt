package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.example.R

val CairoFontFamily = FontFamily(
  Font(R.font.cairo, FontWeight.Normal),
  Font(R.font.cairo, FontWeight.Medium),
  Font(R.font.cairo, FontWeight.SemiBold),
  Font(R.font.cairo, FontWeight.Bold),
)

val IbmPlexArabicFontFamily = FontFamily(
  Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal),
  Font(R.font.ibm_plex_sans_arabic, FontWeight.Medium),
  Font(R.font.ibm_plex_sans_arabic, FontWeight.SemiBold),
)

val HebrTypography = Typography(
  displaySmall = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 42.sp,
    textDirection = TextDirection.ContentOrRtl,
  ),
  headlineMedium = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 34.sp,
    textDirection = TextDirection.ContentOrRtl,
  ),
  headlineSmall = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    textDirection = TextDirection.ContentOrRtl,
  ),
  titleLarge = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    lineHeight = 26.sp,
    textDirection = TextDirection.ContentOrRtl,
  ),
  titleMedium = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    textDirection = TextDirection.ContentOrRtl,
  ),
  titleSmall = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    textDirection = TextDirection.ContentOrRtl,
  ),
  bodyLarge = TextStyle(
    fontFamily = IbmPlexArabicFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    textDirection = TextDirection.ContentOrRtl,
  ),
  bodyMedium = TextStyle(
    fontFamily = IbmPlexArabicFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp,
    textDirection = TextDirection.ContentOrRtl,
  ),
  bodySmall = TextStyle(
    fontFamily = IbmPlexArabicFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 18.sp,
    textDirection = TextDirection.ContentOrRtl,
  ),
  labelLarge = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    textDirection = TextDirection.ContentOrRtl,
  ),
  labelMedium = TextStyle(
    fontFamily = IbmPlexArabicFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    textDirection = TextDirection.ContentOrRtl,
  ),
  labelSmall = TextStyle(
    fontFamily = IbmPlexArabicFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    textDirection = TextDirection.ContentOrRtl,
  ),
)
