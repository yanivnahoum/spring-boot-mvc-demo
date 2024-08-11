FROM eclipse-temurin:11-jre-focal as builder
WORKDIR application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:11-jre-focal

WORKDIR application
ARG USER_NAME=demouser
ARG GROUP_NAME=demogroup
RUN groupadd $GROUP_NAME -g 1001 \
    && useradd $USER_NAME -u 1000 -g $GROUP_NAME \
    && chown $USER_NAME:$GROUP_NAME ./
USER $USER_NAME

COPY --from=builder --chown=$USER_NAME:$GROUP_NAME application/dependencies/ ./
COPY --from=builder --chown=$USER_NAME:$GROUP_NAME application/spring-boot-loader/ ./
COPY --from=builder --chown=$USER_NAME:$GROUP_NAME application/snapshot-dependencies/ ./
COPY --from=builder --chown=$USER_NAME:$GROUP_NAME application/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
