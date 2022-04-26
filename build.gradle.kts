buildscript {
    repositories {
        gradlePluginPortal()
        google()
    }
    dependencies {
        val kotlinVersion = "1.6.10"
        val gradleVersion = "7.1.2"

        classpath("com.android.tools.build:gradle:$gradleVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}