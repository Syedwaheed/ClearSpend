plugins {
    alias(libs.plugins.clearspend.android.ui.feature)
}

android {
    namespace = "com.edu.feature.payments.presentation"


}

dependencies {
    implementation(projects.feature.payments.domain)
    implementation(projects.core.domain)
}