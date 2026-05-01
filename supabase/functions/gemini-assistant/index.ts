import { corsHeaders, jsonResponse } from "../_shared/cors.ts";
import { createServiceClient, requireUser } from "../_shared/client.ts";
import { generateGeminiText } from "../_shared/gemini.ts";

type VenueAvailabilityRow = {
  venueId: string;
  venueName: string;
  sportType: string;
  address: string;
  timeSlotId: string;
  startTime: string;
  endTime: string;
  capacity: number | null;
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return jsonResponse({ error: "Method not allowed" }, 405);
  }

  try {
    const user = await (async () => {
      try {
        return await requireUser(req);
      } catch {
        return null;
      }
    })();
    const body = await req.json();
    const prompt = String(body?.prompt ?? "").trim();

    if (!prompt) {
      return jsonResponse({ error: "prompt is required" }, 400);
    }

    const context = body?.context ?? null;
    const relatedIds = body?.relatedIds ?? null;
    const loweredPrompt = prompt.toLowerCase();
    const venueIntent =
      loweredPrompt.includes("venue") ||
      loweredPrompt.includes("field") ||
      loweredPrompt.includes("court") ||
      loweredPrompt.includes("available") ||
      loweredPrompt.includes("slot");

    const sportType = detectSportType(loweredPrompt);
    const preferredHour = detectPreferredHour(loweredPrompt);

    const availabilityRows: VenueAvailabilityRow[] = venueIntent
      ? await getVenueAvailability({
          sportType,
          preferredHour
        })
      : [];

    const systemPrompt = [
      "You are Dakti Assistant, a chat assistant for sports venues and practical sports guidance.",
      "Answer conversationally and clearly.",
      "If venue availability context is provided, use it as source-of-truth and mention concrete venue names/times.",
      "Do not propose automatic booking or workflow execution."
    ].join(" ");

    const compiledPrompt = [
      systemPrompt,
      `User Prompt: ${prompt}`,
      availabilityRows.length > 0
        ? `Venue Availability Context: ${JSON.stringify(availabilityRows)}`
        : "",
      context ? `App Context: ${JSON.stringify(context)}` : "",
      relatedIds ? `Related IDs: ${JSON.stringify(relatedIds)}` : ""
    ].filter(Boolean).join("\n\n");

    const geminiText = await generateGeminiText(compiledPrompt);
    const assistantText = geminiText ??
      buildFallbackText({
        venueIntent,
        availabilityRows
      });

    const suggestions = buildSuggestionCards({
      venueIntent,
      availabilityRows
    });

    if (user?.id) {
      const supabase = createServiceClient();
      const { data: requestRow } = await supabase
        .from("ai_requests")
        .insert({
          user_id: user.id,
          request_text: prompt,
          request_type: "assistant_chat"
        })
        .select("id")
        .single();

      if (requestRow?.id) {
        await supabase
          .from("ai_suggestions")
          .insert([
            {
              request_id: requestRow.id,
              suggestion_type: "assistant_text",
              suggestion_text: assistantText,
              payload: { suggestions, venueIntent }
            }
          ]);
      }
    }

    return jsonResponse({
      assistantText,
      suggestions,
      provider: geminiText ? "gemini" : "fallback"
    });
  } catch (error) {
    return jsonResponse({ error: (error as Error).message }, 500);
  }
});

function detectSportType(loweredPrompt: string): string | null {
  if (loweredPrompt.includes("football") || loweredPrompt.includes("soccer")) return "Football";
  if (loweredPrompt.includes("basketball")) return "Basketball";
  if (loweredPrompt.includes("tennis")) return "Tennis";
  if (loweredPrompt.includes("volleyball")) return "Volleyball";
  return null;
}

function detectPreferredHour(loweredPrompt: string): number | null {
  const amPm = loweredPrompt.match(/\b(\d{1,2})(?::(\d{2}))?\s*(am|pm)\b/);
  if (amPm) {
    let hour = Number(amPm[1]);
    const meridiem = amPm[3];
    if (meridiem === "pm" && hour < 12) hour += 12;
    if (meridiem === "am" && hour === 12) hour = 0;
    if (hour >= 0 && hour <= 23) return hour;
  }

  const twentyFour = loweredPrompt.match(/\b([01]?\d|2[0-3]):([0-5]\d)\b/);
  if (twentyFour) {
    return Number(twentyFour[1]);
  }

  if (loweredPrompt.includes("morning")) return 9;
  if (loweredPrompt.includes("afternoon")) return 15;
  if (loweredPrompt.includes("evening") || loweredPrompt.includes("night")) return 19;
  return null;
}

async function getVenueAvailability(input: {
  sportType: string | null;
  preferredHour: number | null;
}): Promise<VenueAvailabilityRow[]> {
  const supabase = createServiceClient();
  const nowIso = new Date().toISOString();

  let venuesQuery = supabase
    .from("venues")
    .select("id,name,sport_type,address,capacity");

  if (input.sportType) {
    venuesQuery = venuesQuery.ilike("sport_type", input.sportType);
  }

  const { data: venues, error: venuesError } = await venuesQuery.limit(20);
  if (venuesError || !venues || venues.length === 0) {
    return [];
  }

  const venueIds = venues.map((venue) => venue.id);
  const { data: slots, error: slotsError } = await supabase
    .from("time_slots")
    .select("id,venue_id,start_time,end_time,is_available")
    .in("venue_id", venueIds)
    .eq("is_available", true)
    .gte("start_time", nowIso)
    .order("start_time", { ascending: true })
    .limit(80);

  if (slotsError || !slots || slots.length === 0) {
    return [];
  }

  const mapped = slots.map((slot) => {
    const venue = venues.find((item) => item.id === slot.venue_id);
    if (!venue) return null;
    return {
      venueId: venue.id,
      venueName: venue.name,
      sportType: venue.sport_type,
      address: venue.address,
      timeSlotId: slot.id,
      startTime: slot.start_time,
      endTime: slot.end_time,
      capacity: venue.capacity ?? null
    } satisfies VenueAvailabilityRow;
  }).filter((item): item is VenueAvailabilityRow => item !== null);

  if (input.preferredHour == null) {
    return mapped.slice(0, 8);
  }

  return mapped
    .sort((a, b) => {
      const aHour = new Date(a.startTime).getHours();
      const bHour = new Date(b.startTime).getHours();
      const aDelta = Math.abs(aHour - input.preferredHour!);
      const bDelta = Math.abs(bHour - input.preferredHour!);
      return aDelta - bDelta;
    })
    .slice(0, 8);
}

function buildSuggestionCards(input: {
  venueIntent: boolean;
  availabilityRows: VenueAvailabilityRow[];
}): Array<{ id: string; title: string; type: string; description?: string }> {
  if (input.venueIntent && input.availabilityRows.length > 0) {
    return input.availabilityRows.slice(0, 3).map((row) => ({
      id: row.timeSlotId,
      title: `${row.venueName} - ${formatTimeRange(row.startTime, row.endTime)}`,
      type: "venue_recommendation",
      description: row.address
    }));
  }

  return [
    {
      id: crypto.randomUUID(),
      title: "Ask for available venues by sport/time",
      type: "venue_recommendation",
      description: "Example: available football venues tomorrow 6 PM"
    },
    {
      id: crypto.randomUUID(),
      title: "Ask practical sports preparation tips",
      type: "general",
      description: "Example: what to wear for an outdoor football game"
    }
  ];
}

function buildFallbackText(input: {
  venueIntent: boolean;
  availabilityRows: VenueAvailabilityRow[];
}): string {
  if (input.venueIntent) {
    if (input.availabilityRows.length == 0) {
      return "I could not find available venue slots right now. Try a different sport or a wider time window.";
    }
    const lines = input.availabilityRows.slice(0, 3).map((row, index) =>
      `${index + 1}. ${row.venueName} (${row.sportType}) - ${formatTimeRange(row.startTime, row.endTime)}`
    );
    return `I found available venue options:\n${lines.join("\n")}`;
  }
  return "I can help with sports questions like gear, warm-up, hydration, and match-day preparation. Ask me anything specific.";
}

function formatTimeRange(startIso: string, endIso: string): string {
  const start = new Date(startIso);
  const end = new Date(endIso);
  return `${start.toLocaleString()} - ${end.toLocaleTimeString()}`;
}
