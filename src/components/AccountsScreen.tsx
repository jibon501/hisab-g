import React, { useState, useRef } from 'react';
import { useHisab } from '../data/useHisab';
import { Account, tk, typeLabel, TYPE_LABELS } from '../data/models';
import { bankSuggestions, findBank } from '../data/bankData';
import { AccountAvatar, defaultIconKey, ACC_ICON_CONFIG } from './AccountAvatar';
import {
  Plus,
  FileSpreadsheet,
  Upload,
  Download,
  Trash2,
  Edit2,
  X,
  AlertTriangle,
  RotateCcw,
  CheckCircle,
  Building2,
  Wallet,
} from 'lucide-react';

export const AccountsScreen: React.FC = () => {
  const {
    accounts,
    addAccount,
    updateAccount,
    deleteAccount,
    exportXlsx,
    importXlsx,
    resetToDefault,
  } = useHisab();

  const [showAdd, setShowAdd] = useState(false);
  const [editingAcc, setEditingAcc] = useState<Account | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<Account | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [importStatus, setImportStatus] = useState<string | null>(null);
  const [showResetConfirm, setShowResetConfirm] = useState(false);

  const fileInputRef = useRef<HTMLInputElement | null>(null);

  // Custom types added by user
  const customTypes = Array.from(
    new Set(accounts.map((a) => a.type).filter((t) => !(t in TYPE_LABELS)))
  );

  const typeOrder = [
    'cash',
    'mobile',
    'bank',
    'dps',
    'asset',
    ...customTypes,
    'other',
  ];

  // Excel Export Handler
  const handleExport = () => {
    try {
      const blob = exportXlsx();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `hisab-backup-${new Date().toISOString().slice(0, 10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      alert('এক্সপোর্ট করা গেল না');
    }
  };

  // Excel Import Handler
  const handleImport = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const ok = await importXlsx(file);
    if (ok) {
      setImportStatus('ডেটা সফলভাবে ইম্পোর্ট করা হয়েছে!');
      setTimeout(() => setImportStatus(null), 3000);
    } else {
      alert('ইম্পোর্ট সফল হয়নি। ফাইলটি সঠিক Hisab এক্সেল ব্যাকআপ নয়।');
    }
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleDelete = (acc: Account) => {
    const success = deleteAccount(acc.id);
    if (!success) {
      setDeleteError(`"${acc.name}" অ্যাকাউন্টে লেনদেন বিদ্যমান রয়েছে, তাই এটি মোছা যাবে না।`);
    } else {
      setDeleteConfirm(null);
      setDeleteError(null);
    }
  };

  return (
    <div className="space-y-4 pb-24">
      {/* Excel Export / Import Bar */}
      <div className="bg-white p-3.5 rounded-2xl border border-slate-200 shadow-xs flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <FileSpreadsheet className="text-emerald-600" size={20} />
          <div>
            <span className="font-bold text-slate-800 text-xs sm:text-sm block">
              এক্সেল ব্যাকআপ ও রিস্টোর
            </span>
            <span className="text-[11px] text-slate-500">
              সমস্ত ডেটা .xlsx ফাইলে সংরক্ষণ বা রিস্টোর করুন
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <input
            type="file"
            ref={fileInputRef}
            accept=".xlsx"
            onChange={handleImport}
            className="hidden"
          />

          <button
            onClick={handleExport}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-xs transition-colors"
          >
            <Download size={14} />
            <span>এক্সপোর্ট (.xlsx)</span>
          </button>

          <button
            onClick={() => fileInputRef.current?.click()}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-50 hover:bg-emerald-100 text-emerald-700 font-semibold text-xs border border-emerald-200 transition-colors"
          >
            <Upload size={14} />
            <span>ইম্পোর্ট (.xlsx)</span>
          </button>

          <button
            onClick={() => setShowAdd(true)}
            className="flex items-center gap-1 px-3 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs shadow-xs transition-colors ml-1"
          >
            <Plus size={15} />
            <span>নতুন অ্যাকাউন্ট</span>
          </button>
        </div>
      </div>

      {importStatus && (
        <div className="p-3 bg-emerald-50 text-emerald-800 text-xs font-semibold rounded-xl border border-emerald-200 flex items-center gap-2 animate-in fade-in">
          <CheckCircle size={16} />
          <span>{importStatus}</span>
        </div>
      )}

      {/* Grouped Account List */}
      <div className="space-y-4">
        {typeOrder.map((type) => {
          const group = accounts.filter((a) => a.type === type);
          if (group.length === 0) return null;

          const groupTotal = group.reduce((sum, a) => sum + a.balance, 0);

          return (
            <div
              key={type}
              className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden"
            >
              {/* Group Header */}
              <div className="px-4 py-2.5 bg-slate-50/80 border-b border-slate-100 flex items-center justify-between">
                <span className="font-bold text-xs sm:text-sm text-emerald-800 tracking-wide uppercase">
                  {typeLabel(type)}
                </span>
                <span className="text-xs font-bold text-slate-600">
                  মোট: {tk(groupTotal)}
                </span>
              </div>

              {/* Group Items */}
              <div className="divide-y divide-slate-100">
                {group.map((a) => (
                  <div
                    key={a.id}
                    onClick={() => setEditingAcc(a)}
                    className="p-3.5 flex items-center justify-between hover:bg-slate-50 transition-colors cursor-pointer group"
                  >
                    <div className="flex items-center gap-3">
                      <AccountAvatar account={a} size="md" />
                      <div>
                        <h4 className="font-bold text-slate-900 text-sm group-hover:text-emerald-700 transition-colors">
                          {a.name}
                        </h4>
                        <p className="text-[11px] text-slate-400">
                          {typeLabel(a.type)} • শুরুর ব্যালেন্স ছিল {tk(a.opening)}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-center gap-3">
                      <span className="font-bold text-slate-900 text-base">
                        {tk(a.balance)}
                      </span>
                      <Edit2
                        size={15}
                        className="text-slate-300 group-hover:text-slate-600 transition-colors"
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>

      {/* Reset Demo Data link */}
      <div className="pt-4 text-center">
        <button
          onClick={() => setShowResetConfirm(true)}
          className="text-xs text-slate-400 hover:text-red-500 font-medium inline-flex items-center gap-1 transition-colors"
        >
          <RotateCcw size={13} />
          <span>প্রাথমিক ডেমো ডেটায় রিসেট করুন</span>
        </button>
      </div>

      {/* ADD ACCOUNT MODAL */}
      {showAdd && (
        <AddAccountModal
          customTypes={customTypes}
          onClose={() => setShowAdd(false)}
          onSave={(name, type, opening, icon) => {
            addAccount(name, type, opening, icon);
            setShowAdd(false);
          }}
        />
      )}

      {/* EDIT ACCOUNT MODAL */}
      {editingAcc && (
        <EditAccountModal
          account={editingAcc}
          onClose={() => setEditingAcc(null)}
          onSave={(name) => {
            updateAccount(editingAcc.id, name);
            setEditingAcc(null);
          }}
          onDelete={() => {
            setDeleteConfirm(editingAcc);
            setEditingAcc(null);
          }}
        />
      )}

      {/* DELETE ACCOUNT CONFIRMATION */}
      {deleteConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-5 space-y-4">
            <div className="flex items-center gap-3 text-red-600">
              <div className="p-2 bg-red-50 rounded-xl">
                <AlertTriangle size={24} />
              </div>
              <h3 className="font-bold text-slate-900 text-base">
                &quot;{deleteConfirm.name}&quot; মুছবেন?
              </h3>
            </div>
            <p className="text-xs text-slate-600 leading-relaxed">
              যে অ্যাকাউন্টে কোনো লেনদেন নেই কেবল সেটাই মোছা যাবে।
            </p>
            {deleteError && (
              <p className="text-xs font-semibold text-red-600 bg-red-50 p-2.5 rounded-xl border border-red-200">
                {deleteError}
              </p>
            )}
            <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
              <button
                onClick={() => {
                  setDeleteConfirm(null);
                  setDeleteError(null);
                }}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
              >
                বাতিল
              </button>
              <button
                onClick={() => handleDelete(deleteConfirm)}
                className="px-4 py-2 text-xs font-bold text-white bg-red-600 hover:bg-red-700 rounded-xl transition-colors"
              >
                মুছে ফেলুন
              </button>
            </div>
          </div>
        </div>
      )}

      {/* RESET CONFIRMATION */}
      {showResetConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-5 space-y-4">
            <h3 className="font-bold text-slate-900 text-base">সবকিছু রিসেট করবেন?</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              বর্তমান সব হিসাব ও লেনদেন মুছে প্রাথমিক নমুনা ডেটা বসবে।
            </p>
            <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
              <button
                onClick={() => setShowResetConfirm(false)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
              >
                বাতিল
              </button>
              <button
                onClick={() => {
                  resetToDefault();
                  setShowResetConfirm(false);
                }}
                className="px-4 py-2 text-xs font-bold text-white bg-red-600 hover:bg-red-700 rounded-xl"
              >
                রিসেট করুন
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

// ---------------- Add Account Modal ----------------
interface AddAccountModalProps {
  customTypes: string[];
  onClose: () => void;
  onSave: (name: string, type: string, opening: number, icon: string) => void;
}

const AddAccountModal: React.FC<AddAccountModalProps> = ({
  customTypes,
  onClose,
  onSave,
}) => {
  const [type, setType] = useState('bank');
  const [newTypeName, setNewTypeName] = useState('');
  const [name, setName] = useState('');
  const [opening, setOpening] = useState('');

  const typeOptions = [
    { key: 'bank', label: 'ব্যাংক' },
    { key: 'mobile', label: 'মোবাইল ব্যাংকিং (বিকাশ/নগদ ইত্যাদি)' },
    { key: 'cash', label: 'নগদ ক্যাশ' },
    { key: 'dps', label: 'ডিপিএস সঞ্চয়' },
    { key: 'asset', label: 'স্থাবর সম্পদ / বিনিয়োগ' },
    ...customTypes.map((t) => ({ key: t, label: typeLabel(t) })),
    { key: '__new_type__', label: '+ নতুন ধরন যোগ করুন' },
  ];

  const suggestions = bankSuggestions(type, name);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const finalType = type === '__new_type__' ? newTypeName.trim() : type;

    if (type === '__new_type__' && !newTypeName.trim()) {
      alert('নতুন ধরনের নাম দিন');
      return;
    }

    if (!name.trim()) {
      alert('অ্যাকাউন্টের নাম দিন');
      return;
    }

    const op = parseFloat(opening) || 0;
    onSave(name.trim(), finalType, op, defaultIconKey(finalType));
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full max-h-[90vh] flex flex-col overflow-hidden">
        <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
          <h3 className="font-bold text-slate-800 text-lg">নতুন অ্যাকাউন্ট যোগ</h3>
          <button onClick={onClose} className="p-1.5 text-slate-400 hover:text-slate-600">
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4 overflow-y-auto">
          {/* Type Selector */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              অ্যাকাউন্টের ধরন
            </label>
            <select
              value={type}
              onChange={(e) => setType(e.target.value)}
              className="w-full p-2.5 text-sm bg-slate-50 border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 font-medium"
            >
              {typeOptions.map((o) => (
                <option key={o.key} value={o.key}>
                  {o.label}
                </option>
              ))}
            </select>
          </div>

          {/* New Type Name if custom */}
          {type === '__new_type__' && (
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                নতুন ধরনের নাম
              </label>
              <input
                type="text"
                placeholder="যেমন: শেয়ার মার্কেট, ক্রিপ্টো..."
                value={newTypeName}
                onChange={(e) => setNewTypeName(e.target.value)}
                className="w-full p-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 font-medium"
                autoFocus
              />
            </div>
          )}

          {/* Bank / MFS / Account Name with Suggestions */}
          <div className="relative">
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              অ্যাকাউন্টের নাম *
            </label>
            <input
              type="text"
              placeholder={
                type === 'bank'
                  ? 'ব্যাংকের নাম (যেমন: ডাচ-বাংলা, ব্র্যাক, সোনালী)...'
                  : type === 'mobile'
                  ? 'মোবাইলের নাম (যেমন: বিকাশ, নগদ, রকেট)...'
                  : 'অ্যাকাউন্টের নাম লিখুন...'
              }
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full p-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 font-semibold"
            />

            {/* Bank suggestions chips/list */}
            {suggestions.length > 0 && (
              <div className="mt-2 bg-slate-50 p-2 rounded-xl border border-slate-200">
                <span className="text-[10px] text-slate-400 block font-semibold mb-1">
                  সাজেশন (ক্লিক করুন):
                </span>
                <div className="flex flex-wrap gap-1.5">
                  {suggestions.map((b) => (
                    <button
                      key={b.en}
                      type="button"
                      onClick={() => setName(b.bn)}
                      className="inline-flex items-center gap-1.5 bg-white border border-slate-200 hover:border-emerald-500 text-xs px-2 py-1 rounded-lg shadow-2xs transition-all"
                    >
                      <img
                        src={`/bank_icons/${b.logo}.png`}
                        alt={b.en}
                        className="w-4 h-4 object-contain"
                      />
                      <span className="font-medium text-slate-800">{b.bn}</span>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Opening balance */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              শুরুর ব্যালেন্স (ঐচ্ছিক)
            </label>
            <div className="relative">
              <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-sm">
                ৳
              </span>
              <input
                type="number"
                placeholder="০"
                value={opening}
                onChange={(e) => setOpening(e.target.value)}
                className="w-full pl-8 pr-3 py-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 font-bold"
              />
            </div>
          </div>

          {/* Footer */}
          <div className="pt-3 border-t border-slate-200 flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
            >
              বাতিল
            </button>
            <button
              type="submit"
              className="px-5 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs"
            >
              অ্যাকাউন্ট যোগ করুন
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// ---------------- Edit Account Modal ----------------
interface EditAccountModalProps {
  account: Account;
  onClose: () => void;
  onSave: (name: string) => void;
  onDelete: () => void;
}

const EditAccountModal: React.FC<EditAccountModalProps> = ({
  account,
  onClose,
  onSave,
  onDelete,
}) => {
  const [name, setName] = useState(account.name);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      alert('নাম খালি রাখা যাবে না');
      return;
    }
    onSave(name.trim());
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-5 space-y-4">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <div className="flex items-center gap-2.5">
            <AccountAvatar account={account} size="md" />
            <div>
              <h3 className="font-bold text-slate-900 text-base">অ্যাকাউন্ট এডিট</h3>
              <p className="text-xs text-slate-500">ব্যালেন্স: {tk(account.balance)}</p>
            </div>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600">
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              অ্যাকাউন্টের নাম
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-3 py-2 text-sm bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-emerald-500 font-bold"
              autoFocus
            />
          </div>

          <div className="pt-2 flex items-center justify-between">
            <button
              type="button"
              onClick={onDelete}
              className="px-3 py-2 text-xs font-bold text-red-600 hover:bg-red-50 rounded-xl transition-colors flex items-center gap-1"
            >
              <Trash2 size={14} />
              <span>মুছুন</span>
            </button>

            <div className="flex gap-2">
              <button
                type="button"
                onClick={onClose}
                className="px-3.5 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
              >
                বাতিল
              </button>
              <button
                type="submit"
                className="px-4 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs"
              >
                সংরক্ষণ
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};
