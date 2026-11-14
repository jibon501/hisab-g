package com.example.data.model

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

enum class TxnType(val wire: String) {
    CREDIT("credit"),
    DEBIT("debit");

    companion object {
        fun fromWire(v: String): TxnType = if (v == "credit") CREDIT else DEBIT
    }
}

enum class DebtKind(val wire: String) {
    LOAN("loan"),
    LENT("lent"),
    SHOP("shop");

    companion object {
        fun fromWire(v: String): DebtKind = when (v) {
            "loan" -> LOAN
            "lent" -> LENT
            else -> SHOP
        }
    }
}

enum class CategoryKind(val wire: String) {
    EXPENSE("expense"),
    INCOME("income");

    companion object {
        fun fromWire(v: String): CategoryKind = if (v == "income") INCOME else EXPENSE
    }
}

data class Account(
    val id: Long,
    val name: String,
    val type: String,
    val balance: Double,
    val opening: Double,
    val icon: String
)

data class Txn(
    val id: Long,
    val type: TxnType,
    val amount: Double,
    val accountId: Long,
    val accountName: String,
    val category: String,
    val note: String,
    val date: String,
    val debtId: Long? = null,
    val isPayment: Boolean = false,
    val ref: String? = null
)

data class Debt(
    val id: Long,
    val kind: DebtKind,
    val person: String,
    val total: Double,
    val remaining: Double,
    val note: String,
    val date: String,
    val isExpense: Boolean = false,
    val category: String = ""
)

data class Category(
    val id: Long,
    val name: String,
    val kind: CategoryKind
)

data class HistoryItem(
    val date: String,
    val label: String,
    val amount: Double,
    val payment: Boolean
)

data class MonthItem(
    val id: Long,
    val title: String,
    val amount: Double,
    val remaining: Double,
    val debtId: Long? = null,
    val date: String,
    val category: String
)

data class Summary(
    val cash: Double = 0.0,
    val loan: Double = 0.0,
    val lent: Double = 0.0,
    val shop: Double = 0.0,
    val dps: Double = 0.0,
    val asset: Double = 0.0,
    val monthExpense: Double = 0.0,
    val monthBaki: Double = 0.0
)

data class SplitAmount(
    val accountId: Long?,
    val amount: Double
)

const val OTHER_ACCOUNT = "অন্যান্য"

val DEFAULT_EXPENSE_CATS = listOf(
    "খাবার",
    "বাজার",
    "বাসা ভাড়া",
    "যাতায়াত",
    "বিল",
    "চিকিৎসা",
    "শিক্ষা",
    "বিনোদন",
    "অন্যান্য"
)

val DEFAULT_INCOME_CATS = listOf(
    "বেতন",
    "ব্যবসা",
    "উপহার",
    "অন্যান্য আয়"
)

val TYPE_LABELS = mapOf(
    "asset" to "সম্পদ",
    "dps" to "ডিপিএস",
    "bank" to "ব্যাংক",
    "mobile" to "মোবাইল",
    "cash" to "নগদ",
    "other" to "অন্যান্য"
)

fun typeLabel(type: String): String = TYPE_LABELS[type] ?: type

private val BN_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

fun toBnDigits(value: String): String {
    val sb = StringBuilder(value.length)
    for (ch in value) {
        if (ch in '0'..'9') {
            sb.append(BN_DIGITS[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

fun tk(amount: Double, useBnDigits: Boolean = true): String {
    val rounded = amount.roundToLong()
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(rounded)
    return if (useBnDigits) "৳ ${toBnDigits(formatted)}" else "৳ $formatted"
}

fun fmtDate(iso: String, useBnDigits: Boolean = true): String {
    if (iso.isBlank()) return ""
    val dateOnly = iso.take(10)
    val parts = dateOnly.split("-")
    val formatted = if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else dateOnly
    return if (useBnDigits) toBnDigits(formatted) else formatted
}
