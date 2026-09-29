# SmartBuild — Android App

Code reference for `SmartBuild/` (Kotlin + Jetpack Compose) — **the main system**. All
accounts, navigation, module screens, progress rules, cloud sync and the Modules 2–4 labs live
here. The embedded Godot engine is only a supporting render surface for the Module 0–1 visuals
(§6). Paths below are relative to `SmartBuild/app/src/main/java/com/example/smart_build/`
unless stated otherwise.

---

## 1. Project configuration

| Item | Value | Where |
|---|---|---|
| Gradle project | `Smart_Build`, single module `:app` | `settings.gradle.kts` |
| Application ID | `com.smartbuild.app` | `app/build.gradle.kts` |
| Namespace (Kotlin packages, `R`) | `com.example.smart_build` — internal only, not visible to users or Play | `app/build.gradle.kts` |
| Version | `versionCode 3`, `versionName "V2.1"` | `app/build.gradle.kts` |
| SDK levels | minSdk 24, targetSdk 35, compileSdk 37.1 | `app/build.gradle.kts` |
| Java target | 11 | `app/build.gradle.kts` |
| ABIs | `arm64-v8a`, `armeabi-v7a` | `ndk.abiFilters` |
| Minification | Off (`optimization { enable = false }`) | release build type |
| Native libs | `useLegacyPackaging = true` (Godot `.so` stored compressed, ~20 MB/ABI instead of ~70 MB) | `packaging.jniLibs` |
| Assets | `ignoreAssetsPattern` overridden so Godot's hidden files are packaged | `aaptOptions` |
| Secrets → BuildConfig | `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_AUTH_SCHEME` (default `smartbuild`), `SUPABASE_AUTH_HOST` (default `auth`) | `loadSmartBuildSecrets()` / `secretOrEnv()` — environment variable first, then `secrets.properties` |
| Custom task | `checkGodotPack` — if `src/main/assets/SmartBuildGodot.pck` is missing or < 1 KB, downloads it from `godotPackUrl` (GitHub release) and checks `godotPackSha256`; fails with instructions if that is impossible. `preBuild` depends on it | `app/build.gradle.kts` |
| Signing | `signingConfigs.release` reads `SmartBuild/keystore.properties` (gitignored). Without that file, release builds are unsigned and debug builds use the debug key | see [RELEASE](./RELEASE.md) |

Main libraries: Compose BOM 2026.06.01, Material 3, material-icons-extended 1.7.8,
navigation-compose 2.9.8, supabase-kt BOM 3.7.0 (`auth-kt`, `postgrest-kt`, `functions-kt`),
Ktor Android 3.5.2, kotlinx-serialization-json 1.8.1, `org.godotengine:godot:4.7.0.stable`,
fragment-ktx 1.8.9, material3-window-size-class 1.4.0.

### Manifest (`app/src/main/AndroidManifest.xml`)

- Permissions: `INTERNET`, `ACCESS_NETWORK_STATE`.
- Single activity `.MainActivity`: `sensorLandscape`, `windowSoftInputMode="adjustResize"`,
  exported (launcher).
- Deep link intent filter: `smartbuild://auth` (VIEW, DEFAULT, BROWSABLE). The scheme and host
  are **hard-coded** here — they are not read from BuildConfig.

---

## 2. Source map

### Root

| File | Role |
|---|---|
| `MainActivity.kt` | `FragmentActivity` + `GodotHost`. Hides system bars, forces landscape, composes `GodotHostLayer` + `AppNav` + `ConnectionLostDialog`, handles auth deep links, registers the Godot plugin. Godot command line: `--main-pack res://SmartBuildGodot.pck` |
| `SmartBuildBridge.kt` | Singleton message hub between Compose and Godot (see §6) |
| `SmartBuildGodotPlugin.kt` | Godot plugin named `SmartBuildBridge` |
| `SmartBuildMessage.kt` | Message types (`Command`, `Event`) |
| `SimulationWarmState.kt` | `Cold → EngineReady → Warming → ModuleReady` |

### `navigation/`

| File | Role |
|---|---|
| `Routes.kt` | Route definitions (§3) |
| `AppNav.kt` | `NavHost` and auth-driven redirects |

### `data/`

| File | Role |
|---|---|
| `client/SupabaseClient.kt` | Supabase client singleton (Auth with PKCE, Postgrest, Functions) |
| `ModuleProgressStore.kt` | Per-user progress cache and sync rules (§5) |
| `ModuleProgressRepository.kt` | `module_progress` fetch / upsert |
| `ModuleProgressRow.kt` | Serializable table row |
| `ReturnToModule.kt` | Tells Home which card to reopen after leaving a module |
| `CssPartsCatalog.kt` | The 40-part CSS encyclopedia used by Component Search |

### `viewmodel/`

| File | Role |
|---|---|
| `auth/AuthViewModel.kt` | All auth logic; also `AuthRecoveryHold`, form/error enums |
| `auth/AuthFormState.kt` | `None`, `SignIn`, `ForgotPassword`, `ResetPassword` |
| `auth/AuthStatusState.kt` | `Loading`, `Submitting`, `Registered`, `SignedIn`, `SignedOut`, `Error` |
| `home/HomeViewModel.kt` | Progress maps for Home, sign-out, delete dialog flag, email |
| `home/ModuleCardData.kt` | Module card model |
| `module/ModuleViewModel.kt` | Godot environment state; `prepareGodot()` attaches the Supabase session |
| `module/GodotEnvState.kt` | `Preparing`, `Ready`, `Error` |

### `screens/`

| Folder | Contents |
|---|---|
| `authenticationpage/` | `AuthPage` (animated container), `SignInForm` (sign-in **and** sign-up), `FPForm` (forgot password), `RPForm` (reset password), `AppLogoWithLoading` |
| `homepage/` | `HomePage` (module list defined inline), `TopBar`, `ModuleCardCarousel`, `ModuleCard`, `ModuleCardExpanded`, `ProgressCard`, `ProfileOverlay`, `HowToUseDialog`, `BenefitItem` |
| `modulepage/` | `ModulePage` — host screen for Godot modules 0 and 1 |
| `composemodule/` | `ComposeModuleScreen` (stations for Modules 2–4), `ScenarioBriefScreen`, `AssessmentScenarios` |
| `composelabs/` | `ComposeLabChrome.kt` — shared lab scaffold (`ComposeLabScaffold`) |
| `networklab/` | Module 2: `CableIntroScreen`, `CableIntroContent`, `NetworkTopologyScreen` |
| `crimplab/` | Module 2: `CrimpLabScreen`, `CrimpBench` (canvas bench), `CrimpLabState` |
| `serverlab/` | Module 3: `ServerTopologyScreen`, `LanDeviceDesk`, `ServerLabScreen`, `ServerDesktop` |
| `packettracer/` | Packet Tracer-style workspace used by `ServerTopologyScreen` (`PacketTracerWorkspace`, `PtModels`) |
| `maintenancelab/` | Module 4: `MaintenanceLabScreen` (Windows desktop service ticket) |
| `search/` | `ComponentSearchPage`, `CategoryChipRow`, `PartCard`, `PartDetailSheet`, `PartImageViewer` |

### Other

| Folder | Contents |
|---|---|
| `godot/GodotTestScreen.kt` | Despite the name, production code: `GodotHostLayer()` and `GodotRuntime.ensureAttached()` |
| `network/` | `ConnectivityObserver`, `NetworkConnectivityObserver` (default-network callback flow) |
| `ui/RuleFeedbackDialog.kt` | Shared "Correct / Incorrect" dialog used by every lab |
| `ui/theme/` | `Color.kt`, `Theme.kt`, `Type.kt`, `Readable.kt` |
| `components/` | `AppIcon`, `AppLogo`, `ConnectionLostDialog`, `ModuleName`; some unused files (see [KNOWN_ISSUES](./KNOWN_ISSUES.md#dead-code)) |

---

## 3. Navigation

Defined in `navigation/Routes.kt`, registered in `navigation/AppNav.kt`.

| Route | Pattern | Screen |
|---|---|---|
| `LoginPage` (start) | `ap` | `AuthPage` |
| `HomePage` | `hp` | `HomePage` |
| `ComponentSearch` | `search` | `ComponentSearchPage` |
| `ComposeModule` | `cm/{moduleId}/{simulationType}` | `ComposeModuleScreen` (Modules 2–4) |
| `ModulePage` | `mp/{moduleId}/{moduleName}/{simulationType}/{progress}` | `ModulePage` (Godot, Modules 0–1). Name is URL-encoded |

`simulationType`: **0 = Guided Simulation**, **1 = Scenario Assessment**.

Redirects (`AppNav`): password recovery → `ap`; signed in on `ap` → `hp`; signed out anywhere
else → `ap`.

Opening a module (`HomePage.openModule`):

1. If Assessment is requested for Module ≥ 1 and Guided is not done:
   progress ≥ 99% auto-marks Guided done; otherwise a Toast says "Finish Guided Simulation
   first" and nothing opens.
2. Modules 2–4 → `cm/...`; Modules 0–1 → `mp/...` with the saved progress.

---

## 4. Authentication

### Supabase client (`data/client/SupabaseClient.kt`)

`createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY)` with:

- `Auth` — `flowType = PKCE`, `scheme = SUPABASE_AUTH_SCHEME`, `host = SUPABASE_AUTH_HOST`
- `Postgrest`
- `Functions`

### Flows (`viewmodel/auth/AuthViewModel.kt`)

| Flow | Behaviour |
|---|---|
| Session observer | `Authenticated` → `SignedIn` and `ModuleProgressStore.bindAccount(userId)`; recovery sessions go to the Reset Password form instead. `NotAuthenticated` / `RefreshFailure` → `SignedOut` |
| Sign up | Validates email format and password ≥ 8 chars. Redirect `smartbuild://auth/confirm`. Without an immediate session → "check your email" |
| Sign in | Same validation. "Email not confirmed" → not-verified message; other failures → invalid credentials |
| Forgot password | `resetPasswordForEmail`, redirect `smartbuild://auth/reset`; starts a 2-hour in-memory `AuthRecoveryHold` |
| Reset password | Needs the recovery session; `updateUser { password }`, then signs out and returns to sign-in |
| Sign out | `auth.signOut()` + `ModuleProgressStore.unbindAccount()` (Home uses `HomeViewModel.signOut`) |
| Delete account | Invokes Edge Function **`delete-user`**, then signs out |

### Deep links (`MainActivity.handleAuthIntent`)

Runs in `onCreate` and `onNewIntent`. Passes the intent to `handleDeeplinks`, then treats it as
password recovery if the URI contains `type=recovery` or its path contains `reset`, or if an
`AuthRecoveryHold` is active.

---

## 5. Progress

### Store (`data/ModuleProgressStore.kt`)

| Aspect | Detail |
|---|---|
| Storage | SharedPreferences `smartbuild_module_progress_<userId>` (`_guest` when signed out) |
| Keys | `progress_<id>` (0–100), `guided_<id>`, `assessment_<id>`, `meta_version` (7) |
| Module ids | 0–4 |
| `setProgressPercent` | `max(current, new)`, capped at 99, ignored after assessment done; then `pushAsync` |
| `markGuidedCompleted` | `guided = true`, progress ≥ 50 (≤ 99) |
| `markAssessmentCompleted` / `markIntroCompleted` | Both flags true, progress 100 |
| `resetForRetake` | Progress 0, flags cleared (the only way down) |
| `pullFromRemote` | Fetch rows → merge (max, flags OR, 100 if assessed else ≤ 99) → push back rows where local is ahead |
| Migration | Old device-wide cache is adopted by the first signed-in account, then deleted |

### Repository (`data/ModuleProgressRepository.kt`)

Table `module_progress`; `fetchAll()` filtered by `user_id`; `upsert()` with
`onConflict = "user_id,module_id"`. Columns in `ModuleProgressRow`: `user_id`, `module_id`,
`percent`, `guided_done`, `assessment_done`, `updated_at`.

### Who writes progress

| Source | Calls |
|---|---|
| `ModulePage` (Godot events) | `progress_update` → `setProgressPercent`; `guided_completed` → `markGuidedCompleted`; `assessment_completed` → `markAssessmentCompleted` / `markIntroCompleted`; `progress_reset` → `resetForRetake` |
| `ComposeModuleScreen` | `bumpProgress(fraction)` after each station; `finishPath()` at the end |
| `HomePage` | Auto-marks Guided when progress ≥ 99% and Assessment is tapped |

---

## 6. Godot integration

Godot is used as a supporting component: Compose hosts its surface, sends it commands, and
interprets its reports. Nothing in Godot decides navigation, unlocking or saved progress.

### Host layer (`godot/GodotTestScreen.kt`)

- `GodotHostLayer()` is composed once in `MainActivity`, **behind** navigation.
- It is a `FrameLayout` that stays VISIBLE, parked at `translationX = 10000f` until
  `SmartBuildBridge.godotSurfaceVisible` becomes true.
- `GodotRuntime.ensureAttached()` adds one `GodotFragment` tagged `GODOT` and never replaces it.

### Bridge (`SmartBuildBridge.kt`)

| Member | Purpose |
|---|---|
| `godotMessages: SharedFlow<String>` | Raw JSON events from Godot. **replay = 0**, buffer 1000 |
| `engineInitialized`, `priorityWarmupReady` | Set by `engine_initialized` / `warmup_ready` |
| `godotSurfaceVisible: StateFlow<Boolean>` | Moves the host layer on/off screen |
| `simulationWarmState: StateFlow` | Drives the Home "Preparing simulations…" label |
| `requestWarmup(moduleId)` | Sends `warmup` |
| `prepare(moduleId, simulationType, progress, tokens…)` | Sends `prepare` |

### Plugin (`SmartBuildGodotPlugin.kt`)

- Signal `message_from_compose(String)` — Compose → Godot.
- `@UsedByGodot sendMessageToCompose(String)` — Godot → Compose.

### Module page (`screens/modulepage/ModulePage.kt`)

Handshake: wait for plugin (30 s) → wait for `engine_initialized` (90 s) → subscribe →
`prepare` → wait for `ready`/`error` (120 s, one automatic retry). On `ready` the surface is
shown; on dispose it is hidden. Full message table: [ARCHITECTURE §5](./ARCHITECTURE.md#5-compose--godot-support-protocol).

---

## 7. Modules 2–4 (Compose labs)

### Orchestrator (`screens/composemodule/ComposeModuleScreen.kt`)

- `isAssessment = simulationType == 1`, `hints = !isAssessment`.
- Assessment starts with `ScenarioBriefScreen` (text from `AssessmentScenarios.forModule`).
- `station` index is kept in memory only — leaving restarts at station 0.
- Back: scenario → Home; station > 0 → previous station; station 0 → scenario/Home.
- Every station receives `onLeave = { stepBack() }`, which shows a Back button in the lab.

| Module | Stations (progress after each) |
|---|---|
| 2 | `CableIntroScreen` (20%) → `CrimpLabScreen` forced straight-through (55%) → `NetworkTopologyScreen` (99%) |
| 3 | `ServerTopologyScreen` (45%) → `ServerLabScreen` (99%) |
| 4 | `MaintenanceLabScreen` (99%) |

After the last station, `finishPath()` marks Guided or Assessment complete and returns Home.

### Shared chrome (`screens/composelabs/ComposeLabChrome.kt`)

`ComposeLabScaffold`: header with Back button (shown when not embedded or when `onBack` is
given), optional goal, yellow coach line (Guided), playground area, status line, Reset and a
primary button that becomes the "complete" button once `statusOk` is true. Used by the
Module 2 screens. The Module 3 and 4 labs draw their own floating chips (Guide, Reset,
counter, Back).

Per-lab behaviour, pass conditions and answer keys: [MODULE_REFERENCE](./MODULE_REFERENCE.md).

---

## 8. Home, search and theme

### Home (`screens/homepage/HomePage.kt`)

- Refreshes progress on entry and on resume (local, then `pullFromRemote`).
- Reopens the last module card via `ReturnToModule`.
- Warms up Godot Module 1 and shows "Preparing simulations…" / "… simulation ready".
- Top bar: logo, search pill (→ `search`), help (→ `HowToUseDialog`), profile (→ `ProfileOverlay`).
- Cards: `HorizontalPager`; tap opens a full-screen expanded card with `ProgressCard`
  (progress ring, **Guided Simulation** and **Scenario Assessment** buttons). Module 0 shows
  **Continue Lesson / Review Lesson** only.
- Card images: `app/src/main/res/drawable-nodpi/module_<N>_card.png`.

### Profile overlay

Email, **Change Password** (not implemented yet), **Delete Account** (confirmation dialog →
`delete-user` Edge Function), **Sign Out**.

### Component Search (`screens/search/`, `data/CssPartsCatalog.kt`)

- 40 parts in 8 categories: Core Hardware, Tools & ESD, Network Devices, Cabling & Media,
  Servers & Storage, Internet & Cloud, Peripherals, Maintenance.
- Each part: title, category, summary, overview, how it works, CSS use, common issues,
  technician tips, related modules, image.
- Images are bundled drawables `res/drawable-nodpi/card_*.png` (no network).
- Search matches title, summary, category, overview and CSS use (case-insensitive).
- Detail sheet with pinch/double-tap image zoom.

### Connectivity

`NetworkConnectivityObserver` → `MainActivity` shows `ConnectionLostDialog` (blocking, no
buttons) while offline.

### Theme (`ui/theme/`)

| Token | Value |
|---|---|
| Primary | `#17A6DF` |
| Black (background) | `#011723` |
| White | `#F3F6FA` |
| Heading font | GS Code (`res/font/gscode_*.ttf`) |
| Body font | GS Flex (`res/font/gsflex_*.ttf`) |
| Helpers | `readableSp`, `labSp` (12–15 sp), `lineGap` in `Readable.kt` |

---

## 9. Logcat tags

Filter these in Android Studio's Logcat when debugging:

| Tag | Area |
|---|---|
| `GODOT_COMM` | Bridge messages and Godot host |
| `AUTH`, `AUTH_SESSION`, `AUTH_EVENT`, `AUTH_RECOVERY`, `AUTH_DEEPLINK` | Authentication |
| `DELETE_ACCOUNT` | Delete account |
| `ModuleProgressStore`, `ModuleProgress` | Progress cache and sync |
| `HomeViewModel` | Home refresh |
