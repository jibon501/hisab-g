package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.SplitAmount
import com.example.data.model.TxnType
import com.example.data.model.fmtDate
import com.example.ui.theme.Amber200
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber600
import com.example.ui.theme.Amber700
import com.example.ui.theme.Amber900
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Red50
import com.example.ui.theme.Red600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTxnDialog(
    accounts: List<Account>,
    expenseCats: List<String>,
    incomeCats: List<String>,
    onClose: () -> Unit,
    onSave: (
        type: TxnType,
        splits: List<SplitAmount>,
        category: String,
        note: String,
        baki: Boolean,
        date: String
    ) -> Unit
) {
    var type by remember { mutableStateOf(TxnType.DEBIT) }
    var lines by remember { mutableStateOf(listOf(SplitLineItem(accountId = accounts.firstOrNull()?.id, amount = ""))) }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var baki by remember { mutableStateOf(false) }
    var bakiAmount by remember { mutableStateOf("") }
    val todayStr = remember { LocalDate.now().toString() }
    var dateStr by remember { mutableStateOf(todayStr) }
    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val activeCats = if (type == TxnType.DEBIT) expenseCats else incomeCats

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "নতুন লেনদেন যোগ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                    }
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_add_txn_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Slate600)
                    }
                }
                HorizontalDivider(color = Slate200)

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Type Toggle: খরচ (Debit) vs আয় (Credit)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate100)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = {
                                type = TxnType.DEBIT
                                category = ""
                                validationError = null
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (type == TxnType.DEBIT) Red600 else Color.Transparent,
                            shadowElevation = if (type == TxnType.DEBIT) 1.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("txn_type_debit")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowCircleUp,
                                    contentDescription = null,
                                    tint = if (type == TxnType.DEBIT) Color.White else Slate600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "খরচ (Debit)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (type == TxnType.DEBIT) Color.White else Slate600
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                type = TxnType.CREDIT
                                baki = false
                                category = ""
                                validationError = null
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (type == TxnType.CREDIT) Emerald600 else Color.Transparent,
                            shadowElevation = if (type == TxnType.CREDIT) 1.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("txn_type_credit")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowCircleDown,
                                    contentDescription = null,
                                    tint = if (type == TxnType.CREDIT) Color.White else Slate600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "আয় (Credit)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (type == TxnType.CREDIT) Color.White else Slate600
                                )
                            }
                        }
                    }

                    // Baki Toggle when Debit
                    if (type == TxnType.DEBIT) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Amber50,
                            border = BorderStroke(1.dp, Amber200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "দোকান বাকি (এখনো টাকা দিইনি)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Amber900
                                    )
                                    Text(
                                        text = "অ্যাকাউন্ট থেকে টাকা কাটবে না, তবে মাসের খরচে যোগ হবে",
                                        fontSize = 11.sp,
                                        color = Amber700
                                    )
                                }
                                Switch(
                                    checked = baki,
                                    onCheckedChange = {
                                        baki = it
                                        validationError = null
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Amber600
                                    ),
                                    modifier = Modifier.testTag("txn_baki_switch")
                                )
                            }
                        }
                    }

                    // Amount / Split Lines
                    if (type == TxnType.DEBIT && baki) {
                        Column {
                            Text(
                                text = "বাকির পরিমাণ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = bakiAmount,
                                onValueChange = { raw ->
                                    bakiAmount = raw.filter { it.isDigit() || it == '.' }
                                    validationError = null
                                },
                                placeholder = { Text("০.০০", color = Slate400) },
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
                                    .testTag("txn_baki_amount_input")
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = if (type == TxnType.DEBIT) {
                                    "টাকা কোন অ্যাকাউন্ট থেকে কাটবে"
                                } else {
                                    "টাকা কোন অ্যাকাউন্টে জমা হবে"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            SplitLines(
                                accounts = accounts,
                                lines = lines,
                                onChange = {
                                    lines = it
                                    validationError = null
                                },
                                accountLabel = if (type == TxnType.DEBIT) "অ্যাকাউন্ট" else "অ্যাকাউন্টে জমা"
                            )
                        }
                    }

                    // Category Selection
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "খাত / ক্যাটাগরি *",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                            if (category.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Emerald50
                                ) {
                                    Text(
                                        text = "নির্বাচিত: $category",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Emerald600,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = category,
                            onValueChange = {
                                category = it
                                validationError = null
                            },
                            placeholder = {
                                Text("খাতের নাম লিখুন বা নিচের থেকে বাছুন...", fontSize = 13.sp, color = Slate400)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate300
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("txn_category_input")
                        )

                        Spacer(Modifier.size(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeCats.forEach { cat ->
                                val selected = category == cat
                                Surface(
                                    onClick = {
                                        category = cat
                                        validationError = null
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) Slate800 else Slate50,
                                    border = BorderStroke(1.dp, if (selected) Slate800 else Slate200),
                                    modifier = Modifier.testTag("txn_category_chip_$cat")
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selected) Color.White else Slate700,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Note Input
                    Column {
                        Text(
                            text = "নোট বা বিবরণ (ঐচ্ছিক)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = {
                                Text(
                                    text = if (baki) "দোকানের নাম বা বিবরণ..." else "বিবরণ লিখুন...",
                                    fontSize = 13.sp,
                                    color = Slate400
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate300
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("txn_note_input")
                        )
                    }

                    // Date Picker Row
                    Column {
                        Text(
                            text = "তারিখ: ${fmtDate(dateStr)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { showDatePicker = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Slate300),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("txn_date_picker_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = "তারিখ নির্বাচন",
                                        tint = Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = fmtDate(dateStr),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate700
                                    )
                                }
                            }

                            Surface(
                                onClick = { dateStr = todayStr },
                                shape = RoundedCornerShape(12.dp),
                                color = Slate100,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("txn_today_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("আজকে", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                }
                            }
                        }
                    }

                    if (validationError != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Red50,
                            border = BorderStroke(1.dp, Red600.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = validationError!!,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Red600,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Slate200)

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onClose) {
                        Text("বাতিল", color = Slate600, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val catTrimmed = category.trim()
                            if (catTrimmed.isEmpty()) {
                                validationError = "অনুগ্রহ করে খাত দিন"
                                return@Button
                            }
                            if (type == TxnType.DEBIT && baki) {
                                val amt = bakiAmount.toDoubleOrNull() ?: 0.0
                                if (amt <= 0.0) {
                                    validationError = "সঠিক টাকার পরিমাণ দিন"
                                    return@Button
                                }
                                onSave(
                                    TxnType.DEBIT,
                                    listOf(SplitAmount(null, amt)),
                                    catTrimmed,
                                    note.trim(),
                                    true,
                                    dateStr
                                )
                            } else {
                                val validSplits = lines
                                    .mapNotNull { l ->
                                        val amt = l.amount.toDoubleOrNull() ?: 0.0
                                        if (amt > 0.0) SplitAmount(l.accountId, amt) else null
                                    }
                                if (validSplits.isEmpty()) {
                                    validationError = "টাকার পরিমাণ দিন"
                                    return@Button
                                }
                                onSave(type, validSplits, catTrimmed, note.trim(), false, dateStr)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("save_txn_button")
                    ) {
                        Text("সংরক্ষণ করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = remember(dateStr) {
            runCatching {
                LocalDate.parse(dateStr).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }.getOrElse { System.currentTimeMillis() }
        }
        val todayMillis = remember {
            LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() - 1
        }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            dateStr = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("ঠিক আছে", color = Emerald600, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("বাতিল", color = Slate600)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
