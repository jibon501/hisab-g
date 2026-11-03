import React, { useState } from 'react';
import { useHisab } from '../data/useHisab';
import { Debt, Account, tk, fmtDate, Kind } from '../data/models';
import { SplitLines, SplitItem } from './SplitLines';
import { HistoryShareModal } from './HistoryShareModal';
import {
  Plus,
  TrendingUp,
  TrendingDown,
  Search,
  CheckCircle2,
  Clock,
  Trash2,
  FileText,
  X,
  AlertTriangle,
  Receipt,
  User,
} from 'lucide-react';
import confetti from 'canvas-confetti';

interface DebtsScreenProps {
  kind: 'lent' | 'loan';
}

export const DebtsScreen: React.FC<DebtsScreenProps> = ({ kind }) => {
  const {
    debts,
    accounts,
    addDebt,
    saveDebtEdit,
    deleteDebt,
    getDebtHistory,
  } = useHisab();

  const isLent = kind === 'lent';
  const debtList = isLent ? debts.lent : debts.loan;

  const [searchTerm, setSearchTerm] = useState('');
  const [showAdd, setShowAdd] = useState(false);
  const [editingDebt, setEditingDebt] = useState<Debt | null>(null);
  const [historyPerson, setHistoryPerson] = useState<string | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<Debt | null>(null);

  // Filtered debts
  const filteredDebts = debtList.filter((d) =>
    d.person.toLowerCase().includes(searchTerm.toLowerCase()) ||
    d.note.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const totalRemaining = debtList.reduce((sum, d) => sum + d.remaining, 0);
  const totalOriginal = debtList.reduce((sum, d) => sum + d.total, 0);
  const totalPaid = Math.max(0, totalOriginal - totalRemaining);

  // Suggestions of person names
  const allKnownNames = Array.from(
    new Set([...debts.lent, ...debts.loan, ...debts.shop].map((d) => d.person))
  );

  return (
    <div className="space-y-4 pb-24">
      {/* Top Header Card */}
      <div
        className={`p-4 rounded-3xl text-white shadow-xs ${
          isLent
            ? 'bg-gradient-to-r from-emerald-700 to-green-600'
            : 'bg-gradient-to-r from-red-700 to-rose-600'
        }`}
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-2xl bg-white/20 backdrop-blur-xs flex items-center justify-center">
              {isLent ? <TrendingUp size={22} /> : <TrendingDown size={22} />}
            </div>
            <div>
              <h2 className="text-lg font-bold">
                {isLent ? 'ধার দেওয়া (পাওনা হিসাব)' : 'ধার নেওয়া (লোন হিসাব)'}
              </h2>
              <p className="text-xs text-white/80">
                {isLent ? 'অন্যের কাছে আপনার পাওনা টাকা' : 'অন্যকে আপনার পরিশোধযোগ্য দেনা'}
              </p>
            </div>
          </div>
          <button
            onClick={() => setShowAdd(true)}
            className="flex items-center gap-1 bg-white text-slate-900 px-3.5 py-2 rounded-xl text-xs font-bold shadow-xs hover:bg-slate-100 transition-colors"
          >
            <Plus size={16} />
            <span>নতুন যোগ</span>
          </button>
        </div>

        {/* Stats Row */}
        <div className="grid grid-cols-3 gap-2 mt-4 pt-3 border-t border-white/20 text-center text-xs">
          <div>
            <span className="text-[11px] text-white/70 block">মোট হিসাব</span>
            <span className="font-bold text-sm sm:text-base">{tk(totalOriginal)}</span>
          </div>
          <div>
            <span className="text-[11px] text-white/70 block">পরিশোধ হয়েছে</span>
            <span className="font-bold text-sm sm:text-base">{tk(totalPaid)}</span>
          </div>
          <div>
            <span className="text-[11px] text-white/70 block">অবশিষ্ট বাকি</span>
            <span className="font-bold text-sm sm:text-base text-yellow-200">
              {tk(totalRemaining)}
            </span>
          </div>
        </div>
      </div>

      {/* Search Bar */}
      <div className="relative">
        <Search
          size={16}
          className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
        />
        <input
          type="text"
          placeholder="নাম বা বিবরণ দিয়ে খুঁজুন..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full pl-9 pr-4 py-2.5 text-sm bg-white border border-slate-200 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-emerald-500 font-medium text-slate-800 shadow-xs"
        />
      </div>

      {/* Debts List */}
      <div className="space-y-2.5">
        {filteredDebts.map((d) => {
          const isDone = d.remaining <= 0;
          const paidAmount = d.total - d.remaining;
          const percent = Math.min(100, Math.round((paidAmount / (d.total || 1)) * 100));

          return (
            <div
              key={d.id}
              className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs hover:shadow-md transition-all group"
            >
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div
                    className={`w-11 h-11 rounded-xl flex items-center justify-center font-bold text-base flex-shrink-0 ${
                      isDone
                        ? 'bg-emerald-50 text-emerald-600'
                        : isLent
                        ? 'bg-emerald-50 text-emerald-600'
                        : 'bg-red-50 text-red-600'
                    }`}
                  >
                    {d.person.charAt(0)}
                  </div>
                  <div>
                    <h4 className="font-bold text-slate-900 text-sm sm:text-base">
                      {d.person}
                    </h4>
                    <p className="text-xs text-slate-500 mt-0.5">
                      <span>{fmtDate(d.date)}</span>
                      {d.note && <span> • {d.note}</span>}
                    </p>
                  </div>
                </div>

                <div className="text-right">
                  <span
                    className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold ${
                      isDone
                        ? 'bg-emerald-100 text-emerald-700'
                        : 'bg-amber-100 text-amber-800'
                    }`}
                  >
                    {isDone ? (
                      <>
                        <CheckCircle2 size={12} />
                        <span>পরিশোধিত ✓</span>
                      </>
                    ) : (
                      <>
                        <Clock size={12} />
                        <span>বাকি {tk(d.remaining)}</span>
                      </>
                    )}
                  </span>
                  <p className="text-xs text-slate-400 mt-1">
                    মোট: {tk(d.total)}
                  </p>
                </div>
              </div>

              {/* Progress bar */}
              <div className="mt-3">
                <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-500 ${
                      isDone
                        ? 'bg-emerald-500'
                        : isLent
                        ? 'bg-emerald-600'
                        : 'bg-red-500'
                    }`}
                    style={{ width: `${percent}%` }}
                  />
                </div>
                <div className="flex justify-between text-[11px] text-slate-400 mt-1">
                  <span>পরিশোধ: {tk(paidAmount)} ({percent}%)</span>
                  <span>অবশিষ্ট: {tk(d.remaining)}</span>
                </div>
              </div>

              {/* Card Actions */}
              <div className="mt-3 pt-3 border-t border-slate-100 flex items-center justify-between">
                <button
                  onClick={() => setHistoryPerson(d.person)}
                  className="flex items-center gap-1.5 text-xs font-semibold text-blue-600 hover:text-blue-700 hover:bg-blue-50 px-2.5 py-1.5 rounded-lg transition-colors"
                >
                  <Receipt size={14} />
                  <span>স্লিপ / রসিদ তৈরি</span>
                </button>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setEditingDebt(d)}
                    className="text-xs font-bold px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg transition-colors"
                  >
                    {isDone ? 'এডিট' : 'পরিশোধ ও এডিট'}
                  </button>
                </div>
              </div>
            </div>
          );
        })}

        {filteredDebts.length === 0 && (
          <div className="p-12 text-center text-slate-400 bg-white rounded-2xl border border-slate-200">
            <p className="text-sm">কোনো হিসাব পাওয়া যায়নি</p>
            <p className="text-xs mt-1">উপরের &quot;নতুন যোগ&quot; বাটনে ক্লিক করে হিসাব খুলুন</p>
          </div>
        )}
      </div>

      {/* ADD DEBT MODAL */}
      {showAdd && (
        <AddDebtModal
          kind={kind}
          accounts={accounts}
          suggestions={allKnownNames}
          onClose={() => setShowAdd(false)}
          onSave={(person, note, splits) => {
            addDebt(kind, person, note, splits);
            setShowAdd(false);
          }}
        />
      )}

      {/* EDIT / REPAY DEBT MODAL */}
      {editingDebt && (
        <EditDebtModal
          debt={editingDebt}
          accounts={accounts}
          onClose={() => setEditingDebt(null)}
          onSave={(newName, splits) => {
            saveDebtEdit(editingDebt.id, newName, splits);
            // celebrate if paid off!
            const addedPay = splits.reduce((s, x) => s + x.amount, 0);
            if (addedPay >= editingDebt.remaining && editingDebt.remaining > 0) {
              confetti({ particleCount: 70, spread: 60, origin: { y: 0.7 } });
            }
            setEditingDebt(null);
          }}
          onDelete={() => {
            setDeleteConfirm(editingDebt);
            setEditingDebt(null);
          }}
        />
      )}

      {/* DELETE CONFIRMATION MODAL */}
      {deleteConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-5 space-y-4">
            <div className="flex items-center gap-3 text-red-600">
              <div className="p-2 bg-red-50 rounded-xl">
                <AlertTriangle size={24} />
              </div>
              <h3 className="font-bold text-slate-900 text-base">হিসাবটি মুছবেন?</h3>
            </div>
            <p className="text-xs text-slate-600 leading-relaxed">
              <span className="font-bold text-slate-800">"{deleteConfirm.person}"</span>-এর হিসাব ও এর সমস্ত লেনদেন মুছে ফেলা হবে এবং অ্যাকাউন্টের ব্যালেন্স আগের অবস্থায় ফিরে যাবে।
            </p>
            <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
              <button
                onClick={() => setDeleteConfirm(null)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
              >
                বাতিল
              </button>
              <button
                onClick={() => {
                  deleteDebt(deleteConfirm.id);
                  setDeleteConfirm(null);
                }}
                className="px-4 py-2 text-xs font-bold text-white bg-red-600 hover:bg-red-700 rounded-xl transition-colors"
              >
                মুছে ফেলুন
              </button>
            </div>
          </div>
        </div>
      )}

      {/* HISTORY / SLIP SHARE MODAL */}
      {historyPerson && (
        <HistoryShareModal
          person={historyPerson}
          kind={kind}
          items={getDebtHistory(kind, historyPerson)}
          onClose={() => setHistoryPerson(null)}
        />
      )}
    </div>
  );
};

// ---------------- Add Debt Modal ----------------
interface AddDebtModalProps {
  kind: 'lent' | 'loan';
  accounts: Account[];
  suggestions: string[];
  onClose: () => void;
  onSave: (
    person: string,
    note: string,
    splits: { accountId: number | null; amount: number }[]
  ) => void;
}

const AddDebtModal: React.FC<AddDebtModalProps> = ({
  kind,
  accounts,
  suggestions,
  onClose,
  onSave,
}) => {
  const isLent = kind === 'lent';
  const [person, setPerson] = useState('');
  const [note, setNote] = useState('');
  const [lines, setLines] = useState<SplitItem[]>([
    { accountId: accounts[0]?.id || null, amount: '' },
  ]);

  const personLabel = isLent ? 'কাকে ধার দিলেন (ব্যক্তির নাম)' : 'কার কাছ থেকে নিলেন (ব্যক্তি / ব্যাংক)';

  const filteredSuggestions = person.trim()
    ? suggestions.filter(
        (s) =>
          s.toLowerCase().includes(person.toLowerCase()) &&
          s.toLowerCase() !== person.toLowerCase()
      ).slice(0, 5)
    : [];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const p = person.trim();
    if (!p) {
      alert('অনুগ্রহ করে নাম দিন');
      return;
    }
    const splits = lines
      .map((l) => ({ accountId: l.accountId, amount: parseFloat(l.amount) || 0 }))
      .filter((s) => s.amount > 0);

    if (splits.length === 0) {
      alert('টাকার পরিমাণ দিন');
      return;
    }

    if (splits.some((s) => s.accountId === null)) {
      alert('প্রতিটি টাকার জন্য অ্যাকাউন্ট সিলেক্ট করুন');
      return;
    }

    onSave(p, note.trim(), splits);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full max-h-[90vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
          <h3 className="font-bold text-slate-800 text-lg">
            {isLent ? 'নতুন ধার দেওয়া হিসাব' : 'নতুন লোন / দেনা হিসাব'}
          </h3>
          <button onClick={onClose} className="p-1.5 text-slate-400 hover:text-slate-600">
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-5 space-y-4">
          {/* Person input */}
          <div className="relative">
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              {personLabel} *
            </label>
            <div className="relative">
              <User size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                type="text"
                placeholder="নাম লিখুন..."
                value={person}
                onChange={(e) => setPerson(e.target.value)}
                className="w-full pl-9 pr-3 py-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 font-medium text-slate-900"
                autoFocus
              />
            </div>

            {/* Suggestions dropdown */}
            {filteredSuggestions.length > 0 && (
              <div className="absolute left-0 right-0 mt-1 bg-white border border-slate-200 rounded-xl shadow-lg z-20 py-1">
                {filteredSuggestions.map((s) => (
                  <button
                    key={s}
                    type="button"
                    onClick={() => setPerson(s)}
                    className="w-full text-left px-3 py-1.5 text-xs hover:bg-slate-50 text-slate-700"
                  >
                    {s}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Split accounts */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-2">
              {isLent ? 'টাকা কোন অ্যাকাউন্ট থেকে গেল' : 'টাকা কোন অ্যাকাউন্টে জমা হলো'}
            </label>
            <SplitLines
              accounts={isLent ? accounts.filter((a) => a.balance > 0) : accounts}
              lines={lines}
              onChange={setLines}
              accountLabel={isLent ? 'টাকা প্রদানের অ্যাকাউন্ট' : 'টাকা জমার অ্যাকাউন্ট'}
            />
          </div>

          {/* Note */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              নোট বা কারণ (ঐচ্ছিক)
            </label>
            <input
              type="text"
              placeholder="যেমন: ৩ মাসের জন্য, জরুরি প্রয়োজন..."
              value={note}
              onChange={(e) => setNote(e.target.value)}
              className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 text-slate-800"
            />
          </div>

          {/* Footer */}
          <div className="pt-3 border-t border-slate-200 flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
            >
              বাতিল
            </button>
            <button
              type="submit"
              className="px-6 py-2 text-sm font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs"
            >
              সংরক্ষণ
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// ---------------- Edit / Repay Debt Modal ----------------
interface EditDebtModalProps {
  debt: Debt;
  accounts: Account[];
  onClose: () => void;
  onSave: (newName: string, splits: { accountId: number; amount: number }[]) => void;
  onDelete: () => void;
}

const EditDebtModal: React.FC<EditDebtModalProps> = ({
  debt,
  accounts,
  onClose,
  onSave,
  onDelete,
}) => {
  const isLent = debt.kind === 'lent';
  const isDone = debt.remaining <= 0;

  const [name, setName] = useState(debt.person);
  const [lines, setLines] = useState<SplitItem[]>([
    {
      accountId: accounts.find((a) => a.balance > 0)?.id || accounts[0]?.id || null,
      amount: '',
    },
  ]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const n = name.trim();
    if (!n) {
      alert('নাম খালি রাখা যাবে না');
      return;
    }

    const splits = lines
      .map((l) => ({ accountId: l.accountId, amount: parseFloat(l.amount) || 0 }))
      .filter((s) => s.amount > 0);

    const totalRepay = splits.reduce((sum, s) => sum + s.amount, 0);

    if (totalRepay > debt.remaining + 0.001) {
      alert('বাকির চেয়ে বেশি পরিশোধ করা যাবে না');
      return;
    }

    if (splits.some((s) => s.accountId === null)) {
      alert('প্রতিটি টাকার জন্য অ্যাকাউন্ট সিলেক্ট করুন');
      return;
    }

    onSave(
      n,
      splits.map((s) => ({ accountId: s.accountId as number, amount: s.amount }))
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full max-h-[90vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
          <div>
            <h3 className="font-bold text-slate-800 text-lg">
              {isDone ? 'হিসাব সম্পন্ন (পরিশোধিত ✓)' : `বাকি: ${tk(debt.remaining)}`}
            </h3>
            <p className="text-xs text-slate-500">মূল পরিমাণ: {tk(debt.total)}</p>
          </div>
          <button onClick={onClose} className="p-1.5 text-slate-400 hover:text-slate-600">
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-5 space-y-4">
          {/* Name input */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">নাম</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 font-bold text-slate-900"
            />
          </div>

          {/* Repayment split lines if not done */}
          {!isDone && (
            <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-2">
              <div className="flex justify-between items-center">
                <span className="text-xs font-bold text-emerald-800">
                  {isLent ? 'টাকা ফেরত পেলে (জমা হবে)' : 'টাকা পরিশোধ করলে (কাটবে)'}
                </span>
                <span className="text-xs text-slate-500">
                  সর্বোচ্চ: {tk(debt.remaining)}
                </span>
              </div>
              <SplitLines
                accounts={isLent ? accounts : accounts.filter((a) => a.balance > 0)}
                lines={lines}
                onChange={setLines}
                accountLabel={isLent ? 'টাকা জমার অ্যাকাউন্ট' : 'টাকা কাটার অ্যাকাউন্ট'}
              />
            </div>
          )}

          {/* Footer buttons */}
          <div className="pt-3 border-t border-slate-200 flex items-center justify-between">
            <button
              type="button"
              onClick={onDelete}
              className="px-3 py-2 text-xs font-bold text-red-600 hover:bg-red-50 rounded-xl transition-colors flex items-center gap-1"
            >
              <Trash2 size={14} />
              <span>হিসাব মুছুন</span>
            </button>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
              >
                বাতিল
              </button>
              <button
                type="submit"
                className="px-5 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs"
              >
                সংরক্ষণ
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};
