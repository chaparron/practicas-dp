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

java {
    sourceCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
}

dependencies {

    implementation("com.newrelic.agent.java:newrelic-api:5.14.0")

    // kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.wabi2b:serializers:1007-8ea7571732f9dbdcca033fa025fe06e3f5179fa8")

    // wabi2b jpmc Sdk
    implementation("wabi2b:jpmc-sdk:1012-4a2029291dd1ebdc9a129c64e024cf691c0f9bf3")


    // Auth0
    implementation("com.auth0:auth0:1.35.0")
    implementation("yopdev.sdk:auth:10002-4d40dfa9419c0b2f93d7dc38d56563b498c32737")

    //wabi2b services
    implementation("wabi2b:payments-sdk:1360-241624d163a197bcd1d89fc0fe93c9ca1d4e5216")
    implementation("wabi2b.sdk:wabi2b-api-sdk:1914-146955aab7fb19d2978d5e302c763d754c970d37")
    implementation("wabi2b:payment-async-notification-sdk:1360-241624d163a197bcd1d89fc0fe93c9ca1d4e5216") {
        exclude("wabi2b.payments.common", "common")
    }

    // security
    implementation("wabi2b:wabi2b-authorizer:1021-4ce0fd69f4ccb17ac12a9778c18dd2488257dfe3")

    // aws
    implementation(platform("software.amazon.awssdk:bom:2.15.7"))
    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation("software.amazon.awssdk:url-connection-client")
    implementation("com.amazonaws:aws-java-sdk-stepfunctions:1.12.125")

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
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-core:1.2.9")
    implementation("ch.qos.logback:logback-classic:1.2.9")

    // Aws xray
    implementation(platform("com.amazonaws:aws-xray-recorder-sdk-bom:2.9.1"))

    //new relic lambda
    implementation("com.newrelic.opentracing:newrelic-java-lambda:2.2.1")
    implementation("com.newrelic.opentracing:java-aws-lambda:2.1.0")
    implementation("io.opentracing:opentracing-util:0.33.0")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")

    // test
    testImplementation(kotlin("test-junit"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("com.amazonaws:aws-lambda-java-tests:1.1.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.0.0")
    testImplementation("org.testcontainers:localstack:1.16.2")
    testImplementation("org.testcontainers:junit-jupiter:1.16.2")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testRuntimeOnly("com.amazonaws:aws-java-sdk-dynamodb:1.11.689")
    testImplementation("com.github.tomakehurst:wiremock:2.24.1")
    testImplementation("io.projectreactor:reactor-core:3.4.14")
    testImplementation("io.projectreactor.netty:reactor-netty:1.0.15")
    testImplementation("io.projectreactor:reactor-test:3.2.11.RELEASE")

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
