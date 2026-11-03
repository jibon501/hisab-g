import React from 'react';
import { Account, tk } from '../data/models';
import { Plus, X } from 'lucide-react';
import { AccountAvatar } from './AccountAvatar';

export interface SplitItem {
  accountId: number | null;
  amount: string;
}

interface SplitLinesProps {
  accounts: Account[];
  lines: SplitItem[];
  onChange: (lines: SplitItem[]) => void;
  accountLabel?: string;
}

export const SplitLines: React.FC<SplitLinesProps> = ({
  accounts,
  lines,
  onChange,
  accountLabel = 'অ্যাকাউন্ট',
}) => {
  const handleUpdate = (index: number, updated: Partial<SplitItem>) => {
    const next = [...lines];
    next[index] = { ...next[index], ...updated };
    onChange(next);
  };

  const handleAddLine = () => {
    onChange([...lines, { accountId: null, amount: '' }]);
  };

  const handleRemoveLine = (index: number) => {
    if (lines.length > 1) {
      onChange(lines.filter((_, i) => i !== index));
    }
  };

  const totalAmount = lines.reduce(
    (sum, l) => sum + (parseFloat(l.amount) || 0),
    0
  );

  return (
    <div className="space-y-3">
      {lines.map((line, idx) => {
        // Accounts available for this line: accounts not selected in other lines, plus this line's selected account
        const availableAccounts = accounts.filter(
          (a) =>
            a.id === line.accountId ||
            !lines.some((l, i) => i !== idx && l.accountId === a.id)
        );

        const selectedAcc = accounts.find((a) => a.id === line.accountId);

        return (
          <div
            key={idx}
            className="flex items-center gap-2 bg-slate-50 p-2.5 rounded-xl border border-slate-200"
          >
            {/* Account Selector */}
            <div className="flex-1">
              <label className="block text-[11px] font-medium text-slate-500 mb-1">
                {lines.length > 1 ? `${accountLabel} (${idx + 1})` : accountLabel}
              </label>
              <div className="relative flex items-center">
                {selectedAcc && (
                  <div className="absolute left-2.5 pointer-events-none">
                    <AccountAvatar account={selectedAcc} size="sm" />
                  </div>
                )}
                <select
                  value={line.accountId || ''}
                  onChange={(e) =>
                    handleUpdate(idx, {
                      accountId: e.target.value ? Number(e.target.value) : null,
                    })
                  }
                  className={`w-full py-2 pr-3 text-sm bg-white border border-slate-300 rounded-lg focus:outline-hidden focus:ring-2 focus:ring-emerald-500 font-medium text-slate-800 ${
                    selectedAcc ? 'pl-11' : 'pl-3'
                  }`}
                >
                  <option value="">অ্যাকাউন্ট সিলেক্ট করুন</option>
                  {availableAccounts.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.name} ({tk(a.balance)})
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Amount Field */}
            <div className="w-32">
              <label className="block text-[11px] font-medium text-slate-500 mb-1">
                টাকা
              </label>
              <div className="relative">
                <span className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-xs">
                  ৳
                </span>
                <input
                  type="number"
                  placeholder="০"
                  value={line.amount}
                  onChange={(e) => handleUpdate(idx, { amount: e.target.value })}
                  className="w-full pl-6 pr-2.5 py-2 text-sm bg-white border border-slate-300 rounded-lg focus:outline-hidden focus:ring-2 focus:ring-emerald-500 font-semibold text-slate-800"
                />
              </div>
            </div>

            {/* Remove button */}
            {lines.length > 1 && (
              <button
                type="button"
                onClick={() => handleRemoveLine(idx)}
                className="mt-4 p-1.5 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                title="লাইন বাদ দিন"
              >
                <X size={16} />
              </button>
            )}
          </div>
        );
      })}

      {/* Add another split line */}
      <div className="flex items-center justify-between pt-1">
        <button
          type="button"
          onClick={handleAddLine}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-emerald-700 hover:text-emerald-800 hover:bg-emerald-50 px-2.5 py-1.5 rounded-lg transition-colors"
        >
          <Plus size={14} />
          <span>আরেকটি অ্যাকাউন্ট যোগ করুন</span>
        </button>

        {lines.length > 1 && (
          <span className="text-xs font-bold text-slate-800 bg-slate-100 px-3 py-1.5 rounded-lg border border-slate-200">
            মোট: {tk(totalAmount)}
          </span>
        )}
      </div>
    </div>
  );
};
