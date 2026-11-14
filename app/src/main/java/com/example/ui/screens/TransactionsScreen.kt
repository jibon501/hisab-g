package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HisabSnapshot
import com.example.data.model.CategoryKind
import com.example.data.model.SplitAmount
import com.example.data.model.Txn
import com.example.data.model.TxnType
import com.example.data.model.fmtDate
import com.example.data.model.tk
import com.example.ui.components.AddTxnDialog
import com.example.ui.components.CategoriesDialog
import com.example.ui.components.TransferDialog
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue200
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue600
import com.example.ui.theme.Blue700
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Red50
import com.example.ui.theme.Red600
import com.example.ui.theme.Red700
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionsScreen(
    snapshot: HisabSnapshot,
    txns: List<Txn>,
    filter: String?,
    onApplyFilter: (String?) -> Unit,
    onAddTxns: (TxnType, List<SplitAmount>, String, String, String) -> Unit,
    onAddBakiExpense: (Double, String, String, String) -> Unit,
    onDeleteTxn: (Long) -> Unit,
    onTransfer: (Long, Long, Double) -> Unit,
    onAddCategory: (String, CategoryKind) -> Unit,
    onRenameCategory: (Long, String) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAdd by remember { mutableStateOf(false) }
    var showTransfer by remember { mutableStateOf(false) }
    var showCats by remember { mutableStateOf(false) }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Txn?>(null) }

    val expenseCats = remember(snapshot.categories) {
        snapshot.categories.filter { it.kind == CategoryKind.EXPENSE }.map { it.name }
    }
    val incomeCats = remember(snapshot.categories) {
        snapshot.categories.filter { it.kind == CategoryKind.INCOME }.map { it.name }
    }

    val totalCredits = remember(txns) {
        txns.filter { it.type == TxnType.CREDIT }.sumOf { it.amount }
    }
    val totalDebits = remember(txns) {
        txns.filter { it.type == TxnType.DEBIT }.sumOf { it.amount }
    }
    val netTotal = totalCredits - totalDebits

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Action & Filter Bar
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Slate200),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box {
                            Surface(
                                onClick = { filterMenuExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                color = if (filter != null) Amber500 else Slate50,
                                border = BorderStroke(1.dp, if (filter != null) Amber500 else Slate200),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("txn_filter_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FilterList,
                                        contentDescription = "খাত ফিল্টার",
                                        tint = if (filter != null) Color.White else Slate700,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (filter != null) "খাত: $filter" else "সব খাত",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (filter != null) Color.White else Slate700
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = filterMenuExpanded,
                                onDismissRequest = { filterMenuExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "সব খাত",
                                                fontWeight = if (filter == null) FontWeight.Bold else FontWeight.Normal,
                                                color = if (filter == null) Emerald600 else Slate700
                                            )
                                            if (filter == null) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        onApplyFilter(null)
                                        filterMenuExpanded = false
                                    }
                                )
                                HorizontalDivider(color = Slate100)
                                if (snapshot.usedCategories.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("কোনো খাত পাওয়া যায়নি", fontSize = 12.sp, color = Slate400) },
                                        onClick = { filterMenuExpanded = false }
                                    )
                                } else {
                                    snapshot.usedCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        cat,
                                                        fontWeight = if (filter == cat) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (filter == cat) Amber500 else Slate700
                                                    )
                                                    if (filter == cat) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = Amber500, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            },
                                            onClick = {
                                                onApplyFilter(cat)
                                                filterMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (filter != null) {
                            IconButton(
                                onClick = { onApplyFilter(null) },
                                modifier = Modifier.testTag("clear_txn_filter_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "ফিল্টার মুছুন", tint = Slate600, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = { showCats = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Slate50,
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("open_categories_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Outlined.LocalOffer, contentDescription = "খাতসমূহ", tint = Slate500, modifier = Modifier.size(15.dp))
                                Text("খাতসমূহ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                            }
                        }

                        Surface(
                            onClick = { showTransfer = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Blue50,
                            border = BorderStroke(1.dp, Blue200),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("open_transfer_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "ট্রান্সফার", tint = Blue700, modifier = Modifier.size(15.dp))
                                Text("ট্রান্সফার", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Blue700)
                            }
                        }

                        Button(
                            onClick = { showAdd = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("txns_add_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("নতুন লেনদেন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Summary Mini Bar
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Slate100.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryMiniTile("মোট আয় (+)", tk(totalCredits), Emerald600, Modifier.weight(1f))
                    SummaryMiniTile("মোট ব্যয় (-)", tk(totalDebits), Red600, Modifier.weight(1f))
                    SummaryMiniTile("নিট মোট", tk(netTotal), if (netTotal >= 0) Emerald700 else Red700, Modifier.weight(1f))
                }
            }
        }

        // Transactions List Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Slate200),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (txns.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("কোনো লেনদেন পাওয়া যায়নি", fontSize = 14.sp, color = Slate400)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (filter != null) {
                                "ফিল্টার পরিবর্তন করুন অথবা নতুন লেনদেন যোগ করুন"
                            } else {
                                "নতুন লেনদেন বাটনে চাপুন"
                            },
                            fontSize = 12.sp,
                            color = Slate400,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column {
                        txns.forEachIndexed { index, t ->
                            val isCredit = t.type == TxnType.CREDIT
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isCredit) Emerald50 else Red50),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isCredit) Icons.Default.SouthWest else Icons.Default.NorthEast,
                                            contentDescription = if (isCredit) "আয়" else "ব্যয়",
                                            tint = if (isCredit) Emerald600 else Red600,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = t.category,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                            if (t.isPayment) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Blue50,
                                                    border = BorderStroke(1.dp, Blue100)
                                                ) {
                                                    Text(
                                                        text = "পরিশোধ",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Blue600,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        val notePart = if (t.note.isNotBlank()) " • ${t.note}" else ""
                                        Text(
                                            text = "${t.accountName.ifBlank { "ক্যাশ" }} • ${fmtDate(t.date)}$notePart",
                                            fontSize = 12.sp,
                                            color = Slate500
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${if (isCredit) "+" else "-"} ${tk(t.amount)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCredit) Emerald600 else Red600
                                    )
                                    IconButton(
                                        onClick = { deleteTarget = t },
                                        modifier = Modifier.testTag("delete_txn_${t.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "মুছুন",
                                            tint = Slate300,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            if (index < txns.lastIndex) {
                                HorizontalDivider(color = Slate100)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }

    // Delete Confirmation Dialog
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = Color.White,
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Red50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = Red600)
                }
            },
            title = { Text("লেনদেনটি মুছবেন?", fontWeight = FontWeight.Bold, color = Slate900) },
            text = {
                Text(
                    text = "${target.category} • ${tk(target.amount)}\nঅ্যাকাউন্টের ব্যালেন্স সেই অনুযায়ী স্বয়ংক্রিয়ভাবে ঠিক হয়ে যাবে।",
                    fontSize = 13.sp,
                    color = Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTxn(target.id)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red600),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_txn_button")
                ) {
                    Text("মুছে ফেলুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("বাতিল", color = Slate600)
                }
            }
        )
    }

    if (showAdd) {
        AddTxnDialog(
            accounts = snapshot.accounts,
            expenseCats = expenseCats,
            incomeCats = incomeCats,
            onClose = { showAdd = false },
            onSave = { type, splits, cat, note, baki, date ->
                if (baki) {
                    onAddBakiExpense(splits.sumOf { it.amount }, cat, note, date)
                } else {
                    onAddTxns(type, splits, cat, note, date)
                }
                showAdd = false
            }
        )
    }

    if (showTransfer) {
        TransferDialog(
            accounts = snapshot.accounts,
            onClose = { showTransfer = false },
            onTransfer = onTransfer
        )
    }

    if (showCats) {
        CategoriesDialog(
            categories = snapshot.categories,
            onAdd = onAddCategory,
            onRename = onRenameCategory,
            onDelete = onDeleteCategory,
            onClose = { showCats = false }
        )
    }
}

@Composable
private fun SummaryMiniTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate200.copy(alpha = 0.8f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Slate400)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}
