import * as XLSX from 'xlsx';
import {
  Account,
  Txn,
  Debt,
  Category,
  HistoryItem,
  MonthItem,
  Summary,
  Kind,
  DEFAULT_EXPENSE_CATS,
  DEFAULT_INCOME_CATS,
  OTHER_ACCOUNT,
} from './models';

const STORAGE_KEY = 'hisab_app_state_v1';

interface StoredData {
  accounts: { id: number; name: string; type: string; opening: number; icon: string }[];
  txns: {
    id: number;
    type: 'credit' | 'debit';
    amount: number;
    accountId: number;
    category: string;
    note: string;
    date: string;
    debtId?: number | null;
    isPayment?: boolean;
    ref?: string | null;
  }[];
  debts: {
    id: number;
    kind: 'loan' | 'lent' | 'shop';
    person: string;
    total: number;
    note: string;
    date: string;
    isExpense?: boolean;
    category?: string;
  }[];
  categories: Category[];
}

const INITIAL_ACCOUNTS: StoredData['accounts'] = [
  { id: 1, name: "ক্যাশ", type: "cash", opening: 15000, icon: "cash" },
  { id: 2, name: "বিকাশ", type: "mobile", opening: 8500, icon: "phone" },
  { id: 3, name: "নগদ", type: "mobile", opening: 3200, icon: "phone2" },
  { id: 4, name: "AB Bank", type: "bank", opening: 45000, icon: "bank" },
  { id: 5, name: "IFIC Bank", type: "bank", opening: 28000, icon: "bank2" },
  { id: 6, name: "DPS ১", type: "dps", opening: 50000, icon: "savings" },
  { id: 7, name: "DPS ২", type: "dps", opening: 30000, icon: "savings2" },
  { id: 8, name: "জমি (ইনভেস্ট)", type: "asset", opening: 500000, icon: "land" },
  { id: 9, name: OTHER_ACCOUNT, type: "asset", opening: 0, icon: "" },
];

const INITIAL_CATEGORIES: Category[] = [
  ...DEFAULT_EXPENSE_CATS.map((name, i) => ({ id: i + 1, name, kind: 'expense' as const })),
  ...DEFAULT_INCOME_CATS.map((name, i) => ({ id: i + 100, name, kind: 'income' as const })),
];

const now = new Date();
const currentIso = now.toISOString();
const yesterdayIso = new Date(now.getTime() - 86400000).toISOString();
const fiveDaysAgoIso = new Date(now.getTime() - 86400000 * 5).toISOString();

const INITIAL_DEBTS: StoredData['debts'] = [
  {
    id: 1,
    kind: 'lent',
    person: 'রাকিব হাসান',
    total: 10000,
    note: 'ব্যবসায়িক প্রয়োজনে ধার নিয়েছিল',
    date: fiveDaysAgoIso,
  },
  {
    id: 2,
    kind: 'lent',
    person: 'তানভীর আহমেদ',
    total: 5000,
    note: 'মেডিকেল বিলের জন্য ধার',
    date: yesterdayIso,
  },
  {
    id: 3,
    kind: 'loan',
    person: 'চাচা',
    total: 25000,
    note: 'জরুরি পারিবারিক লোন',
    date: fiveDaysAgoIso,
  },
  {
    id: 4,
    kind: 'shop',
    person: 'আল-মদিনা স্টোর',
    total: 1450,
    note: 'মাসিক মুদি বাজার বাকি',
    date: currentIso,
    isExpense: true,
    category: 'বাজার',
  },
];

const INITIAL_TXNS: StoredData['txns'] = [
  {
    id: 1,
    type: 'credit',
    amount: 65000,
    accountId: 4, // AB Bank
    category: 'বেতন',
    note: 'অক্টোবর মাসের বেতন',
    date: fiveDaysAgoIso,
  },
  {
    id: 2,
    type: 'debit',
    amount: 15000,
    accountId: 4,
    category: 'বাসা ভাড়া',
    note: 'চলতি মাসের ফ্ল্যাট ভাড়া পরিশোধ',
    date: yesterdayIso,
  },
  {
    id: 3,
    type: 'debit',
    amount: 3200,
    accountId: 1, // ক্যাশ
    category: 'বাজার',
    note: 'কাঁচাবাজার ও মাছ',
    date: yesterdayIso,
  },
  {
    id: 4,
    type: 'debit',
    amount: 10000,
    accountId: 1,
    category: 'ধার প্রদান',
    note: 'রাকিব হাসান',
    date: fiveDaysAgoIso,
    debtId: 1,
    isPayment: false,
  },
  {
    id: 5,
    type: 'credit',
    amount: 4000,
    accountId: 2, // bKash
    category: 'ধার ফেরত',
    note: 'রাকিব হাসান',
    date: yesterdayIso,
    debtId: 1,
    isPayment: true,
  },
  {
    id: 6,
    type: 'debit',
    amount: 5000,
    accountId: 2,
    category: 'ধার প্রদান',
    note: 'তানভীর আহমেদ',
    date: yesterdayIso,
    debtId: 2,
    isPayment: false,
  },
  {
    id: 7,
    type: 'credit',
    amount: 25000,
    accountId: 4,
    category: 'লোন গ্রহণ',
    note: 'চাচা',
    date: fiveDaysAgoIso,
    debtId: 3,
    isPayment: false,
  },
  {
    id: 8,
    type: 'debit',
    amount: 5000,
    accountId: 1,
    category: 'লোন পরিশোধ',
    note: 'চাচা',
    date: currentIso,
    debtId: 3,
    isPayment: true,
  },
  {
    id: 9,
    type: 'debit',
    amount: 1250,
    accountId: 2,
    category: 'বিল',
    note: 'ইন্টারনেট বিল ও বিদ্যুৎ বিল',
    date: currentIso,
  },
];

export class HisabStore {
  private data: StoredData;
  private listeners: (() => void)[] = [];

  constructor() {
    this.data = this.loadFromStorage();
  }

  private loadFromStorage(): StoredData {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw);
        if (parsed && Array.isArray(parsed.accounts) && Array.isArray(parsed.txns)) {
          return parsed;
        }
      }
    } catch {
      // fallback to initial
    }
    return {
      accounts: INITIAL_ACCOUNTS,
      txns: INITIAL_TXNS,
      debts: INITIAL_DEBTS,
      categories: INITIAL_CATEGORIES,
    };
  }

  private persist() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.data));
    } catch {
      // ignore
    }
    this.notify();
  }

  public subscribe(cb: () => void): () => void {
    this.listeners.push(cb);
    return () => {
      this.listeners = this.listeners.filter((l) => l !== cb);
    };
  }

  private notify() {
    this.listeners.forEach((l) => l());
  }

  public resetToDefault() {
    this.data = {
      accounts: INITIAL_ACCOUNTS,
      txns: INITIAL_TXNS,
      debts: INITIAL_DEBTS,
      categories: INITIAL_CATEGORIES,
    };
    this.persist();
  }

  // ---------------- Computations ----------------

  public getAccounts(): Account[] {
    return this.data.accounts.map((acc) => {
      // Balance = opening + credits - debits
      const credits = this.data.txns
        .filter((t) => t.accountId === acc.id && t.type === 'credit')
        .reduce((sum, t) => sum + t.amount, 0);
      const debits = this.data.txns
        .filter((t) => t.accountId === acc.id && t.type === 'debit')
        .reduce((sum, t) => sum + t.amount, 0);

      const balance = acc.opening + credits - debits;
      return {
        id: acc.id,
        name: acc.name,
        type: acc.type,
        balance,
        opening: acc.opening || 0,
        icon: acc.icon,
      };
    });
  }

  public getAccountById(id: number): Account | undefined {
    return this.getAccounts().find((a) => a.id === id);
  }

  public getDebts(kind?: 'loan' | 'lent' | 'shop'): Debt[] {
    const list = kind ? this.data.debts.filter((d) => d.kind === kind) : this.data.debts;

    return list.map((d) => {
      // Calculate remaining
      // For both loan and lent, repayment transactions have debtId === d.id and isPayment === true
      const paid = this.data.txns
        .filter((t) => t.debtId === d.id && t.isPayment)
        .reduce((sum, t) => sum + t.amount, 0);
      const remaining = Math.max(0, d.total - paid);

      return {
        ...d,
        remaining,
      };
    });
  }

  public getDebtById(id: number): Debt | undefined {
    return this.getDebts().find((d) => d.id === id);
  }

  public getDebtHistory(kind: 'loan' | 'lent' | 'shop', person: string): HistoryItem[] {
    const matchingDebts = this.data.debts.filter(
      (d) => d.kind === kind && d.person.trim().toLowerCase() === person.trim().toLowerCase()
    );
    const debtIds = new Set(matchingDebts.map((d) => d.id));

    const items: HistoryItem[] = [];

    matchingDebts.forEach((d) => {
      const isShop = d.kind === 'shop';
      const label = isShop
        ? d.category || 'দোকান বাকি'
        : d.kind === 'loan'
        ? 'লোন গ্রহণ'
        : 'ধার প্রদান';
      items.push({
        date: d.date,
        label,
        amount: d.total,
        payment: false,
      });
    });

    this.data.txns
      .filter((t) => t.debtId && debtIds.has(t.debtId) && t.isPayment)
      .forEach((t) => {
        items.push({
          date: t.date,
          label: t.category || (kind === 'lent' ? 'ধার ফেরত' : 'পরিশোধ'),
          amount: t.amount,
          payment: true,
        });
      });

    return items.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
  }

  public getTxns(categoryFilter?: string | null): Txn[] {
    const accMap = new Map(this.data.accounts.map((a) => [a.id, a.name]));
    let list = this.data.txns.map((t) => ({
      ...t,
      accountName: accMap.get(t.accountId) || OTHER_ACCOUNT,
    }));

    if (categoryFilter) {
      list = list.filter((t) => t.category === categoryFilter);
    }

    return list.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  }

  public getUsedCategories(): string[] {
    const cats = new Set<string>();
    this.data.txns.forEach((t) => {
      if (t.category) cats.add(t.category);
    });
    return Array.from(cats);
  }

  public getCategories(): Category[] {
    return this.data.categories;
  }

  public getMonthItems(): MonthItem[] {
    const currentYearMonth = new Date().toISOString().slice(0, 7); // e.g. "2026-11"

    const items: MonthItem[] = [];

    // All debits in current month
    this.data.txns
      .filter((t) => t.type === 'debit' && t.date.startsWith(currentYearMonth))
      .forEach((t) => {
        items.push({
          id: t.id,
          title: t.note ? `${t.category} (${t.note})` : t.category,
          amount: t.amount,
          remaining: 0,
          date: t.date,
          category: t.category,
        });
      });

    // All shop baki debts in current month
    this.getDebts('shop')
      .filter((d) => d.date.startsWith(currentYearMonth))
      .forEach((d) => {
        items.push({
          id: 100000 + d.id,
          title: d.note ? `${d.person} - ${d.note}` : d.person,
          amount: d.total,
          remaining: d.remaining,
          debtId: d.id,
          date: d.date,
          category: d.category || 'দোকান বাকি',
        });
      });

    return items.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  }

  public getSummary(): Summary {
    const accounts = this.getAccounts();
    const debts = this.getDebts();
    const month = this.getMonthItems();

    const cashTypes = new Set(['cash', 'mobile', 'bank']);
    const cash = accounts
      .filter((a) => cashTypes.has(a.type))
      .reduce((sum, a) => sum + a.balance, 0);

    const dps = accounts
      .filter((a) => a.type === 'dps')
      .reduce((sum, a) => sum + a.balance, 0);

    const asset = accounts
      .filter((a) => !cashTypes.has(a.type) && a.type !== 'dps')
      .reduce((sum, a) => sum + a.balance, 0);

    const loan = debts
      .filter((d) => d.kind === 'loan')
      .reduce((sum, d) => sum + d.remaining, 0);

    const lent = debts
      .filter((d) => d.kind === 'lent')
      .reduce((sum, d) => sum + d.remaining, 0);

    const shop = debts
      .filter((d) => d.kind === 'shop')
      .reduce((sum, d) => sum + d.remaining, 0);

    const monthExpense = month.reduce((sum, m) => sum + m.amount, 0);
    const monthBaki = month.reduce((sum, m) => sum + m.remaining, 0);

    return {
      cash,
      loan,
      lent,
      shop,
      dps,
      asset,
      monthExpense,
      monthBaki,
    };
  }

  // ---------------- Mutations ----------------

  public addAccount(name: string, type: string, opening: number, icon: string): Account {
    const newId = this.data.accounts.length > 0 ? Math.max(...this.data.accounts.map((a) => a.id)) + 1 : 1;
    this.data.accounts.push({
      id: newId,
      name,
      type,
      opening,
      icon,
    });
    this.persist();
    return this.getAccountById(newId)!;
  }

  public updateAccount(id: number, name: string, icon?: string) {
    const target = this.data.accounts.find((a) => a.id === id);
    if (target) {
      target.name = name;
      if (icon !== undefined) target.icon = icon;
      this.persist();
    }
  }

  public deleteAccount(id: number): boolean {
    const hasTxns = this.data.txns.some((t) => t.accountId === id);
    if (hasTxns) return false;
    this.data.accounts = this.data.accounts.filter((a) => a.id !== id);
    this.persist();
    return true;
  }

  public addCategory(name: string, kind: 'expense' | 'income') {
    const trimmed = name.trim();
    if (!trimmed) return;
    if (this.data.categories.some((c) => c.name.toLowerCase() === trimmed.toLowerCase() && c.kind === kind)) {
      return;
    }
    const newId = this.data.categories.length > 0 ? Math.max(...this.data.categories.map((c) => c.id)) + 1 : 1;
    this.data.categories.push({ id: newId, name: trimmed, kind });
    this.persist();
  }

  public renameCategory(id: number, newName: string) {
    const c = this.data.categories.find((cat) => cat.id === id);
    if (c && newName.trim()) {
      const oldName = c.name;
      c.name = newName.trim();
      this.data.txns.forEach((t) => {
        if (t.category === oldName) t.category = c.name;
      });
      this.persist();
    }
  }

  public deleteCategory(id: number) {
    this.data.categories = this.data.categories.filter((c) => c.id !== id);
    this.persist();
  }

  public addTxns(
    type: 'credit' | 'debit',
    splits: { accountId: number | null; amount: number }[],
    category: string,
    note: string,
    date: string = new Date().toISOString()
  ) {
    const defaultAcc = this.data.accounts.find((a) => a.name === OTHER_ACCOUNT)?.id || this.data.accounts[0].id;
    let nextId = this.data.txns.length > 0 ? Math.max(...this.data.txns.map((t) => t.id)) + 1 : 1;

    splits.forEach((s) => {
      if (s.amount > 0) {
        this.data.txns.push({
          id: nextId++,
          type,
          amount: s.amount,
          accountId: s.accountId || defaultAcc,
          category,
          note,
          date,
        });
      }
    });

    this.persist();
  }

  public addBakiExpense(amount: number, category: string, note: string, date: string = new Date().toISOString()) {
    const newDebtId = this.data.debts.length > 0 ? Math.max(...this.data.debts.map((d) => d.id)) + 1 : 1;
    this.data.debts.push({
      id: newDebtId,
      kind: 'shop',
      person: note.trim() || category,
      total: amount,
      note: note.trim(),
      date,
      isExpense: true,
      category,
    });
    this.persist();
  }

  public deleteTxn(id: number) {
    this.data.txns = this.data.txns.filter((t) => t.id !== id);
    this.persist();
  }

  public transfer(fromId: number, toId: number, amount: number) {
    if (amount <= 0 || fromId === toId) return;
    const fromAcc = this.data.accounts.find((a) => a.id === fromId);
    const toAcc = this.data.accounts.find((a) => a.id === toId);
    if (!fromAcc || !toAcc) return;

    let nextId = this.data.txns.length > 0 ? Math.max(...this.data.txns.map((t) => t.id)) + 1 : 1;
    const nowIso = new Date().toISOString();

    // Outflow from source
    this.data.txns.push({
      id: nextId++,
      type: 'debit',
      amount,
      accountId: fromId,
      category: 'ট্রান্সফার',
      note: `→ ${toAcc.name}`,
      date: nowIso,
    });

    // Inflow to destination
    this.data.txns.push({
      id: nextId++,
      type: 'credit',
      amount,
      accountId: toId,
      category: 'ট্রান্সফার',
      note: `${fromAcc.name} →`,
      date: nowIso,
    });

    this.persist();
  }

  public addDebt(
    kind: 'loan' | 'lent' | 'shop',
    person: string,
    note: string,
    splits: { accountId: number | null; amount: number }[]
  ) {
    const totalAmount = splits.reduce((sum, s) => sum + s.amount, 0);
    if (totalAmount <= 0) return;

    const newDebtId = this.data.debts.length > 0 ? Math.max(...this.data.debts.map((d) => d.id)) + 1 : 1;
    const nowIso = new Date().toISOString();

    this.data.debts.push({
      id: newDebtId,
      kind,
      person,
      total: totalAmount,
      note,
      date: nowIso,
    });

    if (kind !== 'shop') {
      let nextTxnId = this.data.txns.length > 0 ? Math.max(...this.data.txns.map((t) => t.id)) + 1 : 1;
      splits.forEach((s) => {
        if (s.accountId && s.amount > 0) {
          this.data.txns.push({
            id: nextTxnId++,
            type: kind === 'loan' ? 'credit' : 'debit',
            amount: s.amount,
            accountId: s.accountId,
            category: kind === 'loan' ? 'লোন গ্রহণ' : 'ধার প্রদান',
            note: person,
            date: nowIso,
            debtId: newDebtId,
            isPayment: false,
          });
        }
      });
    }

    this.persist();
  }

  public payDebt(debtId: number, amount: number, accountId: number) {
    const debt = this.getDebtById(debtId);
    if (!debt || amount <= 0) return;

    let nextTxnId = this.data.txns.length > 0 ? Math.max(...this.data.txns.map((t) => t.id)) + 1 : 1;
    const nowIso = new Date().toISOString();

    this.data.txns.push({
      id: nextTxnId,
      type: debt.kind === 'lent' ? 'credit' : 'debit',
      amount,
      accountId,
      category:
        debt.kind === 'lent'
          ? 'ধার ফেরত'
          : debt.kind === 'loan'
          ? 'লোন পরিশোধ'
          : 'দোকান বাকি পরিশোধ',
      note: debt.person,
      date: nowIso,
      debtId: debt.id,
      isPayment: true,
    });

    this.persist();
  }

  public saveDebtEdit(debtId: number, newName: string, splits: { accountId: number; amount: number }[]) {
    const debt = this.getDebtById(debtId);
    if (!debt) return;

    // Rename
    if (newName.trim() && newName.trim() !== debt.person) {
      const storedDebt = this.data.debts.find((d) => d.id === debtId);
      if (storedDebt) storedDebt.person = newName.trim();
      this.data.txns.forEach((t) => {
        if (t.debtId === debtId) t.note = newName.trim();
      });
    }

    // Repayments
    splits.forEach((s) => {
      if (s.amount > 0 && s.accountId) {
        this.payDebt(debtId, s.amount, s.accountId);
      }
    });

    this.persist();
  }

  public deleteDebt(debtId: number) {
    this.data.txns = this.data.txns.filter((t) => t.debtId !== debtId);
    this.data.debts = this.data.debts.filter((d) => d.id !== debtId);
    this.persist();
  }

  // ---------------- Excel Import / Export ----------------

  public exportXlsx(): Blob {
    const wb = XLSX.utils.book_new();

    // 1. Accounts sheet
    const accRows = this.data.accounts.map((a) => ({
      id: a.id,
      name: a.name,
      type: a.type,
      opening: a.opening,
      icon: a.icon || '',
    }));
    const wsAcc = XLSX.utils.json_to_sheet(accRows);
    XLSX.utils.book_append_sheet(wb, wsAcc, 'Accounts');

    // 2. Transactions sheet
    const txnRows = this.data.txns.map((t) => ({
      id: t.id,
      type: t.type,
      amount: t.amount,
      account_id: t.accountId,
      category: t.category,
      note: t.note || '',
      date: t.date,
      debt_id: t.debtId || '',
      is_payment: t.isPayment ? 1 : 0,
      ref: t.ref || '',
    }));
    const wsTxn = XLSX.utils.json_to_sheet(txnRows);
    XLSX.utils.book_append_sheet(wb, wsTxn, 'Transactions');

    // 3. Debts sheet
    const debtRows = this.data.debts.map((d) => ({
      id: d.id,
      kind: d.kind,
      person: d.person,
      total: d.total,
      note: d.note || '',
      date: d.date,
      is_expense: d.isExpense ? 1 : 0,
      category: d.category || '',
    }));
    const wsDebt = XLSX.utils.json_to_sheet(debtRows);
    XLSX.utils.book_append_sheet(wb, wsDebt, 'Debts');

    // 4. Categories sheet
    const catRows = this.data.categories.map((c) => ({
      id: c.id,
      name: c.name,
      kind: c.kind,
    }));
    const wsCat = XLSX.utils.json_to_sheet(catRows);
    XLSX.utils.book_append_sheet(wb, wsCat, 'Categories');

    const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
    return new Blob([wbout], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  }

  public async importXlsx(file: File): Promise<boolean> {
    try {
      const buffer = await file.arrayBuffer();
      const wb = XLSX.read(buffer, { type: 'array' });

      if (!wb.SheetNames.includes('Accounts') || !wb.SheetNames.includes('Transactions') || !wb.SheetNames.includes('Debts')) {
        throw new Error('সঠিক ফাইল নয় (Accounts, Transactions, Debts শিট প্রয়োজন)');
      }

      const accRows = XLSX.utils.sheet_to_json<any>(wb.Sheets['Accounts']);
      const txnRows = XLSX.utils.sheet_to_json<any>(wb.Sheets['Transactions']);
      const debtRows = XLSX.utils.sheet_to_json<any>(wb.Sheets['Debts']);
      const catRows = wb.SheetNames.includes('Categories')
        ? XLSX.utils.sheet_to_json<any>(wb.Sheets['Categories'])
        : [];

      this.data.accounts = accRows.map((r) => ({
        id: Number(r.id),
        name: String(r.name || ''),
        type: String(r.type || 'bank'),
        opening: Number(r.opening || 0),
        icon: String(r.icon || ''),
      }));

      this.data.txns = txnRows.map((r) => ({
        id: Number(r.id),
        type: (r.type === 'credit' ? 'credit' : 'debit') as 'credit' | 'debit',
        amount: Number(r.amount || 0),
        accountId: Number(r.account_id || r.accountId || 1),
        category: String(r.category || ''),
        note: String(r.note || ''),
        date: String(r.date || new Date().toISOString()),
        debtId: r.debt_id ? Number(r.debt_id) : null,
        isPayment: Number(r.is_payment) === 1 || r.isPayment === true,
        ref: r.ref ? String(r.ref) : null,
      }));

      this.data.debts = debtRows.map((r) => ({
        id: Number(r.id),
        kind: (r.kind === 'loan' ? 'loan' : r.kind === 'lent' ? 'lent' : 'shop') as 'loan' | 'lent' | 'shop',
        person: String(r.person || ''),
        total: Number(r.total || 0),
        note: String(r.note || ''),
        date: String(r.date || new Date().toISOString()),
        isExpense: Number(r.is_expense) === 1 || r.isExpense === true,
        category: r.category ? String(r.category) : '',
      }));

      if (catRows.length > 0) {
        this.data.categories = catRows.map((r) => ({
          id: Number(r.id),
          name: String(r.name || ''),
          kind: (r.kind === 'income' ? 'income' : 'expense') as 'income' | 'expense',
        }));
      }

      this.persist();
      return true;
    } catch (e) {
      console.error('Import failed', e);
      return false;
    }
  }
}

export const hisabStore = new HisabStore();
