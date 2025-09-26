# Procesador de Señales Digitales - Modulación ASK

## Descripción
Simulador de procesamiento de señales digitales que implementa una cadena completa de transmisión:

1. 📊 **Muestreo de señal analógica**
2. 💻 **Codificación PCM** (Pulse Code Modulation)
3. ⚡ **Codificación Polar NRZ**
4. 📡 **Modulación ASK** (Amplitude Shift Keying)

## Características
- Procesamiento de archivos de audio reales (WAV, MP3, AU, AIFF)
- Generación de señales matemáticas (senoidales)
- Visualizador interactivo con:
  - Zoom y desplazamiento (pan)
  - Downsampling eficiente para señales largas
  - Exportación de imágenes PNG
  - Reproducción de audio modulado
  - Captura de micrófono en tiempo real
- Interfaz gráfica Swing intuitiva
- JAR ejecutable independiente

## Requisitos del Sistema
- **Java**: JRE/JDK 11 o superior
- **Maven**: Para compilar desde código fuente
- **SO**: Windows, Linux, macOS (con soporte para GUI)

## Instalación y Ejecución

### Opción 1: Ejecutar JAR precompilado
```bash
# Descargar el JAR desde releases o usar dist/projectSignals.jar
cd dist/
./run.sh
# O directamente:
java -jar projectSignals.jar
```

### Opción 2: Compilar desde código fuente
```bash
# Clonar repositorio
git clone https://github.com/tu-usuario/projectSignals.git
cd projectSignals

# Compilar y ejecutar
./run_gui.sh
```

### Opción 3: Desarrollo con Maven
```bash
mvn compile exec:java
# O para empaquetar:
mvn package
java -jar target/projectSignals-1.0-SNAPSHOT.jar
```

## Uso de la Aplicación

### 1. Procesamiento de Archivos de Audio
- Click en **"Explorar..."** para seleccionar archivo WAV/MP3
- Configurar duración límite si es necesario
- Click en **"🎵 Procesar Archivo de Audio"**

### 2. Señales Matemáticas
- Ajustar frecuencia en Hz
- Click en **"📐 Procesar Señal Matemática"**

### 3. Visualizador Avanzado
- Click en **"👁️ Ver Visualizador"** para abrir ventana de gráficos
- **Controles disponibles:**
  - **Zoom:** Slider para acercar/alejar
  - **Pan:** Scrollbar horizontal o arrastrar con mouse
  - **Guardar Imagen:** Exportar vista actual como PNG
  - **Reproducir/Detener:** Audio de la señal ASK modulada
  - **Micrófono:** Captura y visualización en tiempo real

### 4. Archivos de Ejemplo
- Click en **"🔧 Generar Archivos de Ejemplo"** para crear WAVs de prueba

## Estructura del Proyecto
```
src/main/java/
├── gui/
│   ├── SignalProcessorGUI.java      # Interfaz principal
│   └── SignalVisualizerGUI.java     # Visualizador avanzado
├── logic/
│   ├── Sampling.java                # Muestreo de señales
│   ├── PCMEncoder.java              # Codificación PCM
│   ├── Polar.java                   # Codificación Polar NRZ
│   ├── ASKModulator.java            # Modulación ASK
│   ├── AudioFileReader.java         # Lectura archivos audio
│   └── AudioGenerator.java          # Generación de ejemplos
├── models/
│   └── Signal.java                  # Modelo de señal
└── interfacee/
    └── SignalApp.java               # Aplicaciones console
```

## Algoritmos Implementados

### Muestreo
- Conversión de función analógica a digital
- Frecuencia de muestreo configurable
- Preservación de información según teorema de Nyquist

### Cuantización PCM
- **Parámetros:** n bits, rango [xmin, xmax]
- **Niveles:** L = 2^n
- **Paso:** q = (xmax - xmin) / L
- **Mapeo:** nivel = floor((x - xmin) / q)

### Codificación Polar NRZ
- **Mapeo:** bit 0 → -1V, bit 1 → +1V
- Preparación para modulación

### Modulación ASK
- **Portadora:** c(t) = cos(2πf_c×t)
- **Modulada:** s(t) = A(t) × c(t)
- A(t) varía según bits (ej: 0.1V para '0', 1.0V para '1')

## Dependencias
- **Audio MP3:** `mp3spi`, `jlayer`, `tritonus-share`
- **Testing:** JUnit 5
- **Build:** Maven 3.8+

## Solución de Problemas

### Error de librerías nativas (Linux/snap)
```bash
# Usar el wrapper que sanea el entorno:
./dist/run.sh
# O manualmente:
env -i LANG="$LANG" DISPLAY="$DISPLAY" java -jar projectSignals.jar
```

### Sin audio/micrófono
- Verificar permisos de audio del sistema
- Comprobar que dispositivos no estén en uso por otras aplicaciones

### Archivos con acentos (InvalidPathException)
- El wrapper `run.sh` preserva `LANG` para evitar este problema

## Contribuir
1. Fork del repositorio
2. Crear branch para features: `git checkout -b feature/nueva-funcionalidad`
3. Commit cambios: `git commit -m "Añadir nueva funcionalidad"`
4. Push: `git push origin feature/nueva-funcionalidad`
5. Crear Pull Request

## Licencia
Este proyecto está bajo licencia MIT. Ver archivo `LICENSE` para detalles.

## Autor
Desarrollado para curso de Transmisión de Datos - [Año 2025]

## Screenshots
_Añadir capturas de pantalla de la GUI principal y visualizador_