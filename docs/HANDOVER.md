# SmartBuild — Handover

This document lists everything delivered to the client, which accounts and credentials
must change hands, and what the new owner should do after receiving the system.

---

## 1. What is delivered

| Item | Location | Notes |
|---|---|---|
| Android app source (main system) | `SmartBuild/` | Kotlin + Jetpack Compose. Git remote `https://github.com/KodeByKarl/SmartBuild.git`, branch `master` |
| Supporting Godot content source | `SmartBuild-Godot/` | Godot 4.7.2 visual content for Modules 0 and 1. Git remote `https://github.com/KodeByKarl/SmartBuild-Godot.git` |
| Exported support pack | `SmartBuild/app/src/main/assets/SmartBuildGodot.pck` | About 60 MB. Gitignored; published on the GitHub release [v2.1](https://github.com/KodeByKarl/SmartBuild/releases/tag/v2.1) and downloaded automatically by the build |
| **Official APK** | `release/SmartBuild-V2.1.apk` | Release-signed, about 132 MB. Package `com.smartbuild.app`, version `V2.1` (versionCode 3) |
| Release keystore | `release-keys/` (`smartbuild-release.jks` + `keystore.properties`) | Signs every official build. Not in Git — see section 4 |
| Build tools | `tools/jdk/jdk-17.0.20.1+1/`, `tools/godot/Godot_v4.7.2-stable_win64.exe` | Exact tool versions used to build the project |
| Documentation | `docs/` | This folder |
| Password-reset bounce page | `SmartBuild/web/auth-reset.html` | Optional static page for email links (see [SUPABASE](./SUPABASE.md)) |

---

## 2. Accounts and ownership to transfer

| Account | What it holds | How to hand over |
|---|---|---|
| **GitHub** (`KodeByKarl/SmartBuild`, `KodeByKarl/SmartBuild-Godot`) | All source history | Transfer both repositories to the client's GitHub account (Settings → General → Transfer ownership), **or** give the client a full zip of both folders including the `.git` directories |
| **Supabase project** | User accounts (Auth), `module_progress` table, `delete-user` Edge Function | Invite the client as **Owner** of the Supabase organization, or transfer the project to the client's organization (Project Settings → General → Transfer project). If the client prefers a new project, follow [SUPABASE → Moving to a new project](./SUPABASE.md#9-moving-to-a-new-supabase-project) |
| **Google Play Console** | Not created yet | The client should register their own developer account. See [PLAYSTORE](./PLAYSTORE.md) |

---

## 3. Credentials and secrets

| Secret | Where it lives | Status |
|---|---|---|
| Supabase URL + anon (public) key | `SmartBuild/secrets.properties` (gitignored), Godot `config/env.local` (gitignored) | Hand over the file. The anon key is designed to be public and is protected by row-level security (RLS) |
| Supabase `service_role` key | Supabase dashboard only | **Never** put it in the app. Only the `delete-user` Edge Function uses it, server-side |
| Supabase dashboard login | Developer's Supabase account | Replaced by the ownership transfer in section 2 |
| Release keystore (app signing key) + passwords | `release-keys/smartbuild-release.jks`, passwords in `release-keys/keystore.properties` | Hand over privately (not by chat or email in plain text). See section 4 |

> **Note:** the current anon key also appears in the committed files
> `SmartBuild/secrets.properties.example` and `SmartBuild-Godot/config/env.example`.
> This is not a breach (the anon key ships inside every APK anyway), but those example
> files should be changed to placeholders. See [KNOWN_ISSUES](./KNOWN_ISSUES.md#security).

---

## 4. About app signing (important)

The official APK is signed with the release keystore in `release-keys/`
(alias `smartbuild`, RSA 2048, valid until 2054). Certificate SHA-256:

```text
85:E7:2B:9A:E5:22:4D:71:70:98:EB:B6:44:C5:E1:54:44:80:EA:9A:B1:A4:60:2C:62:6A:56:5A:A9:69:14:B9
```

- Android only installs an update over an existing install when **both are signed with the
  same key**. Every future official build must be signed with this keystore.
- **Keep two offline backups** of the `release-keys/` folder (e.g. encrypted USB drive and a
  password manager). Losing it means installed copies and a Play listing can never be updated.
- To build with it, place `keystore.properties` in `SmartBuild/` (adjust `storeFile` if the
  folder moves). Steps: [RELEASE](./RELEASE.md).
- Earlier demo APKs used the package `com.example.smart_build` and a debug key. The official
  app is a **separate app** (`com.smartbuild.app`): uninstall the old demo from phones to
  avoid two SmartBuild icons. Cloud progress carries over after signing in.

---

## 5. Post-handover checklist

Tick these off after receiving the system.

### Source code

- [ ] Both Git repositories received (transferred on GitHub or zipped with `.git`)
- [ ] All local changes committed and pushed. At handover time **both repos had uncommitted
      work**, including the 29 Sep 2026 revisions — run `git status` in each folder
- [ ] `SmartBuildGodot.pck` received or re-exported ([SETUP_AND_BUILD §4](./SETUP_AND_BUILD.md#4-export-the-godot-pack))

### Build

- [ ] A clean machine can build the app by following [SETUP_AND_BUILD](./SETUP_AND_BUILD.md)
- [ ] `org.gradle.java.home` in `SmartBuild/gradle.properties` updated for the new machine
- [ ] Release keystore created and backed up ([RELEASE](./RELEASE.md))

### Backend

- [ ] Client has Owner access to the Supabase project
- [ ] `module_progress` table and RLS policy present ([SUPABASE §4](./SUPABASE.md#4-database-table))
- [ ] `delete-user` Edge Function present (Delete Account depends on it — [SUPABASE §8](./SUPABASE.md#8-edge-function-delete-user))
- [ ] Auth redirect URLs allow `smartbuild://auth`, `smartbuild://auth/confirm`, `smartbuild://auth/reset`
- [ ] Test on a phone: sign up → finish one Guided → the row appears in the Table Editor

### Recommended clean-up

- [ ] Replace real values in the two `*.example` files with placeholders
- [ ] Back up `release-keys/` in two offline places

---

## 6. Quick facts

| Fact | Value |
|---|---|
| App name | SmartBuild |
| Package (application ID) | `com.smartbuild.app` |
| Version | `V2.1` (versionCode 3) |
| Android support | Android 7.0 (API 24) and newer, **arm64-v8a / armeabi-v7a phones only** (no x86 emulators) |
| Orientation | Landscape only |
| Internet | Required. While offline the app shows a blocking "connection lost" dialog until the connection returns |
| Backend | Supabase (Auth + Postgres via PostgREST + one Edge Function) |
| Main system | Kotlin / Jetpack Compose (accounts, navigation, modules, progress, Modules 2–4 labs) |
| Supporting render component | Godot 4.7, embedded as a library for Module 0–1 visuals only (content in `SmartBuildGodot.pck`) |
