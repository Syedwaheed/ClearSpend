plugins {
    alias(libs.plugins.clearspend.android.ui.feature)
}

android {
    namespace = "com.edu.feature.auth.presentation"
}

dependencies {
    implementation(projects.feature.auth.domain)
    implementation(projects.core.domain)

    implementation(libs.androidx.browser)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
