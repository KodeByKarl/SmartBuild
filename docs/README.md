# SmartBuild — Documentation

SmartBuild is an Android trainer for **TESDA Computer Systems Servicing (CSS) NC II**.
Students sign in, study five modules, and practise each one in a **Guided Simulation**
and a **Scenario Assessment**. Progress is saved per account in Supabase.

The system is a **Kotlin / Jetpack Compose** Android app. An embedded Godot engine serves only
as a supporting render component for the Module 0 slides and the Module 1 3D bench; all logic,
navigation and data stay in Compose.

This folder is the complete handover documentation for the system.

---

## Start here

| If you are… | Read, in order |
|---|---|
| **The client / project owner** | [HANDOVER](./HANDOVER.md) → [BACKEND](./BACKEND.md) → [WORKFLOW](./WORKFLOW.md) → [USER_MANUAL](./USER_MANUAL.md) |
| **A panelist / teacher** | [WORKFLOW](./WORKFLOW.md) → [MODULE_REFERENCE](./MODULE_REFERENCE.md) → [USER_MANUAL](./USER_MANUAL.md) |
| **A student** | [USER_MANUAL](./USER_MANUAL.md) |
| **A developer taking over the code** | [ARCHITECTURE](./ARCHITECTURE.md) → [SETUP_AND_BUILD](./SETUP_AND_BUILD.md) → [ANDROID_APP](./ANDROID_APP.md) → [GODOT_PROJECT](./GODOT_PROJECT.md) → [MAINTENANCE_GUIDE](./MAINTENANCE_GUIDE.md) → [KNOWN_ISSUES](./KNOWN_ISSUES.md) |
| **Whoever publishes the app** | [RELEASE](./RELEASE.md) → [PLAYSTORE](./PLAYSTORE.md) → [SUPABASE](./SUPABASE.md) |

---

## All documents

### Project and handover

| Document | What it covers |
|---|---|
| [HANDOVER.md](./HANDOVER.md) | What was delivered, accounts and credentials to transfer, post-handover checklist |
| [CHANGELOG.md](./CHANGELOG.md) | Version history, including the latest revision round |
| [ADJUSTMENTS.md](./ADJUSTMENTS.md) | Product changes made during development (plain language, Taglish) |

### Users and panel

| Document | What it covers |
|---|---|
| [USER_MANUAL.md](./USER_MANUAL.md) | Step-by-step guide for students and teachers |
| [WORKFLOW.md](./WORKFLOW.md) | Learner flow through Modules 0–4 (Taglish) |
| [MODULE_REFERENCE.md](./MODULE_REFERENCE.md) | Every module in detail: stations, pass conditions, Guided vs Assessment, answer keys |
| [BACKEND.md](./BACKEND.md) | What the cloud side does, in plain language (Taglish) |

### Technical

| Document | What it covers |
|---|---|
| [ARCHITECTURE.md](./ARCHITECTURE.md) | System overview, tech stack, runtime flow, how Compose drives the Godot support layer |
| [SETUP_AND_BUILD.md](./SETUP_AND_BUILD.md) | Tools to install, secrets, building the Godot pack and the APK, tests |
| [ANDROID_APP.md](./ANDROID_APP.md) | Main system code map (Kotlin / Jetpack Compose): navigation, auth, progress, labs |
| [GODOT_PROJECT.md](./GODOT_PROJECT.md) | Supporting Godot content: Module 0 slide visuals, Module 1 3D bench, tools |
| [SUPABASE.md](./SUPABASE.md) | Supabase Auth, `module_progress` table, SQL, Edge Function, troubleshooting |
| [MAINTENANCE_GUIDE.md](./MAINTENANCE_GUIDE.md) | How to make common changes (content, steps, parts, versions, package name) |
| [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) | Build, runtime, Godot, Supabase and device problems with fixes |
| [KNOWN_ISSUES.md](./KNOWN_ISSUES.md) | Known limitations and technical debt, with suggested fixes |

### Publishing

| Document | What it covers |
|---|---|
| [RELEASE.md](./RELEASE.md) | Release signing, AAB, versioning, pre-release checklist |
| [PLAYSTORE.md](./PLAYSTORE.md) | Play Store readiness and listing checklist |

---

## Repositories at a glance

| Folder | What it is |
|---|---|
| `SmartBuild/` | The main system: Android app (Kotlin, Jetpack Compose). Git: `KodeByKarl/SmartBuild` |
| `SmartBuild-Godot/` | Supporting visual content for Modules 0 and 1 (Godot 4.7.2), exported as `SmartBuildGodot.pck`. Git: `KodeByKarl/SmartBuild-Godot` |
| `docs/` | This documentation |
| `tools/` | Bundled JDK 17 and Godot 4.7.2 editor used to build the project |

Older developer notes live in `SmartBuild-Godot/assets/docs/`. Some of them predate the
final revisions; where they disagree, the documents in this folder are authoritative.
