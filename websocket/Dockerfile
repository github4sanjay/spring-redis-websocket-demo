FROM openjdk:17-jdk-slim-buster as builder
ARG JAR_FILE=build/libs/websocket.jar
WORKDIR app

COPY ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM openjdk:17-jdk-slim-buster
WORKDIR /app

COPY --from=builder app/dependencies/ ./
COPY --from=builder app/spring-boot-loader/ ./
COPY --from=builder app/snapshot-dependencies/ ./
COPY --from=builder app/application/ ./

ENTRYPOINT java $JAVA_OPTS org.springframework.boot.loader.JarLauncher