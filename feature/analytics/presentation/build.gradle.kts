

plugins {
    alias(libs.plugins.clearspend.android.ui.feature)
}

android {
    namespace = "com.edu.feature.analytics.presentation"


}

dependencies {
    implementation(projects.feature.analytics.domain)
    implementation(projects.core.domain)
}