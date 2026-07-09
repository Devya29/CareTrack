plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.maptrackapplication"
    compileSdk = 36   // ✅ fixed

    defaultConfig {
        applicationId = "com.example.maptrackapplication"
        minSdk = 24
        targetSdk = 34    // fine to keep 34
        versionCode = 1
        versionName = "1.0"   // ✅ removed stray 'a'
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    // 🔥 Lifecycle (MVVM)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime:2.6.2")

    // 🔥 Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // 🔥 Maps (OSMDroid)
    implementation("org.osmdroid:osmdroid-android:6.1.16")

    // 🔥 Networking
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // 🔥 Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-database")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}