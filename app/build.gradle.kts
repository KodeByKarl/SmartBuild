import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

fun loadSmartBuildSecrets(): Properties {
    val props = Properties()
    val file = rootProject.file("secrets.properties")
    if (file.exists()) {
        file.inputStream().use { props.load(it) }
    }
    return props
}

fun secretOrEnv(props: Properties, key: String, default: String = ""): String {
    val fromEnv = System.getenv(key)?.trim().orEmpty()
    if (fromEnv.isNotEmpty()) {
        return fromEnv
    }
    return props.getProperty(key, default)?.trim().orEmpty()
}

fun escapeBuildConfigString(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
}

val smartBuildSecrets = loadSmartBuildSecrets()
val supabaseUrl = secretOrEnv(smartBuildSecrets, "SUPABASE_URL")
val supabaseAnonKey = secretOrEnv(smartBuildSecrets, "SUPABASE_ANON_KEY")
val supabaseAuthScheme = secretOrEnv(smartBuildSecrets, "SUPABASE_AUTH_SCHEME", "smartbuild")
val supabaseAuthHost = secretOrEnv(smartBuildSecrets, "SUPABASE_AUTH_HOST", "auth")

if (supabaseUrl.isEmpty() || supabaseAnonKey.isEmpty()) {
    logger.warn(
        "SmartBuild: SUPABASE_URL / SUPABASE_ANON_KEY missing. " +
            "Copy secrets.properties.example → secrets.properties or set CI env vars."
    )
}

android {
    namespace = "com.example.smart_build"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.smart_build"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${escapeBuildConfigString(supabaseUrl)}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${escapeBuildConfigString(supabaseAnonKey)}\"")
        buildConfigField("String", "SUPABASE_AUTH_SCHEME", "\"${escapeBuildConfigString(supabaseAuthScheme)}\"")
        buildConfigField("String", "SUPABASE_AUTH_HOST", "\"${escapeBuildConfigString(supabaseAuthHost)}\"")

        // The default ignore pattern for the 'assets' directory includes hidden files and
        // directories which are used by Godot projects, so we override it with the following.
        aaptOptions {
            ignoreAssetsPattern = "!.svn:!.git:!.gitignore:!.ds_store:!*.scc:<dir>_*:!CVS:!thumbs.db:!picasa.ini:!*~"
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation(platform("io.github.jan-tennert.supabase:bom:3.7.0"))
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:functions-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("io.ktor:ktor-client-android:3.5.2")
    implementation("androidx.navigation:navigation-compose:2.9.8")
//    implementation("org.godotengine:godot:4.7.stable")
    implementation("org.godotengine:godot:4.7.0.stable")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation(libs.androidx.compose.material3.ripple)
    implementation("androidx.compose.material3:material3-window-size-class:1.4.0")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

val godotPack = file("src/main/assets/SmartBuildGodot.pck")

tasks.register("checkGodotPack") {
    group = "verification"
    description = "Fails the build if the embedded Godot pack is missing."
    doLast {
        if (!godotPack.exists() || godotPack.length() < 1024L) {
            throw GradleException(
                "Missing SmartBuildGodot.pck (${godotPack.path}). " +
                    "Export it from SmartBuild-Godot: .\\tools\\export_android_pck.ps1"
            )
        }
    }
}

tasks.named("preBuild") {
    dependsOn("checkGodotPack")
}
