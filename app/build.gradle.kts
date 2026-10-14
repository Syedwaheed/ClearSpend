

plugins {
    alias(libs.plugins.clearspend.android.application.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.clearspend.jvm.ktor)
    alias(libs.plugins.firebase.distribution)
}

android {
    namespace = "com.edu.clearspend"


}



dependencies {
    //Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    //Coil
    implementation(libs.coil.compose)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    //Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    //KotlinxSerialization
    implementation(libs.kotlinx.serialization.json)

    // Koin DI with Compose support
    implementation(libs.bundles.koin.compose)

    //tink
    implementation(libs.androidx.google.tink)
    //DataStore
    implementation(libs.androidx.datastore.preference)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //Splash Screen
    implementation(libs.androidx.core.splashscreen)

    //Timber
    implementation(libs.timber)

    //Firebase
    implementation(platform(libs.firebase.bom))


    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.core.network)
    implementation(projects.core.presentation.ui)
    implementation(projects.core.presentation.designsystem)

    implementation(projects.feature.auth.data)
    implementation(projects.feature.auth.domain)
    implementation(projects.feature.auth.presentation)

    implementation(projects.feature.accounts.data)
    implementation(projects.feature.accounts.domain)
    implementation(projects.feature.accounts.presentation)

    implementation(projects.feature.analytics.data)
    implementation(projects.feature.analytics.domain)
    implementation(projects.feature.analytics.presentation)

    implementation(projects.feature.dashboard.data)
    implementation(projects.feature.dashboard.domain)
    implementation(projects.feature.dashboard.presentation)

    implementation(projects.feature.payments.data)
    implementation(projects.feature.payments.domain)
    implementation(projects.feature.payments.presentation)

    implementation(projects.feature.transactions.data)
    implementation(projects.feature.transactions.domain)
    implementation(projects.feature.transactions.presentation)
}
