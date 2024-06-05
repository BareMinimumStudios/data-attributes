import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.0"
    id("fabric-loom")
    `maven-publish`
    java
}

group = property("maven_group")!!
version = "${property("mod_version")!!}-${property("loader")!!}"

repositories {
    maven("https://maven.wispforest.io")
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")

    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")

    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${properties["fabric_kotlin_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_api_version"]}")

    implementation("io.wispforest:endec:${properties["endec_version"]}")?.let(::include)
    implementation("io.wispforest.endec:gson:${properties["endec_version_2"]}")?.let(::include)
    implementation("io.wispforest.endec:codec:${properties["endec_version_2"]}")?.let(::include)
    implementation("io.wispforest.endec:netty:${properties["endec_version_2"]}")?.let(::include)

    implementation("io.github.llamalad7:mixinextras-fabric:${properties["mixinextras_version"]}")?.let {
        include(it)
        annotationProcessor(it)
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
            }
        }

        repositories {}
    }

    compileKotlin {
        compilerOptions.freeCompilerArgs.set(listOf("-Xjvm-default=all-compatibility"))
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }

}

java {
    withSourcesJar()
}