plugins {
    alias(libs.plugins.clearspend.android.data)
    alias(libs.plugins.clearspend.android.room)
}

android {
    namespace = "com.edu.core.database"


}

dependencies {
    implementation(projects.core.domain)
}