# QuestLog

**QuestLog** is an Android habit tracker built around tabletop RPG mechanics. Instead of ticking boxes, you log real-life actions against six character stats, earn XP, level up, and unlock feats — with a small friends-only crew to keep you honest.

It is built offline-first: everything is written to the device first and reconciled with the cloud in the background, so the app is fully usable on a plane and never loses a log.

<!-- TODO: add 3–4 screenshots here. This is the single biggest improvement you can make to this README.
     Suggested: character sheet, quest log list, pathway detail, crew feed.
     ![Character](docs/screenshots/character.png) -->

## Features

- **Six-stat character sheet** — every logged action feeds one of six stats, so progress is visible as a character rather than a streak counter.
- **XP and levelling** — an XP engine with daily caps (so one heroic day can't buy a week of progress) and an exponential levelling curve.
- **Feats** — milestone levels unlock a choice of feats, presented as a decision rather than an automatic grant.
- **Pathways** — multi-step quest chains with staged progress. Pathways use an escrow mechanic: XP is held until the chain is carried through, and abandoned on inactivity.
- **Habit slots** — recurring commitments tracked separately from one-off quests, with streak tracking.
- **Proof** — optional photo or note evidence attached to a completion, at a configurable proof level.
- **Crew** — a small friends-only group with a shared activity feed, in-group chat and push notifications. No global leaderboard by design: comparison is limited to people you actually know.
- **Reminders** — scheduled local reminders that survive a device reboot.
- **Offline-first sync** — full functionality with no connection; background sync to Firestore when one returns.
- **Theming and language** — three colour palettes, light/dark support, and in-app Turkish/English switching.

## Architecture

The app is a single Gradle module organised into four layers, with roughly 160 Kotlin source files.

```
com.mehmetbozkurt.questlog
├── core/           # Cross-cutting infrastructure
│   ├── common/     # Result, UiText, dispatchers, MVI base
│   ├── database/   # Room database, DAOs, entities, migrations
│   ├── designsystem/  # Theme, palettes, shared components
│   ├── di/         # Hilt modules
│   ├── navigation/ # NavHost, routes, bottom navigation
│   ├── notification/  # Reminders, FCM, notification channels
│   ├── settings/   # Language and palette preferences (DataStore)
│   ├── sync/       # Sync manager, scheduler, worker
│   ├── auth/       # Google credential handling
│   └── media/      # Proof photo storage
├── data/           # Repository implementations, Firestore data sources, mappers
├── domain/         # Models, repository interfaces, progression rules
└── feature/        # One package per screen (UI + ViewModel + Contract)
```

### MVI

Every screen follows the same contract: a `UiState`, a `UiEvent` the UI sends up, and a `UiEffect` for one-shot actions. They share a small base class:

```kotlin
abstract class MviViewModel<S : UiState, E : UiEvent, F : UiEffect>(
    initialState: S
) : ViewModel() {
    val state: StateFlow<S>      // survives configuration changes, always has a value
    val effect: Flow<F>          // Channel-backed, consumed exactly once

    protected fun setState(reducer: S.() -> S)
    protected fun sendEffect(newEffect: F)
    abstract fun onEvent(event: E)
}
```

State and effects are deliberately separated. A navigation command or a toast must fire exactly once, so it goes through a `Channel`; screen state must always have a current value and be replayable, so it lives in a `StateFlow`. Putting both in the same stream causes duplicate navigation on rotation — a bug this split makes structurally impossible.

Every screen being written to the same shape means adding a new one is mechanical rather than a design decision each time.

### Progression as a domain layer

The game rules live in `domain/progression`, isolated from both the UI and the database:

| Object | Responsibility |
|---|---|
| `XpEngine` | Awards XP for a completion |
| `XpCurve` | Level thresholds (exponential) |
| `XpLimits` | Daily caps and anti-farming limits |
| `StreakEngine` | Streak continuation and breakage |
| `HabitRules` | Recurring-commitment behaviour |
| `PathwayRules` | Quest-chain progress, escrow and abandonment |
| `CatalogRules` | Pre-defined task catalogue behaviour |
| `CrewRules` | Group visibility and feed rules |

Keeping these as plain objects with no Android or Room dependency means balance changes happen in one place and can be reasoned about (and unit-tested) without standing up a database or an emulator. Game balance is the part of this app most likely to change, so it is the part most deliberately isolated.

### Offline-first sync

The device is the source of truth during use; the cloud is a replica that catches up.

- Records are created with a **UUID generated on-device**, so an entity has a stable identity before the server has ever seen it.
- Each record carries a **sync state**, so the worker knows what is still outstanding.
- Deletions are **soft**: a `pending_deletions` table records what must be removed remotely, because a row that is simply gone locally cannot be replayed to the server.
- **`SyncWorker`** is a Hilt-injected `CoroutineWorker` scheduled through WorkManager. It retries on network and Firestore exceptions rather than dropping work, and guards concurrent runs so two syncs cannot interleave on the same records.

The schema has been migrated **eleven times** (v1 → v12) with hand-written Room migrations rather than destructive fallback, so installs from early versions keep their data.

### Authentication

Google sign-in goes through Credential Manager. The result is a sealed type:

```kotlin
sealed interface GoogleIdTokenResult {
    data class Success(val idToken: String) : GoogleIdTokenResult
    data object Cancelled : GoogleIdTokenResult
    data class Failed(val message: UiText) : GoogleIdTokenResult
}
```

A user dismissing the sheet is a normal outcome, not an error — modelling it separately keeps the UI from showing a failure message for something the user did on purpose.

### Notifications and reminders

- Quest reminders are scheduled with `AlarmManager` and re-registered by a `BootReceiver`, since alarms do not survive a reboot on their own.
- Crew messages arrive via FCM, suppressed while the user is already looking at that chat.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVI (State / Event / Effect), layered core–data–domain–feature |
| DI | Hilt |
| Local database | Room (with hand-written migrations) |
| Preferences | DataStore |
| Background work | WorkManager |
| Backend | Firebase (Auth, Firestore, Cloud Messaging, Storage) |
| Auth | Credential Manager + Google ID |
| Navigation | Navigation Compose |
| Async | Coroutines & Flow |
| Images | Coil |
| Build | Gradle Kotlin DSL, version catalog |

## Getting Started

### Requirements

- Android Studio (latest stable)
- JDK 17+
- A Firebase project with Authentication, Firestore, Cloud Messaging and Storage enabled

### Firebase setup

`app/google-services.json` is required to build but is gitignored, since it is project-specific. Create your own Firebase project, enable Google sign-in under Authentication, and place the downloaded file at:

```
app/google-services.json
```

You will also need the Web client ID from that project for Credential Manager sign-in.

### Build

```bash
./gradlew :app:assembleDebug
```

## Project Status

Actively developed. <!-- TODO: replace with a real status — e.g. "In internal testing on Play Console", or a Play Store link once published. -->

## License

<!-- TODO: add a LICENSE file and name it here. MIT matches your other repositories. -->
