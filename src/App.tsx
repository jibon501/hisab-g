import React, { useState } from 'react';
import { Navigation, TabType } from './components/Navigation';
import { DashboardScreen } from './components/DashboardScreen';
import { TransactionsScreen } from './components/TransactionsScreen';
import { DebtsScreen } from './components/DebtsScreen';
import { AccountsScreen } from './components/AccountsScreen';
import { AddTxnDialog } from './components/AddTxnDialog';
import { useHisab } from './data/useHisab';

export default function App() {
  const [activeTab, setActiveTab] = useState<TabType>('dashboard');
  const [globalAddTxn, setGlobalAddTxn] = useState(false);

  const { accounts, categories, addTxns, addBakiExpense, addCategory } = useHisab();

  const expenseCats = categories.filter((c) => c.kind === 'expense').map((c) => c.name);
  const incomeCats = categories.filter((c) => c.kind === 'income').map((c) => c.name);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col text-slate-900 selection:bg-emerald-100 selection:text-emerald-900">
      {/* Navigation header and bottom bar */}
      <Navigation
        activeTab={activeTab}
        onTabChange={setActiveTab}
        onOpenAddTxn={() => setGlobalAddTxn(true)}
      />

      {/* Main Container */}
      <main className="flex-1 max-w-4xl w-full mx-auto px-3 sm:px-6 pt-4 sm:pt-6">
        {activeTab === 'dashboard' && <DashboardScreen />}
        {activeTab === 'txns' && <TransactionsScreen />}
        {activeTab === 'lent' && <DebtsScreen kind="lent" />}
        {activeTab === 'loan' && <DebtsScreen kind="loan" />}
        {activeTab === 'accounts' && <AccountsScreen />}
      </main>

      {/* Global Add Transaction Dialog */}
      {globalAddTxn && (
        <AddTxnDialog
          accounts={accounts}
          expenseCats={expenseCats}
          incomeCats={incomeCats}
          onClose={() => setGlobalAddTxn(false)}
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
            setGlobalAddTxn(false);
          }}
        />
      )}
    </div>
  );
}
