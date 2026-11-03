import { useState, useEffect, useCallback, useMemo } from 'react';
import { hisabStore } from './storage';
import { Account, Txn, Debt, Category, MonthItem, Summary, HistoryItem } from './models';

export function useHisab() {
  const [, setTick] = useState(0);
  const [filter, setFilter] = useState<string | null>(null);

  useEffect(() => {
    const unsub = hisabStore.subscribe(() => {
      setTick((t) => t + 1);
    });
    return unsub;
  }, []);

  const accounts = useMemo(() => hisabStore.getAccounts(), []);
  const txns = useMemo(() => hisabStore.getTxns(filter), [filter]);
  const monthExpenses = useMemo(() => hisabStore.getMonthItems(), []);
  const categories = useMemo(() => hisabStore.getCategories(), []);
  const usedCategories = useMemo(() => hisabStore.getUsedCategories(), []);
  const summary = useMemo(() => hisabStore.getSummary(), []);

  const debts = useMemo(() => ({
    loan: hisabStore.getDebts('loan'),
    lent: hisabStore.getDebts('lent'),
    shop: hisabStore.getDebts('shop'),
  }), []);

  const applyFilter = useCallback((cat: string | null) => {
    setFilter(cat);
  }, []);

  const addTxns = useCallback(
    (
      type: 'credit' | 'debit',
      splits: { accountId: number | null; amount: number }[],
      category: string,
      note: string,
      date?: string
    ) => {
      hisabStore.addTxns(type, splits, category, note, date);
    },
    []
  );

  const addBakiExpense = useCallback(
    (amount: number, category: string, note: string, date?: string) => {
      hisabStore.addBakiExpense(amount, category, note, date);
    },
    []
  );

  const deleteTxn = useCallback((id: number) => {
    hisabStore.deleteTxn(id);
  }, []);

  const transfer = useCallback((fromId: number, toId: number, amount: number) => {
    hisabStore.transfer(fromId, toId, amount);
  }, []);

  const addDebt = useCallback(
    (
      kind: 'loan' | 'lent' | 'shop',
      person: string,
      note: string,
      splits: { accountId: number | null; amount: number }[]
    ) => {
      hisabStore.addDebt(kind, person, note, splits);
    },
    []
  );

  const payDebt = useCallback((debtId: number, amount: number, accountId: number) => {
    hisabStore.payDebt(debtId, amount, accountId);
  }, []);

  const saveDebtEdit = useCallback(
    (debtId: number, newName: string, splits: { accountId: number; amount: number }[]) => {
      hisabStore.saveDebtEdit(debtId, newName, splits);
    },
    []
  );

  const deleteDebt = useCallback((debtId: number) => {
    hisabStore.deleteDebt(debtId);
  }, []);

  const getDebtHistory = useCallback((kind: 'loan' | 'lent' | 'shop', person: string): HistoryItem[] => {
    return hisabStore.getDebtHistory(kind, person);
  }, []);

  const addAccount = useCallback((name: string, type: string, opening: number, icon: string) => {
    return hisabStore.addAccount(name, type, opening, icon);
  }, []);

  const updateAccount = useCallback((id: number, name: string, icon?: string) => {
    hisabStore.updateAccount(id, name, icon);
  }, []);

  const deleteAccount = useCallback((id: number): boolean => {
    return hisabStore.deleteAccount(id);
  }, []);

  const addCategory = useCallback((name: string, kind: 'expense' | 'income') => {
    hisabStore.addCategory(name, kind);
  }, []);

  const renameCategory = useCallback((id: number, newName: string) => {
    hisabStore.renameCategory(id, newName);
  }, []);

  const deleteCategory = useCallback((id: number) => {
    hisabStore.deleteCategory(id);
  }, []);

  const exportXlsx = useCallback(() => {
    return hisabStore.exportXlsx();
  }, []);

  const importXlsx = useCallback((file: File) => {
    return hisabStore.importXlsx(file);
  }, []);

  const resetToDefault = useCallback(() => {
    hisabStore.resetToDefault();
  }, []);

  return {
    accounts,
    txns,
    debts,
    monthExpenses,
    categories,
    usedCategories,
    summary,
    filter,
    applyFilter,
    addTxns,
    addBakiExpense,
    deleteTxn,
    transfer,
    addDebt,
    payDebt,
    saveDebtEdit,
    deleteDebt,
    getDebtHistory,
    addAccount,
    updateAccount,
    deleteAccount,
    addCategory,
    renameCategory,
    deleteCategory,
    exportXlsx,
    importXlsx,
    resetToDefault,
  };
}
