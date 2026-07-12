FROM eclipse-temurin:25-jre AS builder
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
    curl \
    unzip

WORKDIR /builder
COPY target/*.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination extracted

RUN curl google.com

FROM eclipse-temurin:25-jre

RUN apt-get update \
    && apt-get -y dist-upgrade \
    && apt-get install -y --no-install-recommends curl jattach \
    && apt-get autoremove -y --purge \
    && apt-get -y clean \
    && apt-get -y autoclean \
    && rm -rf /var/lib/apt/lists/* \
    && find /var/cache/debconf -type f -print0 | xargs -0 rm -f \
    && find /var/cache/apt -type f -print0 | xargs -0 rm -f

ARG USER_NAME=demouser
ARG UID=1001
ARG GROUP_NAME=demogroup
ARG GID=1001
RUN groupadd --gid "$GID" "$GROUP_NAME" \
    && useradd --uid "$UID" --gid "$GID" "$USER_NAME"
USER $USER_NAME
WORKDIR /application

COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
