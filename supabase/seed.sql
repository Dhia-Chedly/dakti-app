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

create temporary table seed_venues_tmp as
with city_seed as (
  select *
  from (
    values
      (1, 'Tunis', 'Tunis', 36.8065::double precision, 10.1815::double precision),
      (2, 'Ariana', 'Ariana', 36.8663::double precision, 10.1647::double precision),
      (3, 'Ben Arous', 'Ben Arous', 36.7545::double precision, 10.2223::double precision),
      (4, 'Manouba', 'Manouba', 36.8101::double precision, 10.0963::double precision),
      (5, 'Nabeul', 'Nabeul', 36.4513::double precision, 10.7357::double precision),
      (6, 'Sousse', 'Sousse', 35.8256::double precision, 10.6369::double precision),
      (7, 'Monastir', 'Monastir', 35.7770::double precision, 10.8262::double precision),
      (8, 'Mahdia', 'Mahdia', 35.5047::double precision, 11.0622::double precision),
      (9, 'Sfax', 'Sfax', 34.7398::double precision, 10.7600::double precision),
      (10, 'Gabes', 'Gabes', 33.8815::double precision, 10.0982::double precision),
      (11, 'Medenine', 'Medenine', 33.3549::double precision, 10.5055::double precision),
      (12, 'Djerba', 'Medenine', 33.8076::double precision, 10.8451::double precision),
      (13, 'Bizerte', 'Bizerte', 37.2744::double precision, 9.8739::double precision),
      (14, 'Kairouan', 'Kairouan', 35.6781::double precision, 10.0963::double precision),
      (15, 'Gafsa', 'Gafsa', 34.4250::double precision, 8.7842::double precision),
      (16, 'Tozeur', 'Tozeur', 33.9197::double precision, 8.1335::double precision)
  ) as c(city_index, city_name, governorate, latitude, longitude)
),
sport_seed as (
  select *
  from (
    values
      (1, 'Football', 140),
      (2, 'Basketball', 90),
      (3, 'Tennis', 40),
      (4, 'Volleyball', 80),
      (5, 'Handball', 100),
      (6, 'Padel', 28)
  ) as s(sport_index, sport_type, base_capacity)
)
select
  public.seed_uuid(
    'venue-' || lower(replace(c.city_name, ' ', '-')) || '-' || lower(s.sport_type)
  ) as id,
  case s.sport_type
    when 'Football' then 'Football Stadium ' || c.city_name
    when 'Basketball' then 'Basket Hall ' || c.city_name
    when 'Tennis' then 'Tennis Club ' || c.city_name
    when 'Volleyball' then 'Volleyball Court ' || c.city_name
    when 'Handball' then 'Handball Arena ' || c.city_name
    else 'Padel Hub ' || c.city_name
  end as name,
  s.sport_type,
  'Central Sports District, ' || c.city_name || ', Tunisia' as address,
  c.latitude + ((s.sport_index - 3)::double precision * 0.0030) as latitude,
  c.longitude + ((c.city_index % 5 - 2)::double precision * 0.0030) as longitude,
  '+216' || lpad((70000000 + (c.city_index * 100) + s.sport_index)::text, 8, '0') as contact_number,
  s.sport_type || ' venue in ' || c.city_name || ' for training and match organization.' as description,
  s.base_capacity + ((c.city_index % 4) * 12) as capacity,
  c.city_index,
  s.sport_index
from city_seed c
cross join sport_seed s;

insert into public.venues (
  id,
  name,
  sport_type,
  address,
  latitude,
  longitude,
  contact_number,
  description,
  capacity
)
select
  v.id,
  v.name,
  v.sport_type,
  v.address,
  v.latitude,
  v.longitude,
  v.contact_number,
  v.description,
  v.capacity
from seed_venues_tmp v
on conflict (id) do update
set
  name = excluded.name,
  sport_type = excluded.sport_type,
  address = excluded.address,
  latitude = excluded.latitude,
  longitude = excluded.longitude,
  contact_number = excluded.contact_number,
  description = excluded.description,
  capacity = excluded.capacity;

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
