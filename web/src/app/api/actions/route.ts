import { NextRequest, NextResponse } from 'next/server';
import { getServiceRoleClient } from '@/lib/supabase-server';

export const dynamic = 'force-dynamic';

export async function GET(req: NextRequest) {
  try {
    const supabase = getServiceRoleClient();
    const { searchParams } = new URL(req.url);
    const targetDeviceId = searchParams.get('targetDeviceId');

    let query = supabase.from('device_actions').select('*').order('created_at', { ascending: false }).limit(50);
    if (targetDeviceId) {
      query = query.eq('target_device_id', targetDeviceId);
    }

    const { data: actions, error } = await query;
    if (error) return NextResponse.json({ error: error.message }, { status: 500 });

    return NextResponse.json({ actions: actions || [] });
  } catch (err: any) {
    return NextResponse.json({ error: err.message }, { status: 500 });
  }
}

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const { userId, sourceDeviceId, targetDeviceId, actionType, payload, riskTier } = body;

    if (!userId || !targetDeviceId || !actionType) {
      return NextResponse.json({ error: 'Missing required action parameters' }, { status: 400 });
    }

    const supabase = getServiceRoleClient();
    const { data, error } = await supabase
      .from('device_actions')
      .insert({
        user_id: userId,
        source_device_id: sourceDeviceId || null,
        target_device_id: targetDeviceId,
        action_type: actionType,
        payload: payload || {},
        risk_tier: riskTier ?? 0,
        requires_confirmation: (riskTier ?? 0) >= 2,
        status: 'pending',
      })
      .select()
      .single();

    if (error) return NextResponse.json({ error: error.message }, { status: 500 });

    return NextResponse.json({ action: data });
  } catch (err: any) {
    return NextResponse.json({ error: err.message }, { status: 500 });
  }
}
