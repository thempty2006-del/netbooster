pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // AndroidLibXrayLite / libv2ray publishes releases as AAR on GitHub / jitpack.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "NetBooster"
include(":app")
