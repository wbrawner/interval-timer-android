apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'

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
        minSdkVersion 28
        targetSdkVersion 30
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
    buildFeatures {
        compose true
    }
    composeOptions {
        kotlinCompilerExtensionVersion compose_version
        kotlinCompilerVersion '1.4.10'
    }
}

dependencies {
    implementation project(':shared')
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation 'androidx.wear:wear:1.1.0'
    implementation 'com.google.android.support:wearable:2.8.1'
    implementation 'androidx.appcompat:appcompat:1.2.0'
    compileOnly 'com.google.android.wearable:wearable:2.8.1'
    testImplementation "junit:junit:$junit_version"
}