plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.walustik"
version = "1.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.1")

    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.xerial:sqlite-jdbc:3.44.0.0")
    implementation("mysql:mysql-connector-java:8.0.33")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set("ReportStaff")
        archiveVersion.set(version.toString())
        relocate("com.zaxxer.hikari", "com.walustik.reportstaff.libs.hikari")
    }

    build {
        dependsOn(shadowJar)
    }
}
