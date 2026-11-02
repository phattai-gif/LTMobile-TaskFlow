// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    id("com.google.gms.google-services") version "4.5.0" apply false
}

// Đổi thư mục build ra ngoài C:/AndroidBuildCache để tránh lỗi OneDrive khóa file
val baseBuildDir = file("C:/AndroidBuildCache/${rootProject.name}")
allprojects {
    layout.buildDirectory.set(baseBuildDir.resolve(name))
}