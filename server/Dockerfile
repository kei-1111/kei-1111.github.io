# fat jar は CI (gradlew :server:buildFatJar) で事前ビルドする前提。このイメージは jar を載せるだけ。
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/server-all.jar app.jar
# 非 root 実行(多層防御)。UID は数値固定して Cloud Run / k8s の runAsNonRoot 判定を通す。
RUN useradd --system --uid 10001 --no-create-home app
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
