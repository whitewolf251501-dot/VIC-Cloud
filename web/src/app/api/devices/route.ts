import { NextRequest, NextResponse } from 'next/server';
import { getServiceRoleClient } from '@/lib/supabase-server';

export const dynamic = 'force-dynamic';

export async function GET(req: NextRequest) {
  try {
    const supabase = getServiceRoleClient();
    const { data: devices, error } = await supabase
      .from('devices')
      .select('*')
      .order('last_seen', { ascending: false });

    if (error) {
      return NextResponse.json({ error: error.message }, { status: 500 });
    }

    return NextResponse.json({ devices: devices || [] });
  } catch (err: any) {
    return NextResponse.json({ error: err.message }, { status: 500 });
  }
}

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const { userId, deviceName, deviceType, clientVersion, status, metadata } = body;

    if (!userId || !deviceName || !deviceType) {
      return NextResponse.json({ error: 'Missing required device fields' }, { status: 400 });
    }

    const supabase = getServiceRoleClient();
    const { data, error } = await supabase
      .from('devices')
      .upsert(
        {
          user_id: userId,
          device_name: deviceName,
          device_type: deviceType,
          client_version: clientVersion || '1.0.0',
          status: status || 'online',
          metadata: metadata || {},
          last_seen: new Date().toISOString(),
        },
        { onConflict: 'user_id,device_name' }
      )
      .select()
      .single();

    if (error) {
      return NextResponse.json({ error: error.message }, { status: 500 });
    }

    return NextResponse.json({ device: data });
  } catch (err: any) {
    return NextResponse.json({ error: err.message }, { status: 500 });
  }
}
