plugins {
    alias(libs.plugins.clearspend.android.data)
}

android {
    namespace = "com.edu.core.data"
}

dependencies {
    implementation(libs.timber)
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(libs.androidx.google.tink)
    implementation(libs.androidx.datastore.preference)
}