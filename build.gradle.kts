plugins {
    java
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
    id("io.freefair.lombok") version "8.4"
}

group = "com.att.training.spring.boot"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}
//<hibernate.version>6.4.1.Final</hibernate.version>
extra["hibernate.version"] = "6.3.2.Final"
dependencies {
    val hazelcast = "5.1.0"
    val datasourceProxy = "1.9"
    val datasourceAssert = "1.0"
    val testcontainers = "1.19.3"
    val hypersistence = "3.7.0"

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:$hypersistence")
    implementation("com.hazelcast:hazelcast-hibernate53:$hazelcast")
    implementation(platform("org.testcontainers:testcontainers-bom:$testcontainers"))

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.ttddyy:datasource-proxy:$datasourceProxy")
    testImplementation("net.ttddyy:datasource-assert:$datasourceAssert")
    testImplementation("org.testcontainers:mysql")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 17
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
