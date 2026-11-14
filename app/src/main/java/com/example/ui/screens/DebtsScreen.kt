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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.data.model.SplitAmount
import com.example.data.model.fmtDate
import com.example.data.model.tk
import com.example.data.model.toBnDigits
import com.example.ui.components.HistoryShareModal
import com.example.ui.components.SplitLineItem
import com.example.ui.components.SplitLines
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber800
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Green600
import com.example.ui.theme.Red50
import com.example.ui.theme.Red500
import com.example.ui.theme.Red600
import com.example.ui.theme.Red700
import com.example.ui.theme.Rose600
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
import kotlin.math.roundToInt

@Composable
fun DebtsScreen(
    kind: DebtKind,
    snapshot: HisabSnapshot,
    getDebtHistory: (DebtKind, String) -> List<HistoryItem>,
    onAddDebt: (DebtKind, String, String, List<SplitAmount>) -> Unit,
    onSaveDebtEdit: (Debt, String, List<Pair<Long, Double>>) -> Unit,
    onDeleteDebt: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLent = kind == DebtKind.LENT
    val debtList = remember(snapshot.debts, kind) {
        snapshot.debts.filter { it.kind == kind }
    }

    var searchTerm by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    var editingDebt by remember { mutableStateOf<Debt?>(null) }
    var historyPerson by remember { mutableStateOf<String?>(null) }
    var deleteConfirm by remember { mutableStateOf<Debt?>(null) }

    val filteredDebts = remember(debtList, searchTerm) {
        if (searchTerm.isBlank()) debtList
        else debtList.filter {
            it.person.contains(searchTerm, ignoreCase = true) ||
                it.note.contains(searchTerm, ignoreCase = true)
        }
    }

    val totalRemaining = debtList.sumOf { it.remaining }
    val totalOriginal = debtList.sumOf { it.total }
    val totalPaid = (totalOriginal - totalRemaining).coerceAtLeast(0.0)

    val allKnownNames = remember(snapshot.debts) {
        snapshot.debts.map { it.person }.distinct()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Gradient Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isLent) listOf(Emerald700, Green600) else listOf(Red700, Rose600)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLent) {
                                        Icons.AutoMirrored.Filled.TrendingUp
                                    } else {
                                        Icons.AutoMirrored.Filled.TrendingDown
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isLent) "ধার দেওয়া (পাওনা হিসাব)" else "ধার নেওয়া (লোন হিসাব)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isLent) "অন্যের কাছে আপনার পাওনা টাকা" else "অন্যকে আপনার পরিশোধযোগ্য দেনা",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Button(
                            onClick = { showAdd = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Slate900
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("add_debt_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("নতুন যোগ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DebtHeaderStat("মোট হিসাব", tk(totalOriginal), Color.White, Modifier.weight(1f))
                        DebtHeaderStat("পরিশোধ হয়েছে", tk(totalPaid), Color.White, Modifier.weight(1f))
                        DebtHeaderStat("অবশিষ্ট বাকি", tk(totalRemaining), Color(0xFFFEF08A), Modifier.weight(1f))
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = { Text("নাম বা বিবরণ দিয়ে খুঁজুন...", fontSize = 13.sp, color = Slate400) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "খুঁজুন", tint = Slate400) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Emerald600,
                    unfocusedBorderColor = Slate200
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debt_search_input")
            )
        }

        // Empty State or Cards
        if (filteredDebts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("কোনো হিসাব পাওয়া যায়নি", fontSize = 14.sp, color = Slate400)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "উপরের \"নতুন যোগ\" বাটনে ক্লিক করে হিসাব খুলুন",
                            fontSize = 12.sp,
                            color = Slate400,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredDebts, key = { it.id }) { d ->
                val isDone = d.remaining <= 0.0
                val paidAmount = (d.total - d.remaining).coerceAtLeast(0.0)
                val percent = ((paidAmount / d.total.coerceAtLeast(1.0)) * 100.0).roundToInt().coerceIn(0, 100)

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Slate200),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debt_card_${d.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isDone || isLent) Emerald50 else Red50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = d.person.take(1),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDone || isLent) Emerald600 else Red600
                                    )
                                }
                                Column {
                                    Text(d.person, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                    val noteSuffix = if (d.note.isNotBlank()) " • ${d.note}" else ""
                                    Text(
                                        text = "${fmtDate(d.date)}$noteSuffix",
                                        fontSize = 12.sp,
                                        color = Slate500
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isDone) Emerald100 else Amber100
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isDone) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,
                                            contentDescription = null,
                                            tint = if (isDone) Emerald700 else Amber800,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = if (isDone) "পরিশোধিত ✓" else "বাকি ${tk(d.remaining)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDone) Emerald700 else Amber800
                                        )
                                    }
                                }
                                Text(
                                    text = "মোট: ${tk(d.total)}",
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            color = when {
                                isDone -> Emerald500
                                isLent -> Emerald600
                                else -> Red500
                            },
                            trackColor = Slate100,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "পরিশোধ: ${tk(paidAmount)} (${toBnDigits(percent.toString())}%)",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                            Text(
                                text = "অবশিষ্ট: ${tk(d.remaining)}",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = Slate100)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { historyPerson = d.person },
                                modifier = Modifier.testTag("debt_slip_button_${d.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    tint = Blue600,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("স্লিপ / রসিদ তৈরি", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Blue600)
                            }

                            Surface(
                                onClick = { editingDebt = d },
                                shape = RoundedCornerShape(10.dp),
                                color = Slate100,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("debt_edit_button_${d.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isDone) "এডিট" else "পরিশোধ ও এডিট",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate700
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }

    if (showAdd) {
        AddDebtModal(
            kind = kind,
            accounts = snapshot.accounts,
            suggestions = allKnownNames,
            onClose = { showAdd = false },
            onSave = { person, note, splits ->
                onAddDebt(kind, person, note, splits)
                showAdd = false
            }
        )
    }

    editingDebt?.let { debt ->
        EditDebtModal(
            debt = debt,
            accounts = snapshot.accounts,
            onClose = { editingDebt = null },
            onSave = { newName, splits ->
                onSaveDebtEdit(debt, newName, splits)
                editingDebt = null
            },
            onDelete = {
                deleteConfirm = debt
                editingDebt = null
            }
        )
    }

    deleteConfirm?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteConfirm = null },
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
            title = { Text("হিসাবটি মুছবেন?", fontWeight = FontWeight.Bold, color = Slate900) },
            text = {
                Text(
                    text = "\"${target.person}\"-এর হিসাব ও এর সমস্ত লেনদেন মুছে ফেলা হবে এবং অ্যাকাউন্টের ব্যালেন্স আগের অবস্থায় ফিরে যাবে।",
                    fontSize = 13.sp,
                    color = Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDebt(target.id)
                        deleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red600),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_debt_button")
                ) {
                    Text("মুছে ফেলুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirm = null }) {
                    Text("বাতিল", color = Slate600)
                }
            }
        )
    }

    historyPerson?.let { person ->
        HistoryShareModal(
            person = person,
            kind = kind,
            items = getDebtHistory(kind, person),
            onClose = { historyPerson = null }
        )
    }
}

@Composable
private fun DebtHeaderStat(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun AddDebtModal(
    kind: DebtKind,
    accounts: List<Account>,
    suggestions: List<String>,
    onClose: () -> Unit,
    onSave: (person: String, note: String, splits: List<SplitAmount>) -> Unit
) {
    val isLent = kind == DebtKind.LENT
    var person by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var lines by remember {
        mutableStateOf(listOf(SplitLineItem(accountId = accounts.firstOrNull()?.id, amount = "")))
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filteredSuggestions = remember(person, suggestions) {
        if (person.trim().isEmpty()) emptyList()
        else suggestions.filter {
            it.contains(person.trim(), ignoreCase = true) && !it.equals(person.trim(), ignoreCase = true)
        }.take(5)
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 640.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isLent) "নতুন ধার দেওয়া হিসাব" else "নতুন লোন / দেনা হিসাব",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Slate500)
                    }
                }
                HorizontalDivider(color = Slate200)

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Text(
                            text = if (isLent) "কাকে ধার দিলেন (ব্যক্তির নাম) *" else "কার কাছ থেকে নিলেন (ব্যক্তি / ব্যাংক) *",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = person,
                            onValueChange = {
                                person = it
                                errorMsg = null
                            },
                            placeholder = { Text("নাম লিখুন...", color = Slate400) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Slate400) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate300
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("debt_person_input")
                        )

                        if (filteredSuggestions.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Slate200),
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Column {
                                    filteredSuggestions.forEach { s ->
                                        Text(
                                            text = s,
                                            fontSize = 13.sp,
                                            color = Slate700,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { person = s }
                                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column {
                        Text(
                            text = if (isLent) "টাকা কোন অ্যাকাউন্ট থেকে গেল" else "টাকা কোন অ্যাকাউন্টে জমা হলো",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        SplitLines(
                            accounts = if (isLent) accounts.filter { it.balance > 0.0 } else accounts,
                            lines = lines,
                            onChange = {
                                lines = it
                                errorMsg = null
                            },
                            accountLabel = if (isLent) "টাকা প্রদানের অ্যাকাউন্ট" else "টাকা জমার অ্যাকাউন্ট"
                        )
                    }

                    Column {
                        Text(
                            text = "নোট বা কারণ (ঐচ্ছিক)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = { Text("যেমন: ৩ মাসের জন্য, জরুরি প্রয়োজন...", color = Slate400) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate300
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("debt_note_input")
                        )
                    }

                    if (errorMsg != null) {
                        Text(errorMsg!!, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Red600)
                    }
                }

                HorizontalDivider(color = Slate200)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onClose) {
                        Text("বাতিল", color = Slate600, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val p = person.trim()
                            if (p.isEmpty()) {
                                errorMsg = "অনুগ্রহ করে নাম দিন"
                                return@Button
                            }
                            val splits = lines.mapNotNull { l ->
                                val amt = l.amount.toDoubleOrNull() ?: 0.0
                                if (amt > 0.0) SplitAmount(l.accountId, amt) else null
                            }
                            if (splits.isEmpty()) {
                                errorMsg = "টাকার পরিমাণ দিন"
                                return@Button
                            }
                            if (splits.any { it.accountId == null }) {
                                errorMsg = "প্রতিটি টাকার জন্য অ্যাকাউন্ট সিলেক্ট করুন"
                                return@Button
                            }
                            onSave(p, note.trim(), splits)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("save_debt_button")
                    ) {
                        Text("সংরক্ষণ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditDebtModal(
    debt: Debt,
    accounts: List<Account>,
    onClose: () -> Unit,
    onSave: (newName: String, splits: List<Pair<Long, Double>>) -> Unit,
    onDelete: () -> Unit
) {
    val isLent = debt.kind == DebtKind.LENT
    val isDone = debt.remaining <= 0.0

    var name by remember(debt.id) { mutableStateOf(debt.person) }
    var lines by remember(debt.id) {
        val defaultId = accounts.find { it.balance > 0.0 }?.id ?: accounts.firstOrNull()?.id
        mutableStateOf(listOf(SplitLineItem(accountId = defaultId, amount = "")))
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onClose,
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isDone) "হিসাব সম্পন্ন (পরিশোধিত ✓)" else "বাকি: ${tk(debt.remaining)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                        Text("মূল পরিমাণ: ${tk(debt.total)}", fontSize = 12.sp, color = Slate500)
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Slate500)
                    }
                }
                HorizontalDivider(color = Slate200)

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Text("নাম", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                errorMsg = null
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate300
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_debt_name_input")
                        )
                    }

                    if (!isDone) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Slate50,
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isLent) "টাকা ফেরত পেলে (জমা হবে)" else "টাকা পরিশোধ করলে (কাটবে)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald800
                                    )
                                    Text(
                                        text = "সর্বোচ্চ: ${tk(debt.remaining)}",
                                        fontSize = 12.sp,
                                        color = Slate500
                                    )
                                }
                                SplitLines(
                                    accounts = if (isLent) accounts else accounts.filter { it.balance > 0.0 },
                                    lines = lines,
                                    onChange = {
                                        lines = it
                                        errorMsg = null
                                    },
                                    accountLabel = if (isLent) "টাকা জমার অ্যাকাউন্ট" else "টাকা কাটার অ্যাকাউন্ট"
                                )
                            }
                        }
                    }

                    if (errorMsg != null) {
                        Text(errorMsg!!, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Red600)
                    }
                }

                HorizontalDivider(color = Slate200)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_debt_button")
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = Red600, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("হিসাব মুছুন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Red600)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onClose) {
                            Text("বাতিল", color = Slate600, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.width(6.dp))
                        Button(
                            onClick = {
                                val n = name.trim()
                                if (n.isEmpty()) {
                                    errorMsg = "নাম খালি রাখা যাবে না"
                                    return@Button
                                }
                                val splits = lines.mapNotNull { l ->
                                    val amt = l.amount.toDoubleOrNull() ?: 0.0
                                    if (amt > 0.0) l.accountId to amt else null
                                }
                                val totalRepay = splits.sumOf { it.second }
                                if (totalRepay > debt.remaining + 0.001) {
                                    errorMsg = "বাকির চেয়ে বেশি পরিশোধ করা যাবে না"
                                    return@Button
                                }
                                if (splits.any { it.first == null }) {
                                    errorMsg = "প্রতিটি টাকার জন্য অ্যাকাউন্ট সিলেক্ট করুন"
                                    return@Button
                                }
                                onSave(n, splits.map { (id, amt) -> id!! to amt })
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("save_debt_edit_button")
                        ) {
                            Text("সংরক্ষণ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
