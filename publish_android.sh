#!/bin/bash

echo "🚀 Rozpoczynam budowanie Satori Bundle (.aab) dla Google Play..."

# Czyścimy buildy
./gradlew clean

# Budujemy bundle dla modułu composeApp
# Zakładamy, że masz skonfigurowane klucze w gradle.properties lub local.properties
./gradlew :composeApp:bundleRelease

if [ $? -eq 0 ]; then
    echo "✅ Sukces! Plik .aab został wygenerowany."
    echo "📍 Lokalizacja: composeApp/build/outputs/bundle/release/composeApp-release.aab"
else
    echo "❌ Błąd budowania. Sprawdź logi powyżej."
    exit 1
fi
