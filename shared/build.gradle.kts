apply plugin: 'com.android.library'
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-kapt'

android {
    compileSdkVersion 30

    defaultConfig {
        minSdkVersion 23
        targetSdkVersion 30
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles "consumer-rules.pro"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
    buildFeatures {
        compose true
    }
    composeOptions {
        kotlinCompilerExtensionVersion compose_version
    }
}

dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    api 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3'
    api 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2'
    api 'org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.3.5'
    api "androidx.compose.ui:ui:$compose_version"
    api "androidx.compose.foundation:foundation:$compose_version"
    api "androidx.compose.material:material:$compose_version"
    api "androidx.compose.material:material-icons-core:$compose_version"
    api "androidx.compose.material:material-icons-extended:$compose_version"
    api "androidx.compose.runtime:runtime-livedata:$compose_version"
    androidTestApi "androidx.compose.ui:ui-test-junit4:$compose_version"
    api 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3'
    api 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2'
    api 'org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.3.5'
    debugApi "androidx.compose.ui:ui-tooling:$compose_version"
    debugApi "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"
    implementation 'androidx.core:core-ktx:1.3.2'
    implementation 'androidx.appcompat:appcompat:1.2.0'
    kapt "androidx.room:room-compiler:$room_version"
    implementation "androidx.room:room-ktx:$room_version"
    testImplementation "androidx.room:room-testing:$room_version"
    api "androidx.room:room-runtime:$room_version"
    api "com.google.android.gms:play-services-wearable:$wearable_play_services"
    api 'com.jakewharton.timber:timber:4.7.1'
    testImplementation "junit:junit:$junit_version"
    androidTestImplementation 'androidx.test.ext:junit:1.1.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
}
