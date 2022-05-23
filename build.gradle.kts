import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionCreator.VERSION_WITH_BRANCH
import java.time.Duration

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("java")
    id("groovy")
    id("maven-publish")
    id("java-library")
    id("com.adarshr.test-logger") version "3.0.0"
    id("net.ltgt.errorprone") version "2.0.0"
    id("pl.allegro.tech.build.axion-release") version "1.13.7"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
    id ("org.barfuin.gradle.taskinfo") version "1.0.5"
    signing
    checkstyle
}

scmVersion {
    versionCreator = VERSION_WITH_BRANCH.versionCreator
}

repositories {
    mavenCentral()
}

dependencies {
    // Other
    implementation("com.fasterxml.jackson.module:jackson-module-paranamer:2.13.0")
    api("javax.xml.bind:jaxb-api:2.3.1")
    implementation("io.sentry:sentry-logback:1.7.28")

    implementation("io.vavr:vavr:0.10.3")
    implementation("javax.inject:javax.inject:1")
    implementation("com.google.guava:guava:30.1-jre")
    errorprone("com.google.errorprone:error_prone_core:2.9.0")

    // GraphQL
    implementation("com.graphql-java-kickstart:graphql-spring-boot-starter:11.0.0")
    implementation("com.graphql-java-kickstart:graphql-java-servlet:11.0.0")

    testImplementation("org.spockframework:spock-core:2.0-M3-groovy-3.0")
    testImplementation("cglib:cglib-nodep:3.3.0")
    testImplementation("org.codehaus.groovy:groovy-all:3.0.9")
    testImplementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:3.10.2")

    testImplementation("org.spockframework:spock-spring:2.0-groovy-3.0")
    testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.1")

    // Spring
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.4.3")
    testImplementation("org.springframework.boot:spring-boot-starter-web:2.5.5")
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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
        events = setOf(PASSED, SKIPPED, FAILED, STANDARD_ERROR)
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
    gradleVersion = "7.2"
}

tasks.create<ConfigureShadowRelocation>("relocateShadowJar") {
    target = tasks.shadowJar.get()
}

tasks.named("shadowJar") {
    dependsOn("relocateShadowJar")
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    minimize()
}

project.version = scmVersion.version
group = "pl.allegro.tech.graphql"

publishing {
    publications {
        val publication = create<MavenPublication>("sonatype")
        project.shadow.component(publication)
        publication.artifact(tasks.named("javadocJar"))
        publication.artifact(tasks.named("sourcesJar"))

        publication.pom {
            name.set("extended-audit-instrumentation")
            description.set("Graphql extended audit instrumentation")
            url.set("https://github.com/allegro/graphql-extended-audit-intstrumentation")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("dkubicki")
                    name.set("Dawid Kubicki")
                }
            }

            scm {
                connection.set("scm:git@github.com:allegro/graphql-extended-audit-intstrumentation")
                developerConnection.set("scm:git@github.com:allegro/graphql-extended-audit-intstrumentation.git")
                url.set("https://github.com/allegro/graphql-extended-audit-intstrumentation")
            }
        }
    }
}

nexusPublishing {
    connectTimeout.set(Duration.ofMinutes(10))
    clientTimeout.set(Duration.ofMinutes(10))

    repositories {
        sonatype {
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}

if (!System.getenv("GPG_KEY_ID").isNullOrBlank()) {
    signing {
        useInMemoryPgpKeys(
            System.getenv("GPG_KEY_ID"),
            System.getenv("GPG_PRIVATE_KEY"),
            System.getenv("GPG_PRIVATE_KEY_PASSWORD")
        )
        sign(publishing.publications)
    }
}
