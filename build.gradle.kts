plugins {
    java
    `maven-publish`
}

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://nexus.telesphoreo.me/repository/plex/")
    }

    maven {
        url = uri("https://repo.infernalsuite.com/repository/maven-snapshots/")
    }

    mavenCentral()
//    mavenLocal()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    compileOnly("dev.plex:server:1.5-SNAPSHOT")
    compileOnly("com.infernalsuite.aswm:api:1.20.6-R0.1-SNAPSHOT") {
        exclude(group = "com.flowpowered")
    }
}

group = "dev.plex"
version = "1.5-SNAPSHOT"
description = "Module-TFMExtras"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.getByName<Jar>("jar") {
    archiveBaseName.set("Module-TFMExtras")
    archiveVersion.set("")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}
