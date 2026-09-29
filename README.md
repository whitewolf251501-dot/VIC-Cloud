# VIC-Cloud — Velmora Intelligence Commander Cloud Ecosystem

[![VIC Status](https://img.shields.io/badge/status-active%20development-blue.svg)](https://github.com/whitewolf251501-dot/VIC-Cloud)
[![Security](https://img.shields.io/badge/security-least--privilege%20RLS-success.svg)](docs/ARCHITECTURE.md)
[![Android](https://img.shields.io/badge/android-default%20assistant%20ready-brightgreen.svg)](docs/ANDROID_SPEC.md)

**Velmora Intelligence Commander (VIC)** Cloud Ecosystem bridges the existing Windows VIC Desktop agent, cloud synchronization, a modern web-based Control Center, and a dedicated native Android assistant capable of acting as the default device assistant.

---

## Architecture Overview

```
                      +-----------------------------+
                      |     Web Control Center      |
                      |   (Vercel / Next.js SSR)    |
                      +--------------+--------------+
                                     |
                                     v
+-----------------------+     +---------------+     +-----------------------+
|  Windows VIC Desktop  | <-> | Supabase Cloud| <-> | Android VIC Assistant |
| (Electron / Node /    |     |  (PostgreSQL, |     | (Kotlin Native, Voice,|
|  3D Living Wolf)      |     |  Auth, RLS,   |     |  ACTION_ASSIST,       |
|                       |     |  Realtime)    |     |  3D Companion Overlay)|
+-----------------------+     +---------------+     +-----------------------+
```

1. **Supabase Cloud Layer (`supabase/`):**
   - High-throughput PostgreSQL database with Row-Level Security (RLS) guaranteeing user-level isolation.
   - Tables: `users`, `devices`, `conversations`, `messages`, `memories`, `knowledge`, `device_actions`, `action_results`.
   - Realtime event channels for sub-second cross-device synchronization and command dispatching.

2. **Web Control Center (`web/`):**
   - Responsive Next.js application hosted on Vercel.
   - Comprehensive dashboard for device monitoring, memory inspection, knowledge browsing, and AI model orchestration.
   - Zero private keys in browser client; all sensitive actions authenticated via server actions and scoped sessions.

3. **Desktop-Cloud Bridge (`desktop-bridge/`):**
   - Lightweight, non-intrusive synchronization adapter connecting the existing Windows desktop VIC to the cloud.
   - Safe command queue with user confirmation gates for sensitive local actions.
   - Real-time heartbeat, memory deduplication sync, and audit logging.

4. **Android VIC Assistant (`android/`):**
   - Native Kotlin application registered with Android `ACTION_ASSIST` intent to serve as the default system assistant.
   - Multilingual voice interaction (English, Hindi, Gujarati).
   - Floating 3D Wolf companion overlay using the 1.09 MB GLB model.
   - Controlled phone actions (alarms, timers, reminders, WhatsApp compose with confirmation).

---

## Directory Structure

```text
VIC-Cloud/
├── .github/              # GitHub Actions workflows & automation
├── android/              # Native Kotlin Android assistant project
├── backend/              # Cloud backend utilities and shared types
├── desktop-bridge/       # Lightweight sync adapter for Windows VIC
├── docs/                 # Architectural specifications, protocols, data schemas
├── supabase/             # Database migrations, seed data, and schema definitions
├── web/                  # Web Control Center (Next.js / Tailwind CSS / Vercel)
├── .env.example          # Environment variable template
├── .gitignore            # Strict security & artifact filtering
├── README.md             # This document
└── VIC_MASTER_ROADMAP.md # 5-Phase master execution roadmap
```

---

## Security Principles

- **Zero Hardcoded Secrets:** No API keys, credentials, or tokens are ever stored in source code or version control.
- **Strict Row-Level Security (RLS):** Every Supabase query is constrained by authenticated user context.
- **Explicit Confirmation Gates:** Sensitive desktop actions dispatched via cloud require local user verification before execution.
- **Separation of Concerns:** Client interfaces (Web & Android) only receive anonymized or scoped tokens; service-role keys remain strictly within secure server environments.

---

## Getting Started

Refer to the comprehensive documentation in [`docs/`](docs/ARCHITECTURE.md) and track progress in [`VIC_MASTER_ROADMAP.md`](VIC_MASTER_ROADMAP.md).
