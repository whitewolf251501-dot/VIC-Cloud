const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');
const path = require('path');

// Read .env.local
const envPath = path.resolve(__dirname, '../../.env.local');
const envContent = fs.readFileSync(envPath, 'utf8');
const env = {};
envContent.split('\n').forEach(line => {
  const match = line.match(/^\s*([A-Za-z0-9_]+)\s*=\s*(.*)$/);
  if (match) env[match[1]] = match[2].trim();
});

const SUPABASE_URL = env.NEXT_PUBLIC_SUPABASE_URL;
const ANON_KEY = env.NEXT_PUBLIC_SUPABASE_ANON_KEY;
const SERVICE_KEY = env.SUPABASE_SERVICE_ROLE_KEY;

if (!SUPABASE_URL || !SERVICE_KEY) {
  console.error('Missing Supabase credentials in .env.local');
  process.exit(1);
}

// 1. Service Role Client (Admin)
const adminClient = createClient(SUPABASE_URL, SERVICE_KEY, {
  auth: { autoRefreshToken: false, persistSession: false }
});

// 2. Anon Client (Public/User)
const anonClient = createClient(SUPABASE_URL, ANON_KEY);

async function runTests() {
  console.log('====================================================');
  console.log('VIC-CLOUD PHASE 2 SUPABASE INTEGRATION TEST SUITE');
  console.log('====================================================');

  const testEmail1 = `vic_test_alpha_${Date.now()}@example.com`;
  const testEmail2 = `vic_test_beta_${Date.now()}@example.com`;
  const testPassword = 'Password#123456Secure!';

  let userId1 = null;
  let userId2 = null;

  try {
    // TEST 1: Table schema existence
    console.log('\n[TEST 1] Verifying all 8 required tables exist in database...');
    const requiredTables = ['users', 'devices', 'conversations', 'messages', 'memories', 'knowledge', 'device_actions', 'action_results'];
    for (const table of requiredTables) {
      const { data, error } = await adminClient.from(table).select('count', { count: 'exact', head: true });
      if (error) throw new Error(`Table check failed for '${table}': ${error.message}`);
      console.log(`  ✓ Table '${table}' verified accessible.`);
    }

    // TEST 2: Create Test User 1
    console.log('\n[TEST 2] Creating Test User 1 via Supabase Auth Admin...');
    const { data: u1, error: u1Err } = await adminClient.auth.admin.createUser({
      email: testEmail1,
      password: testPassword,
      email_confirm: true,
      user_metadata: { full_name: 'VIC Test Commander Alpha' }
    });
    if (u1Err) throw u1Err;
    userId1 = u1.user.id;
    console.log(`  ✓ User 1 created with ID: ${userId1}`);

    // Wait 500ms for trigger
    await new Promise(r => setTimeout(r, 600));

    // Verify public.users trigger populated profile
    const { data: profile1, error: profErr } = await adminClient.from('users').select('*').eq('id', userId1).single();
    if (profErr || !profile1) throw new Error(`User profile trigger failed: ${profErr?.message}`);
    console.log(`  ✓ Public user profile verified for User 1 (Name: ${profile1.full_name})`);

    // TEST 3: Create Test User 2 (for RLS isolation test)
    console.log('\n[TEST 3] Creating Test User 2 for RLS cross-tenant isolation testing...');
    const { data: u2, error: u2Err } = await adminClient.auth.admin.createUser({
      email: testEmail2,
      password: testPassword,
      email_confirm: true,
      user_metadata: { full_name: 'VIC Test Commander Beta' }
    });
    if (u2Err) throw u2Err;
    userId2 = u2.user.id;
    console.log(`  ✓ User 2 created with ID: ${userId2}`);

    // TEST 4: User 1 Authenticated Session
    console.log('\n[TEST 4] Authenticating User 1 client session...');
    const userClient1 = createClient(SUPABASE_URL, ANON_KEY);
    const { data: authSession1, error: authErr1 } = await userClient1.auth.signInWithPassword({
      email: testEmail1,
      password: testPassword
    });
    if (authErr1) throw authErr1;
    console.log(`  ✓ User 1 session active (Token scoped to UID: ${authSession1.user.id})`);

    // TEST 5: User 1 Registers Devices (Windows Desktop & Android Phone)
    console.log('\n[TEST 5] Registering devices for User 1...');
    const { data: dev1, error: dev1Err } = await userClient1.from('devices').insert({
      user_id: userId1,
      device_name: 'Workstation-Win11-Primary',
      device_type: 'windows_desktop',
      client_version: '1.0.0',
      status: 'online'
    }).select().single();
    if (dev1Err) throw dev1Err;
    console.log(`  ✓ Windows Desktop registered: ${dev1.device_name} (ID: ${dev1.id})`);

    const { data: dev2, error: dev2Err } = await userClient1.from('devices').insert({
      user_id: userId1,
      device_name: 'Redmi-Note-12-Pro-Plus',
      device_type: 'android_phone',
      client_version: '1.0.0',
      status: 'online'
    }).select().single();
    if (dev2Err) throw dev2Err;
    console.log(`  ✓ Android Phone registered: ${dev2.device_name} (ID: ${dev2.id})`);

    // TEST 6: Memory Insertion and Category Constraint
    console.log('\n[TEST 6] Testing shared memory vault...');
    const { data: mem1, error: mem1Err } = await userClient1.from('memories').insert({
      user_id: userId1,
      category: 'preference',
      content: 'User prefers concise technical responses with forward slashes for file links.',
      importance: 0.95
    }).select().single();
    if (mem1Err) throw mem1Err;
    console.log(`  ✓ Memory inserted: "${mem1.content}"`);

    // TEST 7: Cross-Device Conversation & Message Flow
    console.log('\n[TEST 7] Testing synchronized conversation & messages...');
    const { data: conv1, error: convErr } = await userClient1.from('conversations').insert({
      user_id: userId1,
      title: 'Voice Assistant Setup Session'
    }).select().single();
    if (convErr) throw convErr;

    const { data: msg1, error: msgErr1 } = await userClient1.from('messages').insert({
      conversation_id: conv1.id,
      user_id: userId1,
      role: 'user',
      content: 'VIC, status report on cloud sync.',
      origin_device_type: 'android_phone'
    }).select().single();
    if (msgErr1) throw msgErr1;

    const { data: msg2, error: msgErr2 } = await userClient1.from('messages').insert({
      conversation_id: conv1.id,
      user_id: userId1,
      role: 'assistant',
      content: 'Cloud synchronization operational. Both desktop and phone nodes linked.',
      origin_device_type: 'windows_desktop'
    }).select().single();
    if (msgErr2) throw msgErr2;
    console.log(`  ✓ Multi-turn message exchange recorded across Android & Desktop.`);

    // TEST 8: Command Queue Routing & Result Execution
    console.log('\n[TEST 8] Testing device action queue & confirmation protocol...');
    const { data: action1, error: actErr } = await userClient1.from('device_actions').insert({
      user_id: userId1,
      source_device_id: dev2.id, // Phone
      target_device_id: dev1.id, // Desktop
      action_type: 'open_application',
      payload: { app: 'code', args: ['C:/Users/shlok/OneDrive/Desktop/VIC-Cloud'] },
      risk_tier: 1,
      status: 'pending'
    }).select().single();
    if (actErr) throw actErr;
    console.log(`  ✓ Action queued from Android to Desktop (ID: ${action1.id}, Status: ${action1.status})`);

    const { data: res1, error: resErr } = await userClient1.from('action_results').insert({
      action_id: action1.id,
      user_id: userId1,
      executing_device_id: dev1.id,
      status: 'success',
      result: { executed: true, pid: 14208 }
    }).select().single();
    if (resErr) throw resErr;
    console.log(`  ✓ Action result reported by Desktop (Status: ${res1.status})`);

    // TEST 9: Row-Level Security (RLS) Isolation Verification
    console.log('\n[TEST 9] Verifying RLS cross-user tenant isolation...');
    const userClient2 = createClient(SUPABASE_URL, ANON_KEY);
    const { error: authErr2 } = await userClient2.auth.signInWithPassword({
      email: testEmail2,
      password: testPassword
    });
    if (authErr2) throw authErr2;

    // User 2 tries to read User 1's memories
    const { data: u2Memories, error: u2MemErr } = await userClient2.from('memories').select('*');
    if (u2MemErr) throw u2MemErr;
    if (u2Memories.length > 0) {
      throw new Error(`CRITICAL SECURITY FAILURE: User 2 accessed User 1's private memories! Count: ${u2Memories.length}`);
    }
    console.log(`  ✓ RLS ISOLATION VERIFIED: User 2 query returned 0 rows for User 1 data.`);

    // User 2 tries to read User 1's devices
    const { data: u2Devices } = await userClient2.from('devices').select('*');
    if (u2Devices.length > 0) {
      throw new Error(`CRITICAL SECURITY FAILURE: User 2 accessed User 1's registered devices!`);
    }
    console.log(`  ✓ RLS ISOLATION VERIFIED: User 2 cannot see User 1's registered devices.`);

    // User 2 tries to read User 1's device actions
    const { data: u2Actions } = await userClient2.from('device_actions').select('*');
    if (u2Actions.length > 0) {
      throw new Error(`CRITICAL SECURITY FAILURE: User 2 accessed User 1's device command queue!`);
    }
    console.log(`  ✓ RLS ISOLATION VERIFIED: User 2 cannot see User 1's command queue.`);

    console.log('\n====================================================');
    console.log('ALL PHASE 2 SUPABASE TESTS PASSED (9/9 SUITES PASS)');
    console.log('====================================================');

  } finally {
    // Teardown test users
    console.log('\nTearing down test users to preserve database hygiene...');
    if (userId1) {
      await adminClient.auth.admin.deleteUser(userId1);
      console.log(`  ✓ Test User 1 (${userId1}) cleaned up.`);
    }
    if (userId2) {
      await adminClient.auth.admin.deleteUser(userId2);
      console.log(`  ✓ Test User 2 (${userId2}) cleaned up.`);
    }
  }
}

runTests().catch(err => {
  console.error('\n❌ Test Suite Failed:', err);
  process.exit(1);
});
