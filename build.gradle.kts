plugins {
    java
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    `maven-publish`
    id("org.sonarqube") version "4.3.0.3225"
}

group = "yk.projects"
description = "spring-boot-jenkins"


val isRelease = project.hasProperty("release")

version = if (isRelease) {
    property("yk.projects.version").toString()
} else {
    property("yk.projects.version").toString() + "-SNAPSHOT"
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

// --------------------
// Dependency Management (Spring BOM)
// --------------------
dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.9")
    }
}

// --------------------
// Dependencies
// --------------------
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.liquibase:liquibase-core")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("com.h2database:h2")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// --------------------
// Maven Publishing
// --------------------
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = "spring-boot-jenkins"
            version = project.version.toString()

            versionMapping {
                allVariants {
                    fromResolutionResult()
                }
            }
        }
    }

    repositories {
        maven {
            name = "nexusSnapshots"
            url = uri("http://nexus:8081/repository/maven-snapshots/")
            credentials {
                username = findProperty("nexusUsername") as String? ?: ""
                password = findProperty("nexusPassword") as String? ?: ""
            }
        }
        maven {
            name = "nexusReleases"
            url = uri("http://nexus:8081/repository/maven-releases/")
            credentials {
                username = findProperty("nexusUsername") as String? ?: ""
                password = findProperty("nexusPassword") as String? ?: ""
            }
        }
    }
}



tasks.register("publishToNexus") {
    dependsOn("build")

    doFirst {
        val isSnapshot = project.version.toString().endsWith("SNAPSHOT")

        val publishTaskName = if (isSnapshot) {
            "publishMavenJavaPublicationToNexusSnapshotsRepository"
        } else {
            "publishMavenJavaPublicationToNexusReleasesRepository"
        }

        dependsOn(publishTaskName)
        println("Publishing version $version using $publishTaskName")
    }
}


// --------------------
// SonarQube configuration
// --------------------
sonarqube {
    properties {
        property("sonar.projectKey", "spring-boot-jenkins")
        property("sonar.projectName", "spring-boot-jenkins")
        property("sonar.host.url", findProperty("sonarHostUrl") ?: "http://host.docker.internal:9000")
        property("sonar.login", findProperty("sonarToken") ?: "")
    }
}
