-- Large Tunisia demo seed for Dakti
-- Demo password for all seeded users: Password123!

create extension if not exists pgcrypto;

-- Deterministic UUID helper so the seed is idempotent across runs.
create or replace function public.seed_uuid(seed_text text)
returns uuid
language sql
immutable
as $$
  select (
    substr(md5(seed_text), 1, 8) || '-' ||
    substr(md5(seed_text), 9, 4) || '-4' ||
    substr(md5(seed_text), 14, 3) || '-a' ||
    substr(md5(seed_text), 18, 3) || '-' ||
    substr(md5(seed_text), 21, 12)
  )::uuid;
$$;

create temporary table seed_profiles_tmp as
with core_profiles as (
  select *
  from (
    values
      (
        '11111111-1111-1111-1111-111111111111'::uuid,
        'youssef@dakti.tn',
        'Youssef Ben Salah',
        '+21620111222',
        'organizer',
        'Football',
        'Available evenings and weekends'
      ),
      (
        '22222222-2222-2222-2222-222222222221'::uuid,
        'amina@dakti.tn',
        'Amina Trabelsi',
        '+21650123456',
        'player',
        'Football',
        'Weekdays after 18:00'
      ),
      (
        '22222222-2222-2222-2222-222222222222'::uuid,
        'karim@dakti.tn',
        'Karim Jlassi',
        '+21655123456',
        'player',
        'Football',
        'Weekends and Friday nights'
      ),
      (
        '22222222-2222-2222-2222-222222222223'::uuid,
        'sarra@dakti.tn',
        'Sarra Ben Amor',
        '+21698111222',
        'player',
        'Basketball',
        'Weekday evenings'
      )
  ) as v(id, email, full_name, phone, role, preferred_sport, availability_note)
),
name_pools as (
  select
    array[
      'Yassine', 'Amina', 'Karim', 'Sarra', 'Nour', 'Walid', 'Imen', 'Mehdi',
      'Rania', 'Skander', 'Lina', 'Hedi', 'Ines', 'Anis', 'Mouna', 'Khaled',
      'Meriem', 'Hatem', 'Rim', 'Tarek', 'Safa', 'Oussama', 'Aya', 'Firas'
    ]::text[] as first_names,
    array[
      'BenSalah', 'Trabelsi', 'Jlassi', 'BenAmor', 'Mansouri', 'Gharbi',
      'Bouzid', 'Kefi', 'Mrad', 'Zaidi', 'Brahmi', 'Chaari', 'Aouadi',
      'Haddad', 'Mahmoudi', 'Toumi', 'BenAli', 'Khalfallah', 'Mnif',
      'Baccar', 'Hamdi', 'Khemiri', 'Cherif', 'Jedidi'
    ]::text[] as last_names,
    array[
      'Football', 'Basketball', 'Tennis', 'Volleyball', 'Handball', 'Padel'
    ]::text[] as sports
),
generated_organizers as (
  select
    public.seed_uuid('organizer-' || g::text) as id,
    lower('organizer.' || g::text || '@dakti.tn') as email,
    (np.first_names[((g - 1) % array_length(np.first_names, 1)) + 1] || ' ' ||
      np.last_names[((g + 5) % array_length(np.last_names, 1)) + 1]) as full_name,
    '+216' || lpad((51000000 + g)::text, 8, '0') as phone,
    'organizer'::text as role,
    np.sports[((g - 1) % array_length(np.sports, 1)) + 1] as preferred_sport,
    case
      when g % 3 = 0 then 'Morning and evening slots'
      when g % 3 = 1 then 'Evening slots only'
      else 'Weekend availability'
    end as availability_note
  from generate_series(1, 16) g
  cross join name_pools np
),
generated_players as (
  select
    public.seed_uuid('player-' || g::text) as id,
    lower('player.' || g::text || '@dakti.tn') as email,
    (np.first_names[((g + 3) % array_length(np.first_names, 1)) + 1] || ' ' ||
      np.last_names[((g + 9) % array_length(np.last_names, 1)) + 1]) as full_name,
    '+216' || lpad((52000000 + g)::text, 8, '0') as phone,
    'player'::text as role,
    np.sports[((g + 1) % array_length(np.sports, 1)) + 1] as preferred_sport,
    case
      when g % 4 = 0 then 'Available every evening'
      when g % 4 = 1 then 'Available after 19:00 on weekdays'
      when g % 4 = 2 then 'Weekend friendly'
      else 'Flexible schedule'
    end as availability_note
  from generate_series(1, 180) g
  cross join name_pools np
)
select * from core_profiles
union all
select * from generated_organizers
union all
select * from generated_players;

insert into auth.users (
  id,
  instance_id,
  aud,
  role,
  email,
  encrypted_password,
  email_confirmed_at,
  raw_app_meta_data,
  raw_user_meta_data,
  created_at,
  updated_at
)
select
  p.id,
  '00000000-0000-0000-0000-000000000000'::uuid,
  'authenticated',
  'authenticated',
  p.email,
  crypt('Password123!', gen_salt('bf')),
  now(),
  '{"provider":"email","providers":["email"]}'::jsonb,
  jsonb_build_object('full_name', p.full_name, 'phone', p.phone),
  now(),
  now()
from seed_profiles_tmp p
on conflict (id) do update
set
  email = excluded.email,
  encrypted_password = excluded.encrypted_password,
  raw_app_meta_data = excluded.raw_app_meta_data,
  raw_user_meta_data = excluded.raw_user_meta_data,
  updated_at = now();

insert into public.profiles (
  id,
  email,
  full_name,
  phone,
  role,
  avatar_url,
  preferred_sport,
  availability_note
)
select
  p.id,
  p.email,
  p.full_name,
  p.phone,
  p.role,
  null,
  p.preferred_sport,
  p.availability_note
from seed_profiles_tmp p
on conflict (id) do update
set
  email = excluded.email,
  full_name = excluded.full_name,
  phone = excluded.phone,
  role = excluded.role,
  avatar_url = excluded.avatar_url,
  preferred_sport = excluded.preferred_sport,
  availability_note = excluded.availability_note,
  updated_at = now();

do $$
begin
  if to_regclass('storage.buckets') is not null then
    if exists (
      select 1
      from information_schema.columns
      where table_schema = 'storage'
        and table_name = 'buckets'
        and column_name = 'public'
    ) then
      insert into storage.buckets (id, name, public)
      values ('venue-images', 'venue-images', true)
      on conflict (id) do update
      set
        name = excluded.name,
        public = excluded.public;
    else
      insert into storage.buckets (id, name)
      values ('venue-images', 'venue-images')
      on conflict (id) do update
      set
        name = excluded.name;
    end if;
  end if;
end
$$;

create temporary table seed_venues_tmp as
with curated_venues as (
  select *
  from (
    values
      (
        'Stade Olympique de Rades',
        'Football',
        'Cite Olympique, Rades, Tunisia',
        36.7469::double precision,
        10.2716::double precision,
        '+21671000001',
        'National football venue in Rades.',
        60000,
        1,
        1,
        'https://gearjfbjjniaalcygmux.supabase.co/storage/v1/object/public/venue-images/football/stade-olympique-de-rades.jpg'
      ),
      (
        'Stade Olympique de Sousse',
        'Football',
        'Avenue du Stade Olympique, Sousse, Tunisia',
        35.8286::double precision,
        10.6292::double precision,
        '+21673000002',
        'Major football stadium in Sousse.',
        28000,
        2,
        1,
        'https://gearjfbjjniaalcygmux.supabase.co/storage/v1/object/public/venue-images/football/stade-olympique-de-sousse.jpg'
      ),
      (
        'Salle Omnisports de Rades',
        'Basketball',
        'Complexe Sportif de Rades, Rades, Tunisia',
        36.7419::double precision,
        10.2800::double precision,
        '+21671000003',
        'Indoor arena regularly used for basketball events.',
        14000,
        3,
        2,
        'https://gearjfbjjniaalcygmux.supabase.co/storage/v1/object/public/venue-images/basketball/salle-omnisports-de-rades.jpg'
      ),
      (
        'Salle Cherif-Bellamine',
        'Basketball',
        'El Gorjani, Tunis, Tunisia',
        36.7949::double precision,
        10.1707::double precision,
        '+21671000004',
        'Historic indoor hall in Tunis.',
        2500,
        4,
        2,
        'https://gearjfbjjniaalcygmux.supabase.co/storage/v1/object/public/venue-images/basketball/salle-cherif-bellamine.jpg'
      ),
      (
        'Tennis Club de Tunis',
        'Tennis',
        '20 Avenue Alain Savary, Tunis, Tunisia',
        36.8092::double precision,
        10.1867::double precision,
        '+21671000005',
        'Well-known tennis club in Tunis.',
        500,
        5,
        3,
        'https://gearjfbjjniaalcygmux.supabase.co/storage/v1/object/public/venue-images/tennis/tennis-club-de-tunis.jpg'
      ),
      (
        'Tennis Club de Bizerte',
        'Tennis',
        'Les Jardins de la Municipalite, Bizerte, Tunisia',
        37.2746::double precision,
        9.8711::double precision,
        '+21671000006',
        'Local tennis club in Bizerte.',
        350,
        6,
        3,
        'https://gearjfbjjniaalcygmux.supabase.co/storage/v1/object/public/venue-images/tennis/tennis-club-de-bizerte.jpg'
      ),
      (
        'Salle Cherif-Bellamine',
        'Volleyball',
        'El Gorjani, Tunis, Tunisia',
        36.7949::double precision,
        10.1707::double precision,
        '+21671000007',
        'Indoor venue hosting volleyball matches.',
        2500,
        7,
        4,
        'https://gearjfbjjniaalcygmux.supabase.co/storage/v1/object/public/venue-images/volleyball/salle-cherif-bellamine.jpg'
      ),
      (
        'Salle Mohamed-Zouaoui',
        'Volleyball',
        'Parc B, Tunis, Tunisia',
        36.8050::double precision,
        10.1726::double precision,
        '+21671000008',
        'Home hall for top volleyball fixtures in Tunis.',
        1800,
        8,
        4,
        'https://gearjfbjjniaalcygmux.supabase.co/storage/v1/object/public/venue-images/volleyball/salle-mohamed-zouaoui.jpg'
      ),
      (
        'Salle Omnisports de Rades',
        'Handball',
        'Complexe Sportif de Rades, Rades, Tunisia',
        36.7419::double precision,
        10.2800::double precision,
        '+21671000009',
        'Primary handball championship venue.',
        14000,
        9,
        5,
        'https://gearjfbjjniaalcygmux.supabase.co/storage/v1/object/public/venue-images/handball/salle-omnisports-de-rades.jpg'
      ),
      (
        'Salle Mohamed-Zouaoui',
        'Handball',
        'Parc B, Tunis, Tunisia',
        36.8050::double precision,
        10.1726::double precision,
        '+21671000010',
        'Indoor handball venue in central Tunis.',
        1800,
        10,
        5,
        'https://gearjfbjjniaalcygmux.supabase.co/storage/v1/object/public/venue-images/handball/salle-mohamed-zouaoui.jpg'
      ),
      (
        'Padel Country Club',
        'Padel',
        '300 Rue du Lac Leman, Tunis, Tunisia',
        36.8422::double precision,
        10.2840::double precision,
        '+21671000011',
        'Popular padel destination in Tunis.',
        120,
        11,
        6,
        'https://gearjfbjjniaalcygmux.supabase.co/storage/v1/object/public/venue-images/padel/padel-country-club.jpg'
      ),
      (
        'Eleven Padel Club',
        'Padel',
        'Km 11 Route de Tunis, Sakiet Ezzit, Sfax, Tunisia',
        34.7707::double precision,
        10.7052::double precision,
        '+21671000012',
        'Padel club serving the Sfax area.',
        120,
        12,
        6,
        'https://gearjfbjjniaalcygmux.supabase.co/storage/v1/object/public/venue-images/padel/eleven-padel-club.jpg'
      )
  ) as v(
    name,
    sport_type,
    address,
    latitude,
    longitude,
    contact_number,
    description,
    capacity,
    city_index,
    sport_index,
    image_url
  )
)
select
  public.seed_uuid(
    'venue-' || lower(replace(regexp_replace(name, '[^a-zA-Z0-9]+', '-', 'g'), '--', '-'))
      || '-' || lower(sport_type)
  ) as id,
  name,
  sport_type,
  address,
  latitude,
  longitude,
  contact_number,
  description,
  capacity,
  city_index,
  sport_index,
  image_url
from curated_venues;

insert into public.venues (
  id,
  name,
  sport_type,
  address,
  city,
  state,
  country,
  latitude,
  longitude,
  contact_number,
  description,
  capacity,
  image_url,
  price_per_hour,
  currency,
  amenities,
  updated_at
)
select
  v.id,
  v.name,
  v.sport_type,
  v.address,
  nullif(trim(split_part(v.address, ',', 2)), '') as city,
  null::text as state,
  'Tunisia' as country,
  v.latitude,
  v.longitude,
  v.contact_number,
  v.description,
  v.capacity,
  v.image_url,
  case v.sport_type
    when 'Football' then 220.0
    when 'Basketball' then 140.0
    when 'Tennis' then 90.0
    when 'Volleyball' then 120.0
    when 'Handball' then 130.0
    when 'Padel' then 100.0
    else 100.0
  end as price_per_hour,
  'TND' as currency,
  jsonb_build_array('Capacity: ' || v.capacity::text) as amenities,
  now() as updated_at
from seed_venues_tmp v
on conflict (id) do update
set
  name = excluded.name,
  sport_type = excluded.sport_type,
  address = excluded.address,
  city = excluded.city,
  state = excluded.state,
  country = excluded.country,
  latitude = excluded.latitude,
  longitude = excluded.longitude,
  contact_number = excluded.contact_number,
  description = excluded.description,
  capacity = excluded.capacity,
  image_url = excluded.image_url,
  price_per_hour = excluded.price_per_hour,
  currency = excluded.currency,
  amenities = excluded.amenities,
  updated_at = now();

insert into public.time_slots (
  id,
  venue_id,
  start_time,
  end_time,
  is_available
)
select
  public.seed_uuid(
    'slot-' || v.id::text || '-' || d.day_index::text || '-' || t.slot_index::text
  ) as id,
  v.id as venue_id,
  date_trunc('day', now())
    + (d.day_index * interval '1 day')
    + (t.start_hour * interval '1 hour') as start_time,
  date_trunc('day', now())
    + (d.day_index * interval '1 day')
    + (t.end_hour * interval '1 hour') as end_time,
  ((d.day_index + t.slot_index + v.city_index + v.sport_index) % 5) <> 0 as is_available
from seed_venues_tmp v
cross join (
  values
    (1, 8, 10),
    (2, 17, 19),
    (3, 20, 22)
) as t(slot_index, start_hour, end_hour)
cross join generate_series(1, 21) as d(day_index)
on conflict (id) do update
set
  venue_id = excluded.venue_id,
  start_time = excluded.start_time,
  end_time = excluded.end_time,
  is_available = excluded.is_available;
