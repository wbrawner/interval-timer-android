import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*

plugins {
    id("com.android.application")
    id("kotlin-android")
}

val keystoreProperties = Properties()
try {
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
} catch (ignored: FileNotFoundException) {
    logger.warn("Unable to load keystore properties. Using debug signing configuration instead")
    keystoreProperties["keyAlias"] = "androiddebugkey"
    keystoreProperties["keyPassword"] = "android"
    keystoreProperties["storeFile"] =
        File(System.getProperty("user.home"), ".android/debug.keystore").absolutePath
    keystoreProperties["storePassword"] = "android"
}

android {
    compileSdk = libs.versions.maxSdk.get().toInt()
    defaultConfig {
        applicationId = "com.wbrawner.trainterval"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.maxSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs["debug"]
    }
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"].toString()
            keyPassword = keystoreProperties["keyPassword"].toString()
            storeFile = file(keystoreProperties["storeFile"].toString())
            storePassword = keystoreProperties["storePassword"].toString()
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs["release"]
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
//    javaCompileOptions {
//        annotationProcessorOptions {
//            arguments += [
//                    "room.schemaLocation"  : "$projectDir/schemas".toString(),
//                    "room.incremental"     : "true",
//                    "room.expandProjection": "true"]
//        }
//    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    namespace = "com.wbrawner.trainterval"
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.wear)
    implementation(libs.support.wearable)
    implementation(libs.androidx.appcompat)
    compileOnly(libs.wearable.wearable)
    testImplementation(libs.junit)
}