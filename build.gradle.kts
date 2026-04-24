plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "9.4.0"
}

group = "com.att.training.spring.boot"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
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

// Overriding vulnerable dependency 11.0.20 from Spring Boot 4.0.5
extra["tomcat.version"] = "11.0.21"
val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
    val guavaVersion = "33.5.0-jre"
    val springdocVersion = "3.0.2"
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
    implementation("com.google.guava:guava:$guavaVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("io.micrometer:micrometer-registry-graphite")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("tools.jackson.module:jackson-module-mrbean")
    mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}

tasks {
    withType<JavaCompile>().configureEach {
        with(options) {
            compilerArgs.add("-Xlint:all,-processing,-auxiliaryclass")
        }
    }

    test {
        useJUnitPlatform()
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }
}
