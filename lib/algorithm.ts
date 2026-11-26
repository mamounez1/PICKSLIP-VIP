/**
 * PickSlip - Statistical Football Prediction Algorithm
 * Pure Deterministic Mathematical Logic (Zero AI / LLM)
 */

export interface TeamStatsInput {
  teamName: string;
  recentForm: string; // e.g., "WWDLW" (last 5 results: W=Win, D=Draw, L=Loss)
  goalsForAvg: number; // average goals scored per match
  goalsAgainstAvg: number; // average goals conceded per match
}

export interface H2HStatsInput {
  bttsRate: number; // Both teams to score rate (0.0 - 1.0)
  over25Rate: number; // Over 2.5 goals rate (0.0 - 1.0)
  homeWins: number;
  draws: number;
  awayWins: number;
}

export interface PredictionOutput {
  pick: string;
  pickType: 'OVER_25' | 'UNDER_25' | 'BTTS_YES' | 'HOME_WIN' | 'AWAY_WIN' | 'DRAW' | 'DC_1X' | 'DC_X2';
  confidence: number; // 50 - 95 %
  reasoning: string; // Short French-language analytical reasoning
  oddsEst: number;
  metrics: {
    expectedGoalsHome: number;
    expectedGoalsAway: number;
    totalExpectedGoals: number;
    formGap: number;
    homeFormIndex: number;
    awayFormIndex: number;
  };
}

interface CandidatePick {
  pick: string;
  pickType: PredictionOutput['pickType'];
  confidence: number;
  reasoning: string;
  oddsEst: number;
}

export function calculatePrediction(
  home: TeamStatsInput,
  away: TeamStatsInput,
  h2h: H2HStatsInput
): PredictionOutput {
  // 1. Calculate Form points out of 15 max
  const homeFormPoints = calculateFormPoints(home.recentForm);
  const awayFormPoints = calculateFormPoints(away.recentForm);
  const homeFormIndex = homeFormPoints / 15.0;
  const awayFormIndex = awayFormPoints / 15.0;
  const formGap = homeFormIndex - awayFormIndex; // -1.0 to +1.0

  // 2. Expected Goals (xG) Calculation + Home field advantage (+12%)
  const homeAdvantage = 1.12;
  const xGHome = Math.max(0.4, (home.goalsForAvg * 0.65 + away.goalsAgainstAvg * 0.35) * homeAdvantage);
  const xGAway = Math.max(0.3, (away.goalsForAvg * 0.55 + home.goalsAgainstAvg * 0.45) * 0.92);
  const totalXG = xGHome + xGAway;

  const candidates: CandidatePick[] = [];

  // Candidate A: Over 2.5 Buts
  if (totalXG >= 2.6 || (h2h.over25Rate >= 0.65 && totalXG >= 2.3)) {
    const conf = clampConfidence(56.0 + (totalXG - 2.5) * 22.0 + h2h.over25Rate * 18.0);
    candidates.push({
      pick: 'Plus de 2.5 Buts',
      pickType: 'OVER_25',
      confidence: conf,
      reasoning: `xG total combiné projeté à ${totalXG.toFixed(1)} buts et ${Math.round(h2h.over25Rate * 100)}% de scores > 2.5 buts en H2H.`,
      oddsEst: roundOdds(1.60 + (95 - conf) * 0.015),
    });
  }

  // Candidate B: Moins de 2.5 Buts
  if (totalXG <= 2.15 || (h2h.over25Rate <= 0.35 && totalXG <= 2.45)) {
    const conf = clampConfidence(58.0 + (2.5 - totalXG) * 24.0 + (1.0 - h2h.over25Rate) * 16.0);
    candidates.push({
      pick: 'Moins de 2.5 Buts',
      pickType: 'UNDER_25',
      confidence: conf,
      reasoning: `Blocs défensifs très étanches (xG total estimé à seulement ${totalXG.toFixed(1)}) avec ${Math.round((1 - h2h.over25Rate) * 100)}% de scores sous 2.5 buts.`,
      oddsEst: roundOdds(1.68 + (95 - conf) * 0.014),
    });
  }

  // Candidate C: Both Teams To Score (BTTS - Oui)
  if (xGHome >= 1.15 && xGAway >= 0.95 && h2h.bttsRate >= 0.55) {
    const conf = clampConfidence(55.0 + h2h.bttsRate * 26.0 + Math.min(xGHome, xGAway) * 12.0);
    candidates.push({
      pick: 'Les 2 équipes marquent (BTTS)',
      pickType: 'BTTS_YES',
      confidence: conf,
      reasoning: `Potentiel offensif bilatéral (${Math.round(h2h.bttsRate * 100)}% BTTS historique et au moins 1 but attendu pour chaque équipe).`,
      oddsEst: roundOdds(1.72 + (95 - conf) * 0.012),
    });
  }

  // Candidate D: Home Win (Victoire 1)
  if (formGap >= 0.20 && xGHome - xGAway >= 0.50) {
    const conf = clampConfidence(58.0 + formGap * 36.0 + (xGHome - xGAway) * 14.0);
    candidates.push({
      pick: `Victoire ${home.teamName}`,
      pickType: 'HOME_WIN',
      confidence: conf,
      reasoning: `${home.teamName} affiche une dynamique supérieure (${homeFormPoints}/15 pts) et un ascendant net à domicile.`,
      oddsEst: roundOdds(1.50 + (95 - conf) * 0.02),
    });
  }

  // Candidate E: Away Win (Victoire 2)
  if (formGap <= -0.25 && xGAway - xGHome >= 0.35) {
    const conf = clampConfidence(56.0 + -formGap * 38.0 + (xGAway - xGHome) * 15.0);
    candidates.push({
      pick: `Victoire ${away.teamName}`,
      pickType: 'AWAY_WIN',
      confidence: conf,
      reasoning: `${away.teamName} est en grande forme (${awayFormPoints}/15 pts) face à un adversaire en méforme défensive.`,
      oddsEst: roundOdds(1.85 + (95 - conf) * 0.025),
    });
  }

  // Candidate F: Double Chance Safety (1X or X2)
  if (formGap >= 0.05) {
    const conf = clampConfidence(72.0 + formGap * 20.0 + 4.0);
    candidates.push({
      pick: `Double Chance ${home.teamName} ou Nul (1X)`,
      pickType: 'DC_1X',
      confidence: conf,
      reasoning: `Couverture statistique optimale avec l'avantage du terrain en faveur de ${home.teamName}.`,
      oddsEst: roundOdds(1.28 + (95 - conf) * 0.008),
    });
  } else {
    const conf = clampConfidence(70.0 + -formGap * 20.0 + 4.0);
    candidates.push({
      pick: `Double Chance ${away.teamName} ou Nul (X2)`,
      pickType: 'DC_X2',
      confidence: conf,
      reasoning: `Sécurisation sur la forme extérieure solide de ${away.teamName} sans risque en cas de score nul.`,
      oddsEst: roundOdds(1.32 + (95 - conf) * 0.008),
    });
  }

  // Select Candidate with highest confidence
  const best = candidates.reduce((prev, current) => (current.confidence > prev.confidence ? current : prev), candidates[0]);

  return {
    pick: best.pick,
    pickType: best.pickType,
    confidence: Number(best.confidence.toFixed(1)),
    reasoning: best.reasoning,
    oddsEst: best.oddsEst,
    metrics: {
      expectedGoalsHome: Number(xGHome.toFixed(2)),
      expectedGoalsAway: Number(xGAway.toFixed(2)),
      totalExpectedGoals: Number(totalXG.toFixed(2)),
      formGap: Number(formGap.toFixed(2)),
      homeFormIndex: Number(homeFormIndex.toFixed(2)),
      awayFormIndex: Number(awayFormIndex.toFixed(2)),
    },
  };
}

function calculateFormPoints(form: string): number {
  let points = 0;
  for (const c of form.toUpperCase().slice(0, 5)) {
    if (c === 'W' || c === 'V') points += 3;
    else if (c === 'D' || c === 'N') points += 1;
  }
  return points;
}

function clampConfidence(val: number): number {
  return Math.min(95, Math.max(50, val));
}

function roundOdds(odds: number): number {
  return Math.round(Math.min(5.0, Math.max(1.15, odds)) * 100) / 100;
}

export function evaluateMatchResult(pickType: PredictionOutput['pickType'], homeScore: number, awayScore: number): 'win' | 'loss' {
  const total = homeScore + awayScore;
  const bothScored = homeScore > 0 && awayScore > 0;

  switch (pickType) {
    case 'OVER_25':
      return total > 2.5 ? 'win' : 'loss';
    case 'UNDER_25':
      return total < 2.5 ? 'win' : 'loss';
    case 'BTTS_YES':
      return bothScored ? 'win' : 'loss';
    case 'HOME_WIN':
      return homeScore > awayScore ? 'win' : 'loss';
    case 'AWAY_WIN':
      return awayScore > homeScore ? 'win' : 'loss';
    case 'DRAW':
      return homeScore === awayScore ? 'win' : 'loss';
    case 'DC_1X':
      return homeScore >= awayScore ? 'win' : 'loss';
    case 'DC_X2':
      return awayScore >= homeScore ? 'win' : 'loss';
    default:
      return 'loss';
  }
}
