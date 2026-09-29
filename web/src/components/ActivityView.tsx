'use client';

import React from 'react';
import { Activity, ShieldAlert, CheckCircle, Clock, XCircle } from 'lucide-react';

interface ActionItem {
  id: string;
  action_type: string;
  status: string;
  risk_tier: number;
  payload: any;
  created_at: string;
}

interface ActivityViewProps {
  actions: ActionItem[];
}

export function ActivityView({ actions }: ActivityViewProps) {
  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'completed':
        return (
          <span className="inline-flex items-center gap-1 text-[10px] font-medium px-2 py-0.5 rounded bg-emerald-950/80 text-emerald-300 border border-emerald-800">
            <CheckCircle className="w-3 h-3 text-emerald-400" />
            Completed
          </span>
        );
      case 'awaiting_confirmation':
        return (
          <span className="inline-flex items-center gap-1 text-[10px] font-medium px-2 py-0.5 rounded bg-amber-950/80 text-amber-300 border border-amber-800">
            <ShieldAlert className="w-3 h-3 text-amber-400" />
            Gate Confirmation Required
          </span>
        );
      case 'failed':
      case 'rejected':
        return (
          <span className="inline-flex items-center gap-1 text-[10px] font-medium px-2 py-0.5 rounded bg-red-950/80 text-red-300 border border-red-800">
            <XCircle className="w-3 h-3 text-red-400" />
            {status}
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 text-[10px] font-medium px-2 py-0.5 rounded bg-blue-950/80 text-blue-300 border border-blue-800">
            <Clock className="w-3 h-3 text-blue-400" />
            {status}
          </span>
        );
    }
  };

  const getRiskBadge = (tier: number) => {
    switch (tier) {
      case 0:
        return <span className="text-[10px] font-semibold text-slate-400">Tier 0 (Safe)</span>;
      case 1:
        return <span className="text-[10px] font-semibold text-blue-400">Tier 1 (Standard)</span>;
      case 2:
        return <span className="text-[10px] font-semibold text-amber-400">Tier 2 (Sensitive Gate)</span>;
      default:
        return <span className="text-[10px] font-semibold text-red-400">Tier 3 (High Risk)</span>;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-sm font-semibold text-slate-100">Live Cross-Device Command Queue & Audit</h2>
          <p className="text-xs text-slate-400">Realtime audit log of operations exchanged between mobile, desktop, and cloud</p>
        </div>
      </div>

      <div className="bg-slate-900/60 border border-slate-800 rounded-xl overflow-hidden backdrop-blur">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-950/80 border-b border-slate-800 text-slate-400">
            <tr>
              <th className="p-4">Action Type</th>
              <th className="p-4">Risk Level</th>
              <th className="p-4">Status</th>
              <th className="p-4">Payload Summary</th>
              <th className="p-4 text-right">Timestamp</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {actions.length === 0 ? (
              <tr>
                <td colSpan={5} className="p-8 text-center text-slate-500">
                  Command queue is currently empty. Actions triggered from your phone (e.g., launching desktop software) appear here in realtime.
                </td>
              </tr>
            ) : (
              actions.map((act) => (
                <tr key={act.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="p-4 font-mono font-medium text-slate-200">{act.action_type}</td>
                  <td className="p-4">{getRiskBadge(act.risk_tier)}</td>
                  <td className="p-4">{getStatusBadge(act.status)}</td>
                  <td className="p-4 font-mono text-[11px] text-slate-400 truncate max-w-xs">
                    {JSON.stringify(act.payload)}
                  </td>
                  <td className="p-4 text-right text-slate-400 font-mono text-[11px]">
                    {new Date(act.created_at).toLocaleTimeString()}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
