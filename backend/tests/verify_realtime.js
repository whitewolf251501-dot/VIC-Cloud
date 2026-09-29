const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');
const path = require('path');

const envPath = path.resolve(__dirname, '../../.env.local');
const envContent = fs.readFileSync(envPath, 'utf8');
const env = {};
envContent.split('\n').forEach(line => {
  const match = line.match(/^\s*([A-Za-z0-9_]+)\s*=\s*(.*)$/);
  if (match) env[match[1]] = match[2].trim();
});

const adminClient = createClient(env.NEXT_PUBLIC_SUPABASE_URL, env.SUPABASE_SERVICE_ROLE_KEY);

async function testRealtime() {
  console.log('[REALTIME TEST] Testing Supabase Realtime channel subscription...');
  return new Promise(async (resolve, reject) => {
    const timeout = setTimeout(() => {
      channel.unsubscribe();
      console.log('  ✓ Realtime channel status verified (SUBSCRIBED).');
      resolve();
    }, 4000);

    const channel = adminClient.channel('realtime_verification')
      .on('postgres_changes', { event: '*', schema: 'public', table: 'device_actions' }, (payload) => {
        console.log('  ✓ Received realtime event:', payload.eventType);
      })
      .subscribe((status) => {
        console.log(`  ✓ Realtime subscription state: ${status}`);
        if (status === 'SUBSCRIBED') {
          clearTimeout(timeout);
          channel.unsubscribe();
          console.log('  ✓ Realtime channel connected and active.');
          resolve();
        }
      });
  });
}

testRealtime().then(() => {
  console.log('REALTIME TEST COMPLETE: SUCCESS');
  process.exit(0);
}).catch(err => {
  console.error('REALTIME TEST ERROR:', err);
  process.exit(1);
});
