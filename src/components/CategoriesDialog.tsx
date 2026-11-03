import React, { useState } from 'react';
import { Category } from '../data/models';
import { X, Plus, Edit2, Trash2, Check, Tag } from 'lucide-react';

interface CategoriesDialogProps {
  categories: Category[];
  onAdd: (name: string, kind: 'expense' | 'income') => void;
  onRename: (id: number, newName: string) => void;
  onDelete: (id: number) => void;
  onClose: () => void;
}

export const CategoriesDialog: React.FC<CategoriesDialogProps> = ({
  categories,
  onAdd,
  onRename,
  onDelete,
  onClose,
}) => {
  const [activeTab, setActiveTab] = useState<'expense' | 'income'>('expense');
  const [newCatName, setNewCatName] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editName, setEditName] = useState('');

  const filteredCategories = categories.filter((c) => c.kind === activeTab);

  const handleAdd = (e: React.FormEvent) => {
    e.preventDefault();
    if (newCatName.trim()) {
      onAdd(newCatName.trim(), activeTab);
      setNewCatName('');
    }
  };

  const handleStartEdit = (cat: Category) => {
    setEditingId(cat.id);
    setEditName(cat.name);
  };

  const handleSaveEdit = (id: number) => {
    if (editName.trim()) {
      onRename(id, editName.trim());
      setEditingId(null);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full max-h-[85vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
          <div className="flex items-center gap-2">
            <Tag size={18} className="text-emerald-600" />
            <h3 className="font-bold text-slate-800 text-lg">খাত ব্যবস্থাপনা</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full hover:bg-slate-200 text-slate-500 transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        {/* Tab switch */}
        <div className="p-3 border-b border-slate-100 flex gap-2 bg-white">
          <button
            onClick={() => setActiveTab('expense')}
            className={`flex-1 py-2 text-sm font-semibold rounded-xl transition-all ${
              activeTab === 'expense'
                ? 'bg-red-50 text-red-600 border border-red-200 shadow-xs'
                : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            খরচের খাত ({categories.filter((c) => c.kind === 'expense').length})
          </button>
          <button
            onClick={() => setActiveTab('income')}
            className={`flex-1 py-2 text-sm font-semibold rounded-xl transition-all ${
              activeTab === 'income'
                ? 'bg-emerald-50 text-emerald-600 border border-emerald-200 shadow-xs'
                : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            আয়ের খাত ({categories.filter((c) => c.kind === 'income').length})
          </button>
        </div>

        {/* Add new category form */}
        <form onSubmit={handleAdd} className="p-3 bg-slate-50 border-b border-slate-200 flex gap-2">
          <input
            type="text"
            placeholder={activeTab === 'expense' ? 'নতুন খরচের খাত...' : 'নতুন আয়ের খাত...'}
            value={newCatName}
            onChange={(e) => setNewCatName(e.target.value)}
            className="flex-1 px-3 py-2 text-sm border border-slate-300 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-emerald-500 bg-white"
          />
          <button
            type="submit"
            disabled={!newCatName.trim()}
            className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white rounded-xl text-sm font-medium flex items-center gap-1 transition-colors"
          >
            <Plus size={16} />
            <span>যোগ</span>
          </button>
        </form>

        {/* Category List */}
        <div className="flex-1 overflow-y-auto p-3 space-y-1.5 divide-y divide-slate-100">
          {filteredCategories.map((c) => (
            <div
              key={c.id}
              className="pt-2 first:pt-0 flex items-center justify-between py-1.5 px-2 rounded-lg hover:bg-slate-50 group"
            >
              {editingId === c.id ? (
                <div className="flex items-center gap-2 flex-1 mr-2">
                  <input
                    type="text"
                    value={editName}
                    onChange={(e) => setEditName(e.target.value)}
                    className="flex-1 px-2.5 py-1 text-sm border border-emerald-500 rounded-lg focus:outline-hidden"
                    autoFocus
                  />
                  <button
                    onClick={() => handleSaveEdit(c.id)}
                    className="p-1.5 text-emerald-600 hover:bg-emerald-50 rounded-lg"
                  >
                    <Check size={16} />
                  </button>
                  <button
                    onClick={() => setEditingId(null)}
                    className="p-1.5 text-slate-400 hover:bg-slate-100 rounded-lg"
                  >
                    <X size={16} />
                  </button>
                </div>
              ) : (
                <>
                  <span className="text-sm font-medium text-slate-800">{c.name}</span>
                  <div className="flex items-center gap-1 opacity-70 group-hover:opacity-100 transition-opacity">
                    <button
                      onClick={() => handleStartEdit(c)}
                      className="p-1 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-md"
                      title="নাম পরিবর্তন"
                    >
                      <Edit2 size={15} />
                    </button>
                    <button
                      onClick={() => onDelete(c.id)}
                      className="p-1 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-md"
                      title="মুছুন"
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>
                </>
              )}
            </div>
          ))}

          {filteredCategories.length === 0 && (
            <div className="text-center py-8 text-sm text-slate-400">
              কোনো খাত যোগ করা হয়নি
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-3 border-t border-slate-200 bg-slate-50 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-200 rounded-xl transition-colors"
          >
            বন্ধ করুন
          </button>
        </div>
      </div>
    </div>
  );
};
