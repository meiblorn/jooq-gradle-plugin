import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    jacoco
    `java-test-fixtures`
    `kotlin-dsl`
    id("com.diffplug.spotless") version "6.11.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.gradle.plugin-publish") version "1.1.0"
    id("pl.droidsonroids.jacoco.testkit") version "1.0.9"
}

group = "io.github.meiblorn.jooq"

repositories { mavenCentral() }

val jooqVersion = "3.17.5"
val flywayVersion = "9.8.1"
val testcontainersVersion = "1.17.5"

configurations { implementation { extendsFrom(shadow.get()) } }

afterEvaluate {
    with(configurations.shadow.get()) { dependencies.remove(project.dependencies.gradleApi()) }
}

dependencies {
    shadow("org.flywaydb:flyway-core:$flywayVersion")
    shadow("org.jooq:jooq-codegen:$jooqVersion")
    shadow("org.testcontainers:jdbc:$testcontainersVersion")
    testFixturesApi("io.mockk:mockk-jvm:1.13.2")
    testFixturesApi("io.strikt:strikt-jvm:0.34.1")
    testFixturesApi("org.junit.jupiter:junit-jupiter")
    testFixturesApi("org.testcontainers:postgresql:$testcontainersVersion")
    testFixturesApi(enforcedPlatform("org.junit:junit-bom:5.9.1"))
    testFixturesApi(gradleTestKit())
}

/**
 * disable test fixtures publishing
 * https://docs.gradle.org/current/userguide/java_testing.html#publishing_test_fixtures
 */
with(components["java"] as AdhocComponentWithVariants) {
    withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
    withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events(STARTED, PASSED, FAILED)
        showExceptions = true
        showStackTraces = true
        showCauses = true
        exceptionFormat = FULL
    }
}

tasks.withType<JacocoReport> {
    reports {
        with(xml) { required.set(true) }
        with(html) { required.set(false) }
    }
    setDependsOn(tasks.withType<Test>())
}

tasks.withType<ProcessResources> {
    filesMatching("**/io.github.meiblorn.jooq.dependency.versions") {
        filter {
            val result = it.replace("@jooq.version@", jooqVersion)
            result.replace("@flyway.version@", flywayVersion)
        }
    }
}

val shadowJar by
    tasks.getting(ShadowJar::class) {
        archiveClassifier.set("")

        configurations = listOf(project.configurations.shadow.get())

        exclude(
            "migrations/*",
            "META-INF/INDEX.LIST",
            "META-INF/*.SF",
            "META-INF/*.DSA",
            "META-INF/*.RSA",
            "META-INF/NOTICE*",
            "META-INF/README*",
            "META-INF/CHANGELOG*",
            "META-INF/DEPENDENCIES*",
            "module-info.class")

        mergeServiceFiles()
    }

val relocateShadowJar by
    tasks.creating(ConfigureShadowRelocation::class) {
        target = shadowJar
        prefix = "io.github.meiblorn.jooq.shadow"
    }

shadowJar.dependsOn(relocateShadowJar)

val jar by tasks.getting(Jar::class) { dependsOn(shadowJar) }

configure<SpotlessExtension> {
    kotlin {
        ktfmt().configure {
            it.setBlockIndent(4)
            it.setContinuationIndent(4)
        }
    }
    kotlinGradle {
        ktfmt().configure {
            it.setBlockIndent(4)
            it.setContinuationIndent(4)
        }
    }
}

gradlePlugin {
    plugins.create("jooqDockerPlugin") {
        id = "io.github.meiblorn.jooq-docker"
        implementationClass = "io.github.meiblorn.jooq.JooqDockerPlugin"
        version = project.version

        displayName = "jOOQ Docker Plugin"
        description = "Generates jOOQ classes using dockerized database"
    }
}

pluginBundle {
    website = "https://github.com/meiblorn/jooq-gradle-plugin"
    vcsUrl = "https://github.com/meiblorn/jooq-gradle-plugin"

    pluginTags =
        mapOf(
            "jooqDockerPlugin" to listOf("jooq", "docker", "db"),
        )
}
