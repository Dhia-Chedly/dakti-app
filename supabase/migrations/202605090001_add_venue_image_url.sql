alter table if exists public.venues
add column if not exists image_url text;

comment on column public.venues.image_url is
  'Public URL for the venue image, typically served from Supabase Storage.';
