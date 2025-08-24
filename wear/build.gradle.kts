plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ontrek.wear"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ontrek"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.wear.ongoing)
    implementation(libs.core)
    implementation(libs.wear)
    implementation(libs.room.runtime)
    implementation(libs.compose.material)
    ksp(libs.room.compiler)
    implementation(libs.play.services.location)
    implementation(libs.material.icons.extended)
    implementation(libs.android.gpx.parser)
    implementation(libs.datastore.preferences)
    implementation(libs.fragment.ktx)
    implementation(libs.play.services.wearable)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.runtime.livedata)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.navigation.compose)
    implementation(libs.play.services.wearable)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.runtime)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.compose.material3)
    implementation(libs.material3.android)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}