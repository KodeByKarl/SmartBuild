# SmartBuild — Maintenance Guide

Recipes for the changes a maintainer is most likely to make. Each recipe lists the files to
edit and what to rebuild afterwards.

Rebuild rules:

- Changed **Kotlin** (`SmartBuild/`) → rebuild the APK.
- Changed **Godot** (`SmartBuild-Godot/`) → run the regression probe → export the `.pck` →
  rebuild the APK. See [SETUP_AND_BUILD §4](./SETUP_AND_BUILD.md#4-export-the-godot-pack).

Kotlin paths are relative to `SmartBuild/app/src/main/java/com/example/smart_build/`.

---

## 1. Edit Module 0 slide text

1. Open `SmartBuild-Godot/modules/module_0/slides/lesson_content.gd`.
2. Find the `match slide_id:` branch for the slide number (1–20) inside `build()`.
3. Edit the `title` / `body` strings of the cards. Keep the same layout call.
4. Help text for a slide is in `help_for(slide_id)` in the same file.
5. Slide 21 (completion) is `slides/slide_21_completion.gd` / `.tscn`.

Adding or removing a slide: add or remove the path in `SLIDE_PATHS` in
`modules/module_0/Main.gd` and create the matching `slide_N.tscn` (copy an existing one and
set its `slide_id`). Progress is computed from the number of slides automatically.

---

## 2. Edit Module 1 pages, steps and order

Everything lives in `SmartBuild-Godot/scripts/module_content_registry.gd`.

| To change | Edit |
|---|---|
| Page titles, descriptions, completion messages | The page dictionaries inside `_build_module_1()` |
| Quiz questions (Phases 01, 04, 05, 06) | The `_step(id, label, instruction, question, tip)` calls. The **label** is the correct answer |
| Disassembly order | `_tesda_disassemble_steps()` (used by Phase 02 and the capstone) |
| Assembly order | The `_install_step(...)` list in Phase 03, and the `as_` list in `_tesda_disassemble_then_rebuild_steps()` |
| Pre-built PC before teardown | `_complete_pc_seed()` |
| Parts in the tray | `module_1_build_parts()` |

Rules to respect:

- Every `install` / `remove` step's `part_id` and `slot_id` must match `PART_HOME` in
  `core/simulation/pc_build_bench.gd`.
- An install order must satisfy `SLOT_PREREQS` (e.g. CPU after motherboard, cooler after CPU,
  24-pin after board **and** PSU).
- The disassembly-done modal is detected by the Phase 02 completion message containing
  "stripped safely" or "assemble the workstation" — keep one of those phrases if you reword it.

After editing, run:

```powershell
cd D:\Porjects\Smartbuild\SmartBuild-Godot
& "D:\Porjects\Smartbuild\tools\godot\Godot_v4.7.2-stable_win64.exe" --headless --path . --script res://tools/regress_probe.gd
```

It must end with `problems: 0`. Then export the `.pck` and rebuild.

---

## 3. Adjust the 3D bench

File: `SmartBuild-Godot/core/simulation/pc_build_bench.gd`.

| To change | Where |
|---|---|
| Bay position / size | `_add_slot(...)` calls in `_build_default_slots()` |
| Part rotation in a bay | `_slot_part_rotation()` |
| How a part sits in its bay | `_seat_part_in_slot()` and the `_seat_*` helpers |
| Where the pulled motherboard rests | `BOARD_BENCH_OFFSET` (and `BOARD_CARRIED_SLOTS` for which bays move with it) |
| Show the case cover again | `HIDE_CASE_COVER = false` |
| Camera start / zoom limits | `_orbit_yaw`, `_orbit_pitch`, `_orbit_distance`, `ZOOM_MIN`, `ZOOM_MAX` |
| Wrong-answer explanations | `_tesda_why()` / `_tesda_why_not()` |

Part meshes: `assets/models/module_1/_factory/module_1_part_factory.gd` (procedural) or a
`model.tscn` in `assets/models/module_1/<part>/` referenced from `module_1_build_parts()`.

---

## 4. Edit the Modules 2–4 labs

| Module / screen | File | Typical edits |
|---|---|---|
| Station order (all) | `screens/composemodule/ComposeModuleScreen.kt` | Add/remove/reorder stations, progress fractions |
| Assessment scenario text | `screens/composemodule/AssessmentScenarios.kt` | Title, situation, faults, goal |
| M2 cable intro cards | `screens/networklab/CableIntroContent.kt` | Card text |
| M2 crimp steps and wire orders | `screens/crimplab/CrimpLabState.kt` | `T568B_WIRES`, `T568A_WIRES`, step instructions |
| M2 topology answer | `screens/networklab/NetworkTopologyScreen.kt` | `ORDER`, `LINKS`, faulted start |
| M3 topology, IPs, ping target | `screens/serverlab/ServerTopologyScreen.kt`, `LanDeviceDesk.kt` | IP strings `192.168.10.x`, mask, cable rules |
| M3 cable rules | `screens/packettracer/PtModels.kt` | `expectedCable()`, `evaluateLink()` |
| M3 file server checklist | `screens/serverlab/ServerLabScreen.kt` (`ServerConfig`), `ServerDesktop.kt` | Folder/group names, hints, faulted start |
| M4 service ticket | `screens/maintenancelab/MaintenanceLabScreen.kt` | `MaintState`, hints, ping targets |

Adding a station: create a composable with parameters `hints`, `embedded`, `startFaulted`,
`onStationComplete`, `onLeave`; add it to the module's `when (station)` in
`ComposeModuleScreen.kt`; call `bumpProgress(...)` before advancing and `finishPath()` after the
last one. Use `ComposeLabScaffold` (`screens/composelabs/ComposeLabChrome.kt`) and pass
`onBack = onLeave` so the Back button shows.

---

## 5. Edit Home module cards

- Titles, descriptions, contents and benefits: the `ModuleCardData(...)` list in
  `screens/homepage/HomePage.kt` (one entry per module, around line 155).
- Card images: `SmartBuild/app/src/main/res/drawable-nodpi/module_<N>_card.png`.
- Help dialog text: `screens/homepage/components/HowToUseDialog.kt`.

---

## 6. Add or edit a part in Component Search

1. Open `data/CssPartsCatalog.kt`.
2. Add a `CssPart(...)` entry to `parts` with a unique `id`, `title`, `category` (one of the
   existing categories), text fields, `relatedModules`, and `imageRes`.
3. Put the image in `SmartBuild/app/src/main/res/drawable-nodpi/` (PNG, lowercase name, e.g.
   `card_ssd.png`) and reference it as `R.drawable.card_ssd`.

---

## 7. Bump the version

In `SmartBuild/app/build.gradle.kts`:

```kotlin
versionCode = 4        // current: 3 — must increase for every release / Play upload
versionName = "V2.2"   // current: "V2.1" — shown to users
```

Add an entry to [CHANGELOG](./CHANGELOG.md).

---

## 8. Change the package name (application ID)

**Already done in V2.1:** the application ID is `com.smartbuild.app` (the old
`com.example.smart_build` is rejected by Google Play). Once the app is published or installed
on students' phones, **do not change it again** — a new ID is a new app.

How it was done (only the application ID in `app/build.gradle.kts`):

```kotlin
defaultConfig {
    applicationId = "com.smartbuild.app"
}
```

Leave `namespace` and the Kotlin package as they are — the app works the same. Changing the
Kotlin package too would require moving every source folder and updating imports.

Notes:

- A new application ID is a **different app** to Android: users must install it fresh.
- Deep links use the `smartbuild://` scheme, not the package name, so Supabase settings stay
  the same.
- The instrumented test `ExampleInstrumentedTest` asserts the package name — keep it in sync.

---

## 9. Change the auth deep link (scheme/host)

The default is `smartbuild://auth`. To change it, update **all** of:

1. `SmartBuild/secrets.properties` → `SUPABASE_AUTH_SCHEME`, `SUPABASE_AUTH_HOST`.
2. `SmartBuild/app/src/main/AndroidManifest.xml` → the `<data android:scheme android:host>`
   intent filter (hard-coded).
3. Supabase dashboard → Authentication → URL Configuration → redirect URLs
   (`<scheme>://<host>`, `/confirm`, `/reset`).
4. `SmartBuild/web/auth-reset.html` if you host it (it hard-codes `smartbuild://auth/reset`).

---

## 10. Point the app to another Supabase project

1. Create the table and policy from [SUPABASE §4](./SUPABASE.md#4-database-table).
2. Deploy the `delete-user` Edge Function ([SUPABASE §8](./SUPABASE.md#8-edge-function-delete-user)).
3. Set the redirect URLs ([SUPABASE §3](./SUPABASE.md#3-auth-deep-links)).
4. Put the new URL and anon key in `SmartBuild/secrets.properties` (and Godot
   `config/env.local` if you run Godot standalone).
5. Rebuild the APK. Existing accounts do **not** move automatically — students must sign up
   again unless users are migrated in Supabase.

---

## 11. Change the app name or icon

- Name: `SmartBuild/app/src/main/res/values/strings.xml` → `app_name`.
- Launcher icon: `SmartBuild/app/src/main/res/drawable/app_icon` (referenced by `android:icon`
  and `android:roundIcon` in the manifest).
- In-app logo: `components/AppLogo.kt` / `AppIcon.kt` and their drawables.

---

## 12. Change progress rules

All rules are in `data/ModuleProgressStore.kt`:

| Rule | Function |
|---|---|
| 99% cap before Assessment | `setProgressPercent` (`coerceAtMost(99f)`) |
| Guided floor 50% | `markGuidedCompleted` |
| 100% on Assessment | `markAssessmentCompleted` |
| Assessment unlock | UI only: `HomePage.openModule` and `ProgressCard` (`progress >= 99` or Guided done) |
| Retake | `resetForRetake` |

The instrumented test `P6SmokeInstrumentedTest` checks these rules — update it with any change.
