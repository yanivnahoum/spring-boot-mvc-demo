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
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("com.mysql:mysql-connector-j")

    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
    implementation("com.hazelcast:hazelcast-hibernate53:5.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.ttddyy:datasource-proxy:1.8.1")
    testImplementation("net.ttddyy:datasource-assert:1.0")

    implementation(platform("org.testcontainers:testcontainers-bom:1.17.6"))
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release.set(17)
    }

    jar {
        enabled = false
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("skipped", "failed")
            showStandardStreams = true
        }
        val hazelcastJvmArgs = listOf(
            "--add-exports java.base/jdk.internal.ref=ALL-UNNAMED",
            "--add-opens java.base/java.lang=ALL-UNNAMED",
            "--add-opens java.base/java.nio=ALL-UNNAMED",
            "--add-opens java.base/sun.nio.ch=ALL-UNNAMED",
            "--add-opens java.management/sun.management=ALL-UNNAMED",
            "--add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"
        ).flatMap { it.split(" ") }

        jvmArgs(hazelcastJvmArgs)
    }
}
