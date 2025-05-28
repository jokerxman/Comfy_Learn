plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-parcelize")
}

android {
    namespace = "com.hompimpa.comfylearn"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hompimpa.comfylearn"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
        buildConfig = true
        mlModelBinding = true
    }
    buildToolsVersion = "35.0.0"
}

dependencies {

    // UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.viewpager2)
    implementation(libs.glide)
    implementation(libs.androidsvg.aar)
    implementation("androidx.core:core-splashscreen:1.0.1")

// Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

// Architecture Components
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment.ktx)

// Authentication
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

// Other
    implementation(libs.tasks.vision)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

// Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}