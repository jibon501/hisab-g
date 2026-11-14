package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val opening: Double,
    val icon: String
)

@Entity(tableName = "transactions")
data class TxnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Double,
    val accountId: Long,
    val category: String,
    val note: String,
    val date: String,
    val debtId: Long? = null,
    val isPayment: Boolean = false,
    val ref: String? = null
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val person: String,
    val total: Double,
    val note: String,
    val date: String,
    val isExpense: Boolean = false,
    val category: String = ""
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: String
)
