# Pitaya

###= Build-script boiler plate:
```groovy
plugins {
    id "com.github.johnrengelman.shadow" version "4.0.2"
    id "org.spongepowered.plugin" version "0.8.1"
    id "java"
}

group "me.dags"
version "0.0.1"

def pluginId = rootProject.name.toLowerCase()
def spongeAPI = "7.2.0"
def spongeChannel = "SNAPSHOT"

sponge {
    plugin.id = pluginId
    plugin.meta {
        name = rootProject.name
        version = rootProject.version
        description = "My cool plugin"
    }
}

configurations {
    shade
    compile.extendsFrom shade
}

repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}

dependencies {
    compile "org.spongepowered:spongeapi:${spongeAPI}-${spongeChannel}"
    shade "com.github.dags-:Pitaya:0.0.4"
}

shadowJar {
    minimize()
    configurations = [project.configurations.shade]
    relocate "me.dags.pitaya.", "me.dags.${pluginId}.pitaya."
    archivesBaseName  = "${project.name}-${project.version}-SpongeAPI-${spongeAPI}.jar"
}

build {
    dependsOn(shadowJar)
}
```