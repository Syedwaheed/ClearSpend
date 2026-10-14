package com.edu.core.presentation.designsystem

import androidx.compose.ui.graphics.Color

// Core palette (DESIGN_SPEC §2)
val Navy = Color(0xFF0A1628)
val Navy2 = Color(0xFF12213D)
val Blue = Color(0xFF2D6EF5)
val BlueDark = Color(0xFF1B4FC7)
val Mint = Color(0xFF00D4AA)

val Success = Color(0xFF00C853)
val SuccessOnWhite = Color(0xFF00A043)
val Error = Color(0xFFFF4B4B)
val ErrorOnWhite = Color(0xFFE03B3B)
val Warning = Color(0xFFFFB020)
val WarningOnWhite = Color(0xFFB36B00)

val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF5F7FA)
val Outline = Color(0xFFE7EAEE)
val OnSurface = Color(0xFF0A1628)
val OnSurfaceVariant = Color(0xFF6B7684)

/** Muted text on navy surfaces only — rgba(245,247,250,0.6). */
val OnNavyMuted = Color(0x99F5F7FA)
val OnNavy = Color(0xFFF5F7FA)

// transaction_category → colour (DESIGN_SPEC §6)
val CategoryPurchase = Blue
val CategoryTransfer = Color(0xFF8A6BFF)
val CategoryDirectDebit = Error
val CategoryBillPayment = Color(0xFFFF9F6B)
val CategoryCashAtm = Warning
val CategoryCredit = Success
val CategoryOther = OnSurfaceVariant

// account_type → colour (DESIGN_SPEC §6)
val AccountTypeTransaction = Blue
val AccountTypeSavings = Color(0xFF00A886)
val AccountTypeBusinessTransaction = Color(0xFF7A4FD8)
val AccountTypeBusinessSavings = Color(0xFF00A886)
