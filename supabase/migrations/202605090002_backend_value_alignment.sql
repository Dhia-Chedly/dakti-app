alter table if exists public.venues
  add column if not exists city text,
  add column if not exists state text,
  add column if not exists country text,
  add column if not exists price_per_hour numeric(10,2),
  add column if not exists currency text,
  add column if not exists amenities jsonb,
  add column if not exists updated_at timestamptz not null default now();

alter table if exists public.reservations
  add column if not exists total_price numeric(10,2),
  add column if not exists currency text,
  add column if not exists updated_at timestamptz not null default now();

alter table if exists public.matches
  add column if not exists title text;

comment on column public.venues.city is
  'City for venue display and filtering.';
comment on column public.venues.state is
  'Administrative region/state for venue display.';
comment on column public.venues.country is
  'Country for venue display.';
comment on column public.venues.price_per_hour is
  'Rental price per hour in the venue currency.';
comment on column public.venues.currency is
  'ISO-like currency code for venue pricing.';
comment on column public.venues.amenities is
  'JSON array of venue amenities.';
comment on column public.reservations.total_price is
  'Total reservation price captured at booking time.';
comment on column public.reservations.currency is
  'Currency for reservation total price.';
comment on column public.matches.title is
  'Display title for the match.';

update public.venues
set
  city = coalesce(nullif(trim(split_part(address, ',', 2)), ''), nullif(trim(split_part(address, ',', 1)), '')),
  state = nullif(trim(split_part(address, ',', 3)), ''),
  country = coalesce(nullif(country, ''), 'Tunisia'),
  currency = coalesce(nullif(currency, ''), 'TND'),
  amenities = case
    when amenities is not null then amenities
    else jsonb_build_array('Capacity: ' || capacity::text)
  end,
  price_per_hour = coalesce(
    price_per_hour,
    case sport_type
      when 'Football' then 220.0
      when 'Basketball' then 140.0
      when 'Tennis' then 90.0
      when 'Volleyball' then 120.0
      when 'Handball' then 130.0
      when 'Padel' then 100.0
      else 100.0
    end
  ),
  updated_at = now();

update public.matches
set
  title = coalesce(nullif(title, ''), sport_type || ' Match'),
  updated_at = now()
where title is null or title = '';

with slot_durations as (
  select
    r.id as reservation_id,
    r.venue_id,
    extract(epoch from (ts.end_time - ts.start_time)) / 3600.0 as duration_hours
  from public.reservations r
  join public.time_slots ts on ts.id = r.time_slot_id
)
update public.reservations r
set
  currency = coalesce(nullif(r.currency, ''), v.currency, 'TND'),
  total_price = coalesce(
    r.total_price,
    round((coalesce(v.price_per_hour, 0) * coalesce(sd.duration_hours, 1))::numeric, 2)
  ),
  updated_at = now()
from slot_durations sd
join public.venues v on v.id = sd.venue_id
where r.id = sd.reservation_id;

drop trigger if exists set_venues_updated_at on public.venues;
create trigger set_venues_updated_at
before update on public.venues
for each row execute function public.set_updated_at();

drop trigger if exists set_reservations_updated_at on public.reservations;
create trigger set_reservations_updated_at
before update on public.reservations
for each row execute function public.set_updated_at();
