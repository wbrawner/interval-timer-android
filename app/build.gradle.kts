plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-kapt'
    id 'dagger.hilt.android.plugin'
}

def keystoreProperties = new Properties()
try {
    def keystorePropertiesFile = rootProject.file("keystore.properties")
    keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
} catch (FileNotFoundException ignored) {
    logger.warn("Unable to load keystore properties. Using debug signing configuration instead")
    keystoreProperties['keyAlias'] = "androiddebugkey"
    keystoreProperties['keyPassword'] = "android"
    keystoreProperties['storeFile'] = new File(System.getProperty("user.home"), ".android/debug.keystore").absolutePath
    keystoreProperties['storePassword'] = "android"
}

android {
    compileSdkVersion 30

    defaultConfig {
        applicationId "com.wbrawner.trainterval"
        minSdkVersion 23
        targetSdkVersion 30
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += [
                        "room.schemaLocation"  : "$projectDir/schemas".toString(),
                        "room.incremental"     : "true",
                        "room.expandProjection": "true"]
            }
        }
        signingConfig signingConfigs.debug
    }
    signingConfigs {
        debug {
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
        }
        release {
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
        }
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    buildFeatures {
        compose true
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

kapt {
    correctErrorTypes true
}

dependencies {
    implementation project(':shared')
    wearApp project(':wear')
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"

    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'androidx.core:core-ktx:1.3.2'
    implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
    implementation 'androidx.transition:transition-ktx:1.4.0'
    implementation 'com.google.android.material:material:1.4.0-alpha01'
    implementation 'androidx.recyclerview:recyclerview:1.1.0'

    implementation 'androidx.legacy:legacy-support-v4:1.0.0'

    implementation "androidx.navigation:navigation-compose:1.0.0-alpha09"
    implementation "dev.chrisbanes.accompanist:accompanist-insets:0.6.2"

    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-common-java8:$lifecycle_version"
    testImplementation "androidx.arch.core:core-testing:$arch_version"

    implementation "com.google.dagger:hilt-android:$dagger_version"
    kapt "com.google.dagger:hilt-compiler:$dagger_version"
    androidTestImplementation  "com.google.dagger:hilt-android-testing:$dagger_version"
    kaptAndroidTest "com.google.dagger:hilt-compiler:$dagger_version"
    testImplementation "com.google.dagger:hilt-android-testing:$dagger_version"
    kaptTest "com.google.dagger:hilt-compiler:$dagger_version"

    implementation 'com.robinhood.ticker:ticker:2.0.2'

    testImplementation "junit:junit:$junit_version"
    androidTestImplementation 'androidx.test.ext:junit:1.1.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
}
