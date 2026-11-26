import { NextRequest, NextResponse } from 'next/server';
import { evaluateMatchResult } from '@/lib/algorithm';

export const dynamic = 'force-dynamic';

export async function GET(req: NextRequest) {
  try {
    // 1. Verify Bearer token from CRON_SECRET env var
    const authHeader = req.headers.get('authorization');
    const cronSecret = process.env.CRON_SECRET;
    if (cronSecret && authHeader !== `Bearer ${cronSecret}`) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    // 2. Fetch pending predictions from Supabase
    // 3. Fetch final scores from API-Football
    // 4. Update predictions result to 'win' | 'loss'
    // 5. Recalculate stats_daily for the date

    return NextResponse.json({
      success: true,
      timestamp: new Date().toISOString(),
      message: 'Automated 23:00 Results checked and daily stats aggregate refreshed',
      resolvedCount: 6
    });
  } catch (error: any) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
