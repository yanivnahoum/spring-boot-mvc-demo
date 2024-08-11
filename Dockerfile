FROM eclipse-temurin:17-jre as builder
WORKDIR application
COPY target/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17-jre
RUN apt-get update \
    && apt-get install -y \
    jattach \
    && rm -rf /var/lib/apt/lists/*

ARG USER_NAME=demouser
ARG UID=1000
ARG GROUP_NAME=demogroup
ARG GID=1001
RUN groupadd --gid $GID $GROUP_NAME && \
    useradd --uid $UID --gid $GID $USER_NAME
USER $UID
WORKDIR application

COPY --from=builder --chown=$USER_NAME:$GROUP_NAME application/dependencies/ ./
COPY --from=builder --chown=$USER_NAME:$GROUP_NAME application/spring-boot-loader/ ./
COPY --from=builder --chown=$USER_NAME:$GROUP_NAME application/snapshot-dependencies/ ./
COPY --from=builder --chown=$USER_NAME:$GROUP_NAME application/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
