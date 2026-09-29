# SmartBuild — Changelog

Newest first. "App" means the Android repository (`SmartBuild`), "Godot" means the Godot
repository (`SmartBuild-Godot`). Short hashes refer to Git commits.

---

## 29 Sep 2026 — Client revision round (still `V2`, versionCode 2)

Not yet committed at handover. Commit these in both repositories, and bump the version
before the next release ([MAINTENANCE_GUIDE §7](./MAINTENANCE_GUIDE.md#7-bump-the-version)).

### Fixed

- **Module 3 crashed after typing a path in the PC File Explorer** (Guided and Assessment).
  The path normalizer used a regex replacement string with an unescaped backslash.
  *App:* `screens/serverlab/ServerLabScreen.kt`.
- **Module 2 had no exit button.** The three stations (cable intro, crimp lab, topology) now
  get a Back action; the top-bar Back button and the phone's back gesture both work.
  *App:* `CableIntroScreen.kt`, `CrimpLabScreen.kt`, `NetworkTopologyScreen.kt`,
  `ComposeModuleScreen.kt`.
- **Module 1 disassembly: RAM, CPU fan and CPU stayed inside the case after the motherboard
  was removed.** The motherboard now comes out to the front of the case with those parts
  still mounted; the learner removes them there. The board disappears after the CPU is
  removed. *Godot:* `core/simulation/pc_build_bench.gd`, tip text in
  `scripts/module_content_registry.gd`.

### Removed

- **Search button inside Module 0.** Component Search is still available from the app's Home
  screen. *Godot:* `modules/module_0/Main.tscn`, `modules/module_0/Main.gd`,
  `slides/lesson_content.gd` (help text).

### Build

- `SmartBuildGodot.pck` re-exported (60,401,260 bytes). The previous pack is backed up as
  `SmartBuildGodot.pck.bak-2026-09-29` in the workspace root.
- Module 1 regression probe: 0 problems.

---

## 16 Sep 2026 — `V2`

- *App* `58ef363`: version bumped to V2 (versionCode 2).

## 9 Sep 2026

- *App* `1ee2f7e`: login screen readability; progress is stored **per account**, so a new
  sign-in never inherits another student's Home state.
- *Godot* `e40ac6d`: TESDA disassembly-then-rebuild assessment for Module 1; Module 0
  rebuilt as 2D slides.

## 4 Sep 2026

- *App* `7deb22f`: Assessment unlocks after Guided is complete; progress holds at 99% until
  the Assessment is finished.
- *Godot* `2eafa8c`: Modules 1–4 shell content with the same-path Guided vs Assessment flow.

## 20 Aug 2026

- *Godot* `0b29499`: initial upload of the Godot project.

## 18 Aug 2026

- *App* `d74048c`: Godot simulations integrated into the app.

## 5 Aug 2026

- *App* `8700635`: first Compose screens (partial).

---

For the design changes made over the project (playground-first labs, Module 4 desktop,
coaching, cleanup), see [ADJUSTMENTS](./ADJUSTMENTS.md).
