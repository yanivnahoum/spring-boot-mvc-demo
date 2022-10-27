plugins {
    java
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.1.0"
    id("io.freefair.lombok") version "6.5.1"
}

group = "com.att.training.spring.boot"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

extra["snakeyaml.version"] = "1.33"
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("com.mysql:mysql-connector-j")

    implementation("com.vladmihalcea:hibernate-types-55:2.20.0")
    implementation("com.hazelcast:hazelcast")
    implementation("com.hazelcast:hazelcast-hibernate53")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.ttddyy:datasource-proxy:1.8")
    testImplementation("net.ttddyy:datasource-assert:1.0")

    implementation(platform("org.testcontainers:testcontainers-bom:1.17.5"))
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
