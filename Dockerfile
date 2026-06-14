# Etap 1: Budowanie aplikacji
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Kopiujemy pliki gradle, aby wykorzystać cache warstw
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY libs.versions.toml gradle/

# Kopiujemy kody źródłowe modułów
COPY shared shared
COPY server server

# Nadajemy uprawnienia i budujemy tylko moduł serwera
RUN chmod +x gradlew
RUN ./gradlew :server:installDist --no-daemon

# Etap 2: Obraz produkcyjny (Runtime)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Kopiujemy zbudowaną aplikację z pierwszego etapu
COPY --from=build /app/server/build/install/server /app

# Eksponujemy port (domyślnie 8080 zgodnie z application.conf)
EXPOSE 8080

# Uruchamiamy aplikację
ENTRYPOINT ["/app/bin/server"]
