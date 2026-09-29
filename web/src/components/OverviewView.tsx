'use client';

import React from 'react';
import { Smartphone, Monitor, Database, Shield, Zap, Sparkles, CheckCircle2 } from 'lucide-react';

interface OverviewProps {
  deviceCount: number;
  memoryCount: number;
  knowledgeCount: number;
  actionCount: number;
}

export function OverviewView({ deviceCount, memoryCount, knowledgeCount, actionCount }: OverviewProps) {
  const stats = [
    { label: 'Active Devices', value: deviceCount, icon: Smartphone, color: 'text-blue-400', border: 'border-blue-500/20' },
    { label: 'Shared Memories', value: memoryCount, icon: Database, color: 'text-emerald-400', border: 'border-emerald-500/20' },
    { label: 'Knowledge Cards', value: knowledgeCount, icon: Sparkles, color: 'text-amber-400', border: 'border-amber-500/20' },
    { label: 'Queued Actions', value: actionCount, icon: Zap, color: 'text-purple-400', border: 'border-purple-500/20' },
  ];

  return (
    <div className="space-y-6">
      {/* Metric Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((stat, i) => {
          const Icon = stat.icon;
          return (
            <div key={i} className={`bg-slate-900/60 border ${stat.border} rounded-xl p-5 backdrop-blur`}>
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-slate-400">{stat.label}</span>
                <Icon className={`w-4 h-4 ${stat.color}`} />
              </div>
              <div className="mt-3 text-2xl font-bold text-slate-100">{stat.value}</div>
            </div>
          );
        })}
      </div>

      {/* Wolf Companion & Surface Connectivity Status */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Wolf Persona Status Card */}
        <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-6 lg:col-span-1 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-blue-400">Companion Core</span>
              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-medium bg-emerald-950 text-emerald-300 border border-emerald-800/40">
                <CheckCircle2 className="w-3 h-3 text-emerald-400" />
                Live 3D
              </span>
            </div>
            <div className="mt-4 flex items-center gap-4">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-blue-700 via-indigo-600 to-sky-400 flex items-center justify-center text-3xl shadow-xl shadow-blue-500/10">
                🐺
              </div>
              <div>
                <h3 className="font-bold text-slate-100 text-lg">VIC Living Wolf</h3>
                <p className="text-xs text-slate-400">Three.js Desktop & Filament Android</p>
                <div className="mt-1 text-[11px] text-slate-500">Asset: 1.09 MB Optimized GLB</div>
              </div>
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-slate-800/80 space-y-2 text-xs">
            <div className="flex justify-between text-slate-400">
              <span>Avatar Mode:</span>
              <span className="text-slate-200 font-medium">3D Living Wolf (Overlay)</span>
            </div>
            <div className="flex justify-between text-slate-400">
              <span>Voice Synthesis:</span>
              <span className="text-slate-200 font-medium">Whisper + Multi-accent TTS</span>
            </div>
            <div className="flex justify-between text-slate-400">
              <span>Languages:</span>
              <span className="text-slate-200 font-medium">EN, HI, GU</span>
            </div>
          </div>
        </div>

        {/* Node Topology */}
        <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-6 lg:col-span-2">
          <h3 className="text-sm font-semibold text-slate-100 mb-4 flex items-center gap-2">
            <Monitor className="w-4 h-4 text-blue-400" />
            Distributed Node Topology
          </h3>

          <div className="space-y-3">
            <div className="p-3.5 rounded-lg bg-slate-950/80 border border-slate-800 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-emerald-500 animate-ping" />
                <div>
                  <div className="text-sm font-medium text-slate-200">Windows Desktop Node</div>
                  <div className="text-xs text-slate-400">Workstation (Velmora-Intelligence-Commander)</div>
                </div>
              </div>
              <span className="text-xs px-2.5 py-1 rounded bg-slate-900 text-emerald-400 border border-emerald-900/40">
                Ready for Bridge
              </span>
            </div>

            <div className="p-3.5 rounded-lg bg-slate-950/80 border border-slate-800 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-blue-500" />
                <div>
                  <div className="text-sm font-medium text-slate-200">Android Mobile Node</div>
                  <div className="text-xs text-slate-400">Redmi Note 12 Pro+ (Default Assistant)</div>
                </div>
              </div>
              <span className="text-xs px-2.5 py-1 rounded bg-slate-900 text-blue-400 border border-blue-900/40">
                ACTION_ASSIST Target
              </span>
            </div>

            <div className="p-3.5 rounded-lg bg-slate-950/80 border border-slate-800 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-purple-500" />
                <div>
                  <div className="text-sm font-medium text-slate-200">Cloud Realtime Core</div>
                  <div className="text-xs text-slate-400">Supabase PostgreSQL + RLS + Channels</div>
                </div>
              </div>
              <span className="text-xs px-2.5 py-1 rounded bg-slate-900 text-purple-400 border border-purple-900/40">
                Operational
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
