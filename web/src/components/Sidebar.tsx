'use client';

import React from 'react';
import { LayoutDashboard, Smartphone, Brain, BookOpen, Cpu, Activity, ShieldCheck } from 'lucide-react';

interface SidebarProps {
  currentTab: string;
  onTabChange: (tab: string) => void;
}

export function Sidebar({ currentTab, onTabChange }: SidebarProps) {
  const navItems = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'devices', label: 'Connected Devices', icon: Smartphone },
    { id: 'memory', label: 'Shared Memory Vault', icon: Brain },
    { id: 'knowledge', label: 'Knowledge Library', icon: BookOpen },
    { id: 'models', label: 'AI Model Providers', icon: Cpu },
    { id: 'activity', label: 'Command Audit & Stream', icon: Activity },
  ];

  return (
    <aside className="w-64 bg-slate-950 border-r border-slate-800 flex flex-col justify-between shrink-0 select-none">
      <div>
        {/* Brand Header */}
        <div className="h-16 flex items-center px-6 border-b border-slate-800 gap-3">
          <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center font-bold text-white shadow-lg shadow-blue-500/20">
            🐺
          </div>
          <div>
            <div className="font-bold text-slate-100 tracking-wide text-sm flex items-center gap-1.5">
              VIC CLOUD
              <span className="text-[10px] uppercase font-semibold px-1.5 py-0.5 rounded bg-blue-900/60 text-blue-300 border border-blue-700/50">
                v1.0
              </span>
            </div>
            <div className="text-[11px] text-slate-400">Velmora Commander</div>
          </div>
        </div>

        {/* Navigation Links */}
        <nav className="p-3 space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onTabChange(item.id)}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-blue-600/15 text-blue-400 border border-blue-500/30'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900/60'
                }`}
              >
                <Icon className={`w-4 h-4 ${isActive ? 'text-blue-400' : 'text-slate-400'}`} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>
      </div>

      {/* Security & RLS status badge */}
      <div className="p-4 border-t border-slate-800/80">
        <div className="flex items-center gap-2 text-xs text-emerald-400 font-medium bg-emerald-950/40 border border-emerald-800/50 rounded-lg p-2.5">
          <ShieldCheck className="w-4 h-4 shrink-0 text-emerald-400" />
          <div>
            <div>RLS Enforced</div>
            <div className="text-[10px] text-slate-400 font-normal">Tenant Isolation Active</div>
          </div>
        </div>
      </div>
    </aside>
  );
}
