# SmartBuild — Release

How to produce a signed release build (APK for sideloading, AAB for Google Play). The
repository currently has **no release signing** configured: every build so far is signed with
the machine's debug key. For store listing requirements see [PLAYSTORE](./PLAYSTORE.md).

---

## 1. Create the release keystore (once)

Use `keytool` from the bundled JDK:

```powershell
& "D:\Porjects\Smartbuild\tools\jdk\jdk-17.0.20.1+1\bin\keytool.exe" -genkeypair -v `
  -keystore D:\SmartBuildKeys\smartbuild-release.jks `
  -alias smartbuild -keyalg RSA -keysize 2048 -validity 10000
```

- Answer the prompts (name, organization, country) and choose strong passwords.
- **Keep the keystore outside the repository** and make at least two offline backups
  (e.g. an encrypted USB drive and a password manager attachment).
- Record: keystore path, keystore password, key alias, key password.

> If this keystore is lost, apps already installed from it can no longer be updated, and a
> Play listing can only continue if Play App Signing holds the app signing key (see §5).

---

## 2. Wire the keystore into Gradle

Create `SmartBuild/keystore.properties` (do **not** commit it):

```properties
storeFile=D:/SmartBuildKeys/smartbuild-release.jks
storePassword=********
keyAlias=smartbuild
keyPassword=********
```

Add to `SmartBuild/.gitignore`:

```gitignore
keystore.properties
*.jks
*.keystore
```

In `SmartBuild/app/build.gradle.kts` add (keep the existing content; add
`import java.util.Properties` at the top if it is not already there):

```kotlin
val keystoreProps = java.util.Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    signingConfigs {
        create("release") {
            storeFile = keystoreProps.getProperty("storeFile")?.let { file(it) }
            storePassword = keystoreProps.getProperty("storePassword")
            keyAlias = keystoreProps.getProperty("keyAlias")
            keyPassword = keystoreProps.getProperty("keyPassword")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // keep the existing release settings (minification stays off)
        }
    }
}
```

Keep minification (R8) **off** unless you add keep rules: Godot and the plugin bridge rely on
reflection (`@UsedByGodot`) and kotlinx-serialization.

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
- [ ] Application ID changed from `com.example.smart_build` (required for Play —
      [MAINTENANCE_GUIDE §8](./MAINTENANCE_GUIDE.md#8-change-the-package-name-application-id))
- [ ] `.pck` re-exported from the current Godot source
- [ ] Production Supabase URL/key in `secrets.properties`
- [ ] Supabase redirect URLs set; `delete-user` function deployed
- [ ] Token logging removed from `SmartBuildBridge.prepare` ([KNOWN_ISSUES](./KNOWN_ISSUES.md#security))
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
- Size: the debug APK is about 146 MB (about 60 MB of it is the Godot `.pck`, plus the Godot
  native library). Check the download size Play reports for the AAB; if it exceeds Play's
  limits, move the `.pck` into a Play Asset Delivery pack.
- Only ARM ABIs are included, which matches almost all real phones.
