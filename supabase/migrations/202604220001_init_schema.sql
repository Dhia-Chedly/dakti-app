-- 202604220001_init_supabase_schema.sql

create extension if not exists pgcrypto;

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text unique not null,
  full_name text not null,
  phone text,
  role text not null check (role in ('organizer', 'player')),
  avatar_url text,
  preferred_sport text,
  availability_note text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.venues (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  sport_type text not null,
  address text not null,
  latitude double precision,
  longitude double precision,
  contact_number text,
  description text,
  capacity int not null check (capacity > 0),
  created_at timestamptz not null default now()
);

create table if not exists public.time_slots (
  id uuid primary key default gen_random_uuid(),
  venue_id uuid not null references public.venues(id) on delete cascade,
  start_time timestamptz not null,
  end_time timestamptz not null,
  is_available boolean not null default true,
  created_at timestamptz not null default now(),
  check (end_time > start_time)
);

create table if not exists public.reservations (
  id uuid primary key default gen_random_uuid(),
  organizer_id uuid not null references public.profiles(id) on delete cascade,
  venue_id uuid not null references public.venues(id) on delete restrict,
  time_slot_id uuid not null references public.time_slots(id) on delete restrict,
  status text not null check (status in ('pending', 'confirmed', 'cancelled')),
  notes text,
  created_at timestamptz not null default now()
);

create table if not exists public.matches (
  id uuid primary key default gen_random_uuid(),
  organizer_id uuid not null references public.profiles(id) on delete cascade,
  venue_id uuid not null references public.venues(id) on delete restrict,
  reservation_id uuid references public.reservations(id) on delete set null,
  sport_type text not null,
  match_time timestamptz not null,
  required_players int not null check (required_players > 1),
  status text not null check (status in ('organizing', 'confirmed', 'cancelled', 'completed')),
  description text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.invitations (
  id uuid primary key default gen_random_uuid(),
  match_id uuid not null references public.matches(id) on delete cascade,
  player_id uuid not null references public.profiles(id) on delete cascade,
  sender_id uuid not null references public.profiles(id) on delete cascade,
  message_text text,
  response_status text not null default 'pending' check (response_status in ('pending', 'accepted', 'declined')),
  sent_at timestamptz not null default now(),
  responded_at timestamptz,
  unique (match_id, player_id)
);

create table if not exists public.notifications (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  type text not null,
  title text not null,
  body text not null,
  related_match_id uuid,
  related_reservation_id uuid,
  is_read boolean not null default false,
  created_at timestamptz not null default now()
);

create table if not exists public.ai_requests (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  request_text text not null,
  request_type text,
  created_at timestamptz not null default now()
);

create table if not exists public.ai_suggestions (
  id uuid primary key default gen_random_uuid(),
  request_id uuid not null references public.ai_requests(id) on delete cascade,
  suggestion_type text not null,
  suggestion_text text not null,
  payload jsonb,
  created_at timestamptz not null default now()
);

create index if not exists idx_profiles_role on public.profiles(role);
create index if not exists idx_venues_sport_type on public.venues(sport_type);
create index if not exists idx_time_slots_venue on public.time_slots(venue_id);
create index if not exists idx_time_slots_start_time on public.time_slots(start_time);
create index if not exists idx_reservations_organizer on public.reservations(organizer_id);
create index if not exists idx_reservations_slot on public.reservations(time_slot_id);
create index if not exists idx_matches_organizer on public.matches(organizer_id);
create index if not exists idx_matches_match_time on public.matches(match_time);
create index if not exists idx_invitations_player on public.invitations(player_id);
create index if not exists idx_invitations_match on public.invitations(match_id);
create index if not exists idx_notifications_user on public.notifications(user_id);
create index if not exists idx_ai_requests_user on public.ai_requests(user_id);
create index if not exists idx_ai_suggestions_request on public.ai_suggestions(request_id);

create trigger set_profiles_updated_at
before update on public.profiles
for each row execute function public.set_updated_at();

create trigger set_matches_updated_at
before update on public.matches
for each row execute function public.set_updated_at();

alter table public.profiles enable row level security;
alter table public.venues enable row level security;
alter table public.time_slots enable row level security;
alter table public.reservations enable row level security;
alter table public.matches enable row level security;
alter table public.invitations enable row level security;
alter table public.notifications enable row level security;
alter table public.ai_requests enable row level security;
alter table public.ai_suggestions enable row level security;

-- profiles
create policy "profiles_select_own"
on public.profiles
for select
using (auth.uid() = id);

create policy "profiles_update_own"
on public.profiles
for update
using (auth.uid() = id)
with check (auth.uid() = id);

create policy "profiles_insert_own"
on public.profiles
for insert
with check (auth.uid() = id);

-- venues & slots
create policy "venues_read_authenticated"
on public.venues
for select
using (auth.role() = 'authenticated');

create policy "time_slots_read_authenticated"
on public.time_slots
for select
using (auth.role() = 'authenticated');

-- reservations
create policy "reservations_select_own"
on public.reservations
for select
using (organizer_id = auth.uid());

create policy "reservations_insert_own"
on public.reservations
for insert
with check (organizer_id = auth.uid());

create policy "reservations_update_own"
on public.reservations
for update
using (organizer_id = auth.uid())
with check (organizer_id = auth.uid());

-- matches
create policy "matches_select_own"
on public.matches
for select
using (organizer_id = auth.uid());

create policy "matches_insert_own"
on public.matches
for insert
with check (organizer_id = auth.uid());

create policy "matches_update_own"
on public.matches
for update
using (organizer_id = auth.uid())
with check (organizer_id = auth.uid());

-- invitations
create policy "invitations_player_select_own"
on public.invitations
for select
using (player_id = auth.uid());

create policy "invitations_player_update_own"
on public.invitations
for update
using (player_id = auth.uid())
with check (player_id = auth.uid());

create policy "invitations_organizer_select_for_owned_match"
on public.invitations
for select
using (
  exists (
    select 1
    from public.matches m
    where m.id = invitations.match_id
      and m.organizer_id = auth.uid()
  )
);

create policy "invitations_organizer_insert_for_owned_match"
on public.invitations
for insert
with check (
  sender_id = auth.uid()
  and exists (
    select 1
    from public.matches m
    where m.id = invitations.match_id
      and m.organizer_id = auth.uid()
  )
);

-- notifications
create policy "notifications_select_own"
on public.notifications
for select
using (user_id = auth.uid());

create policy "notifications_update_own"
on public.notifications
for update
using (user_id = auth.uid())
with check (user_id = auth.uid());

-- ai history
create policy "ai_requests_select_own"
on public.ai_requests
for select
using (user_id = auth.uid());

create policy "ai_requests_insert_own"
on public.ai_requests
for insert
with check (user_id = auth.uid());

create policy "ai_suggestions_select_owned_requests"
on public.ai_suggestions
for select
using (
  exists (
    select 1
    from public.ai_requests r
    where r.id = ai_suggestions.request_id
      and r.user_id = auth.uid()
  )
);

create policy "ai_suggestions_insert_owned_requests"
on public.ai_suggestions
for insert
with check (
  exists (
    select 1
    from public.ai_requests r
    where r.id = ai_suggestions.request_id
      and r.user_id = auth.uid()
  )
);