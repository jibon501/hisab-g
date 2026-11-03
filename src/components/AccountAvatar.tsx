import React from 'react';
import { Account } from '../data/models';
import { findBank } from '../data/bankData';
import {
  Banknote,
  Smartphone,
  Landmark,
  PiggyBank,
  Building,
  Home,
  CreditCard,
  Wallet,
} from 'lucide-react';

export const ACC_ICON_CONFIG: Record<
  string,
  { label: string; icon: React.ElementType; color: string; bg: string }
> = {
  cash: { label: 'ক্যাশ', icon: Banknote, color: '#16a34a', bg: '#dcfce7' },
  phone: { label: 'বিকাশ / মোবাইল', icon: Smartphone, color: '#db2777', bg: '#fce7f3' },
  phone2: { label: 'নগদ / মোবাইল', icon: Smartphone, color: '#ea580c', bg: '#ffedd5' },
  bank: { label: 'ব্যাংক', icon: Landmark, color: '#2563eb', bg: '#dbeafe' },
  bank2: { label: 'ব্যাংক ২', icon: Landmark, color: '#0891b2', bg: '#cffafe' },
  card: { label: 'কার্ড', icon: CreditCard, color: '#9333ea', bg: '#f3e8ff' },
  wallet: { label: 'ওয়ালেট', icon: Wallet, color: '#475569', bg: '#f1f5f9' },
  savings: { label: 'ডিপিএস', icon: PiggyBank, color: '#4f46e5', bg: '#e0e7ff' },
  savings2: { label: 'ডিপিএস ২', icon: PiggyBank, color: '#0d9488', bg: '#ccfbf1' },
  land: { label: 'জমি', icon: Building, color: '#854d0e', bg: '#fef9c3' },
  home: { label: 'বাড়ি / ফ্ল্যাট', icon: Home, color: '#ca8a04', bg: '#fef08a' },
};

export function defaultIconKey(type: string): string {
  switch (type) {
    case 'cash':
      return 'cash';
    case 'mobile':
      return 'phone';
    case 'bank':
      return 'bank';
    case 'dps':
      return 'savings';
    case 'asset':
      return 'land';
    default:
      return 'wallet';
  }
}

export function getLogoForAccount(account: { name: string; type: string }): string | null {
  const n = account.name.toLowerCase();

  // 1. Direct bank/mfs lookup
  const bank = findBank(account.name);
  if (bank) return `/bank_icons/${bank.logo}.png`;

  // 2. Keyword heuristic
  if (n.includes('bkash') || n.includes('বিকাশ')) return '/bank_icons/mfs_02.png';
  if (n.includes('nagad') || n.includes('নগদ')) return '/bank_icons/mfs_03.png';
  if (n.includes('rocket') || n.includes('রকেট')) return '/bank_icons/mfs_04.png';
  if (n.includes('upay') || n.includes('উপায়') || n.includes('উপায়')) return '/bank_icons/mfs_05.png';
  if (n.includes('ab bank') || n.startsWith('ab ') || n === 'ab') return '/bank_icons/bank_02.png';
  if (n.includes('ific')) return '/bank_icons/bank_27.png';
  if (n.includes('islami') || n.includes('ইসলামী')) return '/bank_icons/bank_28.png';
  if (n.includes('brac') || n.includes('ব্র্যাক')) return '/bank_icons/bank_12.png';
  if (n.includes('city') || n.includes('সিটি')) return '/bank_icons/bank_15.png';
  if (n.includes('dbbl') || n.includes('dutch') || n.includes('ডাচ')) return '/bank_icons/bank_19.png';
  if (n.includes('sonali') || n.includes('সোনালী')) return '/bank_icons/bank_53.png';
  if (n.includes('janata') || n.includes('জনতা')) return '/bank_icons/bank_30.png';
  if (n.includes('agrani') || n.includes('অগ্রণী')) return '/bank_icons/bank_03.png';
  if (n.includes('rupali') || n.includes('রূপালী')) return '/bank_icons/bank_48.png';
  if (n.includes('pubali') || n.includes('পূবালী')) return '/bank_icons/bank_46.png';
  if (n.includes('eastern') || n.includes('ইস্টার্ন')) return '/bank_icons/bank_20.png';
  if (n.includes('prime') || n.includes('প্রাইম')) return '/bank_icons/bank_44.png';
  if (n.includes('dhaka bank') || n.includes('ঢাকা ব্যাংক')) return '/bank_icons/bank_18.png';
  if (n.includes('ucb') || n.includes('united commercial')) return '/bank_icons/bank_60.png';

  return null;
}

interface AccountAvatarProps {
  account: { name: string; type: string; icon?: string };
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export const AccountAvatar: React.FC<AccountAvatarProps> = ({
  account,
  size = 'md',
  className = '',
}) => {
  const logoUrl = getLogoForAccount(account);
  const iconKey = account.icon || defaultIconKey(account.type);
  const conf = ACC_ICON_CONFIG[iconKey] || ACC_ICON_CONFIG.wallet;
  const IconComponent = conf.icon;

  const sizeStyles = {
    sm: 'w-8 h-8 rounded-lg',
    md: 'w-11 h-11 rounded-xl',
    lg: 'w-14 h-14 rounded-2xl',
  };

  const imgSizeStyles = {
    sm: 'h-6 max-w-[36px]',
    md: 'h-8 max-w-[50px]',
    lg: 'h-10 max-w-[64px]',
  };

  if (logoUrl) {
    return (
      <div
        className={`flex items-center justify-center bg-white shadow-xs border border-slate-200 p-1 flex-shrink-0 overflow-hidden ${sizeStyles[size]} ${className}`}
      >
        <img
          src={logoUrl}
          alt={account.name}
          className={`object-contain ${imgSizeStyles[size]}`}
          onError={(e) => {
            // fallback if icon image missing
            (e.target as HTMLElement).style.display = 'none';
          }}
        />
      </div>
    );
  }

  const iconSizes = {
    sm: 16,
    md: 20,
    lg: 26,
  };

  return (
    <div
      className={`flex items-center justify-center flex-shrink-0 ${sizeStyles[size]} ${className}`}
      style={{ backgroundColor: conf.bg, color: conf.color }}
    >
      <IconComponent size={iconSizes[size]} />
    </div>
  );
};
