plugins {
    id("com.android.library")
    id("kotlin-android")
    alias(libs.plugins.ksp)
}

android {
    compileSdk = libs.versions.maxSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.maxSdk.get().toInt()
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    namespace = "com.wbrawner.trainterval.shared"
}


dependencies {
    api(libs.bundles.coroutines)
    api(libs.bundles.compose)
    debugApi(libs.kotlin.reflect)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    api(libs.room.runtime)
    testImplementation(libs.room.testing)
    api(libs.play.wearable)
    api(libs.timber)
    testImplementation(libs.junit)
    androidTestImplementation(libs.test.ext)
    androidTestImplementation(libs.espresso)
}
