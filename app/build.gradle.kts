plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.umg.mysportzac"
    compileSdk = 34

   buildFeatures {
       viewBinding = true
   }
    dataBinding {
        enable = true
    }

    defaultConfig {
        applicationId = "com.umg.mysportzac"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.compose.foundation:foundation-layout-android:1.6.4")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("androidx.activity:activity:1.8.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-firestore:24.10.2")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.facebook.android:facebook-login:latest.release")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.facebook.android:facebook-android-sdk:9.1.1")
    //implementation("androidx.core:core-ktx:2.2.0")
    //seekBar circle, doc oficial https://github.com//tabkery/CircularSeekBar
    implementation ("me.tankery.lib:circularSeekBar:1.3.2")
    implementation ("com.google.android.gms:play-services-location:17.0.0")

    //camara
    implementation ("androidx.camera:camera-view:1.0.0-alpha23")
    implementation("androidx.camera:camera-core:1.0.1")
    implementation("androidx.camera:camera-camera2:1.0.1")
// If you want to additionally use the CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:1.0.1")
// If you want to additionally use the CameraX View class
    implementation("androidx.camera:camera-view:1.0.0-alpha27")
// If you want to additionally use the CameraX Extensions library
    implementation("androidx.camera:camera-extensions:1.0.0-alpha27")

    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

}