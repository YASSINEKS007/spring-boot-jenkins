plugins {
    java
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    `maven-publish`
    id("org.sonarqube") version "4.3.0.3225"
}

group = "yk.projects"
description = "spring-boot-jenkins"

/**
 * Version handling
 * Base version comes from gradle.properties
 * -Prelease controls SNAPSHOT vs release
 */
val baseVersion = findProperty("yk.projects.version")?.toString()
    ?: error("Missing required property: yk.projects.version")

version = if (project.hasProperty("release")) {
    baseVersion
} else {
    "$baseVersion-SNAPSHOT"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
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
    annotationProcessor("org.projectlombok:lombok")

    runtimeOnly("com.h2database:h2")

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

            groupId = group.toString()
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
            isAllowInsecureProtocol  = true
            credentials {
                username = findProperty("nexusUsername") as String?
                password = findProperty("nexusPassword") as String?
            }
        }
        maven {
            name = "nexusReleases"
            url = uri("http://nexus:8081/repository/maven-releases/")
            isAllowInsecureProtocol  = true
            credentials {
                username = findProperty("nexusUsername") as String?
                password = findProperty("nexusPassword") as String?
            }
        }
    }
}

// --------------------
// SonarQube configuration
// --------------------
sonarqube {
    properties {
        property("sonar.projectKey", "spring-boot-jenkins")
        property("sonar.projectName", "spring-boot-jenkins")
        property(
            "sonar.host.url",
            findProperty("sonarHostUrl") ?: "http://host.docker.internal:9000"
        )
        property("sonar.login", findProperty("sonarToken") ?: "")
    }
}
