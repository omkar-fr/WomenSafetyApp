plugins {
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.womensafety" // Replace with your actual package name
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.womensafety" // Ensure this matches your package
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }

    kotlin {
        jvmToolchain(22)
    }
}

dependencies {
    implementation("androidx.compose.compiler:compiler:1.5.11")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui:1.7.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.0")
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.junit.ktx)
    testImplementation(libs.junit.junit)
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}