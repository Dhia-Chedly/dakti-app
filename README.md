# Dakti Android App

Dakti is an Android app built with Kotlin and Jetpack Compose for sports venue reservation, match organization, player invitations, and AI-assisted user workflows.

## What is included

- Android app foundation with clean package organization
- Layered MVVM architecture in a single app module
- Compose navigation shell with bottom navigation
- Placeholder screens for all major user journeys
- Feature ViewModels using `StateFlow`
- Repository contracts with compile-safe placeholder implementations
- Hilt dependency injection setup
- Base networking/database/background dependencies configured

## Planned enhancements

- Real authentication and user management
- Backend API integration
- Persistent local storage with Room entities/DAO usage
- Real AI assistant integration
- Real external integrations (WhatsApp, Email, Maps, Calendar, Dialer)
- Production notification and worker flows

## Tech stack

- Kotlin
- Jetpack Compose (Material 3)
- Navigation Compose
- MVVM + Repository pattern
- Hilt (DI)
- Kotlin Coroutines + StateFlow
- Retrofit + Gson + OkHttp Logging
- Room
- WorkManager

## Architecture overview

Main package: `com.dakti.app`

- `ui/`: Compose screens, components, navigation, theme
- `presentation/`: ViewModels and UI state by feature
- `domain/`: models, repository interfaces, use case placeholders
- `data/`: local/remote/data-source placeholders and repository implementations
- `ai/`: prompt/parser/service/suggestion placeholders
- `integration/`: external integration placeholders
- `notification/`: notification helper placeholder
- `worker/`: worker placeholder
- `di/`: Hilt modules
- `util/`: shared constants and wrappers

## Navigation

Bottom tabs:
- Home
- Venues
- Matches
- Assistant
- Profile

Additional routes:
- Welcome/Splash
- Login
- Register
- Venue Details
- Reservation Confirmation
- My Reservations
- Create Match
- Match Details
- My Matches
- Invitations

## Getting started

### Prerequisites

- Android Studio (latest stable recommended)
- Android SDK installed (`compileSdk = 36`)
- JDK **17 or 21** recommended


### Run in Android Studio

1. Open the project root in Android Studio.
2. Let Gradle sync complete.
3. Set Gradle JDK to 17 or 21:
   - `File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle`
4. Select emulator/device.
5. Run the `app` configuration.

## Venue Images with Supabase

The seed now keeps 2 real Tunisian venues per sport and stores `image_url` in `public.venues`.

1. Reset local Supabase to apply migrations + seed:
   - `npx supabase db reset`
2. Ensure a public bucket exists:
   - Bucket: `venue-images`
3. Upload images using these object paths:
   - `football/stade-olympique-de-rades.jpg`
   - `football/stade-olympique-de-sousse.jpg`
   - `basketball/salle-omnisports-de-rades.jpg`
   - `basketball/salle-cherif-bellamine.jpg`
   - `tennis/tennis-club-de-tunis.jpg`
   - `tennis/tennis-club-de-bizerte.jpg`
   - `volleyball/salle-cherif-bellamine.jpg`
   - `volleyball/salle-mohamed-zouaoui.jpg`
   - `handball/salle-omnisports-de-rades.jpg`
   - `handball/salle-mohamed-zouaoui.jpg`
   - `padel/padel-country-club.jpg`
   - `padel/eleven-padel-club.jpg`

If your project ref is not `dakti`, update seeded URLs after reset:

```sql
update public.venues
set image_url = replace(image_url, 'https://dakti.supabase.co', 'https://<your-project-ref>.supabase.co');
```
