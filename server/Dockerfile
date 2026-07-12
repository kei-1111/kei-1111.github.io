# fat jar は CI (gradlew :server:buildFatJar) で事前ビルドする前提。このイメージは jar を載せるだけ。
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/server-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
