export interface Account {
  id: number;
  name: string;
  type: string; // 'cash' | 'mobile' | 'bank' | 'dps' | 'asset' | custom
  balance: number;
  opening: number;
  icon: string;
}

export interface Txn {
  id: number;
  type: 'credit' | 'debit';
  amount: number;
  accountId: number;
  accountName?: string;
  category: string;
  note: string;
  date: string; // ISO string e.g. "2026-11-03T10:00:00" or "2026-11-03"
  debtId?: number | null;
  isPayment?: boolean;
  ref?: string | null;
}

export interface Debt {
  id: number;
  kind: 'loan' | 'lent' | 'shop'; // loan: ধার নেওয়া, lent: ধার দেওয়া, shop: দোকান বাকি
  person: string;
  total: number;
  remaining: number;
  note: string;
  date: string;
  isExpense?: boolean;
  category?: string;
}

export interface Category {
  id: number;
  name: string;
  kind: 'expense' | 'income';
}

export interface HistoryItem {
  date: string;
  label: string;
  amount: number;
  payment: boolean;
}

export interface MonthItem {
  id?: number;
  title: string;
  amount: number;
  remaining: number;
  debtId?: number;
  date: string;
  category: string;
}

export interface Summary {
  cash: number;
  loan: number;
  lent: number;
  shop: number;
  dps: number;
  asset: number;
  monthExpense: number;
  monthBaki: number;
}

export const OTHER_ACCOUNT = "অন্যান্য";

export const DEFAULT_EXPENSE_CATS = [
  "খাবার",
  "বাজার",
  "বাসা ভাড়া",
  "যাতায়াত",
  "বিল",
  "চিকিৎসা",
  "শিক্ষা",
  "বিনোদন",
  "অন্যান্য"
];

export const DEFAULT_INCOME_CATS = [
  "বেতন",
  "ব্যবসা",
  "উপহার",
  "অন্যান্য আয়"
];

export const SYSTEM_CATS = [
  "ট্রান্সফার",
  "লোন গ্রহণ",
  "ধার প্রদান",
  "লোন পরিশোধ",
  "ধার ফেরত",
  "দোকান বাকি পরিশোধ"
];

export const TYPE_LABELS: Record<string, string> = {
  asset: "সম্পদ",
  dps: "ডিপিএস",
  bank: "ব্যাংক",
  mobile: "মোবাইল",
  cash: "নগদ",
  other: "অন্যান্য",
};

export function typeLabel(type: string): string {
  return TYPE_LABELS[type] || type;
}

export const Kind = {
  LOAN: "loan" as const,
  LENT: "lent" as const,
  SHOP: "shop" as const,
};

/** Convert Western digits to Bengali digits */
export function toBnDigits(val: number | string): string {
  const bnDigits = ['০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯'];
  return String(val).replace(/\d/g, (d) => bnDigits[parseInt(d, 10)]);
}

/** Format currency with Taka symbol: e.g. ৳ ১,২৫০ */
export function tk(amount: number, useBnDigits = true): string {
  const rounded = Math.round(amount);
  const formatted = new Intl.NumberFormat('en-US').format(rounded);
  if (!useBnDigits) {
    return `৳ ${formatted}`;
  }
  return `৳ ${toBnDigits(formatted)}`;
}

/** Format date string ISO to Bengali DD-MM-YYYY (e.g. ০৩-১১-২০২৬) */
export function fmtDate(iso: string, useBnDigits = true): string {
  if (!iso) return "";
  const dateOnly = iso.slice(0, 10);
  const parts = dateOnly.split("-");
  const formatted = parts.length === 3 ? `${parts[2]}-${parts[1]}-${parts[0]}` : dateOnly;
  return useBnDigits ? toBnDigits(formatted) : formatted;
}

/** Check if account has positive money */
export function withMoney(accounts: Account[]): Account[] {
  return accounts.filter((a) => a.balance > 0);
}
