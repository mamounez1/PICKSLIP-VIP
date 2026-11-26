import { NextRequest, NextResponse } from 'next/server';
import { evaluateMatchResult, PredictionOutput } from '@/lib/algorithm';
import { checkCronAuth } from '@/lib/cronAuth';
import { supabaseAdmin } from '@/lib/supabaseAdmin';
import { getFixturesByIds } from '@/lib/apiFootball';

export const dynamic = 'force-dynamic';
export const maxDuration = 60;

const FINISHED = new Set(['FT', 'AET', 'PEN']);
const VOIDED: Record<string, 'canceled' | 'postponed'> = {
  CANC: 'canceled', ABD: 'canceled', AWD: 'canceled', WO: 'canceled', PST: 'postponed',
};

export async function GET(req: NextRequest) {
  const denied = checkCronAuth(req);
  if (denied) return denied;

  try {
    const db = supabaseAdmin();
    const cutoff = new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString(); // coup d'envoi + 3h

    // 1. Pronostics en attente dont le match devrait etre termine
    const { data: pending, error } = await db
      .from('predictions')
      .select('id, pick_type, fixture_id, fixtures!inner(id, kickoff)')
      .eq('result', 'pending')
      .lt('fixtures.kickoff', cutoff);
    if (error) throw new Error(error.message);
    if (!pending?.length) return NextResponse.json({ success: true, resolved: 0 });

    const fixtureIds = [...new Set(pending.map((p: any) => p.fixture_id as number))];
    const dates = new Set<string>();
    let resolved = 0;

    // 2. Scores finaux (API-Football accepte 20 ids max par appel)
    for (let i = 0; i < fixtureIds.length; i += 20) {
      const chunk = fixtureIds.slice(i, i + 20);
      const apiFixtures = await getFixturesByIds(chunk);

      for (const f of apiFixtures) {
        const short = f.fixture.status.short;
        const preds = pending.filter((p: any) => p.fixture_id === f.fixture.id);
        dates.add(f.fixture.date.slice(0, 10));

        if (FINISHED.has(short)) {
          // Les marches se jouent sur le temps reglementaire (90 min)
          const h = f.score.fulltime.home ?? f.goals.home;
          const a = f.score.fulltime.away ?? f.goals.away;
          if (h === null || a === null) continue;
          await db.from('fixtures').update({ status: 'finished', home_score: h, away_score: a }).eq('id', f.fixture.id);
          for (const p of preds as any[]) {
            const result = evaluateMatchResult(p.pick_type as PredictionOutput['pickType'], h, a);
            await db.from('predictions').update({ result }).eq('id', p.id);
            resolved++;
          }
        } else if (VOIDED[short]) {
          await db.from('fixtures').update({ status: VOIDED[short] }).eq('id', f.fixture.id);
          for (const p of preds as any[]) {
            await db.from('predictions').update({ result: 'void' }).eq('id', p.id);
            resolved++;
          }
        }
        // sinon (match en cours / pas fini): on reessaiera au prochain passage
      }
    }

    // 3. Recalcul des stats journalieres (track record public)
    for (const date of dates) {
      const next = new Date(new Date(date).getTime() + 86400000).toISOString().slice(0, 10);
      const { data: rows } = await db
        .from('predictions')
        .select('result, fixtures!inner(kickoff)')
        .gte('fixtures.kickoff', `${date}T00:00:00Z`)
        .lt('fixtures.kickoff', `${next}T00:00:00Z`);
      const wins = (rows ?? []).filter((r: any) => r.result === 'win').length;
      const losses = (rows ?? []).filter((r: any) => r.result === 'loss').length;
      const total = wins + losses;
      await db.from('stats_daily').upsert(
        { date, picks_count: total, wins, losses, win_rate: total ? Math.round((wins / total) * 10000) / 100 : 0 },
        { onConflict: 'date' }
      );
    }

    return NextResponse.json({ success: true, resolved, daysUpdated: [...dates] });
  } catch (error: any) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
