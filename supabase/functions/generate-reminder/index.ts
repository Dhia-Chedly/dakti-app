import { corsHeaders, jsonResponse } from "../_shared/cors.ts";
import { createServiceClient, requireUser } from "../_shared/client.ts";
import { generateGeminiText } from "../_shared/gemini.ts";

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
    const audienceType = String(body?.audienceType ?? "players").trim();

    if (!matchId) {
      return jsonResponse({ error: "matchId is required" }, 400);
    }

    const supabase = createServiceClient();
    const { data: match, error: matchError } = await supabase
      .from("matches")
      .select("id,sport_type,match_time,required_players,venue_id")
      .eq("id", matchId)
      .single();

    if (matchError || !match) {
      return jsonResponse({ error: "Match not found" }, 404);
    }

    const { data: venue } = await supabase
      .from("venues")
      .select("name,address")
      .eq("id", match.venue_id)
      .single();

    const kickoff = new Date(match.match_time).toLocaleString();
    const baseText = `Reminder: ${match.sport_type} match at ${venue?.name ?? "venue"} (${venue?.address ?? "location shared soon"}) on ${kickoff}. Please arrive 20 minutes early.`;

    const geminiRewrite = await generateGeminiText(
      `Create a ${audienceType}-focused reminder message:\n${baseText}`
    );

    return jsonResponse({
      matchId,
      audienceType,
      reminderText: geminiRewrite ?? baseText,
      variants: [
        baseText,
        `Reminder: kickoff is ${kickoff}. Please confirm attendance now.`
      ]
    });
  } catch (error) {
    return jsonResponse({ error: (error as Error).message }, 500);
  }
});