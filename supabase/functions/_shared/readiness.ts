export type ReadinessStatus = "ready" | "at_risk" | "insufficient_players" | "needs_action";

export interface ParticipationSummary {
  requiredPlayers: number;
  confirmedPlayers: number;
  pendingPlayers: number;
  declinedPlayers: number;
  minutesUntilMatch: number;
}

export interface ReadinessResult {
  status: ReadinessStatus;
  issueSummary: string;
  suggestedActions: string[];
}

export function evaluateReadiness(input: ParticipationSummary): ReadinessResult {
  const missing = Math.max(0, input.requiredPlayers - input.confirmedPlayers);

  if (missing <= 0) {
    return {
      status: "ready",
      issueSummary: "Match has enough confirmed players.",
      suggestedActions: ["Send final reminder 2-3 hours before kickoff"]
    };
  }

  if (input.minutesUntilMatch <= 180) {
    return {
      status: "insufficient_players",
      issueSummary: `Match starts soon and is short by ${missing} player(s).`,
      suggestedActions: [
        "Invite additional players now",
        "Send urgent reminder to pending players",
        "Prepare a postponement option"
      ]
    };
  }

  if (input.pendingPlayers >= missing) {
    return {
      status: "at_risk",
      issueSummary: `Need ${missing} more confirmations. Pending responses may still fill the spots.`,
      suggestedActions: [
        "Remind pending players",
        "Set a response deadline",
        "Prepare one backup slot"
      ]
    };
  }

  return {
    status: "needs_action",
    issueSummary: `Current confirmations plus pending responses are below target by ${missing}.`,
    suggestedActions: [
      "Invite more players",
      "Consider an alternative slot",
      "Send match update message"
    ]
  };
}
