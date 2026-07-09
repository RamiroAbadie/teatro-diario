# ============================================================
# Dockerfile del backend (va en /backend/Dockerfile)
# ============================================================
# Concepto clave: build MULTI-STAGE. Dos etapas:
#   1) "build":   una imagen con Maven + JDK completo compila el jar.
#   2) "runtime": una imagen chica solo con la JRE ejecuta el jar.
# ¿Por qué? La imagen final NO carga Maven, ni el código fuente, ni
# las dependencias de compilación: pesa ~300 MB en vez de ~1 GB,
# y expone menos superficie. Lo que viaja al VPS es solo la etapa 2.
# ============================================================

# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Truco de caché: copiamos SOLO el pom primero y bajamos dependencias.
# Docker cachea cada instrucción: si el pom no cambió, este paso no se
# re-ejecuta aunque cambies código -> rebuilds de segundos, no minutos.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Recién ahora el código. Un cambio acá solo invalida de acá para abajo.
COPY src ./src
RUN mvn package -DskipTests -q
# (los tests corren en CI con Testcontainers, no en el build de la imagen)

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Seguridad básica: correr como usuario sin privilegios, no como root.
RUN useradd --system --create-home appuser
USER appuser

# Traemos SOLO el jar desde la etapa de build.
COPY --from=build /app/target/*.jar app.jar

# Documenta el puerto (la exposición real la maneja Compose).
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
