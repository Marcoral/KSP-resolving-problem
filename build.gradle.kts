import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project

plugins {
    `kotlin-dsl`
    kotlin("jvm")
    `maven-publish`
}

allprojects {
    group = "com.examples.Example"
    version = "1.0.0"

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")

    publishing {
        publications {
            create<MavenPublication>("myLibrary") {
                from(components["kotlin"])
            }
        }

        repositories {
            mavenLocal()
        }
    }
}

dependencies {
    api(project(":api-annotation"))
    api(project(":api-processor"))
    api(project(":main"))
    api(project("::annotations-library"))
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
