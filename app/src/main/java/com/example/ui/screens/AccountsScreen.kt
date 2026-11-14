package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.HisabSnapshot
import com.example.data.bankSuggestions
import com.example.data.drawableResForLogo
import com.example.data.model.Account
import com.example.data.model.TYPE_LABELS
import com.example.data.model.tk
import com.example.data.model.typeLabel
import com.example.ui.components.AccountAvatar
import com.example.ui.components.AvatarSize
import com.example.ui.components.defaultIconKey
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald800
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
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountsScreen(
    snapshot: HisabSnapshot,
    statusMessage: String?,
    errorMessage: String?,
    onClearMessages: () -> Unit,
    onAddAccount: (name: String, type: String, opening: Double, icon: String) -> Unit,
    onUpdateAccount: (id: Long, name: String) -> Unit,
    onDeleteAccount: (id: Long, name: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onExportXlsx: (Uri) -> Unit,
    onImportXlsx: (Uri) -> Unit,
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts = snapshot.accounts

    var showAdd by remember { mutableStateOf(false) }
    var editingAcc by remember { mutableStateOf<Account?>(null) }
    var deleteConfirm by remember { mutableStateOf<Account?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage, errorMessage) {
        if (statusMessage != null || errorMessage != null) {
            delay(3500)
            onClearMessages()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    ) { uri ->
        if (uri != null) onExportXlsx(uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onImportXlsx(uri)
    }

    val customTypes = remember(accounts) {
        accounts.map { it.type }.filter { it !in TYPE_LABELS }.distinct()
    }

    val typeOrder = remember(customTypes) {
        listOf("cash", "mobile", "bank", "dps", "asset") + customTypes + listOf("other")
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Excel Export / Import Bar
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Slate200),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TableChart,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "এক্সেল ব্যাকআপ ও রিস্টোর",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                            Text(
                                text = "সমস্ত ডেটা .xlsx ফাইলে সংরক্ষণ বা রিস্টোর করুন",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { exportLauncher.launch("hisab-backup-${LocalDate.now()}.xlsx") },
                            shape = RoundedCornerShape(12.dp),
                            color = Slate100,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("export_xlsx_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Outlined.Download, contentDescription = null, tint = Slate700, modifier = Modifier.size(16.dp))
                                Text("এক্সপোর্ট (.xlsx)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                            }
                        }

                        Surface(
                            onClick = {
                                importLauncher.launch(
                                    arrayOf(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "application/octet-stream",
                                        "*/*"
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Emerald50,
                            border = BorderStroke(1.dp, Emerald200),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("import_xlsx_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Outlined.Upload, contentDescription = null, tint = Emerald800, modifier = Modifier.size(16.dp))
                                Text("ইম্পোর্ট (.xlsx)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Emerald800)
                            }
                        }

                        Button(
                            onClick = { showAdd = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("add_account_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("নতুন অ্যাকাউন্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (statusMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Emerald50,
                    border = BorderStroke(1.dp, Emerald200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Emerald800, modifier = Modifier.size(18.dp))
                        Text(statusMessage, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Emerald800)
                    }
                }
            }
        }

        if (errorMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Red50,
                    border = BorderStroke(1.dp, Red600.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = Red600, modifier = Modifier.size(18.dp))
                        Text(errorMessage, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Red600)
                    }
                }
            }
        }

        // Grouped Account List
        items(typeOrder, key = { it }) { type ->
            val group = accounts.filter { it.type == type }
            if (group.isNotEmpty()) {
                val groupTotal = group.sumOf { it.balance }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Slate200),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate50.copy(alpha = 0.85f))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = typeLabel(type),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald800
                            )
                            Text(
                                text = "মোট: ${tk(groupTotal)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate600
                            )
                        }
                        HorizontalDivider(color = Slate100)

                        group.forEachIndexed { idx, acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editingAcc = acc }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("account_row_${acc.id}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AccountAvatar(acc.name, acc.type, acc.icon, AvatarSize.MD)
                                    Column {
                                        Text(acc.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                        Text(
                                            text = "${typeLabel(acc.type)} • শুরুর ব্যালেন্স ছিল ${tk(acc.opening)}",
                                            fontSize = 11.sp,
                                            color = Slate400
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(tk(acc.balance), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "এডিট",
                                        tint = Slate300,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (idx < group.lastIndex) {
                                HorizontalDivider(color = Slate100)
                            }
                        }
                    }
                }
            }
        }

        // Reset Demo Data Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.testTag("reset_demo_button")
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = Slate400, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("প্রাথমিক ডেমো ডেটায় রিসেট করুন", fontSize = 12.sp, color = Slate400)
                }
            }
        }
    }

    if (showAdd) {
        AddAccountModal(
            customTypes = customTypes,
            onClose = { showAdd = false },
            onSave = { name, type, opening, icon ->
                onAddAccount(name, type, opening, icon)
                showAdd = false
            }
        )
    }

    editingAcc?.let { acc ->
        EditAccountModal(
            account = acc,
            onClose = { editingAcc = null },
            onSave = { newName ->
                onUpdateAccount(acc.id, newName)
                editingAcc = null
            },
            onDelete = {
                deleteConfirm = acc
                deleteError = null
                editingAcc = null
            }
        )
    }

    deleteConfirm?.let { target ->
        AlertDialog(
            onDismissRequest = {
                deleteConfirm = null
                deleteError = null
            },
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
            title = { Text("\"${target.name}\" মুছবেন?", fontWeight = FontWeight.Bold, color = Slate900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "যে অ্যাকাউন্টে কোনো লেনদেন নেই কেবল সেটাই মোছা যাবে।",
                        fontSize = 13.sp,
                        color = Slate600
                    )
                    if (deleteError != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Red50,
                            border = BorderStroke(1.dp, Red600.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = deleteError!!,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Red600,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAccount(target.id, target.name) { ok, err ->
                            if (ok) {
                                deleteConfirm = null
                                deleteError = null
                            } else {
                                deleteError = err
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red600),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_account_button")
                ) {
                    Text("মুছে ফেলুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deleteConfirm = null
                        deleteError = null
                    }
                ) {
                    Text("বাতিল", color = Slate600)
                }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = Color.White,
            title = { Text("সবকিছু রিসেট করবেন?", fontWeight = FontWeight.Bold, color = Slate900) },
            text = {
                Text(
                    text = "বর্তমান সব হিসাব ও লেনদেন মুছে প্রাথমিক নমুনা ডেটা বসবে।",
                    fontSize = 13.sp,
                    color = Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetToDefault()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red600),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_reset_demo_button")
                ) {
                    Text("রিসেট করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("বাতিল", color = Slate600)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddAccountModal(
    customTypes: List<String>,
    onClose: () -> Unit,
    onSave: (name: String, type: String, opening: Double, icon: String) -> Unit
) {
    var type by remember { mutableStateOf("bank") }
    var newTypeName by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var opening by remember { mutableStateOf("") }
    var typeDropdownOpen by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val typeOptions = remember(customTypes) {
        listOf(
            "bank" to "ব্যাংক",
            "mobile" to "মোবাইল ব্যাংকিং (বিকাশ/নগদ ইত্যাদি)",
            "cash" to "নগদ ক্যাশ",
            "dps" to "ডিপিএস সঞ্চয়",
            "asset" to "স্থাবর সম্পদ / বিনিয়োগ"
        ) + customTypes.map { it to typeLabel(it) } + listOf("__new_type__" to "+ নতুন ধরন যোগ করুন")
    }

    val suggestions = remember(type, name) { bankSuggestions(type, name) }

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
                    Text("নতুন অ্যাকাউন্ট যোগ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Slate800)
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
                    // Type selector
                    Column {
                        Text("অ্যাকাউন্টের ধরন", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                        Spacer(Modifier.height(4.dp))
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate50)
                                    .border(1.dp, Slate300, RoundedCornerShape(12.dp))
                                    .clickable { typeDropdownOpen = true }
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                                    .testTag("account_type_selector"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = typeOptions.find { it.first == type }?.second ?: type,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate800
                                )
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Slate400)
                            }
                            DropdownMenu(
                                expanded = typeDropdownOpen,
                                onDismissRequest = { typeDropdownOpen = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                typeOptions.forEach { (key, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label, fontSize = 13.sp, color = Slate800) },
                                        onClick = {
                                            type = key
                                            typeDropdownOpen = false
                                            errorMsg = null
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (type == "__new_type__") {
                        Column {
                            Text("নতুন ধরনের নাম", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = newTypeName,
                                onValueChange = {
                                    newTypeName = it
                                    errorMsg = null
                                },
                                placeholder = { Text("যেমন: শেয়ার মার্কেট, ক্রিপ্টো...", color = Slate400) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Emerald600,
                                    unfocusedBorderColor = Slate300
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_account_type_input")
                            )
                        }
                    }

                    // Name + Bank/MFS Suggestions
                    Column {
                        Text("অ্যাকাউন্টের নাম *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                errorMsg = null
                            },
                            placeholder = {
                                Text(
                                    text = when (type) {
                                        "bank" -> "ব্যাংকের নাম (যেমন: ডাচ-বাংলা, ব্র্যাক, সোনালী)..."
                                        "mobile" -> "মোবাইলের নাম (যেমন: বিকাশ, নগদ, রকেট)..."
                                        else -> "অ্যাকাউন্টের নাম লিখুন..."
                                    },
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
                                .testTag("account_name_input")
                        )

                        if (suggestions.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Slate50,
                                border = BorderStroke(1.dp, Slate200),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "সাজেশন (ক্লিক করুন):",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate400,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        suggestions.forEach { b ->
                                            val logoRes = drawableResForLogo(b.logo)
                                            Surface(
                                                onClick = { name = b.bn },
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color.White,
                                                border = BorderStroke(1.dp, Slate200)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (logoRes != null) {
                                                        Image(
                                                            painter = painterResource(logoRes),
                                                            contentDescription = b.en,
                                                            contentScale = ContentScale.Fit,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Text(b.bn, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate800)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Opening Balance
                    Column {
                        Text("শুরুর ব্যালেন্স (ঐচ্ছিক)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = opening,
                            onValueChange = { raw -> opening = raw.filter { it.isDigit() || it == '.' } },
                            placeholder = { Text("০", color = Slate400) },
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
                                .testTag("account_opening_input")
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
                            val finalType = if (type == "__new_type__") newTypeName.trim() else type
                            if (type == "__new_type__" && finalType.isEmpty()) {
                                errorMsg = "নতুন ধরনের নাম দিন"
                                return@Button
                            }
                            if (name.trim().isEmpty()) {
                                errorMsg = "অ্যাকাউন্টের নাম দিন"
                                return@Button
                            }
                            val op = opening.toDoubleOrNull() ?: 0.0
                            onSave(name.trim(), finalType, op, defaultIconKey(finalType))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("save_new_account_button")
                    ) {
                        Text("অ্যাকাউন্ট যোগ করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditAccountModal(
    account: Account,
    onClose: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember(account.id) { mutableStateOf(account.name) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AccountAvatar(account.name, account.type, account.icon, AvatarSize.MD)
                        Column {
                            Text("অ্যাকাউন্ট এডিট", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text("ব্যালেন্স: ${tk(account.balance)}", fontSize = 12.sp, color = Slate500)
                        }
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Slate400)
                    }
                }
                HorizontalDivider(color = Slate100)

                Column {
                    Text("অ্যাকাউন্টের নাম", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
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
                            .testTag("edit_account_name_input")
                    )
                }

                if (errorMsg != null) {
                    Text(errorMsg!!, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Red600)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_account_button")
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = Red600, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("মুছুন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Red600)
                    }

                    Row {
                        TextButton(onClick = onClose) {
                            Text("বাতিল", color = Slate600, fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (name.trim().isEmpty()) {
                                    errorMsg = "নাম খালি রাখা যাবে না"
                                } else {
                                    onSave(name.trim())
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            modifier = Modifier.testTag("save_edit_account_button")
                        ) {
                            Text("সংরক্ষণ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
