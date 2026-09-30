/**
 * @vic-cloud/desktop-bridge
 * Desktop-to-Cloud synchronization bridge adapter types & client
 */

export type DeviceType = 'windows_desktop' | 'android_phone' | 'web_dashboard';
export type DeviceStatus = 'online' | 'offline' | 'busy';
export type RiskTier = 0 | 1 | 2 | 3;
export type ActionStatus =
  | 'pending'
  | 'in_progress'
  | 'awaiting_confirmation'
  | 'completed'
  | 'rejected'
  | 'failed';

export interface VicCloudDevice {
  id: string;
  user_id: string;
  device_name: string;
  device_type: DeviceType;
  client_version: string;
  status: DeviceStatus;
  metadata?: Record<string, unknown>;
  last_seen: string;
  created_at?: string;
  updated_at?: string;
}

export interface RemoteDeviceAction {
  id?: string;
  user_id: string;
  source_device_id?: string | null;
  target_device_id: string;
  action_type: string;
  payload: Record<string, any>;
  risk_tier: RiskTier;
  requires_confirmation?: boolean;
  status: ActionStatus;
  created_at?: string;
  expires_at?: string;
}

export interface RemoteActionResult {
  id?: string;
  action_id: string;
  user_id: string;
  executing_device_id: string;
  status: 'success' | 'failure';
  result?: Record<string, unknown>;
  error?: string;
  completed_at?: string;
}

export interface BridgeConfig {
  supabaseUrl?: string;
  supabaseAnonKey?: string;
  supabaseServiceRoleKey?: string;
  userId?: string;
  deviceId?: string;
  deviceName?: string;
  heartbeatIntervalMs?: number;
  syncIntervalMs?: number;
  autoSyncMemories?: boolean;
}
