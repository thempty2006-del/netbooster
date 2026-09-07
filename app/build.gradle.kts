plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.netbooster.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.netbooster.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-service:2.8.4")

    // --- Xray core bindings ---
    // This is the piece that actually runs the Xray protocol engine (Reality/XHTTP/Vision,
    // fragment, sockopt, etc.) inside the app. It is a prebuilt native library, published by
    // the AndroidLibXrayLite project (same one v2rayNG uses), so it must be fetched from the
    // network — I could not download it in this offline environment.
    //
    // Steps for you to finish this (one-time, needs internet on your build machine):
    //   1) Go to https://github.com/2dust/AndroidLibXrayLite releases and grab the latest
    //      libv2ray.aar (or build it yourself with the Go toolchain per that repo's README).
    //   2) Put the .aar file in app/libs/libv2ray.aar
    //   3) Uncomment the line below.
    //
    // implementation(files("libs/libv2ray.aar"))
}
