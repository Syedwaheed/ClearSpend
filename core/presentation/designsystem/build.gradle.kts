plugins {
    alias(libs.plugins.clearspend.android.library.compose)
}

android {
    namespace = "com.edu.core.presentation.designsystem"


}

dependencies {
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3.adaptive)
    api(libs.androidx.compose.material3)
    // Coil — poster image loading
    implementation(libs.coil.compose)
}