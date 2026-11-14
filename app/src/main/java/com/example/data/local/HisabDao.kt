package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HisabDao {

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun observeAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun observeTxns(): Flow<List<TxnEntity>>

    @Query("SELECT * FROM debts ORDER BY id ASC")
    fun observeDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun accountCount(): Int

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    suspend fun getAccountsOnce(): List<AccountEntity>

    @Query("SELECT * FROM transactions ORDER BY id ASC")
    suspend fun getTxnsOnce(): List<TxnEntity>

    @Query("SELECT * FROM debts ORDER BY id ASC")
    suspend fun getDebtsOnce(): List<DebtEntity>

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getCategoriesOnce(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun txnCountForAccount(accountId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Query("UPDATE accounts SET name = :name WHERE id = :id")
    suspend fun updateAccountName(id: Long, name: String)

    @Query("UPDATE accounts SET name = :name, icon = :icon WHERE id = :id")
    suspend fun updateAccount(id: Long, name: String, icon: String)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTxn(txn: TxnEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTxns(txns: List<TxnEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTxnById(id: Long)

    @Query("DELETE FROM transactions WHERE debtId = :debtId")
    suspend fun deleteTxnsByDebtId(debtId: Long)

    @Query("UPDATE transactions SET note = :person WHERE debtId = :debtId")
    suspend fun updateTxnNotesForDebt(debtId: Long, person: String)

    @Query("UPDATE transactions SET category = :newCategory WHERE category = :oldCategory")
    suspend fun renameTxnCategory(oldCategory: String, newCategory: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebts(debts: List<DebtEntity>)

    @Query("UPDATE debts SET person = :person WHERE id = :id")
    suspend fun updateDebtPerson(id: Long, person: String)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebtById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE LOWER(name) = LOWER(:name) AND kind = :kind LIMIT 1")
    suspend fun findCategory(name: String, kind: String): CategoryEntity?

    @Query("UPDATE categories SET name = :newName WHERE id = :id")
    suspend fun updateCategoryName(id: Long, newName: String)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)

    @Query("DELETE FROM accounts")
    suspend fun clearAccounts()

    @Query("DELETE FROM transactions")
    suspend fun clearTxns()

    @Query("DELETE FROM debts")
    suspend fun clearDebts()

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Transaction
    suspend fun replaceAllData(
        accounts: List<AccountEntity>,
        txns: List<TxnEntity>,
        debts: List<DebtEntity>,
        categories: List<CategoryEntity>?
    ) {
        clearTxns()
        clearDebts()
        clearAccounts()
        insertAccounts(accounts)
        insertDebts(debts)
        insertTxns(txns)
        if (!categories.isNullOrEmpty()) {
            clearCategories()
            insertCategories(categories)
        }
    }

    @Transaction
    suspend fun deleteDebtAndTxns(debtId: Long) {
        deleteTxnsByDebtId(debtId)
        deleteDebtById(debtId)
    }

    @Transaction
    suspend fun renameCategoryAndTxns(id: Long, newName: String) {
        val cat = getCategoryById(id) ?: return
        val oldName = cat.name
        updateCategoryName(id, newName)
        renameTxnCategory(oldName, newName)
    }
}
