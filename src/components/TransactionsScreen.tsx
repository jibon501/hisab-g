import React, { useState } from 'react';
import { useHisab } from '../data/useHisab';
import { Txn, tk, fmtDate } from '../data/models';
import { AddTxnDialog } from './AddTxnDialog';
import { TransferDialog } from './TransferDialog';
import { CategoriesDialog } from './CategoriesDialog';
import {
  Plus,
  ArrowRightLeft,
  Tag,
  Filter,
  ArrowDownLeft,
  ArrowUpRight,
  Trash2,
  X,
  Calendar,
  AlertTriangle,
} from 'lucide-react';

export const TransactionsScreen: React.FC = () => {
  const {
    txns,
    accounts,
    categories,
    usedCategories,
    filter,
    applyFilter,
    addTxns,
    addBakiExpense,
    deleteTxn,
    transfer,
    addCategory,
    renameCategory,
    deleteCategory,
  } = useHisab();

  const [showAdd, setShowAdd] = useState(false);
  const [showTransfer, setShowTransfer] = useState(false);
  const [showCats, setShowCats] = useState(false);
  const [filterMenu, setFilterMenu] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<Txn | null>(null);

  const expenseCats = categories.filter((c) => c.kind === 'expense').map((c) => c.name);
  const incomeCats = categories.filter((c) => c.kind === 'income').map((c) => c.name);

  // Net total of displayed transactions
  const netTotal = txns.reduce(
    (sum, t) => sum + (t.type === 'credit' ? t.amount : -t.amount),
    0
  );

  const totalCredits = txns
    .filter((t) => t.type === 'credit')
    .reduce((sum, t) => sum + t.amount, 0);

  const totalDebits = txns
    .filter((t) => t.type === 'debit')
    .reduce((sum, t) => sum + t.amount, 0);

  return (
    <div className="space-y-3 pb-24">
      {/* Top Action & Filter Bar */}
      <div className="bg-white p-3 sm:p-4 rounded-2xl border border-slate-200 shadow-xs flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          {/* Category Filter Dropdown */}
          <div className="relative">
            <button
              onClick={() => setFilterMenu(!filterMenu)}
              className={`flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-xl border transition-all ${
                filter
                  ? 'bg-amber-500 text-white border-amber-500 shadow-xs'
                  : 'bg-slate-50 text-slate-700 border-slate-200 hover:bg-slate-100'
              }`}
            >
              <Filter size={14} />
              <span>{filter ? `খাত: ${filter}` : 'সব খাত'}</span>
            </button>

            {filterMenu && (
              <div className="absolute left-0 mt-2 w-52 bg-white rounded-2xl shadow-xl border border-slate-200 py-1 z-30 animate-in fade-in zoom-in-95 duration-100">
                <button
                  onClick={() => {
                    applyFilter(null);
                    setFilterMenu(false);
                  }}
                  className={`w-full text-left px-3.5 py-2 text-xs font-semibold hover:bg-slate-50 flex items-center justify-between ${
                    filter === null ? 'text-emerald-600 bg-emerald-50' : 'text-slate-700'
                  }`}
                >
                  <span>সব খাত</span>
                  {filter === null && <span>✓</span>}
                </button>
                <div className="border-t border-slate-100 my-1" />
                <div className="max-h-56 overflow-y-auto">
                  {usedCategories.map((c) => (
                    <button
                      key={c}
                      onClick={() => {
                        applyFilter(c);
                        setFilterMenu(false);
                      }}
                      className={`w-full text-left px-3.5 py-2 text-xs font-medium hover:bg-slate-50 flex items-center justify-between ${
                        filter === c ? 'text-amber-600 bg-amber-50 font-semibold' : 'text-slate-700'
                      }`}
                    >
                      <span className="truncate">{c}</span>
                      {filter === c && <span>✓</span>}
                    </button>
                  ))}
                  {usedCategories.length === 0 && (
                    <div className="px-3 py-2 text-xs text-slate-400">কোনো খাত পাওয়া যায়নি</div>
                  )}
                </div>
              </div>
            )}
          </div>

          {filter && (
            <button
              onClick={() => applyFilter(null)}
              className="text-xs text-slate-500 hover:text-slate-800 p-1 rounded-full hover:bg-slate-100"
              title="ফিল্টার মুছুন"
            >
              <X size={15} />
            </button>
          )}
        </div>

        {/* Action Buttons: Categories, Transfer, Add Transaction */}
        <div className="flex items-center gap-1.5">
          <button
            onClick={() => setShowCats(true)}
            className="flex items-center gap-1 px-2.5 py-2 text-xs font-semibold text-slate-700 bg-slate-50 hover:bg-slate-100 border border-slate-200 rounded-xl transition-colors"
            title="খাত ব্যবস্থাপনা"
          >
            <Tag size={14} className="text-slate-500" />
            <span className="hidden sm:inline">খাতসমূহ</span>
          </button>
          <button
            onClick={() => setShowTransfer(true)}
            className="flex items-center gap-1 px-2.5 py-2 text-xs font-semibold text-blue-700 bg-blue-50 hover:bg-blue-100 border border-blue-200 rounded-xl transition-colors"
            title="অ্যাকাউন্ট ট্রান্সফার"
          >
            <ArrowRightLeft size={14} />
            <span className="hidden sm:inline">ট্রান্সফার</span>
          </button>
          <button
            onClick={() => setShowAdd(true)}
            className="flex items-center gap-1 px-3 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs transition-colors"
          >
            <Plus size={16} />
            <span>নতুন লেনদেন</span>
          </button>
        </div>
      </div>

      {/* Summary Mini Bar */}
      <div className="grid grid-cols-3 gap-2 bg-slate-100/80 p-2.5 rounded-2xl text-xs">
        <div className="bg-white p-2 rounded-xl text-center border border-slate-200/80">
          <span className="text-[10px] text-slate-400 block font-medium">মোট আয় (+)</span>
          <span className="font-bold text-emerald-600">{tk(totalCredits)}</span>
        </div>
        <div className="bg-white p-2 rounded-xl text-center border border-slate-200/80">
          <span className="text-[10px] text-slate-400 block font-medium">মোট ব্যয় (-)</span>
          <span className="font-bold text-red-600">{tk(totalDebits)}</span>
        </div>
        <div className="bg-white p-2 rounded-xl text-center border border-slate-200/80">
          <span className="text-[10px] text-slate-400 block font-medium">নিট মোট</span>
          <span className={`font-bold ${netTotal >= 0 ? 'text-emerald-700' : 'text-red-700'}`}>
            {tk(netTotal)}
          </span>
        </div>
      </div>

      {/* Transactions List */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs divide-y divide-slate-100 overflow-hidden">
        {txns.map((t) => {
          const isCredit = t.type === 'credit';
          return (
            <div
              key={t.id}
              className="p-3.5 flex items-center justify-between hover:bg-slate-50 transition-colors group"
            >
              <div className="flex items-center gap-3">
                {/* Arrow icon badge */}
                <div
                  className={`w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 ${
                    isCredit ? 'bg-emerald-50 text-emerald-600' : 'bg-red-50 text-red-600'
                  }`}
                >
                  {isCredit ? <ArrowDownLeft size={20} /> : <ArrowUpRight size={20} />}
                </div>

                <div>
                  <div className="flex items-center gap-1.5">
                    <span className="font-bold text-slate-900 text-sm">{t.category}</span>
                    {t.isPayment && (
                      <span className="text-[10px] bg-blue-50 text-blue-600 font-semibold px-1.5 py-0.5 rounded-md border border-blue-100">
                        পরিশোধ
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-slate-500 mt-0.5">
                    <span className="font-medium text-slate-700">{t.accountName || 'ক্যাশ'}</span>
                    <span> • </span>
                    <span>{fmtDate(t.date)}</span>
                    {t.note && <span className="text-slate-600"> • {t.note}</span>}
                  </p>
                </div>
              </div>

              {/* Amount and delete button */}
              <div className="flex items-center gap-2">
                <span
                  className={`text-sm sm:text-base font-bold ${
                    isCredit ? 'text-emerald-600' : 'text-red-600'
                  }`}
                >
                  {isCredit ? '+' : '-'} {tk(t.amount)}
                </span>
                <button
                  onClick={() => setDeleteTarget(t)}
                  className="p-1.5 text-slate-300 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors opacity-80 group-hover:opacity-100"
                  title="মুছুন"
                >
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          );
        })}

        {txns.length === 0 && (
          <div className="p-12 text-center text-slate-400">
            <p className="text-sm">কোনো লেনদেন পাওয়া যায়নি</p>
            <p className="text-xs mt-1">
              {filter ? 'ফিল্টার পরিবর্তন করুন অথবা নতুন লেনদেন যোগ করুন' : 'নতুন লেনদেন বাটনে চাপুন'}
            </p>
          </div>
        )}
      </div>

      {/* Delete Confirmation Modal */}
      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-5 space-y-4">
            <div className="flex items-center gap-3 text-red-600">
              <div className="p-2 bg-red-50 rounded-xl">
                <AlertTriangle size={24} />
              </div>
              <h3 className="font-bold text-slate-900 text-base">লেনদেনটি মুছবেন?</h3>
            </div>
            <p className="text-xs text-slate-600 leading-relaxed">
              <span className="font-bold text-slate-800">{deleteTarget.category}</span> •{' '}
              <span className="font-bold text-slate-800">{tk(deleteTarget.amount)}</span>
              <br />
              অ্যাকাউন্টের ব্যালেন্স সেই অনুযায়ী স্বয়ংক্রিয়ভাবে ঠিক হয়ে যাবে।
            </p>
            <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
              <button
                onClick={() => setDeleteTarget(null)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
              >
                বাতিল
              </button>
              <button
                onClick={() => {
                  deleteTxn(deleteTarget.id);
                  setDeleteTarget(null);
                }}
                className="px-4 py-2 text-xs font-bold text-white bg-red-600 hover:bg-red-700 rounded-xl transition-colors"
              >
                মুছে ফেলুন
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Add Transaction Dialog */}
      {showAdd && (
        <AddTxnDialog
          accounts={accounts}
          expenseCats={expenseCats}
          incomeCats={incomeCats}
          onClose={() => setShowAdd(false)}
          onSave={(type, splits, cat, note, baki, date) => {
            addCategory(cat, type === 'debit' ? 'expense' : 'income');
            if (baki) {
              addBakiExpense(
                splits.reduce((sum, s) => sum + s.amount, 0),
                cat,
                note,
                date
              );
            } else {
              addTxns(type, splits, cat, note, date);
            }
            setShowAdd(false);
          }}
        />
      )}

      {/* Transfer Dialog */}
      {showTransfer && (
        <TransferDialog
          accounts={accounts}
          onClose={() => setShowTransfer(false)}
          onTransfer={(fromId, toId, amount) => {
            transfer(fromId, toId, amount);
          }}
        />
      )}

      {/* Categories Dialog */}
      {showCats && (
        <CategoriesDialog
          categories={categories}
          onAdd={addCategory}
          onRename={renameCategory}
          onDelete={deleteCategory}
          onClose={() => setShowCats(false)}
        />
      )}
    </div>
  );
};
