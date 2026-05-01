import { corsHeaders, jsonResponse } from "../_shared/cors.ts";
import { createServiceClient, requireUser } from "../_shared/client.ts";
import { generateGeminiText } from "../_shared/gemini.ts";
import { evaluateReadiness } from "../_shared/readiness.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return jsonResponse({ error: "Method not allowed" }, 405);
  }

  try {
    await requireUser(req);
    const body = await req.json();
    const matchId = String(body?.matchId ?? "").trim();

    if (!matchId) {
      return jsonResponse({ error: "matchId is required" }, 400);
    }

    const supabase = createServiceClient();
    const { data: match, error: matchError } = await supabase
      .from("matches")
      .select("id,sport_type,match_time,required_players,venue_id,status")
      .eq("id", matchId)
      .single();

    if (matchError || !match) {
      return jsonResponse({ error: "Match not found" }, 404);
    }

    const { data: invitations } = await supabase
      .from("invitations")
      .select("response_status")
      .eq("match_id", matchId);

    const confirmed = (invitations ?? []).filter((it) => it.response_status === "accepted").length;
    const pending = (invitations ?? []).filter((it) => it.response_status === "pending").length;
    const declined = (invitations ?? []).filter((it) => it.response_status === "declined").length;
    const minutesUntilMatch = Math.floor((new Date(match.match_time).getTime() - Date.now()) / 60000);

    const readiness = evaluateReadiness({
      requiredPlayers: match.required_players,
      confirmedPlayers: confirmed,
      pendingPlayers: pending,
      declinedPlayers: declined,
      minutesUntilMatch
    });

    const { data: venue } = await supabase
      .from("venues")
      .select("name,address")
      .eq("id", match.venue_id)
      .single();

    const { data: alternativeSlots } = await supabase
      .from("time_slots")
      .select("id,venue_id,start_time,end_time,is_available")
      .eq("venue_id", match.venue_id)
      .eq("is_available", true)
      .gte("start_time", match.match_time)
      .order("start_time", { ascending: true })
      .limit(3);

    const reminderText = await generateGeminiText(
      `Generate a reminder for pending players. Match: ${match.sport_type}, venue: ${venue?.name}, time: ${match.match_time}, pending: ${pending}`
    ) ?? `Reminder: please confirm your response for ${match.sport_type} at ${venue?.name} on ${new Date(match.match_time).toLocaleString()}.`;

    const updateText = await generateGeminiText(
      `Generate a short match update when readiness status is ${readiness.status}. Confirmed=${confirmed}, pending=${pending}, required=${match.required_players}.`
    ) ?? `Update: we currently have ${confirmed}/${match.required_players} confirmed players. Please respond soon.`;

    return jsonResponse({
      matchId,
      readinessStatus: readiness.status,
      issueSummary: readiness.issueSummary,
      suggestedNextActions: readiness.suggestedActions,
      participation: {
        requiredPlayers: match.required_players,
        confirmedPlayers: confirmed,
        pendingPlayers: pending,
        declinedPlayers: declined,
        remainingSpots: Math.max(0, match.required_players - confirmed)
      },
      alternativeSlots: (alternativeSlots ?? []).map((slot) => ({
        timeSlotId: slot.id,
        venueId: slot.venue_id,
        startTime: slot.start_time,
        endTime: slot.end_time
      })),
      reminderText,
      updateText
    });
  } catch (error) {
    return jsonResponse({ error: (error as Error).message }, 500);
  }
});