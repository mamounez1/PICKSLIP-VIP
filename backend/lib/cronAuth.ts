import { NextRequest, NextResponse } from 'next/server';

// Refuse tout appel sans le bon secret (ou si CRON_SECRET n'est pas configure).
export function checkCronAuth(req: NextRequest): NextResponse | null {
  const secret = process.env.CRON_SECRET;
  if (!secret || req.headers.get('authorization') !== `Bearer ${secret}`) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  return null;
}
