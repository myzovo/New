FROM openjdk:17-slim
VOLUME /tmp
COPY target/noto-ai-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
