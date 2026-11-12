# Kitsune

App Android de catálogo de anime (AniList) con reproductor Media3. Kotlin + Jetpack Compose.

## Requisitos
- JDK 17
- Android Studio (Ladybug o superior) o Gradle 8.11.1

## Compilar
./gradlew assembleMobileDebug
./gradlew assembleTvDebug

## Firma release (opcional, por variables de entorno)
KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD y el archivo app/keystore.jks (nunca se commitea).

## Estructura
- data/catalog — fuente AniList (GraphQL) con resultados tipados y manejo de errores
- data/provider — servidores y persistencia (DataStore)
- player — PlayerActivity (ExoPlayer, HLS, PiP)
- ui — Compose: pantallas, componentes y tema
