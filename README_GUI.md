# Procesador de Señales Digitales - Interfaz Gráfica 🎵

Una aplicación de escritorio con interfaz gráfica para procesar señales digitales, implementando una cadena completa de transmisión digital con modulación ASK.

## 🚀 Cómo Ejecutar

### Opción 1: Script Automático (Recomendado)

#### Linux/Mac:
```bash
./run_gui.sh
```

#### Windows:
```cmd
run_gui.bat
```

### Opción 2: Comandos Maven
```bash
mvn compile
mvn exec:java
```

### Opción 3: Ejecutar Directamente
```bash
mvn compile
java -cp target/classes gui.SignalProcessorGUI
```

## 📋 Requisitos

- **Java 11 o superior**
- **Apache Maven**
- **Sistema gráfico** (X11 en Linux, GUI en Windows/Mac)

## 🎛️ Características de la Interfaz

### 🎵 Procesamiento de Archivos de Audio
- **Explorador de archivos** integrado
- **Formatos soportados**: WAV, MP3, AU, AIFF  
- **Filtro automático** de archivos de audio
- **Límite de duración** configurable
- **Procesamiento en tiempo real**

### 📐 Señales Matemáticas
- **Generador de senoides** configurable
- **Frecuencia ajustable** (50-5000 Hz)
- **Parámetros predefinidos** optimizados

### 🔧 Generador de Archivos de Ejemplo
- **Creación automática** de archivos WAV de prueba
- **Diferentes frecuencias**: 300Hz, 440Hz, 1000Hz
- **Duraciones variables**: 2-5 segundos

### 📊 Visualización de Resultados
- **Área de texto con scroll** para resultados detallados
- **Información completa** del procesamiento
- **Formato legible** con iconos y separadores
- **Progreso visual** con barra de estado

## 🔬 Proceso de Análisis

La interfaz muestra el procesamiento paso a paso:

1. **📊 Análisis de Señal Original**
   - Número de muestras
   - Frecuencia de muestreo  
   - Duración total
   - Primeras 10 muestras

2. **💻 Codificación PCM**
   - 8 bits por muestra
   - Rango [-1.0, 1.0]
   - Visualización de bits generados

3. **⚡ Codificación Polar NRZ**
   - Conversión a niveles ±1V
   - Símbolos polares generados

4. **📡 Modulación ASK**
   - Portadora de 2000 Hz
   - Tasa de 1000 bps
   - Amplitudes configurables
   - Señal lista para transmisión

## 🎯 Cómo Usar

### Para Archivos de Audio:
1. Haz clic en **"Explorar..."**
2. Selecciona tu archivo de audio
3. (Opcional) Configura límite de duración
4. Haz clic en **"🎵 Procesar Archivo de Audio"**

### Para Señales Matemáticas:
1. Ajusta la frecuencia deseada (Hz)
2. Haz clic en **"📐 Procesar Señal Matemática"**

### Para Generar Ejemplos:
1. Haz clic en **"🔧 Generar Archivos de Ejemplo"**
2. Los archivos se crearán en `audio-samples/`

## 📁 Estructura de Archivos

```
projectSignals/
├── src/main/java/
│   ├── gui/
│   │   └── SignalProcessorGUI.java    # Interfaz gráfica principal
│   ├── interfacee/
│   │   ├── SignalApp.java             # Versión consola original
│   │   ├── AudioSignalApp.java        # Versión consola con audio
│   │   └── SimpleAudioTest.java       # Pruebas simples
│   ├── logic/
│   │   ├── AudioFileReader.java       # Lector de archivos de audio
│   │   ├── AudioGenerator.java        # Generador de ejemplos
│   │   ├── ASKModulator.java          # Modulación ASK
│   │   ├── PCMEncoder.java            # Codificación PCM
│   │   ├── Polar.java                 # Codificación Polar
│   │   └── Sampling.java              # Muestreo de señales
│   └── models/
│       └── Signal.java                # Modelo de datos
├── audio-samples/                     # Archivos de audio de ejemplo
├── run_gui.sh                         # Script de lanzamiento Linux/Mac
├── run_gui.bat                        # Script de lanzamiento Windows
└── pom.xml                            # Configuración Maven
```

## 🎨 Capturas de Pantalla

La interfaz incluye:
- **🎯 Panel de selección** con explorador de archivos
- **⚙️ Panel de configuración** con opciones ajustables  
- **🎛️ Panel de botones** con acciones principales
- **📄 Área de resultados** con scroll y formato profesional
- **📊 Barra de estado** con progreso visual

## 🔧 Solución de Problemas

### Error: "Java no encontrado"
```bash
# Instalar Java (Ubuntu/Debian)
sudo apt install openjdk-11-jdk

# Verificar instalación
java -version
```

### Error: "Maven no encontrado"  
```bash
# Instalar Maven (Ubuntu/Debian)
sudo apt install maven

# Verificar instalación
mvn -version
```

### Error: "Display no disponible"
- Asegúrate de tener un entorno gráfico activo
- En SSH, usa `ssh -X` para habilitar forwarding de X11

## 📚 Tecnologías Utilizadas

- **Java Swing** - Interfaz gráfica nativa
- **Java Sound API** - Procesamiento de audio
- **SwingWorker** - Procesamiento asíncrono
- **Maven** - Gestión de dependencias
- **MP3SPI** - Soporte para archivos MP3

## 🎓 Conceptos Implementados

- **Procesamiento Digital de Señales**
- **Modulación ASK (Amplitude Shift Keying)**  
- **Codificación PCM (Pulse Code Modulation)**
- **Codificación Polar NRZ**
- **Muestreo de señales analógicas**
- **Interfaz gráfica moderna con Swing**

---

## 🎵 ¡Disfruta experimentando con el procesamiento de señales digitales! 🎵