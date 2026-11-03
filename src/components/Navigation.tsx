import React from 'react';
import {
  LayoutDashboard,
  Receipt,
  TrendingUp,
  TrendingDown,
  WalletCards,
  Plus,
} from 'lucide-react';
import { tk } from '../data/models';
import { useHisab } from '../data/useHisab';

export type TabType = 'dashboard' | 'txns' | 'lent' | 'loan' | 'accounts';

interface NavigationProps {
  activeTab: TabType;
  onTabChange: (tab: TabType) => void;
  onOpenAddTxn: () => void;
}

export const Navigation: React.FC<NavigationProps> = ({
  activeTab,
  onTabChange,
  onOpenAddTxn,
}) => {
  const { summary } = useHisab();

  const tabs: { id: TabType; label: string; icon: React.ElementType }[] = [
    { id: 'dashboard', label: 'ড্যাশবোর্ড', icon: LayoutDashboard },
    { id: 'txns', label: 'লেনদেন', icon: Receipt },
    { id: 'lent', label: 'ধার দেওয়া', icon: TrendingUp },
    { id: 'loan', label: 'ধার নেওয়া', icon: TrendingDown },
    { id: 'accounts', label: 'অ্যাকাউন্ট', icon: WalletCards },
  ];

  return (
    <>
      {/* Top Navbar */}
      <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-slate-200/80 px-4 py-3 shadow-2xs">
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-emerald-600 text-white font-extrabold flex items-center justify-center text-lg shadow-xs shadow-emerald-600/30">
              হি
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <span className="font-extrabold text-slate-900 tracking-tight text-base sm:text-lg">
                  হিসাব
                </span>
                <span className="text-[10px] font-bold text-emerald-800 bg-emerald-100 px-1.5 py-0.5 rounded-sm">
                  HISAB
                </span>
              </div>
              <p className="text-[11px] text-slate-500 hidden sm:block">
                ব্যক্তিগত আয়, ব্যয় ও দেনা-পাওনা ব্যবস্থাপক
              </p>
            </div>
          </div>

          {/* Desktop Tab Links */}
          <div className="hidden md:flex items-center gap-1 bg-slate-100 p-1 rounded-2xl">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              const isActive = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => onTabChange(tab.id)}
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                    isActive
                      ? 'bg-white text-emerald-700 shadow-xs'
                      : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  <Icon size={15} />
                  <span>{tab.label}</span>
                </button>
              );
            })}
          </div>

          {/* Quick Cash Pill & Action */}
          <div className="flex items-center gap-2">
            <div className="hidden sm:flex flex-col text-right pr-1">
              <span className="text-[10px] font-medium text-slate-400">ক্যাশ ব্যালেন্স</span>
              <span className="text-xs font-bold text-emerald-700">{tk(summary.cash)}</span>
            </div>
            <button
              onClick={onOpenAddTxn}
              className="flex items-center gap-1.5 bg-emerald-600 hover:bg-emerald-700 text-white px-3 py-1.5 sm:px-3.5 sm:py-2 rounded-xl text-xs sm:text-sm font-bold shadow-xs shadow-emerald-600/20 transition-all hover:scale-102 active:scale-98"
            >
              <Plus size={16} />
              <span className="hidden xs:inline">নতুন লেনদেন</span>
              <span className="xs:hidden">যোগ</span>
            </button>
          </div>
        </div>
      </header>

      {/* Bottom Nav Bar (Mobile / Tablet) */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-white/95 backdrop-blur-md border-t border-slate-200 px-2 py-1.5 shadow-lg">
        <div className="max-w-md mx-auto grid grid-cols-5 gap-1">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => onTabChange(tab.id)}
                className={`flex flex-col items-center justify-center py-1.5 px-1 rounded-xl transition-all ${
                  isActive
                    ? 'text-emerald-700 font-bold bg-emerald-50/70'
                    : 'text-slate-500 hover:text-slate-700 font-medium'
                }`}
              >
                <Icon size={19} className={isActive ? 'stroke-[2.5]' : 'stroke-[1.8]'} />
                <span className="text-[10px] mt-0.5 leading-tight">{tab.label}</span>
              </button>
            );
          })}
        </div>
      </nav>
    </>
  );
};
