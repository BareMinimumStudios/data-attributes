plugins {
    `maven-publish`
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
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

    maven("https://thedarkcolour.github.io/KotlinForForge/")
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
        description = "A data-driven entity attribute framework with datapack and server configuration support."
        license = "BML-1.0"

        author {
            name = "karuzumi"
            contact = "https://github.com/karuzumi"
        }

        url = "https://github.com/BareMinimumStudios/data-attributes"
        sources = "https://github.com/BareMinimumStudios/data-attributes"
        issues = "https://github.com/BareMinimumStudios/data-attributes/issues"

    }

    common {
        mixins.from("src/main/data_attributes.mixins.json")

        mappings {
            official()
        }

        dependencies {
            implementation(libs.kotlinx.serialization.json)
            compileOnly(libs.mixinextras)
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
            modImplementation(libs.fzzy.config.neoforge)
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
                dependency {
                    modId = "fzzy_config"
                    version(libs.versions.fzzy.neoforge.get())
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
            modImplementation(libs.fzzy.config.fabric)
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
                dependency {
                    modId = "fzzy_config"
                    version(libs.versions.fzzy.fabric.get())
                }
            }

            entrypoint("main") {
                adapter = "kotlin"
                value = "net.bms.data_attributes.DataAttributesFabricEntrypoint"
            }
            entrypoint("client") {
                adapter = "kotlin"
                value = "net.bms.data_attributes.DataAttributesFabricClientEntrypoint"
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
