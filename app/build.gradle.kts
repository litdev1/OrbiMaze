plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.litdev.orbimaze"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.litdev.orbimaze"
        minSdk = 24
        targetSdk = 35
        versionCode = 5
        versionName = "1.0"
        ndk { debugSymbolLevel = "FULL" }

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // SceneView
    implementation("io.github.sceneview:sceneview:2.2.1")


    // Filament
    implementation("com.google.android.filament:filament-android:1.52.0")
    implementation("com.google.android.filament:filament-utils-android:1.52.0")
    implementation("com.google.android.filament:gltfio-android:1.52.0")
}