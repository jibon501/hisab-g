import React, { useState } from 'react';
import { Account, fmtDate } from '../data/models';
import { SplitLines, SplitItem } from './SplitLines';
import { X, Calendar, PlusCircle, ArrowDownCircle, ArrowUpCircle } from 'lucide-react';

interface AddTxnDialogProps {
  accounts: Account[];
  expenseCats: string[];
  incomeCats: string[];
  onClose: () => void;
  onSave: (
    type: 'credit' | 'debit',
    splits: { accountId: number | null; amount: number }[],
    category: string,
    note: string,
    baki: boolean,
    date: string
  ) => void;
}

export const AddTxnDialog: React.FC<AddTxnDialogProps> = ({
  accounts,
  expenseCats,
  incomeCats,
  onClose,
  onSave,
}) => {
  const [type, setType] = useState<'debit' | 'credit'>('debit');
  const [lines, setLines] = useState<SplitItem[]>([{ accountId: accounts[0]?.id || null, amount: '' }]);
  const [category, setCategory] = useState('');
  const [note, setNote] = useState('');
  const [baki, setBaki] = useState(false);
  const [bakiAmount, setBakiAmount] = useState('');

  // Default to today ISO date string
  const todayStr = new Date().toISOString().slice(0, 10);
  const [dateStr, setDateStr] = useState(todayStr);

  const activeCats = type === 'debit' ? expenseCats : incomeCats;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const catTrimmed = category.trim();
    if (!catTrimmed) {
      alert('অনুগ্রহ করে খাত দিন');
      return;
    }

    if (type === 'debit' && baki) {
      const amt = parseFloat(bakiAmount);
      if (!amt || amt <= 0) {
        alert('সঠিক টাকার পরিমাণ দিন');
        return;
      }
      onSave('debit', [{ accountId: null, amount: amt }], catTrimmed, note.trim(), true, dateStr);
      return;
    }

    const validSplits = lines
      .map((l) => ({
        accountId: l.accountId,
        amount: parseFloat(l.amount) || 0,
      }))
      .filter((s) => s.amount > 0);

    if (validSplits.length === 0) {
      alert('টাকার পরিমাণ দিন');
      return;
    }

    onSave(type, validSplits, catTrimmed, note.trim(), false, dateStr);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full max-h-[92vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
          <div className="flex items-center gap-2">
            <PlusCircle className="text-emerald-600" size={20} />
            <h3 className="font-bold text-slate-800 text-lg">নতুন লেনদেন যোগ</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full hover:bg-slate-200 text-slate-500 transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-5 space-y-4">
          {/* Type Toggle: খরচ (Debit) vs আয় (Credit) */}
          <div className="grid grid-cols-2 gap-2 p-1 bg-slate-100 rounded-xl">
            <button
              type="button"
              onClick={() => {
                setType('debit');
                setCategory('');
              }}
              className={`flex items-center justify-center gap-2 py-2.5 text-sm font-bold rounded-lg transition-all ${
                type === 'debit'
                  ? 'bg-red-600 text-white shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <ArrowUpCircle size={16} />
              <span>খরচ (Debit)</span>
            </button>
            <button
              type="button"
              onClick={() => {
                setType('credit');
                setBaki(false);
                setCategory('');
              }}
              className={`flex items-center justify-center gap-2 py-2.5 text-sm font-bold rounded-lg transition-all ${
                type === 'credit'
                  ? 'bg-emerald-600 text-white shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <ArrowDownCircle size={16} />
              <span>আয় (Credit)</span>
            </button>
          </div>

          {/* If Debit, show Baki toggle */}
          {type === 'debit' && (
            <div className="flex items-center justify-between p-3 bg-amber-50 rounded-xl border border-amber-200">
              <div>
                <span className="text-sm font-semibold text-amber-900 block">
                  দোকান বাকি (এখনো টাকা দিইনি)
                </span>
                <span className="text-xs text-amber-700">
                  অ্যাকাউন্ট থেকে টাকা কাটবে না, তবে মাসের খরচে যোগ হবে
                </span>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={baki}
                  onChange={(e) => setBaki(e.target.checked)}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-slate-200 peer-focus:outline-hidden rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-amber-600"></div>
              </label>
            </div>
          )}

          {/* Amount / Split lines */}
          {type === 'debit' && baki ? (
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                বাকির পরিমাণ
              </label>
              <div className="relative">
                <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-sm">
                  ৳
                </span>
                <input
                  type="number"
                  placeholder="০.০০"
                  value={bakiAmount}
                  onChange={(e) => setBakiAmount(e.target.value)}
                  className="w-full pl-8 pr-3 py-2.5 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 font-bold text-slate-900"
                  autoFocus
                />
              </div>
            </div>
          ) : (
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-2">
                {type === 'debit' ? 'টাকা কোন অ্যাকাউন্ট থেকে কাটবে' : 'টাকা কোন অ্যাকাউন্টে জমা হবে'}
              </label>
              <SplitLines
                accounts={accounts}
                lines={lines}
                onChange={setLines}
                accountLabel={type === 'debit' ? 'অ্যাকাউন্ট' : 'অ্যাকাউন্টে জমা'}
              />
            </div>
          )}

          {/* Category selection */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="text-xs font-semibold text-slate-700">
                খাত / ক্যাটাগরি *
              </label>
              {category && (
                <span className="text-[11px] font-medium text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">
                  নির্বাচিত: {category}
                </span>
              )}
            </div>
            <input
              type="text"
              placeholder="খাতের নাম লিখুন বা নিচের থেকে বাছুন..."
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 font-medium text-slate-800"
            />
            {/* Category chips */}
            <div className="flex flex-wrap gap-1.5 mt-2 max-h-24 overflow-y-auto">
              {activeCats.map((cat) => (
                <button
                  key={cat}
                  type="button"
                  onClick={() => setCategory(cat)}
                  className={`text-xs px-2.5 py-1 rounded-lg border transition-all ${
                    category === cat
                      ? 'bg-slate-800 text-white border-slate-800 font-semibold'
                      : 'bg-slate-50 text-slate-700 border-slate-200 hover:bg-slate-100'
                  }`}
                >
                  {cat}
                </button>
              ))}
            </div>
          </div>

          {/* Note Input */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              নোট বা বিবরণ (ঐচ্ছিক)
            </label>
            <input
              type="text"
              placeholder={baki ? 'দোকানের নাম বা বিবরণ...' : 'বিবরণ লিখুন...'}
              value={note}
              onChange={(e) => setNote(e.target.value)}
              className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 text-slate-800"
            />
          </div>

          {/* Date Picker */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              তারিখ: <span className="font-bold text-slate-900">{fmtDate(dateStr)}</span>
            </label>
            <div className="flex items-center gap-2">
              <div className="relative flex-1">
                <Calendar
                  size={16}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                />
                <input
                  type="date"
                  value={dateStr}
                  max={todayStr}
                  onChange={(e) => setDateStr(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 text-sm bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 text-slate-700"
                />
              </div>
              <button
                type="button"
                onClick={() => setDateStr(todayStr)}
                className="px-3 py-2 text-xs font-semibold bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl transition-colors"
              >
                আজকে
              </button>
            </div>
          </div>

          {/* Modal Footer */}
          <div className="pt-3 border-t border-slate-200 flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 text-sm font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
            >
              বাতিল
            </button>
            <button
              type="submit"
              className="px-6 py-2.5 text-sm font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs transition-colors flex items-center gap-1.5"
            >
              <span>সংরক্ষণ করুন</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
