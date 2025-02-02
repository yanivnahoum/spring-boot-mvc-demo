plugins {
    java
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.12"
}

group = "com.att.training.spring.boot"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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

dependencies {
    val guavaVersion = "33.4.0-jre"
    val wireMockVersion = "3.11.0"
    val mockwebserverVersion = "4.12.0"
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.google.guava:guava:$guavaVersion")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly("org.apache.httpcomponents.client5:httpclient5")
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.wiremock:wiremock-standalone:$wireMockVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:$mockwebserverVersion")
    testImplementation("io.projectreactor:reactor-test")
}

tasks {
    withType<JavaCompile>().configureEach {
        with(options) {
            compilerArgs.add("-Xlint:all,-processing,-auxiliaryclass")
        }
    }

    test {
        useJUnitPlatform {
            if (project.hasProperty("onlyITs")) {
                includeTags("slow")
            } else if (!project.hasProperty("withITs")) {
                excludeTags("slow")
            }
        }
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }
}
