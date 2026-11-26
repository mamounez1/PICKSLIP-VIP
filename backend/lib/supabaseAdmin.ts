import { createClient } from '@supabase/supabase-js';
import { requireEnv } from './env';

// Client "service role": contourne le RLS. A utiliser UNIQUEMENT cote serveur.
export function supabaseAdmin() {
  return createClient(requireEnv('NEXT_PUBLIC_SUPABASE_URL'), requireEnv('SUPABASE_SERVICE_ROLE_KEY'), {
    auth: { persistSession: false },
  });
}
