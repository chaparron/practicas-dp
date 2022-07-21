rootProject.name = "wabi2b-digital-payments"

pluginManagement {

    plugins {
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion apply false
        kotlin("stdlib-jdk8") version kotlinVersion apply false
        kotlin("plugin.serialization") version kotlinVersion apply false
    }

    repositories {
        val awsMavenUrlRepository: String by settings
        gradlePluginPortal()
        mavenLocal()
        maven(awsMavenUrlRepository)
    }
}

include("api")
include("sdk")