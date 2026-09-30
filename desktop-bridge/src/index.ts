import { createClient, SupabaseClient, RealtimeChannel } from '@supabase/supabase-js';
import type { BridgeConfig, RemoteDeviceAction, RemoteActionResult, VicCloudDevice } from './types.js';

export * from './types.js';

export class VicDesktopBridgeClient {
  private client: SupabaseClient | null = null;
  private config: BridgeConfig;
  private deviceId: string | null = null;
  private userId: string | null = null;
  private channel: RealtimeChannel | null = null;
  private heartbeatTimer: any = null;

  constructor(config: BridgeConfig = {}) {
    this.config = config;
    const url = config.supabaseUrl || process.env['NEXT_PUBLIC_SUPABASE_URL'] || '';
    const key =
      config.supabaseServiceRoleKey ||
      process.env['SUPABASE_SERVICE_ROLE_KEY'] ||
      config.supabaseAnonKey ||
      process.env['NEXT_PUBLIC_SUPABASE_ANON_KEY'] ||
      '';

    if (url && key) {
      this.client = createClient(url, key, {
        auth: { persistSession: false },
      });
    }
  }

  public getClient(): SupabaseClient | null {
    return this.client;
  }

  public async registerDevice(
    userId: string,
    deviceName: string,
    metadata?: Record<string, unknown>,
  ): Promise<VicCloudDevice | null> {
    if (!this.client) return null;
    this.userId = userId;

    const { data: existing } = await this.client
      .from('devices')
      .select('*')
      .eq('user_id', userId)
      .eq('device_name', deviceName)
      .maybeSingle();

    if (existing) {
      this.deviceId = existing.id;
      await this.client
        .from('devices')
        .update({
          status: 'online',
          last_seen: new Date().toISOString(),
          metadata: metadata || existing.metadata,
        })
        .eq('id', existing.id);
      return existing as VicCloudDevice;
    }

    const { data: created, error } = await this.client
      .from('devices')
      .insert({
        user_id: userId,
        device_name: deviceName,
        device_type: 'windows_desktop',
        client_version: '1.0.0',
        status: 'online',
        metadata: metadata || {},
        last_seen: new Date().toISOString(),
      })
      .select()
      .single();

    if (error || !created) {
      return null;
    }

    this.deviceId = created.id;
    return created as VicCloudDevice;
  }

  public async sendHeartbeat(status = 'online'): Promise<boolean> {
    if (!this.client || !this.deviceId) return false;
    const { error } = await this.client
      .from('devices')
      .update({ status, last_seen: new Date().toISOString() })
      .eq('id', this.deviceId);
    return !error;
  }

  public async setOffline(): Promise<void> {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
    if (this.client && this.deviceId) {
      await this.client
        .from('devices')
        .update({ status: 'offline', last_seen: new Date().toISOString() })
        .eq('id', this.deviceId);
    }
  }

  public async reportResult(result: RemoteActionResult): Promise<boolean> {
    if (!this.client) return false;
    const { error } = await this.client.from('action_results').insert(result);
    if (!error) {
      await this.client
        .from('device_actions')
        .update({ status: result.status === 'success' ? 'completed' : 'failed' })
        .eq('id', result.action_id);
    }
    return !error;
  }
}
