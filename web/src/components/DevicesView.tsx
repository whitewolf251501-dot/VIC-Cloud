'use client';

import React, { useState } from 'react';
import { Smartphone, Monitor, Globe, Plus, Send, CheckCircle2, AlertCircle } from 'lucide-react';

interface Device {
  id: string;
  device_name: string;
  device_type: string;
  client_version: string;
  status: string;
  last_seen: string;
}

interface DevicesViewProps {
  devices: Device[];
  onRefresh: () => void;
}

export function DevicesView({ devices, onRefresh }: DevicesViewProps) {
  const [selectedDevice, setSelectedDevice] = useState<string | null>(null);
  const [actionType, setActionType] = useState('ping');
  const [isSending, setIsSending] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const handleSendAction = async () => {
    if (!selectedDevice) return;
    setIsSending(true);
    setMessage(null);

    try {
      const res = await fetch('/api/actions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: '00000000-0000-0000-0000-000000000000', // Mock/Master ID
          targetDeviceId: selectedDevice,
          actionType,
          payload: { command: actionType, timestamp: new Date().toISOString() },
          riskTier: actionType === 'ping' ? 0 : 1,
        }),
      });

      if (res.ok) {
        setMessage(`Action '${actionType}' successfully queued!`);
        setTimeout(() => setMessage(null), 3000);
      } else {
        const err = await res.json();
        setMessage(`Failed: ${err.error}`);
      }
    } catch (e: any) {
      setMessage(`Error: ${e.message}`);
    } finally {
      setIsSending(false);
    }
  };

  const getDeviceIcon = (type: string) => {
    switch (type) {
      case 'windows_desktop':
        return <Monitor className="w-5 h-5 text-blue-400" />;
      case 'android_phone':
        return <Smartphone className="w-5 h-5 text-emerald-400" />;
      default:
        return <Globe className="w-5 h-5 text-purple-400" />;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-sm font-semibold text-slate-100">Registered Endpoints</h2>
          <p className="text-xs text-slate-400">Authenticated devices authorized for cloud synchronization</p>
        </div>
      </div>

      {message && (
        <div className="p-3 text-xs rounded-lg bg-blue-950/60 border border-blue-800 text-blue-300">
          {message}
        </div>
      )}

      {/* Device List Table */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl overflow-hidden backdrop-blur">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-950/80 border-b border-slate-800 text-slate-400">
            <tr>
              <th className="p-4">Device</th>
              <th className="p-4">Type</th>
              <th className="p-4">Version</th>
              <th className="p-4">Status</th>
              <th className="p-4">Last Seen</th>
              <th className="p-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {devices.length === 0 ? (
              <tr>
                <td colSpan={6} className="p-8 text-center text-slate-500">
                  No active devices registered yet. Devices register automatically upon connecting to the desktop bridge or mobile app.
                </td>
              </tr>
            ) : (
              devices.map((device) => (
                <tr key={device.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="p-4 flex items-center gap-3">
                    {getDeviceIcon(device.device_type)}
                    <div>
                      <div className="font-semibold text-slate-200">{device.device_name}</div>
                      <div className="text-[10px] text-slate-500 font-mono">{device.id}</div>
                    </div>
                  </td>
                  <td className="p-4 text-slate-300 capitalize">{device.device_type.replace('_', ' ')}</td>
                  <td className="p-4 text-slate-400 font-mono">{device.client_version}</td>
                  <td className="p-4">
                    <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-medium bg-emerald-950 text-emerald-400 border border-emerald-900">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
                      {device.status}
                    </span>
                  </td>
                  <td className="p-4 text-slate-400">{new Date(device.last_seen).toLocaleString()}</td>
                  <td className="p-4 text-right">
                    <button
                      onClick={() => setSelectedDevice(device.id)}
                      className="px-2.5 py-1 text-xs font-medium rounded bg-blue-600/20 text-blue-400 border border-blue-500/30 hover:bg-blue-600/30 transition-colors"
                    >
                      Send Command
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Command Dispatch Box */}
      {selectedDevice && (
        <div className="bg-slate-900/80 border border-blue-500/30 rounded-xl p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-100 flex items-center gap-2">
              <Send className="w-4 h-4 text-blue-400" />
              Dispatch Remote Command to Target Device
            </h3>
            <button
              onClick={() => setSelectedDevice(null)}
              className="text-xs text-slate-400 hover:text-slate-200"
            >
              Cancel
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <select
              value={actionType}
              onChange={(e) => setActionType(e.target.value)}
              className="bg-slate-950 border border-slate-800 rounded-lg p-2 text-xs text-slate-200 focus:outline-none focus:border-blue-500"
            >
              <option value="ping">Safe Ping (Tier 0)</option>
              <option value="status_query">Query Battery / Active Task (Tier 0)</option>
              <option value="open_application">Launch Application (Tier 1)</option>
              <option value="capture_screen">Request Screen Capture (Tier 2 - Gate)</option>
            </select>

            <button
              onClick={handleSendAction}
              disabled={isSending}
              className="px-4 py-2 rounded-lg bg-blue-600 text-white font-medium text-xs hover:bg-blue-500 disabled:opacity-50 transition-colors flex items-center justify-center gap-2"
            >
              {isSending ? 'Transmitting...' : 'Transmit Command via Realtime Queue'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
