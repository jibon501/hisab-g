import React, { useState } from 'react';
import { Account, tk } from '../data/models';
import { AccountAvatar } from './AccountAvatar';
import { X, ArrowRightLeft, AlertCircle } from 'lucide-react';

interface TransferDialogProps {
  accounts: Account[];
  onClose: () => void;
  onTransfer: (fromId: number, toId: number, amount: number) => void;
}

export const TransferDialog: React.FC<TransferDialogProps> = ({
  accounts,
  onClose,
  onTransfer,
}) => {
  const [fromId, setFromId] = useState<number>(accounts[0]?.id || 0);
  const [toId, setToId] = useState<number>(accounts[1]?.id || 0);
  const [amount, setAmount] = useState('');

  const fromAcc = accounts.find((a) => a.id === fromId);
  const toAcc = accounts.find((a) => a.id === toId);

  const parsedAmount = parseFloat(amount) || 0;
  const isOverBalance = fromAcc ? parsedAmount > fromAcc.balance : false;
  const isSameAccount = fromId === toId;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (parsedAmount <= 0) {
      alert('সঠিক টাকার পরিমাণ দিন');
      return;
    }
    if (isSameAccount) {
      alert('উৎস ও গন্তব্য অ্যাকাউন্ট ভিন্ন হতে হবে');
      return;
    }
    if (isOverBalance) {
      alert('উৎস অ্যাকাউন্টে পর্যাপ্ত ব্যালেন্স নেই');
      return;
    }

    onTransfer(fromId, toId, parsedAmount);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full overflow-hidden">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
          <div className="flex items-center gap-2">
            <ArrowRightLeft className="text-blue-600" size={20} />
            <h3 className="font-bold text-slate-800 text-lg">অ্যাকাউন্ট ট্রান্সফার</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full hover:bg-slate-200 text-slate-500 transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {/* From Account */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              উৎস অ্যাকাউন্ট (টাকা কোথা থেকে কাটবে)
            </label>
            <div className="relative flex items-center">
              {fromAcc && (
                <div className="absolute left-3 pointer-events-none">
                  <AccountAvatar account={fromAcc} size="sm" />
                </div>
              )}
              <select
                value={fromId}
                onChange={(e) => setFromId(Number(e.target.value))}
                className={`w-full py-2.5 pr-3 text-sm bg-slate-50 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 font-medium text-slate-800 ${
                  fromAcc ? 'pl-12' : 'pl-3'
                }`}
              >
                {accounts.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.name} ({tk(a.balance)})
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Direction indicator */}
          <div className="flex justify-center -my-1 text-slate-400">
            <div className="bg-slate-100 p-1.5 rounded-full border border-slate-200">
              <ArrowRightLeft size={16} className="text-blue-600 rotate-90" />
            </div>
          </div>

          {/* To Account */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              গন্তব্য অ্যাকাউন্ট (টাকা কোথায় জমা হবে)
            </label>
            <div className="relative flex items-center">
              {toAcc && (
                <div className="absolute left-3 pointer-events-none">
                  <AccountAvatar account={toAcc} size="sm" />
                </div>
              )}
              <select
                value={toId}
                onChange={(e) => setToId(Number(e.target.value))}
                className={`w-full py-2.5 pr-3 text-sm bg-slate-50 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 font-medium text-slate-800 ${
                  toAcc ? 'pl-12' : 'pl-3'
                }`}
              >
                {accounts.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.name} ({tk(a.balance)})
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Amount Field */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              ট্রান্সফারের পরিমাণ
            </label>
            <div className="relative">
              <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-sm">
                ৳
              </span>
              <input
                type="number"
                placeholder="০"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full pl-8 pr-3 py-2.5 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 font-bold text-slate-900"
                autoFocus
              />
            </div>
          </div>

          {/* Warning notice if needed */}
          {isOverBalance && (
            <div className="flex items-center gap-2 p-2.5 bg-red-50 text-red-700 rounded-xl text-xs border border-red-200">
              <AlertCircle size={16} className="flex-shrink-0" />
              <span>নির্বাচিত অ্যাকাউন্টে রয়েছে {fromAcc ? tk(fromAcc.balance) : 0}, যা প্রয়োজনের চেয়ে কম।</span>
            </div>
          )}

          {isSameAccount && (
            <div className="flex items-center gap-2 p-2.5 bg-amber-50 text-amber-700 rounded-xl text-xs border border-amber-200">
              <AlertCircle size={16} className="flex-shrink-0" />
              <span>উৎস এবং গন্তব্য একই অ্যাকাউন্ট হতে পারবে না।</span>
            </div>
          )}

          {/* Footer actions */}
          <div className="pt-3 border-t border-slate-200 flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
            >
              বাতিল
            </button>
            <button
              type="submit"
              disabled={isSameAccount || isOverBalance || parsedAmount <= 0}
              className="px-5 py-2 text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 rounded-xl shadow-xs transition-colors flex items-center gap-1.5"
            >
              <span>ট্রান্সফার সম্পন্ন করুন</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
