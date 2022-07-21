plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("maven-publish")
    id("yopdev.sdk-publisher") version "0.0.6"
}

sdkPublisher {
    artifact.id = "digital-payments-sdk"
    artifact.groupId = "wabi2b"
}

repositories {
    val awsMavenUrlRepository: String by project
    gradlePluginPortal()
    mavenCentral()
    maven(awsMavenUrlRepository)
    mavenLocal()
}

dependencies {

    val wiremockVersion: String by project
    val kotlinxSerializationVersion: String by project
    val wabiSerializersVersion: String by project
    val mockitoCoreVersion: String by project
    val junit5Version: String by project
    val testReactorCoreVersion: String by project
    val testReactorNettyVersion: String by project
    val testReactorTestVersion: String by project
    val springWebFluxVersion: String by project
    val springBootVersion: String by project
    val slf4jVersion: String by project
    val logbackVersion: String by project
    val wabiSdkVersion: String by project

    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("com.wabi2b:serializers:$wabiSerializersVersion")
    implementation("org.springframework:spring-webflux:$springWebFluxVersion")
    api("yopdev.wabi:sdk:$wabiSdkVersion") {
        exclude("org.jetbrains.kotlin:kotlin-reflect")
    }

    // logging
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation(project(":api")) {
        exclude("wabi2b", "wabi2b-authorizer")
    }
    testImplementation(kotlin("test-junit"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("com.github.tomakehurst:wiremock:$wiremockVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoCoreVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoCoreVersion")
    testImplementation("io.projectreactor:reactor-core:$testReactorCoreVersion")
    testImplementation("io.projectreactor.netty:reactor-netty:$testReactorNettyVersion")
    testImplementation("io.projectreactor:reactor-test:$testReactorTestVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
}
