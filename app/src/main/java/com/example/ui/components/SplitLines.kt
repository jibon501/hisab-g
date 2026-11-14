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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.tk
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Red500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800

data class SplitLineItem(
    val accountId: Long? = null,
    val amount: String = ""
)

@Composable
fun SplitLines(
    accounts: List<Account>,
    lines: List<SplitLineItem>,
    onChange: (List<SplitLineItem>) -> Unit,
    accountLabel: String = "অ্যাকাউন্ট",
    modifier: Modifier = Modifier
) {
    val totalAmount = lines.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        lines.forEachIndexed { idx, line ->
            val availableAccounts = accounts.filter { acc ->
                acc.id == line.accountId || lines.withIndex().none { (i, l) -> i != idx && l.accountId == acc.id }
            }
            val selectedAcc = accounts.find { it.id == line.accountId }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Slate50,
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Account Selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lines.size > 1) "$accountLabel (${idx + 1})" else accountLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate500,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        var expanded by remember(idx, line.accountId) { mutableStateOf(false) }

                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .border(1.dp, Slate300, RoundedCornerShape(10.dp))
                                    .clickable { expanded = true }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("split_account_selector_$idx"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (selectedAcc != null) {
                                    AccountAvatar(
                                        accountName = selectedAcc.name,
                                        accountType = selectedAcc.type,
                                        iconKey = selectedAcc.icon,
                                        size = AvatarSize.SM
                                    )
                                }
                                Text(
                                    text = selectedAcc?.let { "${it.name} (${tk(it.balance)})" }
                                        ?: "অ্যাকাউন্ট সিলেক্ট করুন",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedAcc != null) Slate800 else Slate400,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "অ্যাকাউন্ট বাছুন",
                                    tint = Slate400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("অ্যাকাউন্ট সিলেক্ট করুন", fontSize = 13.sp, color = Slate500) },
                                    onClick = {
                                        val next = lines.toMutableList()
                                        next[idx] = line.copy(accountId = null)
                                        onChange(next)
                                        expanded = false
                                    }
                                )
                                availableAccounts.forEach { acc ->
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
                                                Text(
                                                    text = "${acc.name} (${tk(acc.balance)})",
                                                    fontSize = 13.sp,
                                                    color = Slate800
                                                )
                                            }
                                        },
                                        onClick = {
                                            val next = lines.toMutableList()
                                            next[idx] = line.copy(accountId = acc.id)
                                            onChange(next)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Amount Field
                    Column(modifier = Modifier.width(116.dp)) {
                        Text(
                            text = "টাকা",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate500,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = line.amount,
                            onValueChange = { raw ->
                                val filtered = raw.filter { it.isDigit() || it == '.' }
                                val next = lines.toMutableList()
                                next[idx] = line.copy(amount = filtered)
                                onChange(next)
                            },
                            placeholder = { Text("০", color = Slate400, fontSize = 13.sp) },
                            prefix = { Text("৳ ", color = Slate400, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate300
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("split_amount_input_$idx")
                        )
                    }

                    if (lines.size > 1) {
                        IconButton(
                            onClick = { onChange(lines.filterIndexed { i, _ -> i != idx }) },
                            modifier = Modifier
                                .padding(top = 18.dp)
                                .size(48.dp)
                                .testTag("remove_split_line_$idx")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "লাইন বাদ দিন",
                                tint = Red500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { onChange(lines + SplitLineItem(accountId = null, amount = "")) },
                modifier = Modifier.testTag("add_split_line_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Emerald700,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "আরেকটি অ্যাকাউন্ট যোগ করুন",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Emerald700
                )
            }

            if (lines.size > 1) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate100,
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Text(
                        text = "মোট: ${tk(totalAmount)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate800,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
