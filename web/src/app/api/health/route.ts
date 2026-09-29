import { NextResponse } from 'next/server';
import { getServiceRoleClient } from '@/lib/supabase-server';

export const dynamic = 'force-dynamic';

export async function GET() {
  const startTime = Date.now();
  let dbStatus = 'disconnected';
  let latencyMs = 0;

  try {
    const supabase = getServiceRoleClient();
    const { error } = await supabase.from('devices').select('count', { count: 'exact', head: true });
    if (!error) {
      dbStatus = 'connected';
      latencyMs = Date.now() - startTime;
    }
  } catch (err: any) {
    dbStatus = `error: ${err.message}`;
  }

  return NextResponse.json({
    service: 'VIC Cloud Control Center API',
    status: dbStatus === 'connected' ? 'healthy' : 'degraded',
    version: '1.0.0',
    timestamp: new Date().toISOString(),
    database: {
      status: dbStatus,
      latencyMs,
    },
    uptimeSeconds: process.uptime(),
  });
}
