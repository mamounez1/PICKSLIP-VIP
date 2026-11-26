import { requireEnv } from './env';
import type { H2HStatsInput } from './algorithm';

const BASE = 'https://v3.football.api-sports.io';

// Limiteur: le plan gratuit autorise 10 requetes/minute. Chaque appel reserve un creneau
// (meme si plusieurs appels partent en parallele). Plan payant: API_MIN_GAP_MS=200.
let nextSlot = 0;
export function minGapMs(): number {
  return Number(process.env.API_MIN_GAP_MS ?? 6500);
}
async function throttle() {
  const now = Date.now();
  const start = Math.max(now, nextSlot);
  nextSlot = start + minGapMs();
  if (start > now) await new Promise((r) => setTimeout(r, start - now));
}

async function af(path: string, params: Record<string, string | number>): Promise<any[]> {
  await throttle();
  const url = new URL(BASE + path);
  Object.entries(params).forEach(([k, v]) => url.searchParams.set(k, String(v)));
  const res = await fetch(url, { headers: { 'x-apisports-key': requireEnv('API_FOOTBALL_KEY') }, cache: 'no-store' });
  if (!res.ok) throw new Error(`API-Football ${path} -> HTTP ${res.status}`);
  const json = await res.json();
  const errors = json.errors;
  if (errors && (Array.isArray(errors) ? errors.length : Object.keys(errors).length)) {
    throw new Error(`API-Football ${path}: ${JSON.stringify(errors)}`);
  }
  return json.response ?? [];
}

// La saison API-Football = annee de debut (de juillet a juin).
export function currentSeason(d = new Date()): number {
  return d.getUTCMonth() >= 6 ? d.getUTCFullYear() : d.getUTCFullYear() - 1;
}

export interface ApiFixture {
  fixture: { id: number; date: string; status: { short: string } };
  league: { id: number; name: string; country: string };
  teams: { home: { id: number; name: string }; away: { id: number; name: string } };
  goals: { home: number | null; away: number | null };
  score: { fulltime: { home: number | null; away: number | null } };
}

// UN SEUL appel pour tous les matchs du jour, filtres ensuite localement par ligue.
export async function getFixturesByDate(date: string, leagueIds: number[]): Promise<ApiFixture[]> {
  const all = (await af('/fixtures', { date })) as ApiFixture[];
  return all.filter((f) => leagueIds.includes(f.league.id));
}

export async function getFixturesByIds(ids: number[]): Promise<ApiFixture[]> {
  return af('/fixtures', { ids: ids.join('-') });
}

export interface TeamStats {
  recentForm: string;
  goalsForAvg: number;
  goalsAgainstAvg: number;
}

// Retourne null si les stats sont indisponibles (on ne devine jamais de valeurs).
export async function getTeamStats(teamId: number, leagueId: number, season: number): Promise<TeamStats | null> {
  const r: any = await af('/teams/statistics', { team: teamId, league: leagueId, season });
  const stats = Array.isArray(r) ? r[0] : r;
  const form: string | undefined = stats?.form;
  const gf = parseFloat(stats?.goals?.for?.average?.total);
  const ga = parseFloat(stats?.goals?.against?.average?.total);
  if (!form || Number.isNaN(gf) || Number.isNaN(ga)) return null;
  return { recentForm: form.slice(-5), goalsForAvg: gf, goalsAgainstAvg: ga };
}

export async function getH2H(homeId: number, awayId: number): Promise<H2HStatsInput | null> {
  const games = (await af('/fixtures/headtohead', { h2h: `${homeId}-${awayId}`, last: 10 })) as ApiFixture[];
  const done = games.filter((g) => g.goals.home !== null && g.goals.away !== null);
  if (done.length === 0) return null;
  let btts = 0, over25 = 0, homeWins = 0, draws = 0, awayWins = 0;
  for (const g of done) {
    const h = g.goals.home as number;
    const a = g.goals.away as number;
    if (h > 0 && a > 0) btts++;
    if (h + a > 2.5) over25++;
    // orienter selon NOTRE equipe a domicile (homeId)
    const ourHomeScore = g.teams.home.id === homeId ? h : a;
    const ourAwayScore = g.teams.home.id === homeId ? a : h;
    if (ourHomeScore > ourAwayScore) homeWins++;
    else if (ourHomeScore === ourAwayScore) draws++;
    else awayWins++;
  }
  return { bttsRate: btts / done.length, over25Rate: over25 / done.length, homeWins, draws, awayWins };
}
