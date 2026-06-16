# ============================================================
#  OdontoHub — imagem Docker (build multi-stage)
#  Execução LOCAL em container (não é deploy).
# ============================================================

# ---- Estágio 1: build do JAR (com Maven + JDK 21) ----
# Usa a imagem oficial do Maven para evitar problemas de fim de linha (CRLF)
# do mvnw vindo de checkouts Windows.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa dependências primeiro (camada cacheável enquanto o pom não muda)
COPY pom.xml ./
RUN mvn -B dependency:go-offline || true

# Copia o código e empacota. O plugin do Vaadin (build-frontend) baixa o Node
# automaticamente durante o build e embute o bundle de produção no JAR, de modo
# que o runtime não precisa de Node.
COPY src/ src/
RUN mvn -B clean package -DskipTests

# ---- Estágio 2: runtime (apenas JRE 21) ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o JAR executável gerado no estágio de build
COPY --from=build /app/target/odontohub-0.0.1-SNAPSHOT.jar app.jar

# Banco H2 em arquivo (durável). Volume para persistir entre reinícios.
VOLUME ["/app/data"]

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
