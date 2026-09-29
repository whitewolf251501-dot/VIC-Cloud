'use client';

import React, { useState } from 'react';
import { Cpu, Zap, Key, ShieldCheck, Check, Sparkles } from 'lucide-react';

export function ModelSettingsView() {
  const [provider, setProvider] = useState<'gemini' | 'groq'>('gemini');
  const [geminiModel, setGeminiModel] = useState('gemini-1.5-flash');
  const [groqModel, setGroqModel] = useState('llama-3.3-70b-versatile');
  const [temperature, setTemperature] = useState(0.7);
  const [systemPersona, setSystemPersona] = useState(
    'You are VIC (Velmora Intelligence Commander), an elite AI companion with deep technical expertise, tactical brevity, and a protective wolf persona.'
  );
  const [saved, setSaved] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 2500);
  };

  return (
    <form onSubmit={handleSave} className="space-y-6 max-w-4xl">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-sm font-semibold text-slate-100">AI Intelligence Core</h2>
          <p className="text-xs text-slate-400">Configure provider routing, parameters, and wolf personality instructions</p>
        </div>

        <button
          type="submit"
          className="px-4 py-2 bg-blue-600 text-white text-xs font-medium rounded-lg hover:bg-blue-500 transition-colors flex items-center gap-1.5"
        >
          {saved ? (
            <>
              <Check className="w-4 h-4 text-emerald-400" />
              Settings Saved
            </>
          ) : (
            'Save Configuration'
          )}
        </button>
      </div>

      {/* Provider Selector Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Gemini Provider */}
        <div
          onClick={() => setProvider('gemini')}
          className={`cursor-pointer rounded-xl p-5 border transition-all ${
            provider === 'gemini'
              ? 'bg-blue-950/30 border-blue-500 shadow-lg shadow-blue-500/10'
              : 'bg-slate-900/40 border-slate-800 hover:border-slate-700'
          }`}
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-lg bg-gradient-to-tr from-blue-500 to-indigo-500 flex items-center justify-center text-white font-bold text-sm">
                G
              </div>
              <div>
                <h3 className="text-sm font-semibold text-slate-100">Google Gemini</h3>
                <span className="text-[10px] text-slate-400">Multimodal Vision & Speed</span>
              </div>
            </div>
            {provider === 'gemini' && (
              <span className="w-2.5 h-2.5 rounded-full bg-blue-400" />
            )}
          </div>

          <div className="mt-4 space-y-2">
            <label className="text-[11px] text-slate-400 block">Default Model</label>
            <select
              value={geminiModel}
              onChange={(e) => setGeminiModel(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2 text-xs text-slate-200 focus:outline-none focus:border-blue-500"
            >
              <option value="gemini-1.5-flash">Gemini 1.5 Flash (Ultra Fast / Low Latency)</option>
              <option value="gemini-1.5-pro">Gemini 1.5 Pro (Deep Reasoning & Large Context)</option>
            </select>
          </div>
        </div>

        {/* Groq Provider */}
        <div
          onClick={() => setProvider('groq')}
          className={`cursor-pointer rounded-xl p-5 border transition-all ${
            provider === 'groq'
              ? 'bg-orange-950/30 border-orange-500 shadow-lg shadow-orange-500/10'
              : 'bg-slate-900/40 border-slate-800 hover:border-slate-700'
          }`}
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-lg bg-gradient-to-tr from-orange-500 to-amber-600 flex items-center justify-center text-white font-bold text-sm">
                Q
              </div>
              <div>
                <h3 className="text-sm font-semibold text-slate-100">Groq LPU Engine</h3>
                <span className="text-[10px] text-slate-400">Near-Instant Voice Responses</span>
              </div>
            </div>
            {provider === 'groq' && (
              <span className="w-2.5 h-2.5 rounded-full bg-orange-400" />
            )}
          </div>

          <div className="mt-4 space-y-2">
            <label className="text-[11px] text-slate-400 block">Default Model</label>
            <select
              value={groqModel}
              onChange={(e) => setGroqModel(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2 text-xs text-slate-200 focus:outline-none focus:border-orange-500"
            >
              <option value="llama-3.3-70b-versatile">Llama 3.3 70B Versatile</option>
              <option value="mixtral-8x7b-32768">Mixtral 8x7B (32k Context)</option>
              <option value="whisper-large-v3-turbo">Whisper Large v3 Turbo (Voice STT)</option>
            </select>
          </div>
        </div>
      </div>

      {/* Temperature Slider */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-5 space-y-3">
        <div className="flex justify-between items-center text-xs">
          <span className="font-semibold text-slate-200">Sampling Temperature: {temperature}</span>
          <span className="text-slate-400">
            {temperature < 0.5 ? 'Precise & Direct' : temperature > 0.8 ? 'Creative & Conversational' : 'Balanced'}
          </span>
        </div>
        <input
          type="range"
          min="0.1"
          max="1.0"
          step="0.05"
          value={temperature}
          onChange={(e) => setTemperature(parseFloat(e.target.value))}
          className="w-full accent-blue-500 cursor-pointer"
        />
      </div>

      {/* Persona Prompt Editor */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-5 space-y-3">
        <div className="flex items-center justify-between">
          <label className="text-xs font-semibold text-slate-200 flex items-center gap-1.5">
            <Sparkles className="w-3.5 h-3.5 text-blue-400" />
            System Persona & Instructions
          </label>
          <span className="text-[10px] text-slate-400">Propagated to Desktop & Android</span>
        </div>
        <textarea
          rows={4}
          value={systemPersona}
          onChange={(e) => setSystemPersona(e.target.value)}
          className="w-full bg-slate-950 border border-slate-800 rounded-lg p-3 text-xs text-slate-200 leading-relaxed focus:outline-none focus:border-blue-500 resize-none font-mono"
        />
      </div>
    </form>
  );
}
