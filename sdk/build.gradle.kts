plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("maven-publish")
    id("yopdev.sdk-publisher") version "0.0.7"
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
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.wabi2b:serializers:1007-8ea7571732f9dbdcca033fa025fe06e3f5179fa8")
    implementation("org.springframework:spring-webflux:5.1.9.RELEASE")
    implementation("yopdev.wabi:sdk:1014-1c81da2cc47e27e789a8829421b38b1520957872")

    // logging
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-core:1.2.9")
    implementation("ch.qos.logback:logback-classic:1.2.9")

    testImplementation(project(":api")) {
        exclude("wabi2b", "wabi2b-authorizer")
    }
    testImplementation(kotlin("test-junit"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("com.github.tomakehurst:wiremock:2.24.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.0.0")
    testImplementation("io.projectreactor:reactor-core:3.4.14")
    testImplementation("io.projectreactor.netty:reactor-netty:1.0.15")
    testImplementation("io.projectreactor:reactor-test:3.2.11.RELEASE")
}
