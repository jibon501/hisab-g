package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalOffer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Category
import com.example.data.model.CategoryKind
import com.example.ui.theme.Blue600
import com.example.ui.theme.Emerald200
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
import com.example.ui.theme.Slate800

@Composable
fun CategoriesDialog(
    categories: List<Category>,
    onAdd: (String, CategoryKind) -> Unit,
    onRename: (Long, String) -> Unit,
    onDelete: (Long) -> Unit,
    onClose: () -> Unit
) {
    var activeTab by remember { mutableStateOf(CategoryKind.EXPENSE) }
    var newCatName by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var editName by remember { mutableStateOf("") }

    val filteredCategories = categories.filter { it.kind == activeTab }
    val expenseCount = categories.count { it.kind == CategoryKind.EXPENSE }
    val incomeCount = categories.count { it.kind == CategoryKind.INCOME }

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
                .heightIn(max = 600.dp)
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
                            imageVector = Icons.Outlined.LocalOffer,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "খাত ব্যবস্থাপনা",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_categories_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Slate600)
                    }
                }
                HorizontalDivider(color = Slate200)

                // Tab Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { activeTab = CategoryKind.EXPENSE },
                        shape = RoundedCornerShape(12.dp),
                        color = if (activeTab == CategoryKind.EXPENSE) Red50 else Color.Transparent,
                        border = if (activeTab == CategoryKind.EXPENSE) BorderStroke(1.dp, Red600.copy(alpha = 0.3f)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("category_tab_expense")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "খরচের খাত ($expenseCount)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (activeTab == CategoryKind.EXPENSE) Red600 else Slate600
                            )
                        }
                    }

                    Surface(
                        onClick = { activeTab = CategoryKind.INCOME },
                        shape = RoundedCornerShape(12.dp),
                        color = if (activeTab == CategoryKind.INCOME) Emerald50 else Color.Transparent,
                        border = if (activeTab == CategoryKind.INCOME) BorderStroke(1.dp, Emerald200) else null,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("category_tab_income")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "আয়ের খাত ($incomeCount)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (activeTab == CategoryKind.INCOME) Emerald600 else Slate600
                            )
                        }
                    }
                }

                HorizontalDivider(color = Slate100)

                // Add form
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        placeholder = {
                            Text(
                                text = if (activeTab == CategoryKind.EXPENSE) "নতুন খরচের খাত..." else "নতুন আয়ের খাত...",
                                fontSize = 13.sp,
                                color = Slate400
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Emerald600,
                            unfocusedBorderColor = Slate300
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_category_input")
                    )
                    Button(
                        onClick = {
                            val trimmed = newCatName.trim()
                            if (trimmed.isNotEmpty()) {
                                onAdd(trimmed, activeTab)
                                newCatName = ""
                            }
                        },
                        enabled = newCatName.trim().isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("add_category_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("যোগ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                HorizontalDivider(color = Slate200)

                // List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (filteredCategories.isEmpty()) {
                        item {
                            Text(
                                text = "কোনো খাত যোগ করা হয়নি",
                                fontSize = 13.sp,
                                color = Slate400,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                            )
                        }
                    } else {
                        items(filteredCategories, key = { it.id }) { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (editingId == cat.id) {
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Emerald600,
                                            unfocusedBorderColor = Emerald600
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("edit_category_input_${cat.id}")
                                    )
                                    IconButton(
                                        onClick = {
                                            if (editName.trim().isNotEmpty()) {
                                                onRename(cat.id, editName.trim())
                                                editingId = null
                                            }
                                        },
                                        modifier = Modifier.testTag("save_category_edit_${cat.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "সংরক্ষণ", tint = Emerald600)
                                    }
                                    IconButton(onClick = { editingId = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "বাতিল", tint = Slate400)
                                    }
                                } else {
                                    Text(
                                        text = cat.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate800,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingId = cat.id
                                                editName = cat.name
                                            },
                                            modifier = Modifier.testTag("rename_category_${cat.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = "নাম পরিবর্তন",
                                                tint = Blue600,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDelete(cat.id) },
                                            modifier = Modifier.testTag("delete_category_${cat.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "মুছুন",
                                                tint = Red600,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = Slate100)
                        }
                    }
                }

                // Footer
                HorizontalDivider(color = Slate200)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onClose) {
                        Text("বন্ধ করুন", color = Slate600, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
