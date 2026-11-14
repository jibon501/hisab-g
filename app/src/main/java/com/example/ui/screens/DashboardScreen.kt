package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.HisabSnapshot
import com.example.data.model.Account
import com.example.data.model.Debt
import com.example.data.model.DebtKind
import com.example.data.model.HistoryItem
import com.example.data.model.tk
import com.example.ui.components.AccountAvatar
import com.example.ui.components.AccountPickerField
import com.example.ui.components.AvatarSize
import com.example.ui.components.HistoryShareModal
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber200
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber600
import com.example.ui.theme.Amber700
import com.example.ui.theme.Amber800
import com.example.ui.theme.Amber900
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Indigo700
import com.example.ui.theme.Indigo800
import com.example.ui.theme.Purple100
import com.example.ui.theme.Purple50
import com.example.ui.theme.Purple600
import com.example.ui.theme.Purple700
import com.example.ui.theme.Purple800
import com.example.ui.theme.Red100
import com.example.ui.theme.Red50
import com.example.ui.theme.Red600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Teal700

private enum class DetailType {
    CASH, LENT, LOAN, EXPENSE, DPS, ASSET
}

@Composable
fun DashboardScreen(
    snapshot: HisabSnapshot,
    getDebtHistory: (DebtKind, String) -> List<HistoryItem>,
    onPayDebt: (Debt, Double, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDetail by remember { mutableStateOf<DetailType?>(null) }
    var historyTarget by remember { mutableStateOf<Pair<DebtKind, String>?>(null) }
    var payBakiTarget by remember { mutableStateOf<Debt?>(null) }

    val summary = snapshot.summary
    val accounts = snapshot.accounts
    val lentDebts = snapshot.debts.filter { it.kind == DebtKind.LENT }
    val loanDebts = snapshot.debts.filter { it.kind == DebtKind.LOAN }
    val shopDebts = snapshot.debts.filter { it.kind == DebtKind.SHOP }

    val lentGrouped = remember(lentDebts) {
        lentDebts.filter { it.remaining > 0.0 }
            .groupBy { it.person }
            .mapValues { (_, list) -> list.sumOf { it.remaining } }
            .entries.sortedByDescending { it.value }
    }

    val loanGrouped = remember(loanDebts) {
        loanDebts.filter { it.remaining > 0.0 }
            .groupBy { it.person }
            .mapValues { (_, list) -> list.sumOf { it.remaining } }
            .entries.sortedByDescending { it.value }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Welcome / Cash Banner
        Surface(
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Emerald800, Teal700)
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                        Text(
                            text = "বর্তমান মোট তারল্য (ক্যাশ ও ব্যাংক)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Emerald200
                        )
                        Text(
                            text = tk(summary.cash),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "নগদ টাকা, বিকাশ, নগদ ও ব্যাংক ব্যালেন্স",
                            fontSize = 12.sp,
                            color = Emerald100,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Surface(
                        onClick = { selectedDetail = DetailType.CASH },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("dashboard_cash_breakdown_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "ভাঙতি বিবরণ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Row 1: Lent + Loan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardSummaryCard(
                title = "মোট পাওনা টাকা",
                amount = tk(summary.lent),
                subtitle = "অন্যের কাছে পাবেন",
                badgeText = "পাওনা",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accentColor = Emerald600,
                softBg = Emerald50,
                borderColor = Emerald100,
                onClick = { selectedDetail = DetailType.LENT },
                testTag = "dashboard_lent_card",
                modifier = Modifier.weight(1f)
            )
            DashboardSummaryCard(
                title = "ধার / লোন নেওয়া",
                amount = tk(summary.loan),
                subtitle = "অন্যকে পরিশোধ করতে হবে",
                badgeText = "দেনা",
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                accentColor = Red600,
                softBg = Red50,
                borderColor = Red100,
                onClick = { selectedDetail = DetailType.LOAN },
                testTag = "dashboard_loan_card",
                modifier = Modifier.weight(1f)
            )
        }

        // Full-width Month Expense Card
        Surface(
            onClick = { selectedDetail = DetailType.EXPENSE },
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Purple100),
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_expense_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Purple50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "চলতি মাসের খরচ",
                            tint = Purple600,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "চলতি মাসের খরচ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate500
                        )
                        Text(
                            text = tk(summary.monthExpense),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple700
                        )
                    }
                }

                if (summary.monthBaki > 0.0) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Amber50,
                        border = BorderStroke(1.dp, Amber200)
                    ) {
                        Text(
                            text = "দোকান বাকি: ${tk(summary.monthBaki)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Amber700,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // Row 2: DPS + Assets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardSummaryCard(
                title = "ডিপিএস / সঞ্চয় মোট",
                amount = tk(summary.dps),
                subtitle = "ভবিষ্যত জমা সঞ্চয়",
                badgeText = null,
                icon = Icons.Outlined.Savings,
                accentColor = Indigo600,
                softBg = Indigo50,
                borderColor = Indigo100,
                onClick = { selectedDetail = DetailType.DPS },
                testTag = "dashboard_dps_card",
                modifier = Modifier.weight(1f)
            )
            DashboardSummaryCard(
                title = "স্থাবর সম্পদ ও বিনিয়োগ",
                amount = tk(summary.asset),
                subtitle = "জমি, ফ্ল্যাট ও অন্যান্য",
                badgeText = null,
                icon = Icons.Outlined.Apartment,
                accentColor = Amber800,
                softBg = Amber50,
                borderColor = Amber100,
                onClick = { selectedDetail = DetailType.ASSET },
                testTag = "dashboard_asset_card",
                modifier = Modifier.weight(1f)
            )
        }

        // Quick Action Bar for Shop Baki
        if (summary.shop > 0.0) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Amber50,
                border = BorderStroke(1.dp, Amber200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Amber200),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("৳", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Amber900)
                        }
                        Column {
                            Text("দোকান বাকি রয়েছে", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Amber900)
                            Text(tk(summary.shop), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Amber800)
                        }
                    }

                    Surface(
                        onClick = { selectedDetail = DetailType.EXPENSE },
                        shape = RoundedCornerShape(12.dp),
                        color = Amber200.copy(alpha = 0.85f),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("dashboard_pay_baki_banner_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "পরিশোধ করুন",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber900
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    // Detail Modal
    selectedDetail?.let { detail ->
        DashboardDetailDialog(
            detail = detail,
            snapshot = snapshot,
            accounts = accounts,
            lentGrouped = lentGrouped,
            loanGrouped = loanGrouped,
            shopDebts = shopDebts,
            onDismiss = { selectedDetail = null },
            onOpenSlip = { kind, person -> historyTarget = kind to person },
            onOpenPayBaki = { debt -> payBakiTarget = debt }
        )
    }

    // Pay Baki Modal
    payBakiTarget?.let { targetDebt ->
        PayBakiDialog(
            debt = targetDebt,
            accounts = accounts,
            onDismiss = { payBakiTarget = null },
            onConfirm = { amount, accountId ->
                onPayDebt(targetDebt, amount, accountId)
                payBakiTarget = null
            }
        )
    }

    // History Share Modal
    historyTarget?.let { (kind, person) ->
        HistoryShareModal(
            person = person,
            kind = kind,
            items = getDebtHistory(kind, person),
            onClose = { historyTarget = null }
        )
    }
}

@Composable
private fun DashboardSummaryCard(
    title: String,
    amount: String,
    subtitle: String,
    badgeText: String?,
    icon: ImageVector,
    accentColor: Color,
    softBg: Color,
    borderColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 1.dp,
        modifier = modifier.testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(softBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = softBg
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Slate500,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = amount,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Slate400,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun DashboardDetailDialog(
    detail: DetailType,
    snapshot: HisabSnapshot,
    accounts: List<Account>,
    lentGrouped: List<Map.Entry<String, Double>>,
    loanGrouped: List<Map.Entry<String, Double>>,
    shopDebts: List<Debt>,
    onDismiss: () -> Unit,
    onOpenSlip: (DebtKind, String) -> Unit,
    onOpenPayBaki: (Debt) -> Unit
) {
    val title = when (detail) {
        DetailType.CASH -> "ক্যাশ ও ব্যাংক ব্যালেন্স"
        DetailType.LENT -> "মোট পাওনা হিসাব (ধার দেওয়া)"
        DetailType.LOAN -> "দেনা ও লোন গ্রহণ হিসাব"
        DetailType.EXPENSE -> "চলতি মাসের খরচের বিস্তারিত"
        DetailType.DPS -> "ডিপিএস সঞ্চয় হিসাব"
        DetailType.ASSET -> "স্থাবর সম্পদ ও বিনিয়োগ"
    }
    val subtitle = when (detail) {
        DetailType.CASH -> "যে অ্যাকাউন্টগুলোতে ব্যালেন্স রয়েছে"
        DetailType.LENT -> "নামে ক্লিক করে স্লিপ দেখুন বা শেয়ার করুন"
        DetailType.LOAN -> "কার কাছে কত টাকা দেনা রয়েছে"
        DetailType.EXPENSE -> "এই মাসের মোট খরচ ও বাকি"
        DetailType.DPS -> "চলতি ডিপিএস হিসাবসমূহ"
        DetailType.ASSET -> "জমি ও অন্যান্য সম্পদের মূল্য"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 620.dp)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        Text(subtitle, fontSize = 12.sp, color = Slate500)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Slate600)
                    }
                }
                HorizontalDivider(color = Slate200)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (detail) {
                        DetailType.CASH -> {
                            val cashAccounts = accounts.filter {
                                it.type in setOf("cash", "mobile", "bank") && it.balance != 0.0
                            }
                            items(cashAccounts, key = { it.id }) { acc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        AccountAvatar(acc.name, acc.type, acc.icon, AvatarSize.MD)
                                        Column {
                                            Text(acc.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
                                            Text(acc.type, fontSize = 12.sp, color = Slate400)
                                        }
                                    }
                                    Text(tk(acc.balance), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                }
                                HorizontalDivider(color = Slate100)
                            }
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("মোট ক্যাশ:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Emerald700)
                                    Text(tk(snapshot.summary.cash), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Emerald700)
                                }
                            }
                        }

                        DetailType.LENT -> {
                            if (lentGrouped.isEmpty()) {
                                item {
                                    Text(
                                        "কোনো পাওনা হিসাব নেই",
                                        fontSize = 13.sp,
                                        color = Slate400,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 28.dp)
                                    )
                                }
                            } else {
                                items(lentGrouped, key = { it.key }) { (person, amount) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onOpenSlip(DebtKind.LENT, person) }
                                            .padding(horizontal = 8.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Emerald50),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = person.take(1),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Emerald600
                                                )
                                            }
                                            Column {
                                                Text(person, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
                                                Text("স্লিপ দেখুন →", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Emerald600)
                                            }
                                        }
                                        Text(tk(amount), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                                    }
                                    HorizontalDivider(color = Slate100)
                                }
                            }
                        }

                        DetailType.LOAN -> {
                            if (loanGrouped.isEmpty()) {
                                item {
                                    Text(
                                        "কোনো দেনা / লোন হিসাব নেই",
                                        fontSize = 13.sp,
                                        color = Slate400,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 28.dp)
                                    )
                                }
                            } else {
                                items(loanGrouped, key = { it.key }) { (person, amount) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onOpenSlip(DebtKind.LOAN, person) }
                                            .padding(horizontal = 8.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Red50),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = person.take(1),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Red600
                                                )
                                            }
                                            Column {
                                                Text(person, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
                                                Text("স্লিপ দেখুন →", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Red600)
                                            }
                                        }
                                        Text(tk(amount), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Red600)
                                    }
                                    HorizontalDivider(color = Slate100)
                                }
                            }
                        }

                        DetailType.EXPENSE -> {
                            val unpaidShops = shopDebts.filter { it.remaining > 0.0 }
                            if (unpaidShops.isNotEmpty()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Amber50,
                                        border = BorderStroke(1.dp, Amber200),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("দোকান বাকি (পরিশোধ করুন):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Amber900)
                                            unpaidShops.forEach { d ->
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color.White,
                                                    border = BorderStroke(1.dp, Amber100),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                                            Text(d.person, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate800)
                                                            if (d.note.isNotBlank()) {
                                                                Text(d.note, fontSize = 11.sp, color = Slate500)
                                                            }
                                                        }
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Text(tk(d.remaining), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Red600)
                                                            Button(
                                                                onClick = { onOpenPayBaki(d) },
                                                                shape = RoundedCornerShape(8.dp),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                                modifier = Modifier.testTag("pay_baki_item_${d.id}")
                                                            ) {
                                                                Text("পরিশোধ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    "সকল খরচসমূহ:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            items(snapshot.monthItems, key = { it.id }) { m ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(m.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                                        Text(m.category, fontSize = 12.sp, color = Slate400)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(tk(m.amount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Purple700)
                                        if (m.remaining > 0.0) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Amber100
                                            ) {
                                                Text(
                                                    text = "বাকি ${tk(m.remaining)}",
                                                    fontSize = 10.sp,
                                                    color = Amber700,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider(color = Slate100)
                            }

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("মাসের মোট খরচ:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Purple800)
                                    Text(tk(snapshot.summary.monthExpense), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Purple800)
                                }
                            }
                        }

                        DetailType.DPS -> {
                            val dpsAccs = accounts.filter { it.type == "dps" }
                            items(dpsAccs, key = { it.id }) { acc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        AccountAvatar(acc.name, acc.type, acc.icon, AvatarSize.MD)
                                        Text(acc.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
                                    }
                                    Text(tk(acc.balance), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Indigo700)
                                }
                                HorizontalDivider(color = Slate100)
                            }
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("মোট ডিপিএস:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Indigo800)
                                    Text(tk(snapshot.summary.dps), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Indigo800)
                                }
                            }
                        }

                        DetailType.ASSET -> {
                            val assetAccs = accounts.filter { it.type !in setOf("cash", "mobile", "bank", "dps") }
                            items(assetAccs, key = { it.id }) { acc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        AccountAvatar(acc.name, acc.type, acc.icon, AvatarSize.MD)
                                        Column {
                                            Text(acc.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
                                            Text(acc.type, fontSize = 12.sp, color = Slate400)
                                        }
                                    }
                                    Text(tk(acc.balance), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Amber900)
                                }
                                HorizontalDivider(color = Slate100)
                            }
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("মোট সম্পদ:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Amber900)
                                    Text(tk(snapshot.summary.asset), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Amber900)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Slate200)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বন্ধ করুন", color = Slate600, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PayBakiDialog(
    debt: Debt,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (Double, Long) -> Unit
) {
    val positiveAccounts = accounts.filter { it.balance > 0.0 }
    val defaultAcc = positiveAccounts.find { it.balance >= debt.remaining }
        ?: positiveAccounts.firstOrNull()
        ?: accounts.firstOrNull()

    var amountText by remember(debt.id) { mutableStateOf(debt.remaining.toInt().toString()) }
    var selectedAccId by remember(debt.id) { mutableLongStateOf(defaultAcc?.id ?: 1L) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("বাকি পরিশোধ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate800)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Slate400)
                    }
                }
                HorizontalDivider(color = Slate200)

                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Amber50,
                        border = BorderStroke(1.dp, Amber200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(debt.person, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
                            Text("মোট বাকি: ${tk(debt.remaining)}", fontSize = 12.sp, color = Amber800)
                        }
                    }

                    Column {
                        Text("কত টাকা পরিশোধ করছেন", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { raw ->
                                amountText = raw.filter { it.isDigit() || it == '.' }
                                errorMsg = null
                            },
                            prefix = { Text("৳ ", color = Slate400, fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate300
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pay_baki_amount_input")
                        )
                    }

                    AccountPickerField(
                        label = "কোন অ্যাকাউন্ট থেকে কাটবে",
                        accounts = positiveAccounts.ifEmpty { accounts },
                        selectedAccount = accounts.find { it.id == selectedAccId },
                        onSelect = { selectedAccId = it.id },
                        testTag = "pay_baki_account_selector"
                    )

                    if (errorMsg != null) {
                        Text(errorMsg!!, fontSize = 12.sp, color = Red600, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("বাতিল", color = Slate600)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                if (amt <= 0.0) {
                                    errorMsg = "সঠিক পরিমাণ দিন"
                                } else if (amt > debt.remaining + 0.001) {
                                    errorMsg = "বাকির চেয়ে বেশি পরিশোধ করা যাবে না"
                                } else {
                                    onConfirm(amt, selectedAccId)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            modifier = Modifier.testTag("confirm_pay_baki_button")
                        ) {
                            Text("পরিশোধ করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
