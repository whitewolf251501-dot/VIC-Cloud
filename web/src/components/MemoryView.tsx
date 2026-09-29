'use client';

import React, { useState } from 'react';
import { Search, Plus, Trash2, Tag, Calendar, Database } from 'lucide-react';

interface Memory {
  id: string;
  category: 'fact' | 'preference' | 'project' | 'instruction';
  content: string;
  importance: number;
  created_at: string;
  source_device?: string;
}

interface MemoryViewProps {
  memories: Memory[];
  onRefresh: () => void;
}

export function MemoryView({ memories, onRefresh }: MemoryViewProps) {
  const [filterCategory, setFilterCategory] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [isAdding, setIsAdding] = useState(false);
  const [newCategory, setNewCategory] = useState<'fact' | 'preference' | 'project' | 'instruction'>('fact');
  const [newContent, setNewContent] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  const categories = ['all', 'fact', 'preference', 'project', 'instruction'];

  const filteredMemories = memories.filter((m) => {
    const matchesCat = filterCategory === 'all' || m.category === filterCategory;
    const matchesSearch = m.content.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCat && matchesSearch;
  });

  const handleAddMemory = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newContent.trim()) return;
    setIsSaving(true);

    try {
      const res = await fetch('/api/memories', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: '00000000-0000-0000-0000-000000000000',
          category: newCategory,
          content: newContent,
          importance: 0.8,
          sourceDevice: 'web_dashboard',
        }),
      });

      if (res.ok) {
        setNewContent('');
        setIsAdding(false);
        onRefresh();
      }
    } catch (err) {
      console.error(err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to remove this memory record?')) return;
    try {
      await fetch(`/api/memories?id=${id}`, { method: 'DELETE' });
      onRefresh();
    } catch (err) {
      console.error(err);
    }
  };

  const getCategoryBadgeClass = (category: string) => {
    switch (category) {
      case 'fact':
        return 'bg-blue-950/80 text-blue-300 border-blue-800';
      case 'preference':
        return 'bg-emerald-950/80 text-emerald-300 border-emerald-800';
      case 'project':
        return 'bg-purple-950/80 text-purple-300 border-purple-800';
      case 'instruction':
        return 'bg-amber-950/80 text-amber-300 border-amber-800';
      default:
        return 'bg-slate-800 text-slate-300 border-slate-700';
    }
  };

  return (
    <div className="space-y-6">
      {/* Controls */}
      <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
        <div className="flex items-center gap-3 w-full sm:w-auto">
          {/* Search Input */}
          <div className="relative w-full sm:w-72">
            <Search className="w-4 h-4 absolute left-3 top-2.5 text-slate-500" />
            <input
              type="text"
              placeholder="Search shared memory..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-slate-900 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-blue-500"
            />
          </div>

          {/* Category Tabs */}
          <div className="flex gap-1 bg-slate-900/60 p-1 rounded-lg border border-slate-800 overflow-x-auto">
            {categories.map((cat) => (
              <button
                key={cat}
                onClick={() => setFilterCategory(cat)}
                className={`px-2.5 py-1 rounded text-xs font-medium capitalize transition-all ${
                  filterCategory === cat
                    ? 'bg-blue-600 text-white'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>

        <button
          onClick={() => setIsAdding(true)}
          className="px-3 py-2 bg-blue-600 text-white text-xs font-medium rounded-lg hover:bg-blue-500 transition-colors flex items-center gap-1.5 shrink-0"
        >
          <Plus className="w-4 h-4" />
          Add Memory
        </button>
      </div>

      {/* Add Memory Modal / Box */}
      {isAdding && (
        <form onSubmit={handleAddMemory} className="bg-slate-900 border border-slate-800 rounded-xl p-4 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-200">Inject New Memory to Vault</span>
            <button
              type="button"
              onClick={() => setIsAdding(false)}
              className="text-xs text-slate-400 hover:text-slate-200"
            >
              Cancel
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-4 gap-3">
            <select
              value={newCategory}
              onChange={(e: any) => setNewCategory(e.target.value)}
              className="bg-slate-950 border border-slate-800 rounded-lg p-2 text-xs text-slate-200 focus:outline-none focus:border-blue-500"
            >
              <option value="fact">Fact</option>
              <option value="preference">Preference</option>
              <option value="project">Project</option>
              <option value="instruction">Instruction</option>
            </select>

            <input
              type="text"
              placeholder="e.g. Always generate GitHub markdown links with forward slashes."
              value={newContent}
              onChange={(e) => setNewContent(e.target.value)}
              className="sm:col-span-3 bg-slate-950 border border-slate-800 rounded-lg p-2 text-xs text-slate-200 focus:outline-none focus:border-blue-500"
            />
          </div>

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={isSaving || !newContent.trim()}
              className="px-4 py-1.5 bg-blue-600 text-white text-xs font-medium rounded-lg hover:bg-blue-500 disabled:opacity-50 transition-colors"
            >
              {isSaving ? 'Saving...' : 'Commit to Vault'}
            </button>
          </div>
        </form>
      )}

      {/* Memories Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filteredMemories.length === 0 ? (
          <div className="col-span-full p-8 text-center bg-slate-900/40 border border-slate-800 rounded-xl text-slate-500 text-xs">
            No memories match your query. Memories stored on your desktop or phone automatically sync here in realtime.
          </div>
        ) : (
          filteredMemories.map((m) => (
            <div
              key={m.id}
              className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-4 flex flex-col justify-between hover:border-slate-700 transition-colors"
            >
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span
                    className={`px-2 py-0.5 rounded text-[10px] font-semibold uppercase tracking-wider border ${getCategoryBadgeClass(
                      m.category
                    )}`}
                  >
                    {m.category}
                  </span>
                  <button
                    onClick={() => handleDelete(m.id)}
                    className="text-slate-500 hover:text-red-400 transition-colors"
                    title="Delete Memory"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
                <p className="text-xs text-slate-200 leading-relaxed">{m.content}</p>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-800/60 flex items-center justify-between text-[10px] text-slate-500">
                <span>Source: {m.source_device || 'cloud'}</span>
                <span>{new Date(m.created_at).toLocaleDateString()}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
