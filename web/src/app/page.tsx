'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Sidebar } from '@/components/Sidebar';
import { Header } from '@/components/Header';
import { OverviewView } from '@/components/OverviewView';
import { DevicesView } from '@/components/DevicesView';
import { MemoryView } from '@/components/MemoryView';
import { KnowledgeView } from '@/components/KnowledgeView';
import { ModelSettingsView } from '@/components/ModelSettingsView';
import { ActivityView } from '@/components/ActivityView';
import { supabaseClient } from '@/lib/supabase-client';

export default function ControlCenterPage() {
  const [currentTab, setCurrentTab] = useState('overview');
  const [devices, setDevices] = useState<any[]>([]);
  const [memories, setMemories] = useState<any[]>([]);
  const [knowledge, setKnowledge] = useState<any[]>([]);
  const [actions, setActions] = useState<any[]>([]);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const fetchData = useCallback(async () => {
    setIsRefreshing(true);
    try {
      // 1. Fetch devices
      const devRes = await fetch('/api/devices');
      if (devRes.ok) {
        const d = await devRes.json();
        setDevices(d.devices || []);
      }

      // 2. Fetch memories
      const memRes = await fetch('/api/memories');
      if (memRes.ok) {
        const m = await memRes.json();
        setMemories(m.memories || []);
      }

      // 3. Fetch actions
      const actRes = await fetch('/api/actions');
      if (actRes.ok) {
        const a = await actRes.json();
        setActions(a.actions || []);
      }
    } catch (e) {
      console.error('Error fetching dashboard data:', e);
    } finally {
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchData();

    // Subscribe to Supabase Realtime channel for live updates
    if (supabaseClient) {
      const channel = supabaseClient
        .channel('dashboard_realtime')
        .on(
          'postgres_changes',
          { event: '*', schema: 'public', table: 'devices' },
          () => fetchData()
        )
        .on(
          'postgres_changes',
          { event: '*', schema: 'public', table: 'device_actions' },
          () => fetchData()
        )
        .on(
          'postgres_changes',
          { event: '*', schema: 'public', table: 'memories' },
          () => fetchData()
        )
        .subscribe();

      return () => {
        channel.unsubscribe();
      };
    }
  }, [fetchData]);

  const getTabTitle = () => {
    switch (currentTab) {
      case 'overview':
        return { title: 'VIC Control Center', subtitle: 'Central nervous system & cross-surface ecosystem' };
      case 'devices':
        return { title: 'Connected Devices', subtitle: 'Active desktop, mobile, and web nodes' };
      case 'memory':
        return { title: 'Shared Memory Vault', subtitle: 'Cross-surface synchronized context & preferences' };
      case 'knowledge':
        return { title: 'Autonomous Knowledge', subtitle: 'Extracted research cards & learning summaries' };
      case 'models':
        return { title: 'AI Model Providers', subtitle: 'Multi-model orchestration (Gemini & Groq)' };
      case 'activity':
        return { title: 'Command Audit Log', subtitle: 'Real-time telemetry and cross-device actions' };
      default:
        return { title: 'Control Center', subtitle: 'Management portal' };
    }
  };

  const { title, subtitle } = getTabTitle();

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-slate-950 font-sans">
      {/* Sidebar Navigation */}
      <Sidebar currentTab={currentTab} onTabChange={setCurrentTab} />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-full overflow-hidden">
        <Header
          title={title}
          subtitle={subtitle}
          isRefreshing={isRefreshing}
          onRefresh={fetchData}
        />

        <main className="flex-1 overflow-y-auto p-8 bg-slate-950/40">
          <div className="max-w-7xl mx-auto">
            {currentTab === 'overview' && (
              <OverviewView
                deviceCount={devices.length}
                memoryCount={memories.length}
                knowledgeCount={knowledge.length}
                actionCount={actions.length}
              />
            )}
            {currentTab === 'devices' && (
              <DevicesView devices={devices} onRefresh={fetchData} />
            )}
            {currentTab === 'memory' && (
              <MemoryView memories={memories} onRefresh={fetchData} />
            )}
            {currentTab === 'knowledge' && (
              <KnowledgeView knowledge={knowledge} />
            )}
            {currentTab === 'models' && <ModelSettingsView />}
            {currentTab === 'activity' && <ActivityView actions={actions} />}
          </div>
        </main>
      </div>
    </div>
  );
}
