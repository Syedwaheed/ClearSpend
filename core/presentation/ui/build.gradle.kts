plugins {
    alias(libs.plugins.clearspend.android.library.compose)
}

android {
    namespace = "com.edu.core.presentation.ui"
}

dependencies {
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)

    implementation(projects.core.domain)
    implementation(projects.core.presentation.designsystem)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}