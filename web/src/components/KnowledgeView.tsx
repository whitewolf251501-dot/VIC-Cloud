'use client';

import React, { useState } from 'react';
import { BookOpen, Search, CheckCircle, HelpCircle, ExternalLink } from 'lucide-react';

interface KnowledgeItem {
  id: string;
  topic: string;
  category: string;
  title: string;
  content: string;
  certainty: string;
  sources?: any[];
  created_at: string;
}

interface KnowledgeViewProps {
  knowledge: KnowledgeItem[];
}

export function KnowledgeView({ knowledge }: KnowledgeViewProps) {
  const [search, setSearch] = useState('');

  const filtered = knowledge.filter(
    (k) =>
      k.title.toLowerCase().includes(search.toLowerCase()) ||
      k.topic.toLowerCase().includes(search.toLowerCase()) ||
      k.content.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-sm font-semibold text-slate-100">Autonomous Knowledge Library</h2>
          <p className="text-xs text-slate-400">Verified research cards and technical concepts indexed by VIC</p>
        </div>

        <div className="relative w-64">
          <Search className="w-4 h-4 absolute left-3 top-2.5 text-slate-500" />
          <input
            type="text"
            placeholder="Search topics..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-slate-900 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-blue-500"
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filtered.length === 0 ? (
          <div className="col-span-full p-8 text-center bg-slate-900/40 border border-slate-800 rounded-xl text-slate-500 text-xs">
            No research cards recorded yet. When you assign research tasks via voice or desktop (`research quantum computing`), cards are autonomously generated and archived here.
          </div>
        ) : (
          filtered.map((item) => (
            <div
              key={item.id}
              className="bg-slate-900/60 border border-slate-800 rounded-xl p-5 flex flex-col justify-between hover:border-slate-700 transition-colors"
            >
              <div>
                <div className="flex items-center justify-between mb-2">
                  <span className="text-[10px] uppercase font-semibold text-blue-400 bg-blue-950/60 px-2 py-0.5 rounded border border-blue-900">
                    {item.topic}
                  </span>
                  <span
                    className={`inline-flex items-center gap-1 text-[10px] font-medium px-2 py-0.5 rounded-full ${
                      item.certainty === 'verified_fact'
                        ? 'bg-emerald-950/80 text-emerald-300 border border-emerald-800'
                        : 'bg-amber-950/80 text-amber-300 border border-amber-800'
                    }`}
                  >
                    {item.certainty === 'verified_fact' ? (
                      <CheckCircle className="w-3 h-3 text-emerald-400" />
                    ) : (
                      <HelpCircle className="w-3 h-3 text-amber-400" />
                    )}
                    {item.certainty.replace('_', ' ')}
                  </span>
                </div>

                <h3 className="font-semibold text-slate-200 text-sm">{item.title}</h3>
                <p className="mt-2 text-xs text-slate-400 leading-relaxed line-clamp-4">{item.content}</p>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-800/60 flex items-center justify-between text-[10px] text-slate-500">
                <span>{item.category}</span>
                <span>{new Date(item.created_at).toLocaleDateString()}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
