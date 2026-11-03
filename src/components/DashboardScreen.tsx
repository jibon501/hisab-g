import React, { useState } from 'react';
import { useHisab } from '../data/useHisab';
import { tk, Kind, Account } from '../data/models';
import { AccountAvatar } from './AccountAvatar';
import { HistoryShareModal } from './HistoryShareModal';
import {
  Banknote,
  TrendingUp,
  TrendingDown,
  ShoppingCart,
  PiggyBank,
  Building,
  ChevronRight,
  X,
  CreditCard,
  CheckCircle2,
  Receipt,
} from 'lucide-react';

type DetailType = 'CASH' | 'LENT' | 'LOAN' | 'EXPENSE' | 'DPS' | 'ASSET';

export const DashboardScreen: React.FC = () => {
  const { summary, accounts, debts, monthExpenses, getDebtHistory, payDebt } = useHisab();
  const [selectedDetail, setSelectedDetail] = useState<DetailType | null>(null);
  const [historyTarget, setHistoryTarget] = useState<{
    kind: 'loan' | 'lent' | 'shop';
    person: string;
  } | null>(null);
  const [payBakiId, setPayBakiId] = useState<number | null>(null);

  // States for Pay Baki modal
  const targetDebt = payBakiId ? debts.shop.find((d) => d.id === payBakiId) : null;
  const [payAmount, setPayAmount] = useState('');
  const [payAccId, setPayAccId] = useState<number>(accounts.find((a) => a.balance > 0)?.id || accounts[0]?.id || 1);

  const handleOpenPayBaki = (debtId: number) => {
    const d = debts.shop.find((s) => s.id === debtId);
    if (d) {
      setPayBakiId(debtId);
      setPayAmount(d.remaining.toString());
      const positiveAcc = accounts.find((a) => a.balance >= d.remaining) || accounts.find((a) => a.balance > 0) || accounts[0];
      if (positiveAcc) setPayAccId(positiveAcc.id);
    }
  };

  const handleExecutePayBaki = (e: React.FormEvent) => {
    e.preventDefault();
    if (!payBakiId || !targetDebt) return;
    const amt = parseFloat(payAmount);
    if (!amt || amt <= 0) {
      alert('সঠিক পরিমাণ দিন');
      return;
    }
    if (amt > targetDebt.remaining) {
      alert('বাকির চেয়ে বেশি পরিশোধ করা যাবে না');
      return;
    }
    payDebt(payBakiId, amt, payAccId);
    setPayBakiId(null);
  };

  // Grouped debts for Lent & Loan summaries
  const lentGrouped = Object.entries(
    debts.lent
      .filter((d) => d.remaining > 0)
      .reduce((acc, d) => {
        acc[d.person] = (acc[d.person] || 0) + d.remaining;
        return acc;
      }, {} as Record<string, number>)
  ).sort((a, b) => b[1] - a[1]);

  const loanGrouped = Object.entries(
    debts.loan
      .filter((d) => d.remaining > 0)
      .reduce((acc, d) => {
        acc[d.person] = (acc[d.person] || 0) + d.remaining;
        return acc;
      }, {} as Record<string, number>)
  ).sort((a, b) => b[1] - a[1]);

  return (
    <div className="space-y-4 pb-20">
      {/* Hero Welcome / Today Banner */}
      <div className="bg-gradient-to-r from-emerald-800 to-teal-700 rounded-3xl p-5 text-white shadow-md relative overflow-hidden">
        <div className="absolute right-0 top-0 translate-x-4 -translate-y-4 w-40 h-40 bg-white/10 rounded-full blur-xl pointer-events-none" />
        <div className="relative z-10 flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold tracking-wider uppercase text-emerald-200">
              বর্তমান মোট তারল্য (ক্যাশ ও ব্যাংক)
            </span>
            <h1 className="text-3xl font-extrabold tracking-tight mt-1 text-white">
              {tk(summary.cash)}
            </h1>
            <p className="text-xs text-emerald-100 mt-1">
              নগদ টাকা, বিকাশ, নগদ ও ব্যাংক ব্যালেন্স
            </p>
          </div>
          <button
            onClick={() => setSelectedDetail('CASH')}
            className="flex items-center gap-1 bg-white/20 hover:bg-white/30 backdrop-blur-xs text-white text-xs font-semibold px-3 py-2 rounded-xl transition-all"
          >
            <span>ভাঙতি বিবরণ</span>
            <ChevronRight size={14} />
          </button>
        </div>
      </div>

      {/* Grid of 5 Summary Cards */}
      <div className="grid grid-cols-2 gap-3 sm:gap-4">
        {/* Lent Card */}
        <div
          onClick={() => setSelectedDetail('LENT')}
          className="bg-white p-4 rounded-2xl border border-emerald-100 shadow-xs hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="flex items-center justify-between">
            <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center group-hover:scale-105 transition-transform">
              <TrendingUp size={22} />
            </div>
            <span className="text-[11px] font-semibold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">
              পাওনা
            </span>
          </div>
          <p className="text-xs font-medium text-slate-500 mt-3">মোট পাওনা টাকা</p>
          <p className="text-xl font-bold text-emerald-600 mt-0.5">{tk(summary.lent)}</p>
          <p className="text-[11px] text-slate-400 mt-1">অন্যের কাছে পাবেন</p>
        </div>

        {/* Loan Card */}
        <div
          onClick={() => setSelectedDetail('LOAN')}
          className="bg-white p-4 rounded-2xl border border-red-100 shadow-xs hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="flex items-center justify-between">
            <div className="w-10 h-10 rounded-xl bg-red-50 text-red-600 flex items-center justify-center group-hover:scale-105 transition-transform">
              <TrendingDown size={22} />
            </div>
            <span className="text-[11px] font-semibold text-red-600 bg-red-50 px-2 py-0.5 rounded-full">
              দেনা
            </span>
          </div>
          <p className="text-xs font-medium text-slate-500 mt-3">ধার / লোন নেওয়া</p>
          <p className="text-xl font-bold text-red-600 mt-0.5">{tk(summary.loan)}</p>
          <p className="text-[11px] text-slate-400 mt-1">অন্যকে পরিশোধ করতে হবে</p>
        </div>

        {/* Month Expense Card */}
        <div
          onClick={() => setSelectedDetail('EXPENSE')}
          className="col-span-2 bg-white p-4 rounded-2xl border border-purple-100 shadow-xs hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center group-hover:scale-105 transition-transform">
                <ShoppingCart size={22} />
              </div>
              <div>
                <p className="text-xs font-medium text-slate-500">চলতি মাসের খরচ</p>
                <p className="text-2xl font-bold text-purple-700">{tk(summary.monthExpense)}</p>
              </div>
            </div>
            {summary.monthBaki > 0 && (
              <div className="text-right">
                <span className="text-[11px] font-bold text-amber-700 bg-amber-50 border border-amber-200 px-2.5 py-1 rounded-full">
                  দোকান বাকি: {tk(summary.monthBaki)}
                </span>
              </div>
            )}
          </div>
        </div>

        {/* DPS Card */}
        <div
          onClick={() => setSelectedDetail('DPS')}
          className="bg-white p-4 rounded-2xl border border-indigo-100 shadow-xs hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center group-hover:scale-105 transition-transform">
            <PiggyBank size={22} />
          </div>
          <p className="text-xs font-medium text-slate-500 mt-3">ডিপিএস / সঞ্চয় মোট</p>
          <p className="text-xl font-bold text-indigo-600 mt-0.5">{tk(summary.dps)}</p>
          <p className="text-[11px] text-slate-400 mt-1">ভবিষ্যত জমা সঞ্চয়</p>
        </div>

        {/* Assets Card */}
        <div
          onClick={() => setSelectedDetail('ASSET')}
          className="bg-white p-4 rounded-2xl border border-amber-100 shadow-xs hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-700 flex items-center justify-center group-hover:scale-105 transition-transform">
            <Building size={22} />
          </div>
          <p className="text-xs font-medium text-slate-500 mt-3">স্থাবর সম্পদ ও বিনিয়োগ</p>
          <p className="text-xl font-bold text-amber-800 mt-0.5">{tk(summary.asset)}</p>
          <p className="text-[11px] text-slate-400 mt-1">জমি, ফ্ল্যাট ও অন্যান্য</p>
        </div>
      </div>

      {/* Quick Action Bar for shop baki */}
      {summary.shop > 0 && (
        <div className="p-4 bg-amber-50 rounded-2xl border border-amber-200 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-amber-200 text-amber-900 flex items-center justify-center font-bold">
              ৳
            </div>
            <div>
              <p className="text-xs font-semibold text-amber-900">দোকান বাকি রয়েছে</p>
              <p className="text-sm font-bold text-amber-800">{tk(summary.shop)}</p>
            </div>
          </div>
          <button
            onClick={() => setSelectedDetail('EXPENSE')}
            className="text-xs font-bold text-amber-900 bg-amber-200/80 hover:bg-amber-300 px-3 py-1.5 rounded-xl transition-colors"
          >
            পরিশোধ করুন
          </button>
        </div>
      )}

      {/* DETAIL MODAL */}
      {selectedDetail && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
          <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full max-h-[85vh] flex flex-col overflow-hidden">
            {/* Modal Header */}
            <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
              <div>
                <h3 className="font-bold text-slate-800 text-lg">
                  {selectedDetail === 'CASH' && 'ক্যাশ ও ব্যাংক ব্যালেন্স'}
                  {selectedDetail === 'LENT' && 'মোট পাওনা হিসাব (ধার দেওয়া)'}
                  {selectedDetail === 'LOAN' && 'দেনা ও লোন গ্রহণ হিসাব'}
                  {selectedDetail === 'EXPENSE' && 'চলতি মাসের খরচের বিস্তারিত'}
                  {selectedDetail === 'DPS' && 'ডিপিএস সঞ্চয় হিসাব'}
                  {selectedDetail === 'ASSET' && 'স্থাবর সম্পদ ও বিনিয়োগ'}
                </h3>
                <p className="text-xs text-slate-500">
                  {selectedDetail === 'CASH' && 'যে অ্যাকাউন্টগুলোতে ব্যালেন্স রয়েছে'}
                  {selectedDetail === 'LENT' && 'নামে ক্লিক করে স্লিপ দেখুন বা শেয়ার করুন'}
                  {selectedDetail === 'LOAN' && 'কার কাছে কত টাকা দেনা রয়েছে'}
                  {selectedDetail === 'EXPENSE' && 'এই মাসের মোট খরচ ও বাকি'}
                  {selectedDetail === 'DPS' && 'চলতি ডিপিএস হিসাবসমূহ'}
                  {selectedDetail === 'ASSET' && 'জমি ও অন্যান্য সম্পদের মূল্য'}
                </p>
              </div>
              <button
                onClick={() => setSelectedDetail(null)}
                className="p-1.5 rounded-full hover:bg-slate-200 text-slate-500 transition-colors"
              >
                <X size={20} />
              </button>
            </div>

            {/* Modal List Content */}
            <div className="flex-1 overflow-y-auto p-4 space-y-2 divide-y divide-slate-100">
              {/* CASH: accounts */}
              {selectedDetail === 'CASH' && (
                <>
                  {accounts
                    .filter((a) => ['cash', 'mobile', 'bank'].includes(a.type) && a.balance !== 0)
                    .map((a) => (
                      <div key={a.id} className="pt-2 first:pt-0 flex items-center justify-between py-2">
                        <div className="flex items-center gap-3">
                          <AccountAvatar account={a} size="md" />
                          <div>
                            <p className="font-bold text-slate-800 text-sm">{a.name}</p>
                            <p className="text-xs text-slate-400 capitalize">{a.type}</p>
                          </div>
                        </div>
                        <p className="font-bold text-slate-900 text-base">{tk(a.balance)}</p>
                      </div>
                    ))}
                  <div className="pt-3 border-t-2 border-slate-200 flex justify-between font-bold text-emerald-700">
                    <span>মোট ক্যাশ:</span>
                    <span>{tk(summary.cash)}</span>
                  </div>
                </>
              )}

              {/* LENT: creditors */}
              {selectedDetail === 'LENT' && (
                <>
                  {lentGrouped.map(([person, amount]) => (
                    <div
                      key={person}
                      onClick={() => setHistoryTarget({ kind: 'lent', person })}
                      className="pt-2 first:pt-0 flex items-center justify-between py-2.5 px-2 hover:bg-slate-50 rounded-xl cursor-pointer group"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center font-bold text-sm">
                          {person[0]}
                        </div>
                        <div>
                          <p className="font-bold text-slate-800 text-sm group-hover:text-emerald-700 transition-colors">
                            {person}
                          </p>
                          <span className="text-[11px] text-emerald-600 font-medium">স্লিপ দেখুন →</span>
                        </div>
                      </div>
                      <p className="font-bold text-emerald-600 text-base">{tk(amount)}</p>
                    </div>
                  ))}
                  {lentGrouped.length === 0 && (
                    <div className="py-8 text-center text-slate-400 text-sm">
                      কোনো পাওনা হিসাব নেই
                    </div>
                  )}
                </>
              )}

              {/* LOAN: borrowers */}
              {selectedDetail === 'LOAN' && (
                <>
                  {loanGrouped.map(([person, amount]) => (
                    <div
                      key={person}
                      onClick={() => setHistoryTarget({ kind: 'loan', person })}
                      className="pt-2 first:pt-0 flex items-center justify-between py-2.5 px-2 hover:bg-slate-50 rounded-xl cursor-pointer group"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-red-50 text-red-600 flex items-center justify-center font-bold text-sm">
                          {person[0]}
                        </div>
                        <div>
                          <p className="font-bold text-slate-800 text-sm group-hover:text-red-700 transition-colors">
                            {person}
                          </p>
                          <span className="text-[11px] text-red-600 font-medium">স্লিপ দেখুন →</span>
                        </div>
                      </div>
                      <p className="font-bold text-red-600 text-base">{tk(amount)}</p>
                    </div>
                  ))}
                  {loanGrouped.length === 0 && (
                    <div className="py-8 text-center text-slate-400 text-sm">
                      কোনো দেনা / লোন হিসাব নেই
                    </div>
                  )}
                </>
              )}

              {/* EXPENSE: Month Expenses + Baki */}
              {selectedDetail === 'EXPENSE' && (
                <>
                  {debts.shop.filter((d) => d.remaining > 0).length > 0 && (
                    <div className="pb-3 mb-2 bg-amber-50 p-3 rounded-xl border border-amber-200">
                      <p className="text-xs font-bold text-amber-900 mb-2">দোকান বাকি (পরিশোধ করুন):</p>
                      <div className="space-y-1.5">
                        {debts.shop
                          .filter((d) => d.remaining > 0)
                          .map((d) => (
                            <div
                              key={d.id}
                              className="flex items-center justify-between text-xs bg-white p-2 rounded-lg border border-amber-100"
                            >
                              <div>
                                <span className="font-bold text-slate-800">{d.person}</span>
                                {d.note && <span className="text-slate-500"> • {d.note}</span>}
                              </div>
                              <div className="flex items-center gap-2">
                                <span className="font-bold text-red-600">{tk(d.remaining)}</span>
                                <button
                                  onClick={() => handleOpenPayBaki(d.id)}
                                  className="px-2 py-1 bg-amber-600 hover:bg-amber-700 text-white rounded-md text-[11px] font-bold"
                                >
                                  পরিশোধ
                                </button>
                              </div>
                            </div>
                          ))}
                      </div>
                    </div>
                  )}

                  <p className="text-xs font-bold text-slate-500 mt-2 mb-1">সকল খরচসমূহ:</p>
                  {monthExpenses.map((m, idx) => (
                    <div key={idx} className="pt-2 first:pt-0 flex items-center justify-between py-1.5">
                      <div>
                        <p className="text-sm font-semibold text-slate-800">{m.title}</p>
                        <p className="text-xs text-slate-400">{m.category}</p>
                      </div>
                      <div className="text-right">
                        <p className="font-bold text-purple-700 text-sm">{tk(m.amount)}</p>
                        {m.remaining > 0 && (
                          <span className="text-[10px] text-amber-700 bg-amber-100 px-1.5 py-0.5 rounded-sm">
                            বাকি {tk(m.remaining)}
                          </span>
                        )}
                      </div>
                    </div>
                  ))}
                  <div className="pt-3 border-t-2 border-slate-200 flex justify-between font-bold text-purple-800">
                    <span>মাসের মোট খরচ:</span>
                    <span>{tk(summary.monthExpense)}</span>
                  </div>
                </>
              )}

              {/* DPS */}
              {selectedDetail === 'DPS' && (
                <>
                  {accounts
                    .filter((a) => a.type === 'dps')
                    .map((a) => (
                      <div key={a.id} className="pt-2 first:pt-0 flex items-center justify-between py-2">
                        <div className="flex items-center gap-3">
                          <AccountAvatar account={a} size="md" />
                          <p className="font-bold text-slate-800 text-sm">{a.name}</p>
                        </div>
                        <p className="font-bold text-indigo-700 text-base">{tk(a.balance)}</p>
                      </div>
                    ))}
                  <div className="pt-3 border-t-2 border-slate-200 flex justify-between font-bold text-indigo-800">
                    <span>মোট ডিপিএস:</span>
                    <span>{tk(summary.dps)}</span>
                  </div>
                </>
              )}

              {/* ASSETS */}
              {selectedDetail === 'ASSET' && (
                <>
                  {accounts
                    .filter((a) => !['cash', 'mobile', 'bank', 'dps'].includes(a.type))
                    .map((a) => (
                      <div key={a.id} className="pt-2 first:pt-0 flex items-center justify-between py-2">
                        <div className="flex items-center gap-3">
                          <AccountAvatar account={a} size="md" />
                          <div>
                            <p className="font-bold text-slate-800 text-sm">{a.name}</p>
                            <p className="text-xs text-slate-400">{a.type}</p>
                          </div>
                        </div>
                        <p className="font-bold text-amber-900 text-base">{tk(a.balance)}</p>
                      </div>
                    ))}
                  <div className="pt-3 border-t-2 border-slate-200 flex justify-between font-bold text-amber-900">
                    <span>মোট সম্পদ:</span>
                    <span>{tk(summary.asset)}</span>
                  </div>
                </>
              )}
            </div>

            {/* Modal Footer */}
            <div className="p-3 border-t border-slate-200 bg-slate-50 flex justify-end">
              <button
                onClick={() => setSelectedDetail(null)}
                className="px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-200 rounded-xl transition-colors"
              >
                বন্ধ করুন
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Pay Baki Dialog */}
      {payBakiId && targetDebt && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full overflow-hidden">
            <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
              <h3 className="font-bold text-slate-800">বাকি পরিশোধ</h3>
              <button onClick={() => setPayBakiId(null)} className="text-slate-400 hover:text-slate-600">
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleExecutePayBaki} className="p-4 space-y-3">
              <div className="p-3 bg-amber-50 rounded-xl border border-amber-200 text-xs">
                <span className="font-bold text-slate-800 block text-sm">{targetDebt.person}</span>
                <span className="text-amber-800">মোট বাকি: {tk(targetDebt.remaining)}</span>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  কত টাকা পরিশোধ করছেন
                </label>
                <div className="relative">
                  <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-sm">
                    ৳
                  </span>
                  <input
                    type="number"
                    value={payAmount}
                    onChange={(e) => setPayAmount(e.target.value)}
                    max={targetDebt.remaining}
                    className="w-full pl-8 pr-3 py-2 text-sm bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 font-bold"
                    autoFocus
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  কোন অ্যাকাউন্ট থেকে কাটবে
                </label>
                <select
                  value={payAccId}
                  onChange={(e) => setPayAccId(Number(e.target.value))}
                  className="w-full p-2.5 text-sm bg-slate-50 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 font-medium"
                >
                  {accounts
                    .filter((a) => a.balance > 0)
                    .map((a) => (
                      <option key={a.id} value={a.id}>
                        {a.name} ({tk(a.balance)})
                      </option>
                    ))}
                </select>
              </div>
              <div className="pt-2 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setPayBakiId(null)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
                >
                  বাতিল
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs"
                >
                  পরিশোধ করুন
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* History Share Modal */}
      {historyTarget && (
        <HistoryShareModal
          person={historyTarget.person}
          kind={historyTarget.kind}
          items={getDebtHistory(historyTarget.kind, historyTarget.person)}
          onClose={() => setHistoryTarget(null)}
        />
      )}
    </div>
  );
};
