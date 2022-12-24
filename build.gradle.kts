// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    ext {
        arch_version = "2.1.0"
        compose_version = '1.0.0-beta02'
        dagger_version = '2.33'
        junit_version = '4.13.2'
        kotlin_version = '1.4.31'
        lifecycle_version = "2.3.0"
        nav_version = "2.3.3"
        room_version = '2.2.6'
        wearable_play_services = '17.0.0'
    }

    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath 'com.android.tools.build:gradle:7.3.1'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "com.google.dagger:hilt-android-gradle-plugin:$dagger_version"

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
