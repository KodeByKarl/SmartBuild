# SmartBuild — Release

How to produce the official signed build (APK for sideloading, AAB for Google Play). Release
signing is set up as of **V2.1**. For store listing requirements see
[PLAYSTORE](./PLAYSTORE.md).

---

## 1. The release keystore

Already created (29 Sep 2026) — **do not create a new one**, or phones with the official app
will refuse the update.

| Item | Value |
|---|---|
| Folder | `release-keys/` (workspace root, outside Git) |
| Keystore | `smartbuild-release.jks` (PKCS12, RSA 2048, valid until 2054) |
| Alias | `smartbuild` |
| Passwords | In `release-keys/keystore.properties` (store and key password are the same) |
| Certificate SHA-256 | `85:E7:2B:9A:E5:22:4D:71:70:98:EB:B6:44:C5:E1:54:44:80:EA:9A:B1:A4:60:2C:62:6A:56:5A:A9:69:14:B9` |

- Keep **two offline backups** of `release-keys/` (e.g. encrypted USB drive and a password
  manager attachment). Never commit it or send the passwords in plain chat/email.

> If this keystore is lost, apps already installed from it can no longer be updated, and a
> Play listing can only continue if Play App Signing holds the app signing key (see §5).

Only if you truly need a brand-new key (new app / new package name):

```powershell
& "D:\Porjects\Smartbuild\tools\jdk\jdk-17.0.20.1+1\bin\keytool.exe" -genkeypair -v `
  -keystore D:\SmartBuildKeys\smartbuild-release.jks -storetype PKCS12 `
  -alias smartbuild -keyalg RSA -keysize 2048 -validity 10000
```

---

## 2. How Gradle uses it

`SmartBuild/app/build.gradle.kts` reads `SmartBuild/keystore.properties` (gitignored, together
with `*.jks` / `*.keystore`):

```properties
storeFile=D:/Porjects/Smartbuild/release-keys/smartbuild-release.jks
storePassword=********
keyAlias=smartbuild
keyPassword=********
```

- On a new machine: copy `release-keys/keystore.properties` into `SmartBuild/` and fix
  `storeFile` if the folder is somewhere else.
- If the file (or the keystore it points to) is missing, the build still works: debug builds
  use the debug key, and release builds come out **unsigned** (not installable) — that is the
  signal that the keystore is not set up.

Minification (R8) stays **off**: Godot and the plugin bridge rely on reflection
(`@UsedByGodot`) and kotlinx-serialization.

---

## 3. Build

Before building:

1. Bump `versionCode` (always higher than the last upload) and `versionName` in
   `app/build.gradle.kts`.
2. If Godot changed: run `regress_probe.gd` and re-export the `.pck`.
3. Make sure `secrets.properties` points to the **production** Supabase project.

```powershell
cd D:\Porjects\Smartbuild\SmartBuild
$env:JAVA_HOME = "D:\Porjects\Smartbuild\tools\jdk\jdk-17.0.20.1+1"
.\gradlew.bat :app:assembleRelease   # signed APK → app\build\outputs\apk\release\app-release.apk
.\gradlew.bat :app:bundleRelease     # signed AAB → app\build\outputs\bundle\release\app-release.aab
```

Verify the signature of an APK:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\<version>\apksigner.bat" verify --print-certs app\build\outputs\apk\release\app-release.apk
```

---

## 4. Pre-release checklist

- [ ] `versionCode` / `versionName` bumped and [CHANGELOG](./CHANGELOG.md) updated
- [ ] Application ID is still `com.smartbuild.app`
- [ ] `SmartBuild/keystore.properties` present; `apksigner` shows the certificate SHA-256 from §1
- [ ] `.pck` re-exported from the current Godot source
- [ ] Production Supabase URL/key in `secrets.properties`
- [ ] Supabase redirect URLs set; `delete-user` function deployed
- [ ] Smoke test on a real phone (below)

### Smoke test (about 15 minutes)

1. Fresh install → sign up → confirm email (if enabled) → sign in.
2. Home shows five modules; "Module 1 simulation ready" appears.
3. Module 0: page through a few slides, go Back, reopen.
4. Module 1 Guided: finish Phase 01 and do a few Phase 02 removals (motherboard comes out with
   RAM/CPU fan/CPU).
5. Module 2 Guided: the Back button works on every station.
6. Module 3 Guided: type `\\FileServer\DeptShares\HR` in the PC File Explorer — no crash.
7. Module 4 Guided: type `ping 192.168.1.1` in CMD.
8. Component Search: search "RAM", open the detail, zoom the image.
9. Turn on airplane mode → connection-lost dialog appears → turn it off → dialog closes.
10. Sign out → sign in again → progress is still there.

---

## 5. Google Play specifics

- Upload the **AAB**, not the APK.
- Enrol in **Play App Signing** (default for new apps): Google keeps the app signing key and
  your keystore becomes the *upload key*. A lost upload key can then be reset through Play
  support.
- Size: the V2.1 release APK is about 132 MB (about 60 MB of it is the Godot `.pck`, plus the Godot
  native library). Check the download size Play reports for the AAB; if it exceeds Play's
  limits, move the `.pck` into a Play Asset Delivery pack.
- Only ARM ABIs are included, which matches almost all real phones.
