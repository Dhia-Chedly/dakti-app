-- Tunisian demo seed for Dakti
-- Demo password for all seeded users: Password123!

create extension if not exists pgcrypto;

-- Optional reset of transactional demo data (uncomment if you want a clean state)
-- delete from public.invitations;
-- delete from public.matches;
-- delete from public.reservations;

-- 1) Auth users (upsert by fixed UUIDs)
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
values
  (
    '11111111-1111-1111-1111-111111111111',
    '00000000-0000-0000-0000-000000000000',
    'authenticated',
    'authenticated',
    'youssef@dakti.tn',
    crypt('Password123!', gen_salt('bf')),
    now(),
    '{"provider":"email","providers":["email"]}',
    '{"full_name":"Youssef Ben Salah","phone":"+21620111222"}',
    now(),
    now()
  ),
  (
    '22222222-2222-2222-2222-222222222221',
    '00000000-0000-0000-0000-000000000000',
    'authenticated',
    'authenticated',
    'amina@dakti.tn',
    crypt('Password123!', gen_salt('bf')),
    now(),
    '{"provider":"email","providers":["email"]}',
    '{"full_name":"Amina Trabelsi","phone":"+21650123456"}',
    now(),
    now()
  ),
  (
    '22222222-2222-2222-2222-222222222222',
    '00000000-0000-0000-0000-000000000000',
    'authenticated',
    'authenticated',
    'karim@dakti.tn',
    crypt('Password123!', gen_salt('bf')),
    now(),
    '{"provider":"email","providers":["email"]}',
    '{"full_name":"Karim Jlassi","phone":"+21655123456"}',
    now(),
    now()
  ),
  (
    '22222222-2222-2222-2222-222222222223',
    '00000000-0000-0000-0000-000000000000',
    'authenticated',
    'authenticated',
    'sarra@dakti.tn',
    crypt('Password123!', gen_salt('bf')),
    now(),
    '{"provider":"email","providers":["email"]}',
    '{"full_name":"Sarra Ben Amor","phone":"+21698111222"}',
    now(),
    now()
  )
on conflict (id) do update
set
  email = excluded.email,
  encrypted_password = excluded.encrypted_password,
  raw_app_meta_data = excluded.raw_app_meta_data,
  raw_user_meta_data = excluded.raw_user_meta_data,
  updated_at = now();

-- 2) Profiles
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
values
  (
    '11111111-1111-1111-1111-111111111111',
    'youssef@dakti.tn',
    'Youssef Ben Salah',
    '+21620111222',
    'organizer',
    null,
    'Football',
    'Disponible en soirée et week-ends'
  ),
  (
    '22222222-2222-2222-2222-222222222221',
    'amina@dakti.tn',
    'Amina Trabelsi',
    '+21650123456',
    'player',
    null,
    'Football',
    'Après 18h en semaine'
  ),
  (
    '22222222-2222-2222-2222-222222222222',
    'karim@dakti.tn',
    'Karim Jlassi',
    '+21655123456',
    'player',
    null,
    'Football',
    'Disponible week-end'
  ),
  (
    '22222222-2222-2222-2222-222222222223',
    'sarra@dakti.tn',
    'Sarra Ben Amor',
    '+21698111222',
    'player',
    null,
    'Basketball',
    'Soirées en semaine'
  )
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

-- 3) Venues (Tunisia)
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
values
  (
    '33333333-3333-3333-3333-333333333331',
    'Stade Olympique de Radès',
    'Football',
    'Radès, Ben Arous, Tunisie',
    36.7700,
    10.2810,
    '+21671300111',
    'Grand stade moderne adapté aux grands matchs.',
    200
  ),
  (
    '33333333-3333-3333-3333-333333333332',
    'Stade El Menzah',
    'Football',
    'El Menzah, Tunis, Tunisie',
    36.8362,
    10.1651,
    '+21671300222',
    'Stade historique au coeur de Tunis.',
    180
  ),
  (
    '33333333-3333-3333-3333-333333333333',
    'Stade Taïeb Mhiri',
    'Football',
    'Route de l’Aéroport, Sfax, Tunisie',
    34.7397,
    10.7597,
    '+21674300333',
    'Stade emblématique de la ville de Sfax.',
    160
  )
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

-- 4) Time slots (mixed availability)
insert into public.time_slots (
  id,
  venue_id,
  start_time,
  end_time,
  is_available
)
values
  (
    '44444444-4444-4444-4444-444444444441',
    '33333333-3333-3333-3333-333333333331',
    date_trunc('hour', now()) + interval '1 day' + interval '18 hour',
    date_trunc('hour', now()) + interval '1 day' + interval '20 hour',
    true
  ),
  (
    '44444444-4444-4444-4444-444444444442',
    '33333333-3333-3333-3333-333333333331',
    date_trunc('hour', now()) + interval '2 day' + interval '17 hour',
    date_trunc('hour', now()) + interval '2 day' + interval '19 hour',
    false
  ),
  (
    '44444444-4444-4444-4444-444444444443',
    '33333333-3333-3333-3333-333333333331',
    date_trunc('hour', now()) + interval '3 day' + interval '19 hour',
    date_trunc('hour', now()) + interval '3 day' + interval '21 hour',
    true
  ),
  (
    '44444444-4444-4444-4444-444444444444',
    '33333333-3333-3333-3333-333333333332',
    date_trunc('hour', now()) + interval '1 day' + interval '16 hour',
    date_trunc('hour', now()) + interval '1 day' + interval '18 hour',
    true
  ),
  (
    '44444444-4444-4444-4444-444444444445',
    '33333333-3333-3333-3333-333333333332',
    date_trunc('hour', now()) + interval '2 day' + interval '20 hour',
    date_trunc('hour', now()) + interval '2 day' + interval '22 hour',
    false
  ),
  (
    '44444444-4444-4444-4444-444444444446',
    '33333333-3333-3333-3333-333333333333',
    date_trunc('hour', now()) + interval '1 day' + interval '15 hour',
    date_trunc('hour', now()) + interval '1 day' + interval '17 hour',
    true
  ),
  (
    '44444444-4444-4444-4444-444444444447',
    '33333333-3333-3333-3333-333333333333',
    date_trunc('hour', now()) + interval '2 day' + interval '18 hour',
    date_trunc('hour', now()) + interval '2 day' + interval '20 hour',
    true
  )
on conflict (id) do update
set
  venue_id = excluded.venue_id,
  start_time = excluded.start_time,
  end_time = excluded.end_time,
  is_available = excluded.is_available;
