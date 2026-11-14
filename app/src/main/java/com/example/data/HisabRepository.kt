package com.example.data

import android.content.Context
import com.example.data.local.AccountEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.DebtEntity
import com.example.data.local.HisabDao
import com.example.data.local.TxnEntity
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryKind
import com.example.data.model.DEFAULT_EXPENSE_CATS
import com.example.data.model.DEFAULT_INCOME_CATS
import com.example.data.model.Debt
import com.example.data.model.DebtKind
import com.example.data.model.HistoryItem
import com.example.data.model.MonthItem
import com.example.data.model.OTHER_ACCOUNT
import com.example.data.model.SplitAmount
import com.example.data.model.Summary
import com.example.data.model.Txn
import com.example.data.model.TxnType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class HisabSnapshot(
    val accounts: List<Account> = emptyList(),
    val txns: List<Txn> = emptyList(),
    val debts: List<Debt> = emptyList(),
    val categories: List<Category> = emptyList(),
    val usedCategories: List<String> = emptyList(),
    val monthItems: List<MonthItem> = emptyList(),
    val summary: Summary = Summary()
)

class HisabRepository(
    private val context: Context,
    private val dao: HisabDao
) {
    private val prefs = context.getSharedPreferences("hisab_prefs", Context.MODE_PRIVATE)

    val snapshotFlow: Flow<HisabSnapshot> = combine(
        dao.observeAccounts(),
        dao.observeTxns(),
        dao.observeDebts(),
        dao.observeCategories()
    ) { accountEntities, txnEntities, debtEntities, categoryEntities ->
        buildSnapshot(accountEntities, txnEntities, debtEntities, categoryEntities)
    }

    suspend fun ensureSeededIfNeeded() {
        val alreadySeeded = prefs.getBoolean("initial_seeded_v1", false)
        if (!alreadySeeded && dao.accountCount() == 0) {
            seedDemoData()
            prefs.edit().putBoolean("initial_seeded_v1", true).apply()
        }
    }

    suspend fun resetToDefault() {
        seedDemoData()
        prefs.edit().putBoolean("initial_seeded_v1", true).apply()
    }

    private suspend fun seedDemoData() {
        val now = Instant.now()
        val currentIso = now.toString()
        val yesterdayIso = now.minus(1, ChronoUnit.DAYS).toString()
        val fiveDaysAgoIso = now.minus(5, ChronoUnit.DAYS).toString()

        val initialAccounts = listOf(
            AccountEntity(1, "ক্যাশ", "cash", 15000.0, "cash"),
            AccountEntity(2, "বিকাশ", "mobile", 8500.0, "phone"),
            AccountEntity(3, "নগদ", "mobile", 3200.0, "phone2"),
            AccountEntity(4, "AB Bank", "bank", 45000.0, "bank"),
            AccountEntity(5, "IFIC Bank", "bank", 28000.0, "bank2"),
            AccountEntity(6, "DPS ১", "dps", 50000.0, "savings"),
            AccountEntity(7, "DPS ২", "dps", 30000.0, "savings2"),
            AccountEntity(8, "জমি (ইনভেস্ট)", "asset", 500000.0, "land"),
            AccountEntity(9, OTHER_ACCOUNT, "asset", 0.0, "")
        )

        val initialCategories =
            DEFAULT_EXPENSE_CATS.mapIndexed { i, name ->
                CategoryEntity(id = i + 1L, name = name, kind = CategoryKind.EXPENSE.wire)
            } + DEFAULT_INCOME_CATS.mapIndexed { i, name ->
                CategoryEntity(id = i + 100L, name = name, kind = CategoryKind.INCOME.wire)
            }

        val initialDebts = listOf(
            DebtEntity(1, "lent", "রাকিব হাসান", 10000.0, "ব্যবসায়িক প্রয়োজনে ধার নিয়েছিল", fiveDaysAgoIso),
            DebtEntity(2, "lent", "তানভীর আহমেদ", 5000.0, "মেডিকেল বিলের জন্য ধার", yesterdayIso),
            DebtEntity(3, "loan", "চাচা", 25000.0, "জরুরি পারিবারিক লোন", fiveDaysAgoIso),
            DebtEntity(4, "shop", "আল-মদিনা স্টোর", 1450.0, "মাসিক মুদি বাজার বাকি", currentIso, isExpense = true, category = "বাজার")
        )

        val initialTxns = listOf(
            TxnEntity(1, "credit", 65000.0, 4, "বেতন", "অক্টোবর মাসের বেতন", fiveDaysAgoIso),
            TxnEntity(2, "debit", 15000.0, 4, "বাসা ভাড়া", "চলতি মাসের ফ্ল্যাট ভাড়া পরিশোধ", yesterdayIso),
            TxnEntity(3, "debit", 3200.0, 1, "বাজার", "কাঁচাবাজার ও মাছ", yesterdayIso),
            TxnEntity(4, "debit", 10000.0, 1, "ধার প্রদান", "রাকিব হাসান", fiveDaysAgoIso, debtId = 1, isPayment = false),
            TxnEntity(5, "credit", 4000.0, 2, "ধার ফেরত", "রাকিব হাসান", yesterdayIso, debtId = 1, isPayment = true),
            TxnEntity(6, "debit", 5000.0, 2, "ধার প্রদান", "তানভীর আহমেদ", yesterdayIso, debtId = 2, isPayment = false),
            TxnEntity(7, "credit", 25000.0, 4, "লোন গ্রহণ", "চাচা", fiveDaysAgoIso, debtId = 3, isPayment = false),
            TxnEntity(8, "debit", 5000.0, 1, "লোন পরিশোধ", "চাচা", currentIso, debtId = 3, isPayment = true),
            TxnEntity(9, "debit", 1250.0, 2, "বিল", "ইন্টারনেট বিল ও বিদ্যুৎ বিল", currentIso)
        )

        dao.replaceAllData(initialAccounts, initialTxns, initialDebts, initialCategories)
    }

    private fun buildSnapshot(
        accountEntities: List<AccountEntity>,
        txnEntities: List<TxnEntity>,
        debtEntities: List<DebtEntity>,
        categoryEntities: List<CategoryEntity>
    ): HisabSnapshot {
        val accNameMap = accountEntities.associate { it.id to it.name }

        val accounts = accountEntities.map { acc ->
            val credits = txnEntities.filter { it.accountId == acc.id && it.type == "credit" }.sumOf { it.amount }
            val debits = txnEntities.filter { it.accountId == acc.id && it.type == "debit" }.sumOf { it.amount }
            Account(
                id = acc.id,
                name = acc.name,
                type = acc.type,
                balance = acc.opening + credits - debits,
                opening = acc.opening,
                icon = acc.icon
            )
        }

        val debts = debtEntities.map { d ->
            val paid = txnEntities.filter { it.debtId == d.id && it.isPayment }.sumOf { it.amount }
            Debt(
                id = d.id,
                kind = DebtKind.fromWire(d.kind),
                person = d.person,
                total = d.total,
                remaining = (d.total - paid).coerceAtLeast(0.0),
                note = d.note,
                date = d.date,
                isExpense = d.isExpense,
                category = d.category
            )
        }

        val txns = txnEntities
            .map { t ->
                Txn(
                    id = t.id,
                    type = TxnType.fromWire(t.type),
                    amount = t.amount,
                    accountId = t.accountId,
                    accountName = accNameMap[t.accountId] ?: OTHER_ACCOUNT,
                    category = t.category,
                    note = t.note,
                    date = t.date,
                    debtId = t.debtId,
                    isPayment = t.isPayment,
                    ref = t.ref
                )
            }
            .sortedByDescending { it.date }

        val categories = categoryEntities.map { c ->
            Category(id = c.id, name = c.name, kind = CategoryKind.fromWire(c.kind))
        }

        val usedCategories = txns.mapNotNull { it.category.takeIf { c -> c.isNotBlank() } }.distinct()

        val currentYearMonth = LocalDate.now().toString().take(7)
        val monthItems = buildList {
            txns.filter { it.type == TxnType.DEBIT && it.date.startsWith(currentYearMonth) }.forEach { t ->
                add(
                    MonthItem(
                        id = t.id,
                        title = if (t.note.isNotBlank()) "${t.category} (${t.note})" else t.category,
                        amount = t.amount,
                        remaining = 0.0,
                        debtId = null,
                        date = t.date,
                        category = t.category
                    )
                )
            }
            debts.filter { it.kind == DebtKind.SHOP && it.date.startsWith(currentYearMonth) }.forEach { d ->
                add(
                    MonthItem(
                        id = 100000L + d.id,
                        title = if (d.note.isNotBlank()) "${d.person} - ${d.note}" else d.person,
                        amount = d.total,
                        remaining = d.remaining,
                        debtId = d.id,
                        date = d.date,
                        category = d.category.ifBlank { "দোকান বাকি" }
                    )
                )
            }
        }.sortedByDescending { it.date }

        val cashTypes = setOf("cash", "mobile", "bank")
        val cash = accounts.filter { it.type in cashTypes }.sumOf { it.balance }
        val dps = accounts.filter { it.type == "dps" }.sumOf { it.balance }
        val asset = accounts.filter { it.type !in cashTypes && it.type != "dps" }.sumOf { it.balance }
        val loan = debts.filter { it.kind == DebtKind.LOAN }.sumOf { it.remaining }
        val lent = debts.filter { it.kind == DebtKind.LENT }.sumOf { it.remaining }
        val shop = debts.filter { it.kind == DebtKind.SHOP }.sumOf { it.remaining }
        val monthExpense = monthItems.sumOf { it.amount }
        val monthBaki = monthItems.sumOf { it.remaining }

        return HisabSnapshot(
            accounts = accounts,
            txns = txns,
            debts = debts,
            categories = categories,
            usedCategories = usedCategories,
            monthItems = monthItems,
            summary = Summary(
                cash = cash,
                loan = loan,
                lent = lent,
                shop = shop,
                dps = dps,
                asset = asset,
                monthExpense = monthExpense,
                monthBaki = monthBaki
            )
        )
    }

    fun computeDebtHistory(
        snapshot: HisabSnapshot,
        kind: DebtKind,
        person: String
    ): List<HistoryItem> {
        val normalizedPerson = person.trim().lowercase()
        val matchingDebts = snapshot.debts.filter {
            it.kind == kind && it.person.trim().lowercase() == normalizedPerson
        }
        val debtIds = matchingDebts.map { it.id }.toSet()

        val items = mutableListOf<HistoryItem>()
        matchingDebts.forEach { d ->
            val label = when (d.kind) {
                DebtKind.SHOP -> d.category.ifBlank { "দোকান বাকি" }
                DebtKind.LOAN -> "লোন গ্রহণ"
                DebtKind.LENT -> "ধার প্রদান"
            }
            items.add(HistoryItem(date = d.date, label = label, amount = d.total, payment = false))
        }

        snapshot.txns
            .filter { it.debtId != null && it.debtId in debtIds && it.isPayment }
            .forEach { t ->
                items.add(
                    HistoryItem(
                        date = t.date,
                        label = t.category.ifBlank { if (kind == DebtKind.LENT) "ধার ফেরত" else "পরিশোধ" },
                        amount = t.amount,
                        payment = true
                    )
                )
            }

        return items.sortedBy { it.date }
    }

    // ---------------- Mutations ----------------

    suspend fun addAccount(name: String, type: String, opening: Double, icon: String) {
        dao.insertAccount(AccountEntity(name = name, type = type, opening = opening, icon = icon))
    }

    suspend fun updateAccount(id: Long, name: String) {
        dao.updateAccountName(id, name)
    }

    suspend fun deleteAccount(id: Long): Boolean {
        if (dao.txnCountForAccount(id) > 0) return false
        dao.deleteAccountById(id)
        return true
    }

    suspend fun addCategory(name: String, kind: CategoryKind) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        if (dao.findCategory(trimmed, kind.wire) != null) return
        dao.insertCategory(CategoryEntity(name = trimmed, kind = kind.wire))
    }

    suspend fun renameCategory(id: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        dao.renameCategoryAndTxns(id, trimmed)
    }

    suspend fun deleteCategory(id: Long) {
        dao.deleteCategoryById(id)
    }

    suspend fun addTxns(
        type: TxnType,
        splits: List<SplitAmount>,
        category: String,
        note: String,
        date: String
    ) {
        val accounts = dao.getAccountsOnce()
        val defaultAccId = accounts.find { it.name == OTHER_ACCOUNT }?.id
            ?: accounts.firstOrNull()?.id
            ?: 1L

        val toInsert = splits
            .filter { it.amount > 0.0 }
            .map { s ->
                TxnEntity(
                    type = type.wire,
                    amount = s.amount,
                    accountId = s.accountId ?: defaultAccId,
                    category = category,
                    note = note,
                    date = date
                )
            }
        if (toInsert.isNotEmpty()) {
            dao.insertTxns(toInsert)
        }
    }

    suspend fun addBakiExpense(
        amount: Double,
        category: String,
        note: String,
        date: String
    ) {
        val trimmedNote = note.trim()
        dao.insertDebt(
            DebtEntity(
                kind = DebtKind.SHOP.wire,
                person = trimmedNote.ifBlank { category },
                total = amount,
                note = trimmedNote,
                date = date,
                isExpense = true,
                category = category
            )
        )
    }

    suspend fun deleteTxn(id: Long) {
        dao.deleteTxnById(id)
    }

    suspend fun transfer(fromId: Long, toId: Long, amount: Double) {
        if (amount <= 0.0 || fromId == toId) return
        val accounts = dao.getAccountsOnce()
        val fromAcc = accounts.find { it.id == fromId } ?: return
        val toAcc = accounts.find { it.id == toId } ?: return
        val nowIso = Instant.now().toString()

        dao.insertTxns(
            listOf(
                TxnEntity(
                    type = TxnType.DEBIT.wire,
                    amount = amount,
                    accountId = fromId,
                    category = "ট্রান্সফার",
                    note = "→ ${toAcc.name}",
                    date = nowIso
                ),
                TxnEntity(
                    type = TxnType.CREDIT.wire,
                    amount = amount,
                    accountId = toId,
                    category = "ট্রান্সফার",
                    note = "${fromAcc.name} →",
                    date = nowIso
                )
            )
        )
    }

    suspend fun addDebt(
        kind: DebtKind,
        person: String,
        note: String,
        splits: List<SplitAmount>
    ) {
        val totalAmount = splits.sumOf { it.amount }
        if (totalAmount <= 0.0) return
        val nowIso = Instant.now().toString()

        val debtId = dao.insertDebt(
            DebtEntity(
                kind = kind.wire,
                person = person,
                total = totalAmount,
                note = note,
                date = nowIso
            )
        )

        if (kind != DebtKind.SHOP) {
            val txns = splits
                .filter { it.accountId != null && it.amount > 0.0 }
                .map { s ->
                    TxnEntity(
                        type = if (kind == DebtKind.LOAN) TxnType.CREDIT.wire else TxnType.DEBIT.wire,
                        amount = s.amount,
                        accountId = s.accountId!!,
                        category = if (kind == DebtKind.LOAN) "লোন গ্রহণ" else "ধার প্রদান",
                        note = person,
                        date = nowIso,
                        debtId = debtId,
                        isPayment = false
                    )
                }
            if (txns.isNotEmpty()) {
                dao.insertTxns(txns)
            }
        }
    }

    suspend fun payDebt(debtId: Long, amount: Double, accountId: Long, debtKind: DebtKind, person: String) {
        if (amount <= 0.0) return
        val nowIso = Instant.now().toString()
        val category = when (debtKind) {
            DebtKind.LENT -> "ধার ফেরত"
            DebtKind.LOAN -> "লোন পরিশোধ"
            DebtKind.SHOP -> "দোকান বাকি পরিশোধ"
        }
        dao.insertTxn(
            TxnEntity(
                type = if (debtKind == DebtKind.LENT) TxnType.CREDIT.wire else TxnType.DEBIT.wire,
                amount = amount,
                accountId = accountId,
                category = category,
                note = person,
                date = nowIso,
                debtId = debtId,
                isPayment = true
            )
        )
    }

    suspend fun saveDebtEdit(
        debt: Debt,
        newName: String,
        splits: List<Pair<Long, Double>>
    ) {
        val trimmed = newName.trim()
        val finalName = if (trimmed.isNotEmpty() && trimmed != debt.person) {
            dao.updateDebtPerson(debt.id, trimmed)
            dao.updateTxnNotesForDebt(debt.id, trimmed)
            trimmed
        } else {
            debt.person
        }

        splits.forEach { (accountId, amount) ->
            if (amount > 0.0) {
                payDebt(debt.id, amount, accountId, debt.kind, finalName)
            }
        }
    }

    suspend fun deleteDebt(debtId: Long) {
        dao.deleteDebtAndTxns(debtId)
    }

    suspend fun exportXlsx(outputStream: OutputStream) {
        val accounts = dao.getAccountsOnce()
        val txns = dao.getTxnsOnce()
        val debts = dao.getDebtsOnce()
        val categories = dao.getCategoriesOnce()
        XlsxCodec.writeWorkbook(outputStream, accounts, txns, debts, categories)
    }

    suspend fun importXlsx(inputStream: InputStream): Boolean {
        return try {
            val wb = XlsxCodec.readWorkbook(inputStream)
            dao.replaceAllData(wb.accounts, wb.txns, wb.debts, wb.categories)
            prefs.edit().putBoolean("initial_seeded_v1", true).apply()
            true
        } catch (_: Exception) {
            false
        }
    }
}
