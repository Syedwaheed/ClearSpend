plugins {
    alias(libs.plugins.clearspend.android.ui.feature)
}

android {
    namespace = "com.edu.feature.transactions.presentation"


}

dependencies {
    implementation(projects.feature.transactions.domain)
    implementation(projects.core.domain)
}