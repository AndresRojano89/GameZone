# Plan de Implementación: GameZone

Desarrollo de una aplicación Android premium para el descubrimiento y seguimiento de videojuegos, utilizando Jetpack Compose, Material 3 y datos locales estáticos. El enfoque es maximizar el impacto visual y la fluidez sin introducir complejidad técnica de servidor o bases de datos externas.

## User Review Required

> [!IMPORTANT]
> **Gestión de Imágenes**: Para evitar dependencia absoluta de Internet y asegurar que la app siempre se vea profesional, se recomienda usar una combinación de recursos locales (`res/drawable`) para elementos clave y URLs de alta calidad para el catálogo extenso. ¿Estás de acuerdo con este enfoque híbrido?

> [!NOTE]
> **Persistencia de Estado**: El estado "Premium" y la "Biblioteca" se mantendrán en memoria (`ViewModel`). Al cerrar la app por completo, los cambios se resetearán. Esto simplifica el desarrollo enormemente al no usar Room/DataStore.

## Open Questions
- ¿Tienes algún logo específico o colores de marca que prefieras? (Por defecto usaré una paleta "Gaming Dark": Negro profundo, Gris oscuro, y Violeta Neón).

---

## Proposed Changes

### FASE 1 — Configuración y estructura
**Objetivo**: Preparar el entorno y las dependencias necesarias.
- **Archivos**: `build.gradle.kts` (app), creación de carpetas `data`, `ui`, `navigation`.
- **Funcionalidades**: Inclusión de bibliotecas de Navegación, Coil (imágenes) y Ciclo de vida.
- **Verificación**: El proyecto debe sincronizar y compilar correctamente.

### FASE 2 — Sistema visual y tema
**Objetivo**: Definir la identidad visual "Premium" de la app.
- **Archivos**: `ui/theme/Color.kt`, `Theme.kt`, `Type.kt`.
- **Funcionalidades**: Paleta de colores oscura, tipografía moderna (ej. Montserrat o Roboto), formas redondeadas para tarjetas.
- **Verificación**: Previews de Compose mostrando los colores y fuentes correctas.

### FASE 3 — Navegación
**Objetivo**: Implementar el esqueleto de movimiento de la app.
- **Archivos**: `navigation/NavGraph.kt`, `navigation/Routes.kt`, `ui/components/MainScaffold.kt`.
- **Funcionalidades**: Bottom Navigation Bar con 3 destinos principales. Gestión de rutas.
- **Verificación**: Poder navegar entre pantallas vacías usando la barra inferior.

### FASE 4 — Modelos y datos locales
**Objetivo**: Definir qué es un "Juego" y crear el catálogo estático.
- **Archivos**: `data/model/Game.kt`, `data/repository/MockDataProvider.kt`.
- **Funcionalidades**: Data classes para Juegos y Categorías. Listas con al menos 20-30 juegos reales.
- **Verificación**: Logs o tests simples que confirmen que los datos son accesibles.

### FASE 5 — Componentes reutilizables
**Objetivo**: Crear los ladrillos de la interfaz.
- **Archivos**: `ui/components/GameCard.kt`, `ui/components/AdBanner.kt`, `ui/components/SectionHeader.kt`, `ui/components/PremiumBadge.kt`.
- **Funcionalidades**: Tarjetas visuales, banners de "publicidad" simulada con lógica de visibilidad basada en el estado Pro.
- **Verificación**: Previews individuales de cada componente.

### FASE 6 — Splash + Home
**Objetivo**: La primera impresión de la app.
- **Archivos**: `ui/screens/SplashScreen.kt`, `ui/screens/HomeScreen.kt`.
- **Funcionalidades**: Animación de entrada. Home con secciones: "Destacados" (Carousel), "Tendencias" y "Próximos".
- **Verificación**: La app inicia con el logo y muestra la Home con datos reales.

### FASE 7 — Explore + búsqueda
**Objetivo**: Facilitar el descubrimiento de contenido.
- **Archivos**: `ui/screens/ExploreScreen.kt`, `ui/viewmodel/ExploreViewModel.kt`.
- **Funcionalidades**: Barra de búsqueda funcional, filtros por género (chips), grilla de resultados.
- **Verificación**: Escribir un nombre y que la lista se filtre en tiempo real.

### FASE 8 — Game Details
**Objetivo**: Mostrar toda la información de un título.
- **Archivos**: `ui/screens/DetailsScreen.kt`.
- **Funcionalidades**: Imagen de cabecera inmersiva, rating con estrellas, descripción larga, plataformas y botón de "Guardar".
- **Verificación**: Al tocar un juego en Home, se abre su ficha técnica correcta.

### FASE 9 — Biblioteca (My Library)
**Objetivo**: Gestión de la colección personal.
- **Archivos**: `ui/screens/LibraryScreen.kt`, `ui/viewmodel/GameViewModel.kt`.
- **Funcionalidades**: Lista de juegos marcados como favoritos.
- **Verificación**: Añadir un juego en detalles y comprobar que aparece en esta pantalla.

### FASE 10 — Perfil
**Objetivo**: Centro de control del usuario.
- **Archivos**: `ui/screens/ProfileScreen.kt`.
- **Funcionalidades**: Información de usuario, estadísticas simples (ej. "Juegos en biblioteca"), estado de suscripción.
- **Verificación**: Visualización coherente del perfil.

### FASE 11 — Premium y monetización simulada
**Objetivo**: Implementar la lógica de negocio Pro.
- **Archivos**: `ui/viewmodel/AppViewModel.kt` (Estado global), `ui/components/SubscriptionModal.kt`.
- **Funcionalidades**: Diálogo de suscripción, lógica para ocultar anuncios, desbloqueo de sección "Retro" en Home.
- **Verificación**: "Comprar" Pro y verificar que los anuncios desaparecen instantáneamente en toda la app.

### FASE 12 — Animaciones y pulido visual
**Objetivo**: Elevar la calidad a nivel "Premium".
- **Archivos**: Ajustes en múltiples pantallas.
- **Funcionalidades**: Transiciones entre pantallas suaves, efectos al presionar botones, elevación dinámica.
- **Verificación**: Sensación de fluidez general.

### FASE 13 — Pruebas y corrección de errores
**Objetivo**: Asegurar estabilidad.
- **Actividades**: Revisión de estados de error, comprobación de rotación de pantalla básica, limpieza de código.
- **Verificación**: La app funciona sin crashes y cumple con todos los requisitos.

---

## Verification Plan

### Automated Tests
- No se requieren tests instrumentales complejos debido a la naturaleza estática, pero se verificará la lógica de filtrado del `ExploreViewModel`.

### Manual Verification
1. **Flujo Premium**: Abrir Perfil -> Suscribirse -> Verificar desaparición de `AdBanner` en Home.
2. **Navegación**: Probar todos los iconos de la BottomBar y el botón Atrás desde Detalles.
3. **Búsqueda**: Buscar "Zelda" (o similar) y verificar que la UI responde correctamente.
4. **Persistencia en sesión**: Añadir a biblioteca, navegar a otra pantalla, volver a biblioteca y verificar que el juego sigue ahí.
