plugins {
    alias(libs.plugins.clearspend.android.ui.feature)
}

android {
    namespace = "com.edu.feature.dashboard.presentation"


}

dependencies {
    implementation(projects.feature.dashboard.domain)
    implementation(projects.core.domain)
}