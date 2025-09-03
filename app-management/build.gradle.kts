plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "dev.anonymous.hurriya.admin"

    defaultConfig {
        applicationId = "dev.anonymous.hurriya.admin"
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
}

dependencies {
    implementation(libs.bundles.core)

    testImplementation(libs.bundles.unit.tests)
    androidTestImplementation(libs.bundles.android.tests)

    implementation(libs.glide)
    ksp(libs.glide.compiler)

    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    implementation(libs.bundles.media)
    implementation(libs.bundles.jetpack)
    implementation(libs.bundles.room)
    implementation(libs.bundles.ui.extras)
    implementation(libs.bundles.utils)

    implementation(project(":core:domain"))
}