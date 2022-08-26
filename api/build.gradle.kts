plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "wabi2b.digital-payments"
version = "1.0-SNAPSHOT"

repositories {
    val awsMavenUrlRepository: String by project
    gradlePluginPortal()
    mavenCentral()
    maven(awsMavenUrlRepository)
    mavenLocal()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    kotlinOptions.jvmTarget = "11"
//}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
}

dependencies {

    val wabiAuthorizerVersion: String by project
    val wabiAuthSdkVersion: String by project
    val wabiSerializersVersion: String by project
    val wabiApiSdkVersion: String by project

    val kotlinxSerializationVersion: String by project

    val awsSdkBomVersion: String by project
    val awsLambdaCoreVersion: String by project
    val awsLambdaTestVersion: String by project

    val awsXrayRecorderVersion: String by project
    val awsDynamoDbVersion: String by project
    val awsStepFunctionVersion: String by project

    val slf4jVersion: String by project
    val logbackVersion: String by project
    val authZeroClientVersion: String by project

    val newRelicLambdaVersion: String by project
    val newRelicAwsLambdaVersion: String by project
    val opentracingUtilVersion: String by project

    val mockitoCoreVersion: String by project
    val mockWebServerVersion: String by project
    val testContainersVersion: String by project
    val junit5Version: String by project
    val assertJVersion: String by project
    val wiremockVersion: String by project
    val testReactorCoreVersion: String by project
    val testReactorNettyVersion: String by project
    val testReactorTestVersion: String by project

    implementation("com.newrelic.agent.java:newrelic-api:5.14.0")

    // kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("com.wabi2b:serializers:$wabiSerializersVersion")

    // wabi2b jpmc Sdk
    implementation("wabi2b:jpmc-sdk:1011-2a0d28df3b29381ebc3d4ae22f015553b229180f")


    // Auth0
    implementation("com.auth0:auth0:$authZeroClientVersion")
    implementation("yopdev.sdk:auth:$wabiAuthSdkVersion")

    //wabi2b services
    implementation("wabi2b:payments-sdk:1348-b8f730edebd6dd957c1dd8c83f95164c673b1005")
    implementation("wabi2b.sdk:wabi2b-api-sdk:$wabiApiSdkVersion")
    implementation("wabi2b:payment-async-notification-sdk:1348-b8f730edebd6dd957c1dd8c83f95164c673b1005") {
        exclude("wabi2b.payments.common", "common")
    }

    // security
    implementation("wabi2b:wabi2b-authorizer:$wabiAuthorizerVersion")

    // aws
    implementation(platform("software.amazon.awssdk:bom:$awsSdkBomVersion"))
    implementation("com.amazonaws:aws-lambda-java-core:$awsLambdaCoreVersion")
    implementation("software.amazon.awssdk:url-connection-client")
    implementation("com.amazonaws:aws-java-sdk-stepfunctions:$awsStepFunctionVersion")

    implementation("software.amazon.awssdk:dynamodb") {
        exclude("software.amazon.awssdk", "apache-client")
        exclude("software.amazon.awssdk", "netty-nio-client")
    }
    implementation("software.amazon.awssdk:sns") {
        exclude("software.amazon.awssdk", "apache-client")
        exclude("software.amazon.awssdk", "netty-nio-client")
    }
    implementation("software.amazon.awssdk:sqs") {
        exclude("software.amazon.awssdk", "apache-client")
        exclude("software.amazon.awssdk", "netty-nio-client")
    }

    // logging
    implementation("org.slf4j:slf4j-api:$$slf4jVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Aws xray
    implementation(platform("com.amazonaws:aws-xray-recorder-sdk-bom:$awsXrayRecorderVersion"))

    //new relic lambda
    implementation("com.newrelic.opentracing:newrelic-java-lambda:$newRelicLambdaVersion")
    implementation("com.newrelic.opentracing:java-aws-lambda:$newRelicAwsLambdaVersion")
    implementation("io.opentracing:opentracing-util:$opentracingUtilVersion")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")

    // test
    testImplementation(kotlin("test-junit"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("com.amazonaws:aws-lambda-java-tests:$awsLambdaTestVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:$mockWebServerVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoCoreVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoCoreVersion")
    testImplementation("org.testcontainers:localstack:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testRuntimeOnly("com.amazonaws:aws-java-sdk-dynamodb:$awsDynamoDbVersion")
    testImplementation("com.github.tomakehurst:wiremock:$wiremockVersion")
    testImplementation("io.projectreactor:reactor-core:$testReactorCoreVersion")
    testImplementation("io.projectreactor.netty:reactor-netty:$testReactorNettyVersion")
    testImplementation("io.projectreactor:reactor-test:$testReactorTestVersion")

    // Wabi
    implementation("yopdev:rest2lambda:1004-bf762288624a4811b08e4be067e4b1e512acc0c0")
    implementation("com.wabi2b:customers-sdk:1148-8c7b6162c410a0f6249879c17392d0544b13a95a")

    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.apache.logging.log4j" && requested.version!! <= "2.16.0") {
                useVersion("2.16.0")
                because("To avoid RCE vulnerability.")
            }
            if (requested.group == "ch.qos.logback" && requested.name == "logback-classic" && requested.version!! <= "1.2.8") {
                useVersion("1.2.8")
                because("To avoid RCE vulnerability.")
            }
            if (requested.group == "ch.qos.logback" && requested.name == "logback-core" && requested.version!! <= "1.2.8") {
                useVersion("1.2.8")
                because("To avoid RCE vulnerability.")
            }
        }
    }
}
