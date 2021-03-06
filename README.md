# Pitaya

### Build-script boiler plate:
```groovy
plugins {
    id "com.github.johnrengelman.shadow" version "4.0.2"
    id "org.spongepowered.plugin" version "0.8.1"
    id "java"
}

group "me.dags"
version "0.0.1"
def spongeAPI = "7.2.0"
def spongeChannel = "SNAPSHOT"

sponge {
    plugin.id = rootProject.name.toLowerCase()
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
    shade "com.github.dags-:Pitaya:0.0.5"
}

shadowJar {
    minimize()
    classifier = null
    configurations = [project.configurations.shade]
    relocate "me.dags.pitaya.", "${project.group}.${sponge.plugin.id}.libs.pitaya."
    archivesBaseName  = "${sponge.plugin.meta.name}-${spongeAPI}"
}

build {
    dependsOn(shadowJar)
}
```