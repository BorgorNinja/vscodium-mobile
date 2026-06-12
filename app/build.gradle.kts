import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// ─── Version (injected by CI, falls back to defaults locally) ────────────────
val ciVersionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
val ciVersionName = (project.findProperty("versionName") as String?) ?: "0.1.0-dev"

// ─── Signing ─────────────────────────────────────────────────────────────────
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) load(FileInputStream(keystorePropsFile))
}
val hasKeystore = keystorePropsFile.exists() &&
    keystoreProps.getProperty("storePassword", "").isNotEmpty()

android {
    namespace   = "dev.vscodium.mobile"
    compileSdk  = 35

    defaultConfig {
        applicationId = "dev.vscodium.mobile"
        minSdk        = 26
        targetSdk     = 35
        versionCode   = ciVersionCode
        versionName   = ciVersionName
    }

    signingConfigs {
        create("release") {
            storeFile     = file(keystoreProps.getProperty("storeFile",     "vscodium-mobile.jks"))
            storePassword = keystoreProps.getProperty("storePassword", "")
            keyAlias      = keystoreProps.getProperty("keyAlias",      "vscodiummobile")
            keyPassword   = keystoreProps.getProperty("keyPassword",   "")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            signingConfig     = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isDebuggable = true
            if (hasKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose     = true
        buildConfig = true
        viewBinding = false
        dataBinding = false
        aidl        = false
        renderScript = false
        resValues    = false
        shaders      = false
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/*.txt",
                "/kotlin/**.kotlin_builtins"
            )
        }
    }

    splits {
        abi {
            isEnable        = false
            isUniversalApk  = true
        }
    }

    lint {
        abortOnError = false
        quiet        = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons.ext)
    implementation(libs.activity.compose)

    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
    implementation(libs.documentfile)
}
