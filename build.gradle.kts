plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val forgeVersion: String by project
val gsonVersion: String by project
val mcpMappings: String by project
val modGroup: String by project
val modId: String by project
val modVersion: String by project
val minecraftVersion: String by project

group = modGroup
version = modVersion

// Toolchains:
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

// Minecraft configuration:
loom {
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

sourceSets.main {
    output.setResourcesDir(file("$buildDir/classes/java/main"))
}

// Dependencies:
repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings("de.oceanlabs.mcp:mcp_stable:${mcpMappings}")
    forge("net.minecraftforge:forge:${forgeVersion}")
    implementation("com.google.code.gson:gson:${gsonVersion}")
}

// Tasks:
tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(Jar::class) {
    archiveBaseName.set(modId)
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
    }
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("all")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.shadowJar {
    archiveClassifier.set("all-dev")
    configurations = listOf(shadowImpl)
    doLast {
        configurations.forEach {
            println("Config: ${it.files}")
        }
    }
}

tasks.processResources {
    filesMatching("mcmod.info") {
        expand(mapOf("version" to modVersion, "mcversion" to minecraftVersion))
    }
}

tasks.assemble.get().dependsOn(tasks.remapJar)
