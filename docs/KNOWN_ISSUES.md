# SmartBuild — Known Issues

Known limitations and technical debt at handover (29 Sep 2026), with suggested fixes. None of
these block normal use of the app. Fixed items are recorded in [CHANGELOG](./CHANGELOG.md).

---

## Security

| Issue | Impact | Suggested fix |
|---|---|---|
| The real Supabase URL and anon key are committed in `SmartBuild/secrets.properties.example` and `SmartBuild-Godot/config/env.example` | Low: the anon key is public by design and ships inside every APK; data is protected by RLS | Replace the values with placeholders and commit. Rotate the key only if you also suspect the RLS policy was weak |
| The release keystore and its passwords live in one folder (`release-keys/`) on the developer PC | If that PC is lost, future updates cannot be signed | Back it up in two offline places and hand it over privately |
| Godot `config/env.local` may contain DEBUG Supabase keys | Only used by the Godot editor; excluded from the export | Keep it out of Git (already gitignored) |

---

## Accounts

| Issue | Impact | Suggested fix |
|---|---|---|
| **Change Password** in the Profile menu does nothing (`ProfileOverlay.kt`, `// TODO`) | Signed-in users cannot change their password from inside the app | For now, sign out and use **Forgot Password?** on the Log In screen. To implement: call `supabase.auth.updateUser { password = ... }` from a small dialog |
| Default Supabase email sending is rate-limited | Confirmation/reset emails may be delayed when many students sign up at once | Configure custom SMTP in Supabase Auth settings |

---

## Progress

| Issue | Impact | Suggested fix |
|---|---|---|
| `ModuleProgressStore.repairFalseIntroComplete()` resets Module 0 to 0% whenever Module 0 is complete but Modules 1–4 are all untouched. It runs on every cloud sync | A student who finishes the Intro and nothing else sees Module 0 drop back to 0% the next time Home syncs | This guards against an old bug where reaching the last slide reported 100%. Confirm that bug no longer happens, then remove the repair or run it only once inside the prefs `VERSION` migration |
| Progress only increases; there is no "undo" except **Retake** | By design | — |
| Local cache and cloud can differ for a moment after going offline | The blocking offline dialog prevents most of this; Home re-syncs on open/resume | — |

---

## Labs

| Issue | Impact | Suggested fix |
|---|---|---|
| In Modules 3 and 4, the **Reset** button always rebuilds a clean bench, even in the Assessment (which starts with a planted fault) | After Reset in an Assessment, the fault is gone, so the task becomes a from-scratch build | In `ServerLabScreen.reset()` and `MaintenanceLabScreen.reset()`, rebuild the faulted state when `startFaulted` is true |
| Leaving a lab loses the current station and state | The learner restarts the module on the next open | Save the station index per module (e.g. in `ModuleProgressStore`) if needed |
| Lab steps are hard-coded in Kotlin | Content changes need a code change and rebuild | Acceptable for this size; see [MAINTENANCE_GUIDE §4](./MAINTENANCE_GUIDE.md#4-edit-the-modules-24-labs) |

---

## Godot

| Issue | Impact | Suggested fix |
|---|---|---|
| `scripts/Main.gd` still has a debug `_on_button_pressed()` that sends an `exit` event; Android does not handle `exit` | None: no scene connects a button to it | Remove it, or handle `exit` in `ModulePage` as a Back action |
| Headless `--check-only --script` reports errors for autoload names (`ResponsiveLayout`, `ResourceWarmup`) | False positives; confuses new developers | Validate by running scenes/probes instead — [SETUP_AND_BUILD §7](./SETUP_AND_BUILD.md#7-tests-and-checks) |
| A duplicate-UID warning is printed on export | Harmless, but stops `export_android_pck.ps1` in Windows PowerShell 5 | Re-save the affected resource in the editor, or use the direct export command |
| First engine boot takes several seconds on low-end phones | Home shows "Preparing simulations…" | By design (warm-up happens once per launch) |

---

## Platform and build

| Issue | Impact | Suggested fix |
|---|---|---|
| Internet is required at all times | Offline classrooms cannot use the app | Would need an offline mode for auth and a queued progress sync |
| ARM phones only (no x86) | Cannot run on most PC emulators | Add `x86_64` to `abiFilters` if emulator support matters (larger APK) |
| Release APK is about 132 MB | Slow downloads; check Play size limits for the AAB | Play Asset Delivery for the `.pck` — [RELEASE §5](./RELEASE.md#5-google-play-specifics) |
| The `delete-user` Edge Function source is not in the repository | Hard to redeploy | Commit the reference in [SUPABASE §8](./SUPABASE.md#8-edge-function-delete-user) under `supabase/functions/` |
| `SmartBuild/gradle.properties` sets `org.gradle.java.home` to a path on the original machine | Build fails on other machines | Remove the line or change it per machine |
| Minification (R8) is off | Larger APK | Only enable with keep rules for Godot and kotlinx-serialization |
| Uncommitted work in both repositories at handover | Changes could be lost | Commit and push right away |

---

## Dead code

Files that are not referenced anywhere and can be deleted:

| File | Note |
|---|---|
| `SmartBuild/app/src/main/java/com/example/smart_build/components/WifiModal.kt` | Empty (package line only) |
| `SmartBuild/app/src/main/java/com/example/smart_build/components/CustomColorOTF.kt` | Text-field helper, not used by any screen |
| `SmartBuild/app/src/main/java/com/example/smart_build/components/CSOutlinedTextField.kt` | Text-field helper, not used by any screen |
| `SmartBuild-Godot/scripts/Main.gd` → `_on_button_pressed()` | Never connected (see [Godot](#godot)) |

Search again before deleting (`rg WifiModal`, etc.) in case new code starts using them.
