FROM openjdk:17-jdk-slim-buster as builder
ARG JAR_FILE=build/libs/otp.jar
WORKDIR app

COPY ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM openjdk:17-jdk-slim-buster
WORKDIR /app

ADD https://github.com/aws-observability/aws-otel-java-instrumentation/releases/download/v1.23.0/aws-opentelemetry-agent.jar opentelemetry-javaagent.jar

COPY --from=builder app/dependencies/ ./
COPY --from=builder app/spring-boot-loader/ ./
COPY --from=builder app/snapshot-dependencies/ ./
COPY --from=builder app/application/ ./

ENTRYPOINT java -javaagent:/app/opentelemetry-javaagent.jar $JAVA_OPTS org.springframework.boot.loader.JarLauncher