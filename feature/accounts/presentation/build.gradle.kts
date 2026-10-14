

plugins {
    alias(libs.plugins.clearspend.android.ui.feature)
}

android {
    namespace = "com.edu.feature.accounts.presentation"


}

dependencies {
    implementation(projects.feature.accounts.domain)
    implementation(projects.core.domain)
}