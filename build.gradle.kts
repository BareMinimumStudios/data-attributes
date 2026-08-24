plugins {
    `maven-publish`
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.cloche)
}

group = "net.bms.data_attributes"
version = "3.0.0"

repositories {
    cloche.librariesMinecraft()

    cloche {
        main()
        mavenFabric()
        mavenForge()
        mavenNeoforgedMeta()
        mavenNeoforged()
        mavenParchment()
    }

    maven("https://api.modrinth.com/maven")
    maven("https://maven.nucleoid.xyz")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.ladysnake.org/releases")
    maven("https://maven.wispforest.io/releases")
    maven("https://maven.shedaniel.me/")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://maven.wispforest.io")
    maven(url = "https://jitpack.io/")

    maven {
        name = "FzzyMaven"
        url = uri("https://maven.fzzyhmstrs.me/")
    }

    mavenCentral()
}

cloche {
    metadata {
        modId = "data_attributes"
        name = "Data Attributes"
        description = "Allows manipulation of Minecraft Attributes dynamically using a configuration."
        license = "BML-1.0"

        author {
            name = "karuzumi"
            contact = "https://github.com/karuzumi"
        }

        url = "https://github.com/BareMinimumStudios/DataAttributes"
        sources = "https://github.com/BareMinimumStudios/DataAttributes"
        issues = "https://github.com/BareMinimumStudios/DataAttributes/issues"

        icon = "assets/data_attributes/icon.png"
    }

    common {
        mixins.from("src/main/data_attributes.mixins.json")

        mappings {
            official()
        }

        dependencies {
            modApi(libs.owo.neo)

            modImplementation(libs.fuzzy.config)

            modImplementation(libs.endec)
            modImplementation(libs.netty.endec)
            modImplementation(libs.gson.endec)
        }
    }

    minecraftVersion = "1.21.1"

    neoforge {
        loaderVersion = libs.versions.neoforge.loader

        runs {
            server()
            client()
        }

        dependencies {
            modImplementation(libs.neoforge.language.kotlin)
            modImplementation(libs.owo.neo)
//            include(libs.sentinel.neo)
        }

        metadata {
            modLoader = "kotlinforforge"
            loaderVersion {
                start = libs.versions.neoforge.language.kotlin.get()
            }
            blurLogo = false
            dependencies {
                dependency {
                    modId = "kotlinforforge"
                    version(libs.versions.neoforge.language.kotlin.get())
                }
            }
        }
    }

    fabric {
        loaderVersion = libs.versions.fabric.loader

        includedClient()

        runs {
            server()
            client()
        }

        dependencies {
            fabricApi(libs.versions.fabric.api)

            modImplementation(libs.fabric.language.kotlin)

            modImplementation(libs.owo.fab)
            include(libs.sentinel.fab)
        }

        metadata {
            dependencies {
                dependency {
                    modId = "fabric-api"
                    version(libs.versions.fabric.api.get())
                }
                dependency {
                    modId = "fabric-language-kotlin"
                    version(libs.versions.fabric.language.kotlin.get())
                }
            }

            entrypoint("main") {
                adapter = "kotlin"
                value = "net.bms.data_attributes.DataAttributesFabricEntrypoint"
            }
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xmulti-platform", "-Xno-check-actual", "-Xexpect-actual-classes")
    }
}
dependencies {
    testImplementation(kotlin("test"))
}