'use client';

import React from 'react';
import { Wifi, Shield, RefreshCw } from 'lucide-react';

interface HeaderProps {
  title: string;
  subtitle: string;
  isRefreshing?: boolean;
  onRefresh?: () => void;
}

export function Header({ title, subtitle, isRefreshing, onRefresh }: HeaderProps) {
  return (
    <header className="h-16 bg-slate-950/80 backdrop-blur border-b border-slate-800 flex items-center justify-between px-8 sticky top-0 z-10">
      <div>
        <h1 className="text-base font-semibold text-slate-100">{title}</h1>
        <p className="text-xs text-slate-400">{subtitle}</p>
      </div>

      <div className="flex items-center gap-4">
        {/* Supabase Status Pill */}
        <div className="flex items-center gap-2 bg-slate-900 border border-slate-800 px-3 py-1.5 rounded-full text-xs">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          <span className="text-slate-300 font-medium">Supabase Cloud</span>
          <span className="text-slate-500 text-[10px]">ap-south-1</span>
        </div>

        {/* Refresh Button */}
        {onRefresh && (
          <button
            onClick={onRefresh}
            disabled={isRefreshing}
            className="p-2 text-slate-400 hover:text-slate-200 bg-slate-900 border border-slate-800 rounded-lg hover:bg-slate-800 transition-colors disabled:opacity-50"
            title="Refresh State"
          >
            <RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin text-blue-400' : ''}`} />
          </button>
        )}
      </div>
    </header>
  );
}
