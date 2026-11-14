package com.example.data

import androidx.annotation.DrawableRes
import com.example.R

data class BankInfo(
    val en: String,
    val bn: String,
    val logo: String
)

val BANK_LIST = listOf(
    BankInfo("AB Bank", "এবি ব্যাংক", "bank_02"),
    BankInfo("Agrani Bank", "অগ্রণী ব্যাংক", "bank_03"),
    BankInfo("Al-Arafah Islami Bank", "আল-আরাফাহ ইসলামী ব্যাংক", "bank_04"),
    BankInfo("Bangladesh Commerce Bank", "বাংলাদেশ কমার্স ব্যাংক", "bank_05"),
    BankInfo("Bangladesh Development Bank", "বাংলাদেশ ডেভেলপমেন্ট ব্যাংক", "bank_06"),
    BankInfo("Bangladesh Krishi Bank", "বাংলাদেশ কৃষি ব্যাংক", "bank_07"),
    BankInfo("Bank Alfalah", "ব্যাংক আলফালাহ", "bank_08"),
    BankInfo("Bank Asia", "ব্যাংক এশিয়া", "bank_09"),
    BankInfo("BASIC Bank", "বেসিক ব্যাংক", "bank_10"),
    BankInfo("Bengal Commercial Bank", "বেঙ্গল কমার্শিয়াল ব্যাংক", "bank_11"),
    BankInfo("BRAC Bank", "ব্র্যাক ব্যাংক", "bank_12"),
    BankInfo("Citibank", "সিটিব্যাংক", "bank_13"),
    BankInfo("Citizens Bank", "সিটিজেন্স ব্যাংক", "bank_14"),
    BankInfo("City Bank", "সিটি ব্যাংক", "bank_15"),
    BankInfo("Commercial Bank of Ceylon", "কমার্শিয়াল ব্যাংক অব সিলন", "bank_16"),
    BankInfo("Community Bank", "কমিউনিটি ব্যাংক", "bank_17"),
    BankInfo("Dhaka Bank", "ঢাকা ব্যাংক", "bank_18"),
    BankInfo("Dutch-Bangla Bank", "ডাচ-বাংলা ব্যাংক", "bank_19"),
    BankInfo("Eastern Bank", "ইস্টার্ন ব্যাংক", "bank_20"),
    BankInfo("EXIM Bank", "এক্সিম ব্যাংক", "bank_21"),
    BankInfo("FSIB PLC", "ফার্স্ট সিকিউরিটি ইসলামী ব্যাংক পিএলসি", "bank_22"),
    BankInfo("Global Islami Bank", "গ্লোবাল ইসলামী ব্যাংক", "bank_23"),
    BankInfo("Habib Bank", "হাবিব ব্যাংক", "bank_24"),
    BankInfo("HSBC Bank", "এইচএসবিসি ব্যাংক", "bank_25"),
    BankInfo("ICB Islamic Bank", "আইসিবি ইসলামিক ব্যাংক", "bank_26"),
    BankInfo("IFIC Bank", "আইএফআইসি ব্যাংক", "bank_27"),
    BankInfo("Islami Bank", "ইসলামী ব্যাংক", "bank_28"),
    BankInfo("Jamuna Bank", "যমুনা ব্যাংক", "bank_29"),
    BankInfo("Janata Bank", "জনতা ব্যাংক", "bank_30"),
    BankInfo("Meghna Bank", "মেঘনা ব্যাংক", "bank_31"),
    BankInfo("Mercantile Bank", "মার্কেটাইল ব্যাংক", "bank_32"),
    BankInfo("Midland Bank", "মিডল্যান্ড ব্যাংক", "bank_33"),
    BankInfo("Modhumoti Bank", "মধুমতি ব্যাংক", "bank_34"),
    BankInfo("Mutual Trust Bank", "মিউচুয়াল ট্রাস্ট ব্যাংক", "bank_35"),
    BankInfo("National Bank", "ন্যাশনাল ব্যাংক", "bank_36"),
    BankInfo("National Bank of Pakistan", "ন্যাশনাল ব্যাংক অব পাকিস্তান", "bank_37"),
    BankInfo("NCC Bank", "এনসিসি ব্যাংক", "bank_38"),
    BankInfo("NRB Bank", "এনআরবি ব্যাংক", "bank_39"),
    BankInfo("NRBC Bank", "এনআরবিসি ব্যাংক", "bank_40"),
    BankInfo("One Bank", "ওয়ান ব্যাংক", "bank_41"),
    BankInfo("Padma Bank", "পদ্মা ব্যাংক", "bank_42"),
    BankInfo("Premier Bank", "প্রিমিয়ার ব্যাংক", "bank_43"),
    BankInfo("Prime Bank", "প্রাইম ব্যাংক", "bank_44"),
    BankInfo("Probashi Kallyan Bank", "প্রবাসী কল্যাণ ব্যাংক", "bank_45"),
    BankInfo("Pubali Bank", "পূবালী ব্যাংক", "bank_46"),
    BankInfo("Rajshahi Krishi Unnayan Bank", "রাজশাহী কৃষি উন্নয়ন ব্যাংক", "bank_47"),
    BankInfo("Rupali Bank", "রূপালী ব্যাংক", "bank_48"),
    BankInfo("SBAC Bank", "এসবিএসি ব্যাংক", "bank_49"),
    BankInfo("Shahjalal Islami Bank", "শাহজালাল ইসলামী ব্যাংক", "bank_50"),
    BankInfo("Shimanto Bank", "সীমান্ত ব্যাংক", "bank_51"),
    BankInfo("Social Islami Bank", "সোশ্যাল ইসলামী ব্যাংক", "bank_52"),
    BankInfo("Sonali Bank", "সোনালী ব্যাংক", "bank_53"),
    BankInfo("Southeast Bank", "সাউথইস্ট ব্যাংক", "bank_54"),
    BankInfo("Standard Chartered Bank", "স্ট্যান্ডার্ড চার্টার্ড ব্যাংক", "bank_55"),
    BankInfo("Standard Islami Bank", "স্ট্যান্ডার্ড ইসলামী ব্যাংক", "bank_56"),
    BankInfo("State Bank of India", "স্টেট ব্যাংক অব ইন্ডিয়া", "bank_57"),
    BankInfo("Trust Bank", "ট্রাস্ট ব্যাংক", "bank_58"),
    BankInfo("Union Bank", "ইউনিয়ন ব্যাংক", "bank_59"),
    BankInfo("United Commercial Bank", "ইউনাইটেড কমার্শিয়াল ব্যাংক", "bank_60"),
    BankInfo("Uttara Bank", "উত্তরা ব্যাংক", "bank_61"),
    BankInfo("Woori Bank", "উরি ব্যাংক", "bank_62")
)

val MFS_LIST = listOf(
    BankInfo("bKash", "বিকাশ", "mfs_02"),
    BankInfo("Nagad", "নগদ", "mfs_03"),
    BankInfo("Rocket", "রকেট", "mfs_04"),
    BankInfo("Upay", "উপায়", "mfs_05"),
    BankInfo("SureCash", "শিউরক্যাশ", "mfs_06"),
    BankInfo("mCash", "এমক্যাশ", "mfs_07"),
    BankInfo("MYCash", "মাইক্যাশ", "mfs_08"),
    BankInfo("Tap", "ট্যাপ", "mfs_09"),
    BankInfo("Islamic Wallet", "ইসলামিক ওয়ালেট", "mfs_10"),
    BankInfo("OK Wallet", "ওকে ওয়ালেট", "mfs_11"),
    BankInfo("TeleCash", "টেলিক্যাশ", "mfs_12"),
    BankInfo("Tap ‘n Pay", "ট্যাপ এন পে", "mfs_13")
)

private val LOGO_DRAWABLES: Map<String, Int> = mapOf(
    "bank_02" to R.drawable.bank_02,
    "bank_03" to R.drawable.bank_03,
    "bank_04" to R.drawable.bank_04,
    "bank_05" to R.drawable.bank_05,
    "bank_06" to R.drawable.bank_06,
    "bank_07" to R.drawable.bank_07,
    "bank_08" to R.drawable.bank_08,
    "bank_09" to R.drawable.bank_09,
    "bank_10" to R.drawable.bank_10,
    "bank_11" to R.drawable.bank_11,
    "bank_12" to R.drawable.bank_12,
    "bank_13" to R.drawable.bank_13,
    "bank_14" to R.drawable.bank_14,
    "bank_15" to R.drawable.bank_15,
    "bank_16" to R.drawable.bank_16,
    "bank_17" to R.drawable.bank_17,
    "bank_18" to R.drawable.bank_18,
    "bank_19" to R.drawable.bank_19,
    "bank_20" to R.drawable.bank_20,
    "bank_21" to R.drawable.bank_21,
    "bank_22" to R.drawable.bank_22,
    "bank_23" to R.drawable.bank_23,
    "bank_24" to R.drawable.bank_24,
    "bank_25" to R.drawable.bank_25,
    "bank_26" to R.drawable.bank_26,
    "bank_27" to R.drawable.bank_27,
    "bank_28" to R.drawable.bank_28,
    "bank_29" to R.drawable.bank_29,
    "bank_30" to R.drawable.bank_30,
    "bank_31" to R.drawable.bank_31,
    "bank_32" to R.drawable.bank_32,
    "bank_33" to R.drawable.bank_33,
    "bank_34" to R.drawable.bank_34,
    "bank_35" to R.drawable.bank_35,
    "bank_36" to R.drawable.bank_36,
    "bank_37" to R.drawable.bank_37,
    "bank_38" to R.drawable.bank_38,
    "bank_39" to R.drawable.bank_39,
    "bank_40" to R.drawable.bank_40,
    "bank_41" to R.drawable.bank_41,
    "bank_42" to R.drawable.bank_42,
    "bank_43" to R.drawable.bank_43,
    "bank_44" to R.drawable.bank_44,
    "bank_45" to R.drawable.bank_45,
    "bank_46" to R.drawable.bank_46,
    "bank_47" to R.drawable.bank_47,
    "bank_48" to R.drawable.bank_48,
    "bank_49" to R.drawable.bank_49,
    "bank_50" to R.drawable.bank_50,
    "bank_51" to R.drawable.bank_51,
    "bank_52" to R.drawable.bank_52,
    "bank_53" to R.drawable.bank_53,
    "bank_54" to R.drawable.bank_54,
    "bank_55" to R.drawable.bank_55,
    "bank_56" to R.drawable.bank_56,
    "bank_57" to R.drawable.bank_57,
    "bank_58" to R.drawable.bank_58,
    "bank_59" to R.drawable.bank_59,
    "bank_60" to R.drawable.bank_60,
    "bank_61" to R.drawable.bank_61,
    "bank_62" to R.drawable.bank_62,
    "mfs_02" to R.drawable.mfs_02,
    "mfs_03" to R.drawable.mfs_03,
    "mfs_04" to R.drawable.mfs_04,
    "mfs_05" to R.drawable.mfs_05,
    "mfs_06" to R.drawable.mfs_06,
    "mfs_07" to R.drawable.mfs_07,
    "mfs_08" to R.drawable.mfs_08,
    "mfs_09" to R.drawable.mfs_09,
    "mfs_10" to R.drawable.mfs_10,
    "mfs_11" to R.drawable.mfs_11,
    "mfs_12" to R.drawable.mfs_12,
    "mfs_13" to R.drawable.mfs_13
)

@DrawableRes
fun drawableResForLogo(logoName: String): Int? = LOGO_DRAWABLES[logoName]

private val NORM_REGEX = Regex("[\\s\\-.'’‘]")
private val BANGLA_REGEX = Regex("[\\u0980-\\u09FF]")

fun normBankQuery(s: String): String = s.lowercase().replace(NORM_REGEX, "")

fun hasBangla(s: String): Boolean = BANGLA_REGEX.containsMatchIn(s)

fun findBank(name: String): BankInfo? {
    val n = normBankQuery(name)
    if (n.isEmpty()) return null
    return (MFS_LIST + BANK_LIST).find { normBankQuery(it.en) == n || normBankQuery(it.bn) == n }
}

fun bankSuggestions(type: String, typed: String): List<BankInfo> {
    val list = when (type) {
        "mobile" -> MFS_LIST
        "bank" -> BANK_LIST
        else -> emptyList()
    }
    val q = normBankQuery(typed)
    if (q.isEmpty()) return emptyList()

    return list
        .filter { normBankQuery(it.en).contains(q) || normBankQuery(it.bn).contains(q) }
        .filter { b ->
            val matchName = if (hasBangla(typed)) b.bn else b.en
            normBankQuery(matchName) != q
        }
        .sortedWith { a, b ->
            val aStarts = normBankQuery(a.en).startsWith(q) || normBankQuery(a.bn).startsWith(q)
            val bStarts = normBankQuery(b.en).startsWith(q) || normBankQuery(b.bn).startsWith(q)
            when {
                aStarts && !bStarts -> -1
                !aStarts && bStarts -> 1
                else -> 0
            }
        }
        .take(6)
}

@DrawableRes
fun getLogoResForAccount(accountName: String): Int? {
    val direct = findBank(accountName)
    if (direct != null) return drawableResForLogo(direct.logo)

    val n = accountName.lowercase()
    val logo = when {
        n.contains("bkash") || n.contains("বিকাশ") -> "mfs_02"
        n.contains("nagad") || n.contains("নগদ") -> "mfs_03"
        n.contains("rocket") || n.contains("রকেট") -> "mfs_04"
        n.contains("upay") || n.contains("উপায়") || n.contains("উপায়") -> "mfs_05"
        n.contains("ab bank") || n.startsWith("ab ") || n == "ab" -> "bank_02"
        n.contains("ific") -> "bank_27"
        n.contains("islami") || n.contains("ইসলামী") -> "bank_28"
        n.contains("brac") || n.contains("ব্র্যাক") -> "bank_12"
        n.contains("city") || n.contains("সিটি") -> "bank_15"
        n.contains("dbbl") || n.contains("dutch") || n.contains("ডাচ") -> "bank_19"
        n.contains("sonali") || n.contains("সোনালী") -> "bank_53"
        n.contains("janata") || n.contains("জনতা") -> "bank_30"
        n.contains("agrani") || n.contains("অগ্রণী") -> "bank_03"
        n.contains("rupali") || n.contains("রূপালী") -> "bank_48"
        n.contains("pubali") || n.contains("পূবালী") -> "bank_46"
        n.contains("eastern") || n.contains("ইস্টার্ন") -> "bank_20"
        n.contains("prime") || n.contains("প্রাইম") -> "bank_44"
        n.contains("dhaka bank") || n.contains("ঢাকা ব্যাংক") -> "bank_18"
        n.contains("ucb") || n.contains("united commercial") -> "bank_60"
        else -> null
    }
    return logo?.let { drawableResForLogo(it) }
}
