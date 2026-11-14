package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.ErrorOutline
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.tk
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber700
import com.example.ui.theme.Blue600
import com.example.ui.theme.Red50
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun TransferDialog(
    accounts: List<Account>,
    onClose: () -> Unit,
    onTransfer: (fromId: Long, toId: Long, amount: Double) -> Unit
) {
    var fromId by remember { mutableLongStateOf(accounts.getOrNull(0)?.id ?: 0L) }
    var toId by remember { mutableLongStateOf(accounts.getOrNull(1)?.id ?: accounts.getOrNull(0)?.id ?: 0L) }
    var amountText by remember { mutableStateOf("") }

    val fromAcc = accounts.find { it.id == fromId }
    val toAcc = accounts.find { it.id == toId }
    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val isOverBalance = fromAcc != null && parsedAmount > fromAcc.balance
    val isSameAccount = fromId == toId && fromId != 0L

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
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = Blue600,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "অ্যাকাউন্ট ট্রান্সফার",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                    }
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_transfer_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Slate600)
                    }
                }
                HorizontalDivider(color = Slate200)

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AccountPickerField(
                        label = "উৎস অ্যাকাউন্ট (টাকা কোথা থেকে কাটবে)",
                        accounts = accounts,
                        selectedAccount = fromAcc,
                        onSelect = { fromId = it.id },
                        testTag = "transfer_from_selector"
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Slate100,
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "দিকে",
                                tint = Blue600,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(18.dp)
                            )
                        }
                    }

                    AccountPickerField(
                        label = "গন্তব্য অ্যাকাউন্ট (টাকা কোথায় জমা হবে)",
                        accounts = accounts,
                        selectedAccount = toAcc,
                        onSelect = { toId = it.id },
                        testTag = "transfer_to_selector"
                    )

                    Column {
                        Text(
                            text = "ট্রান্সফারের পরিমাণ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { raw -> amountText = raw.filter { it.isDigit() || it == '.' } },
                            placeholder = { Text("০", color = Slate400) },
                            prefix = { Text("৳ ", color = Slate400, fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue600,
                                unfocusedBorderColor = Slate300
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transfer_amount_input")
                        )
                    }

                    if (isOverBalance && fromAcc != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Red50,
                            border = BorderStroke(1.dp, Red700.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Red700, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "নির্বাচিত অ্যাকাউন্টে রয়েছে ${tk(fromAcc.balance)}, যা প্রয়োজনের চেয়ে কম।",
                                    fontSize = 12.sp,
                                    color = Red700
                                )
                            }
                        }
                    }

                    if (isSameAccount) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Amber50,
                            border = BorderStroke(1.dp, Amber700.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Amber700, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "উৎস এবং গন্তব্য একই অ্যাকাউন্ট হতে পারবে না।",
                                    fontSize = 12.sp,
                                    color = Amber700
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Slate200)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onClose) {
                            Text("বাতিল", color = Slate600, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onTransfer(fromId, toId, parsedAmount)
                                onClose()
                            },
                            enabled = !isSameAccount && !isOverBalance && parsedAmount > 0.0 && fromId != 0L && toId != 0L,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("confirm_transfer_button")
                        ) {
                            Text("ট্রান্সফার সম্পন্ন করুন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountPickerField(
    label: String,
    accounts: List<Account>,
    selectedAccount: Account?,
    onSelect: (Account) -> Unit,
    testTag: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Slate700,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate50)
                    .border(1.dp, Slate300, RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag(testTag),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedAccount != null) {
                    AccountAvatar(
                        accountName = selectedAccount.name,
                        accountType = selectedAccount.type,
                        iconKey = selectedAccount.icon,
                        size = AvatarSize.SM
                    )
                }
                Text(
                    text = selectedAccount?.let { "${it.name} (${tk(it.balance)})" } ?: "অ্যাকাউন্ট বাছুন",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Slate400)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                accounts.forEach { acc ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AccountAvatar(
                                    accountName = acc.name,
                                    accountType = acc.type,
                                    iconKey = acc.icon,
                                    size = AvatarSize.SM
                                )
                                Text("${acc.name} (${tk(acc.balance)})", fontSize = 13.sp, color = Slate800)
                            }
                        },
                        onClick = {
                            onSelect(acc)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
