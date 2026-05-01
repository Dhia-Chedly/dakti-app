import { corsHeaders, jsonResponse } from "../_shared/cors.ts";
import { createServiceClient, requireUser } from "../_shared/client.ts";
import { generateGeminiText } from "../_shared/gemini.ts";

interface OrganizeMatchRequest {
  sport: string;
  preferredDateTime?: string;
  desiredPlayerCount?: number;
  venuePreference?: string;
  confirmAction?: boolean;
  selectedVenueId?: string;
  selectedSlotId?: string;
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return jsonResponse({ error: "Method not allowed" }, 405);
  }

  try {
    const user = await requireUser(req);
    const body = (await req.json()) as OrganizeMatchRequest;

    const sport = body.sport?.trim();
    if (!sport) {
      return jsonResponse({ error: "sport is required" }, 400);
    }

    const preferredDateTime = body.preferredDateTime ? new Date(body.preferredDateTime) : null;
    const desiredPlayerCount = body.desiredPlayerCount ?? 10;

    const supabase = createServiceClient();

    let venuesQuery = supabase
      .from("venues")
      .select("id,name,sport_type,address,capacity")
      .ilike("sport_type", sport);

    if (body.venuePreference?.trim()) {
      venuesQuery = venuesQuery.ilike("name", `%${body.venuePreference.trim()}%`);
    }

    const { data: venues, error: venuesError } = await venuesQuery.limit(10);
    if (venuesError) {
      throw venuesError;
    }

    const venueIds = (venues ?? []).map((v) => v.id);
    const nowIso = new Date().toISOString();

    const { data: slots, error: slotsError } = await supabase
      .from("time_slots")
      .select("id,venue_id,start_time,end_time,is_available")
      .in("venue_id", venueIds.length > 0 ? venueIds : ["00000000-0000-0000-0000-000000000000"])
      .eq("is_available", true)
      .gte("start_time", nowIso)
      .order("start_time", { ascending: true });

    if (slotsError) {
      throw slotsError;
    }

    const suggestions = (slots ?? []).slice(0, 8).map((slot) => {
      const venue = (venues ?? []).find((v) => v.id === slot.venue_id);
      const slotDate = new Date(slot.start_time);
      const preferredGapMinutes = preferredDateTime
        ? Math.abs((slotDate.getTime() - preferredDateTime.getTime()) / 60000)
        : null;

      return {
        venueId: slot.venue_id,
        venueName: venue?.name ?? "Unknown venue",
        venueAddress: venue?.address ?? "",
        timeSlotId: slot.id,
        startTime: slot.start_time,
        endTime: slot.end_time,
        capacity: venue?.capacity ?? null,
        recommendedReason: preferredGapMinutes != null && preferredGapMinutes <= 120
          ? "Close to preferred kickoff"
          : "Available option",
        isCapacityFit: venue?.capacity ? venue.capacity >= desiredPlayerCount : true
      };
    });

    const aiExplanation = await generateGeminiText(
      `Sport: ${sport}\nPreferred: ${body.preferredDateTime ?? "not set"}\nDesired players: ${desiredPlayerCount}\n` +
      `Rank these options and explain briefly: ${JSON.stringify(suggestions.slice(0, 3))}`
    );

    if (body.confirmAction && body.selectedVenueId && body.selectedSlotId) {
      const selectedSlot = (slots ?? []).find((s) => s.id === body.selectedSlotId && s.venue_id === body.selectedVenueId);
      if (!selectedSlot) {
        return jsonResponse({ error: "Selected slot is unavailable" }, 409);
      }

      const { data: reservation, error: reservationError } = await supabase
        .from("reservations")
        .insert({
          organizer_id: user.id,
          venue_id: body.selectedVenueId,
          time_slot_id: body.selectedSlotId,
          status: "confirmed"
        })
        .select("id")
        .single();

      if (reservationError) {
        throw reservationError;
      }

      await supabase
        .from("time_slots")
        .update({ is_available: false })
        .eq("id", body.selectedSlotId);

      const { data: match, error: matchError } = await supabase
        .from("matches")
        .insert({
          organizer_id: user.id,
          venue_id: body.selectedVenueId,
          reservation_id: reservation.id,
          sport_type: sport,
          match_time: selectedSlot.start_time,
          required_players: desiredPlayerCount,
          status: "organizing",
          description: "Created from organize-match function"
        })
        .select("id")
        .single();

      if (matchError) {
        throw matchError;
      }

      return jsonResponse({
        mode: "executed",
        reservationId: reservation.id,
        matchId: match.id,
        message: "Reservation and match created from confirmed assistant proposal."
      });
    }

    return jsonResponse({
      mode: "proposal",
      sport,
      desiredPlayerCount,
      suggestions,
      explanation: aiExplanation ?? "Review options and confirm one to create reservation/match.",
      requiresConfirmation: true
    });
  } catch (error) {
    return jsonResponse({ error: (error as Error).message }, 500);
  }
});