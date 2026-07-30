import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "de.mathiiis.notes"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.mathiiis.notes"
        minSdk = 24

        targetSdk = 36

        versionCode = (providers.gradleProperty("versionCode").orNull ?: "1").toInt()
        versionName = providers.gradleProperty("versionName").orNull ?: "1.0.0"
    }

    signingConfigs {
        create("release") {
            val storePath =
                providers.gradleProperty("RELEASE_STORE_FILE").orNull
                    ?: System.getenv("RELEASE_STORE_FILE")
            if (storePath != null) {
                storeFile = file(storePath)
                storePassword =
                    providers.gradleProperty("RELEASE_STORE_PASSWORD").orNull
                        ?: System.getenv("RELEASE_STORE_PASSWORD")
                keyAlias =
                    providers.gradleProperty("RELEASE_KEY_ALIAS").orNull
                        ?: System.getenv("RELEASE_KEY_ALIAS")
                keyPassword =
                    providers.gradleProperty("RELEASE_KEY_PASSWORD").orNull
                        ?: System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            val hasKeystore =
                providers.gradleProperty("RELEASE_STORE_FILE").orNull != null ||
                    System.getenv("RELEASE_STORE_FILE") != null
            signingConfig = if (hasKeystore) signingConfigs.getByName("release") else null

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes +=
            setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
            )
    }

    lint {
        warningsAsErrors = false
        abortOnError = true
        checkDependencies = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    // ==== core + lifecycle ====
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.activity:activity-compose:1.13.0")

    // ==== compose ====
    val composeBom = platform("androidx.compose:compose-bom:2026.06.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.9.8")

    // ==== material 3 expressive ====
    implementation("androidx.compose.material3:material3:1.5.0-alpha12")
    implementation("androidx.graphics:graphics-shapes:1.1.0")

    // ==== room ====
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    // ==== markdown + images ====
    implementation("com.mikepenz:multiplatform-markdown-renderer:0.38.1")
    implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.38.1")
    implementation("com.mikepenz:multiplatform-markdown-renderer-coil3:0.38.1")

    // ==== debug tooling ====
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ==== tests ====
    testImplementation("junit:junit:4.13.2")
}
