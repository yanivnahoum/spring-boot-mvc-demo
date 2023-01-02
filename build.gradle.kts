plugins {
    java
    id("org.springframework.boot") version "3.0.1"
    id("io.spring.dependency-management") version "1.1.0"
    id("io.freefair.lombok") version "6.6.1"
}

group = "com.att.training.spring.boot"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.google.guava:guava:31.1-jre")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.0"))
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")

    implementation(platform("org.testcontainers:testcontainers-bom:1.17.6"))
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("io.projectreactor:reactor-test")
}

tasks {
    withType<JavaCompile>().configureEach {
        with(options) {
            release.set(17)
            compilerArgs.add("-Xlint:all,-processing,-auxiliaryclass")
        }
    }

    jar {
        enabled = false
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
