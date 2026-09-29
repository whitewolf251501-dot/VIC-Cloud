/**
 * VIC Cloud Shared Types and Contracts
 */

export type DeviceType = 'windows_desktop' | 'android_phone' | 'web_dashboard';
export type DeviceStatus = 'online' | 'offline' | 'busy';

export interface VicDevice {
  id: string;
  userId: string;
  deviceName: string;
  deviceType: DeviceType;
  clientVersion: string;
  status: DeviceStatus;
  lastSeen: string;
  createdAt: string;
}

export type MemoryCategory = 'fact' | 'preference' | 'project' | 'instruction';

export interface VicMemory {
  id: string;
  userId: string;
  category: MemoryCategory;
  content: string;
  importance: number;
  createdAt: string;
  updatedAt: string;
}

export interface VicConversation {
  id: string;
  userId: string;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface VicMessage {
  id: string;
  conversationId: string;
  userId: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  metadata?: Record<string, unknown>;
  createdAt: string;
}

export type ActionStatus = 'pending' | 'in_progress' | 'awaiting_confirmation' | 'completed' | 'rejected' | 'failed';

export interface DeviceAction {
  id: string;
  userId: string;
  targetDeviceId: string;
  actionType: string;
  payload: Record<string, unknown>;
  status: ActionStatus;
  createdAt: string;
}

export interface ActionResult {
  id: string;
  actionId: string;
  userId: string;
  status: 'success' | 'failure';
  result?: Record<string, unknown>;
  error?: string;
  completedAt: string;
}
