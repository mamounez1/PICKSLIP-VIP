import { NextRequest, NextResponse } from 'next/server';
import { calculatePrediction } from '@/lib/algorithm';
import { checkCronAuth } from '@/lib/cronAuth';
import { supabaseAdmin } from '@/lib/supabaseAdmin';
import { currentSeason, getFixturesByDate, getH2H, getTeamStats, minGapMs } from '@/lib/apiFootball';

export const dynamic = 'force-dynamic';
export const maxDuration = 60;

const TRACKED_LEAGUES = [
  { id: 39, name: 'Premier League' },
  { id: 140, name: 'La Liga' },
  { id: 61, name: 'Ligue 1' },
  { id: 135, name: 'Serie A' },
  { id: 78, name: 'Bundesliga' },
  { id: 2, name: 'UEFA Champions League' },
];

export async function GET(req: NextRequest) {
  const denied = checkCronAuth(req);
  if (denied) return denied;

  const startedAt = Date.now();
  const BUDGET_MS = 50_000; // maxDuration = 60s

  try {
    const date = req.nextUrl.searchParams.get('date') ?? new Date().toISOString().slice(0, 10);
    const season = currentSeason(new Date(date));
    const maxFixtures = Number(process.env.MAX_FIXTURES_PER_DAY ?? 8);
    const freeCount = Number(process.env.FREE_PICKS_COUNT ?? 3);
    const db = supabaseAdmin();

    // 1. Matchs du jour (pas encore commences)
    const upcoming = (await getFixturesByDate(date, TRACKED_LEAGUES.map((l) => l.id)))
      .filter((f) => f.fixture.status.short === 'NS')
      .sort((a, b) => a.fixture.date.localeCompare(b.fixture.date))
      .slice(0, maxFixtures);

    // 2. Idempotence: on saute les matchs qui ont deja un pronostic
    const ids = upcoming.map((f) => f.fixture.id);
    const { data: existing } = await db.from('predictions').select('fixture_id').in('fixture_id', ids);
    const done = new Set((existing ?? []).map((p) => p.fixture_id as number));

    const fixtureRows: any[] = [];
    const predictionRows: any[] = [];
    const skipped: { id: number; reason: string }[] = [];
    let freeAssigned = (existing?.length ?? 0) > 0 ? freeCount : 0; // deja des gratuits aujourd'hui

    for (const f of upcoming) {
      if (done.has(f.fixture.id)) continue;
      // 3 appels par match: on s'arrete avant le timeout. Relancer la route reprend la ou ca s'est arrete.
      if (Date.now() - startedAt + 3 * minGapMs() + 3000 > BUDGET_MS) {
        skipped.push({ id: f.fixture.id, reason: 'temps ecoule, relancer la route' });
        continue;
      }
      const leagueId = f.league.id;
      const [home, away, h2h] = await Promise.all([
        getTeamStats(f.teams.home.id, leagueId, season),
        getTeamStats(f.teams.away.id, leagueId, season),
        getH2H(f.teams.home.id, f.teams.away.id),
      ]);
      // Pas de donnees reelles = pas de pronostic (jamais de valeurs inventees)
      if (!home || !away || !h2h) {
        skipped.push({ id: f.fixture.id, reason: 'stats indisponibles' });
        continue;
      }
      const p = calculatePrediction(
        { teamName: f.teams.home.name, ...home },
        { teamName: f.teams.away.name, ...away },
        h2h
      );
      fixtureRows.push({
        id: f.fixture.id,
        league: f.league.name,
        home_team: f.teams.home.name,
        away_team: f.teams.away.name,
        kickoff: f.fixture.date,
        home_team_id: f.teams.home.id,
        away_team_id: f.teams.away.id,
        status: 'scheduled',
      });
      predictionRows.push({
        fixture_id: f.fixture.id,
        pick: p.pick,
        pick_type: p.pickType,
        confidence: p.confidence,
        reasoning: p.reasoning,
        is_free_pick: freeAssigned < freeCount,
        result: 'pending',
        odds_est: p.oddsEst,
      });
      freeAssigned++;
    }

    if (fixtureRows.length) {
      const { error: e1 } = await db.from('fixtures').upsert(fixtureRows, { onConflict: 'id' });
      if (e1) throw new Error(`fixtures: ${e1.message}`);
      const { error: e2 } = await db.from('predictions').insert(predictionRows);
      if (e2) throw new Error(`predictions: ${e2.message}`);
    }

    return NextResponse.json({
      success: true,
      date,
      found: upcoming.length,
      generated: predictionRows.length,
      alreadyDone: done.size,
      skipped,
    });
  } catch (error: any) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
