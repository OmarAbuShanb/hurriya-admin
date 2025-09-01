plugins {
    id("android-application-common")
    alias(libs.plugins.kotlin.android)
    id("dev.anonymous.android.application")

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
    implementation(libs.bundles.android.core)

    testImplementation(libs.bundles.unit.tests)
    androidTestImplementation(libs.bundles.android.tests)

    // sdp ssp
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    // Splash API
    implementation(libs.androidx.core.splashscreen)

    // Pdf View
    implementation(libs.pdfview.android)

    // Calendar
    implementation(libs.materialdatetimepicker)

    // Glide for load image from internet
    implementation(libs.glide)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // View Model && Live Data
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Data Store
    implementation(libs.androidx.datastore.preferences)

    // Paging
    implementation(libs.androidx.paging.runtime.ktx)

    // Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Gson
    implementation (libs.gson)

}