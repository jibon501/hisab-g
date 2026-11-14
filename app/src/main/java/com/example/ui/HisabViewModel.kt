package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HisabApplication
import com.example.data.HisabSnapshot
import com.example.data.model.CategoryKind
import com.example.data.model.Debt
import com.example.data.model.DebtKind
import com.example.data.model.HistoryItem
import com.example.data.model.SplitAmount
import com.example.data.model.Txn
import com.example.data.model.TxnType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HisabUiState(
    val snapshot: HisabSnapshot = HisabSnapshot(),
    val categoryFilter: String? = null,
    val filteredTxns: List<Txn> = emptyList(),
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val celebrateTick: Int = 0
)

class HisabViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as HisabApplication).repository

    private val filterFlow = MutableStateFlow<String?>(null)
    private val statusFlow = MutableStateFlow<String?>(null)
    private val errorFlow = MutableStateFlow<String?>(null)
    private val celebrateFlow = MutableStateFlow(0)

    val uiState: StateFlow<HisabUiState> = combine(
        repo.snapshotFlow,
        filterFlow,
        statusFlow,
        errorFlow,
        celebrateFlow
    ) { snapshot, filter, status, error, celebrate ->
        val filtered = if (filter == null) {
            snapshot.txns
        } else {
            snapshot.txns.filter { it.category == filter }
        }
        HisabUiState(
            snapshot = snapshot,
            categoryFilter = filter,
            filteredTxns = filtered,
            statusMessage = status,
            errorMessage = error,
            celebrateTick = celebrate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HisabUiState()
    )

    init {
        viewModelScope.launch {
            repo.ensureSeededIfNeeded()
        }
    }

    fun applyFilter(category: String?) {
        filterFlow.value = category
    }

    fun clearMessages() {
        statusFlow.value = null
        errorFlow.value = null
    }

    fun addTxns(
        type: TxnType,
        splits: List<SplitAmount>,
        category: String,
        note: String,
        date: String
    ) {
        viewModelScope.launch {
            repo.addCategory(
                category,
                if (type == TxnType.DEBIT) CategoryKind.EXPENSE else CategoryKind.INCOME
            )
            repo.addTxns(type, splits, category, note, date)
        }
    }

    fun addBakiExpense(
        amount: Double,
        category: String,
        note: String,
        date: String
    ) {
        viewModelScope.launch {
            repo.addCategory(category, CategoryKind.EXPENSE)
            repo.addBakiExpense(amount, category, note, date)
        }
    }

    fun deleteTxn(id: Long) {
        viewModelScope.launch {
            repo.deleteTxn(id)
        }
    }

    fun transfer(fromId: Long, toId: Long, amount: Double) {
        viewModelScope.launch {
            repo.transfer(fromId, toId, amount)
        }
    }

    fun addDebt(
        kind: DebtKind,
        person: String,
        note: String,
        splits: List<SplitAmount>
    ) {
        viewModelScope.launch {
            repo.addDebt(kind, person, note, splits)
        }
    }

    fun payDebt(debt: Debt, amount: Double, accountId: Long) {
        viewModelScope.launch {
            repo.payDebt(debt.id, amount, accountId, debt.kind, debt.person)
        }
    }

    fun saveDebtEdit(
        debt: Debt,
        newName: String,
        splits: List<Pair<Long, Double>>
    ) {
        viewModelScope.launch {
            repo.saveDebtEdit(debt, newName, splits)
            val addedPay = splits.sumOf { it.second }
            if (addedPay >= debt.remaining && debt.remaining > 0.0) {
                celebrateFlow.value = celebrateFlow.value + 1
            }
        }
    }

    fun deleteDebt(debtId: Long) {
        viewModelScope.launch {
            repo.deleteDebt(debtId)
        }
    }

    fun getDebtHistory(kind: DebtKind, person: String): List<HistoryItem> {
        return repo.computeDebtHistory(uiState.value.snapshot, kind, person)
    }

    fun addAccount(name: String, type: String, opening: Double, icon: String) {
        viewModelScope.launch {
            repo.addAccount(name, type, opening, icon)
        }
    }

    fun updateAccount(id: Long, name: String) {
        viewModelScope.launch {
            repo.updateAccount(id, name)
        }
    }

    fun deleteAccount(id: Long, accountName: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val ok = repo.deleteAccount(id)
            if (ok) {
                onResult(true, null)
            } else {
                onResult(false, "\"$accountName\" অ্যাকাউন্টে লেনদেন বিদ্যমান রয়েছে, তাই এটি মোছা যাবে না।")
            }
        }
    }

    fun addCategory(name: String, kind: CategoryKind) {
        viewModelScope.launch {
            repo.addCategory(name, kind)
        }
    }

    fun renameCategory(id: Long, newName: String) {
        viewModelScope.launch {
            repo.renameCategory(id, newName)
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            repo.deleteCategory(id)
        }
    }

    fun exportXlsxToUri(uri: Uri) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                        repo.exportXlsx(out)
                    } ?: error("Output stream unavailable")
                }
                statusFlow.value = "এক্সেল ব্যাকআপ সফলভাবে সংরক্ষণ করা হয়েছে!"
            } catch (_: Exception) {
                errorFlow.value = "এক্সপোর্ট করা গেল না"
            }
        }
    }

    fun importXlsxFromUri(uri: Uri) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                    repo.importXlsx(input)
                } ?: false
            }
            if (ok) {
                statusFlow.value = "ডেটা সফলভাবে ইম্পোর্ট করা হয়েছে!"
            } else {
                errorFlow.value = "ইম্পোর্ট সফল হয়নি। ফাইলটি সঠিক Hisab এক্সেল ব্যাকআপ নয়।"
            }
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            repo.resetToDefault()
            statusFlow.value = "প্রাথমিক ডেমো ডেটায় রিসেট হয়েছে"
        }
    }
}
