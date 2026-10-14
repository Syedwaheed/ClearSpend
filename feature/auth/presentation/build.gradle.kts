

plugins {
    alias(libs.plugins.clearspend.android.ui.feature)
}

android {
    namespace = "com.edu.feature.auth.presentation"


}

dependencies {
    implementation(projects.feature.auth.domain)
    implementation(projects.core.domain)
}