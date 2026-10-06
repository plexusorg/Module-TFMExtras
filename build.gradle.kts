plugins {
    java
    checkstyle
    `maven-publish`
}

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://nexus.telesphoreo.me/repository/plex/")
    }

    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    implementation("org.apache.commons:commons-lang3:3.21.0")
    compileOnly("dev.plex:api:2.0-SNAPSHOT")
}

group = "dev.plex"
version = "2.0-SNAPSHOT"
description = "Module-TFMExtras"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

checkstyle {
    toolVersion = "14.3.0"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
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
