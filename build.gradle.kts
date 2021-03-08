import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.samordning"
version = "1"
description = "samordning-hendelse-api"

val logbackClassicVersion = "1.2.3"
val logstashLogbackEncoder = "5.2"
val slf4jVersion = "1.7.30"
val log4jVersion = "2.13.3"

plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("plugin.spring") version "1.4.31"
    id("org.springframework.boot") version "2.3.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.auth0","java-jwt","3.8.3")
    implementation("com.vladmihalcea","hibernate-types-52","2.9.10")
    implementation("io.micrometer","micrometer-registry-prometheus","1.5.1")
    implementation("org.postgresql","postgresql","42.2.12")
    implementation("org.hibernate.validator","hibernate-validator","6.0.10.Final")
    implementation("org.springframework.boot","spring-boot-starter-web")
    implementation("org.springframework.boot","spring-boot-starter-jdbc")
    implementation("org.springframework.boot","spring-boot-starter-webflux")
    implementation("org.springframework.boot","spring-boot-starter-actuator")
    implementation("org.springframework.boot","spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.security.oauth","spring-security-oauth2","2.5.0.RELEASE")
    implementation("org.springframework.cloud","spring-cloud-vault-config-databases","2.2.2.RELEASE")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.springframework.cloud","spring-cloud-starter-vault-config","2.1.3.RELEASE")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.springframework.boot","spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("com.h2database","h2","1.4.200")
    testImplementation("org.testcontainers","postgresql","1.15.1")
    testImplementation("org.testcontainers","mockserver","1.14.3")
    testImplementation("org.mock-server","mockserver-client-java","3.12")

    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoder")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
}

tasks{
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "13"
    }
    withType<Wrapper> {
        gradleVersion = "6.4"
    }
    test {
        useJUnitPlatform()
    }
    withType<Test> {
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }
}