# SmartBuild — Troubleshooting

Problems seen during development and support, with fixes. Supabase-specific errors are also
listed in [SUPABASE §6](./SUPABASE.md#6-common-supabase-problems).

For live debugging, connect the phone by USB and filter Logcat (Android Studio) by the tags in
[ANDROID_APP §9](./ANDROID_APP.md#9-logcat-tags), e.g. `GODOT_COMM` or `AUTH`.

---

## 1. Build problems

| Symptom | Cause | Fix |
|---|---|---|
| `JAVA_HOME is not set and no 'java' command could be found` | The Gradle wrapper needs a JDK to start | `$env:JAVA_HOME = "D:\Porjects\Smartbuild\tools\jdk\jdk-17.0.20.1+1"` (or Android Studio's `jbr`), or build from Android Studio |
| `Gradle JVM ... org.gradle.java.home ... does not exist` | `gradle.properties` points to a JDK path from the original machine | Edit or delete `org.gradle.java.home` in `SmartBuild/gradle.properties` |
| `Missing SmartBuildGodot.pck` | The `.pck` is gitignored and was never exported on this machine | Export it: [SETUP_AND_BUILD §4](./SETUP_AND_BUILD.md#4-export-the-godot-pack) |
| Build succeeds with a warning about empty `SUPABASE_URL` | `secrets.properties` missing | Create it from `secrets.properties.example`; the app crashes at start-up without it |
| `SDK location not found` | No `local.properties` | Open the project once in Android Studio, or create `local.properties` with `sdk.dir=...` |
| `compileSdk 37.1 ... not installed` | Missing SDK platform | Android Studio → SDK Manager → install API 37 |
| Gradle reports `UP-TO-DATE` but no APK in `outputs/apk/debug` | Stale packaging state | `.\gradlew.bat :app:packageDebug --rerun` |
| `export_android_pck.ps1` stops on "UID duplicate detected" or other warnings | Windows PowerShell treats Godot's stderr warnings as errors | Run the direct Godot command in [SETUP_AND_BUILD §4 option B](./SETUP_AND_BUILD.md#option-b--direct-command) |
| Godot re-imports everything / `.godot` changes after opening | Opened in a different Godot version | Use Godot **4.7.2** from `tools/godot/` |

---

## 2. Installing on a phone

| Symptom | Cause | Fix |
|---|---|---|
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Installed copy signed with a different key (e.g. a debug build over the official release) | Uninstall first: `adb uninstall com.smartbuild.app`. Sign official builds only with the release keystore ([RELEASE](./RELEASE.md)) |
| Two SmartBuild icons on the phone | The old demo (`com.example.smart_build`) and the official app (`com.smartbuild.app`) are separate apps | Uninstall the old demo; progress returns after signing in to the official app |
| `INSTALL_FAILED_NO_MATCHING_ABIS` | x86 emulator or x86 device | Use a real ARM phone. The APK ships only `arm64-v8a` and `armeabi-v7a` |
| "App not installed" when sideloading | Not enough storage (APK is ~132 MB, needs more to install), or unknown-sources blocked | Free space; allow "Install unknown apps" for the file manager / browser |
| `adb` does not see the phone | USB debugging off or driver missing | Enable Developer options → USB debugging; accept the RSA prompt; try another cable |

---

## 3. App start-up and sign-in

| Symptom | Cause | Fix |
|---|---|---|
| App closes immediately on launch | Empty Supabase URL/key compiled in | Fill `secrets.properties`, rebuild |
| "Connection lost" dialog never goes away | No validated internet on the phone | Connect to a working network. The dialog is blocking by design |
| "Invalid credentials" with the right password | Account not confirmed, or typo | Check email for the confirmation link, or turn off email confirmation in Supabase |
| Sign-up says "check your email" but nothing arrives | Supabase default SMTP is rate-limited | Check spam; configure custom SMTP in Supabase Auth settings for production |
| Tapping the reset/confirm link does nothing or opens a blank page | Redirect URL not allow-listed, or the mail client cannot open `smartbuild://` links | Allow `smartbuild://auth/reset` and `/confirm` in Supabase; open the email on the phone; optionally host `web/auth-reset.html` |
| Reset password says the session expired | The reset link was opened too late or on another device | Request a new reset email and open it on the phone with the app installed |
| Delete Account fails | `delete-user` Edge Function not deployed | Deploy it: [SUPABASE §8](./SUPABASE.md#8-edge-function-delete-user) |

---

## 4. Godot modules (0 and 1)

| Symptom | Cause | Fix |
|---|---|---|
| Home stays on "Preparing simulations…" for a long time | First engine boot on a slow phone | Wait; it happens once per app launch. Subsequent module opens are fast |
| Module page shows an error with **Try again** | Engine or scene took too long (timeouts 30 s / 90 s / 120 s) | Tap Try again. If it repeats, force-close and reopen the app; check Logcat `GODOT_COMM` |
| Module opens to a black or blank screen | Stale `.pck` or a Godot script error | Re-export the `.pck` from current source, rebuild; run the Godot regression probe |
| Changes in Godot don't appear on the phone | `.pck` not re-exported, or APK not rebuilt | Export the `.pck` **then** rebuild and reinstall |
| Module 1 feels slow / low frame rate | Low-end GPU | `PerformanceProfile` lowers quality automatically on Android; close background apps |
| Taps on small parts hit the wrong thing | Camera too far | Zoom in with **Zoom +** or pinch before tapping |

---

## 5. Labs (Modules 2–4)

| Symptom | Cause | Fix |
|---|---|---|
| App crashed after typing a path in Module 3's PC File Explorer | Bug in path normalization (regex replacement) | **Fixed** in the 29 Sep 2026 revision — install the latest APK |
| No way to exit Module 2 | Back button hidden on embedded stations | **Fixed** in the 29 Sep 2026 revision |
| Leaving a lab and coming back restarts it | Station position is not saved (by design) | Finish the module in one sitting |
| Pressing **Reset** in an Assessment removes the planted fault | Reset always returns to a clean bench | Known limitation — see [KNOWN_ISSUES](./KNOWN_ISSUES.md#labs). Exit and reopen the Assessment instead |
| The Scenario Assessment button does nothing / shows a Toast | Guided not finished yet | Finish the Guided Simulation first (or reach 99%) |

---

## 6. Progress

| Symptom | Cause | Fix |
|---|---|---|
| Progress differs between phones | Other phone has not synced yet | Open Home (it pulls from Supabase on open and resume) |
| Progress missing after reinstall | Local cache cleared | Sign in with the same account; Home pulls from the cloud |
| Rows never appear in `module_progress` | RLS policy missing, schema cache stale, or not signed in | Run the SQL in [SUPABASE §4](./SUPABASE.md#4-database-table), reload schema; check Logcat `ModuleProgress` for `PGRST204` |
| Module 0 progress dropped back to 0 | `repairFalseIntroComplete` resets a completed Intro when Modules 1–4 are untouched | Known issue — see [KNOWN_ISSUES](./KNOWN_ISSUES.md#progress). Finish Module 0 again |
| Progress stuck at 99% | Assessment not completed yet | Complete the Scenario Assessment to reach 100% |
