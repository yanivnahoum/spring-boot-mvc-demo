FROM adoptopenjdk/openjdk11:alpine-jre as builder
WORKDIR application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM adoptopenjdk/openjdk11:alpine-jre
ARG USER_ID=1000
ARG GROUP_ID=1001
ARG USER_NAME=demouser
ARG GROUP_NAME=demogroup
RUN addgroup -g $GROUP_ID -S $GROUP_NAME \
    && adduser -u $USER_ID -S $USER_NAME -G $GROUP_NAME

WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./

RUN chown -R $USER_NAME:$GROUP_NAME ./
USER $USER_ID

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
#ENTRYPOINT ["sh", "-c", "top"]