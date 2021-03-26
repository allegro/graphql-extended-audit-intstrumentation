import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionCreator.VERSION_WITH_BRANCH
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
    }
}

plugins {
    id("java")
    id("groovy")
    id("application")
    id("maven-publish")
    id("com.adarshr.test-logger") version "2.0.0"
    id("net.ltgt.errorprone") version "1.3.0"
    id ("pl.allegro.tech.build.axion-release") version "1.12.1"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    checkstyle
}

scmVersion {
    tag = TagNameSerializationConfig()
    tag.prefix = "graphql-extended-audit-instrumentation"
    versionCreator = VERSION_WITH_BRANCH.versionCreator
}

application {
    mainClassName = "pl.allegro.tech.graphqlaudit.auditlog.AuditLogInstrumentationCreator"
}

repositories {
    mavenCentral()
}

dependencies {
    // Other
    implementation("com.fasterxml.jackson.module:jackson-module-paranamer:2.11.1")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("io.sentry:sentry-logback:1.7.28")

    implementation("io.vavr:vavr:0.10.3")
    implementation("javax.inject:javax.inject:1")
    implementation( "com.google.guava:guava:30.1-jre")
    errorprone("com.google.errorprone:error_prone_core:2.5.1")

    // GraphQL
    implementation("com.graphql-java-kickstart:graphql-spring-boot-starter:11.0.0")
    implementation("com.graphql-java-kickstart:graphql-java-servlet:11.0.0")

    testImplementation("org.spockframework:spock-core:2.0-M3-groovy-3.0")
    testImplementation("cglib:cglib-nodep:3.3.0")
    testImplementation("org.codehaus.groovy:groovy-all:3.0.7")
    testImplementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:latest.release")

    testImplementation("org.spockframework:spock-spring:2.0-M3-groovy-3.0")
    testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.1")

    // Spring
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.4.3")
    testImplementation("org.springframework.boot:spring-boot-starter-web:2.4.3")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile> {
    options.isIncremental = true
    options.compilerArgs.add("-parameters")
    options.errorprone.disable("UnusedVariable", "UnnecessaryParentheses")
}

tasks.withType<Test> {
    maxParallelForks = 1
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(PASSED, SKIPPED, FAILED, STANDARD_ERROR )
    }
    useJUnitPlatform()
}

checkstyle {
    toolVersion = "8.37"
    isIgnoreFailures = false
    maxWarnings = 0
    configFile = rootProject.file("config/checkstyle/google_checkstyle.xml")
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.isEnabled = false
        html.isEnabled = true
    }
}

tasks.named("check") {
        dependsOn("checkstyleMain")
    }

tasks.withType<Wrapper> {
    gradleVersion = "6.8.0"
}
