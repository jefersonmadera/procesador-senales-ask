# Directorio para Archivos de Audio de Prueba

Coloca aquí tus archivos de audio para probar el procesamiento de señales:

## Formatos Soportados:
- **.wav** - Formato WAV (recomendado para mejor compatibilidad)
- **.mp3** - Formato MP3 (requiere las dependencias adicionales)
- **.au** - Formato AU
- **.aiff** - Formato AIFF

## Ejemplos de archivos que puedes usar:
- Grabaciones de voz
- Fragmentos de música
- Tonos generados
- Cualquier archivo de audio corto (recomendado < 10 segundos para pruebas)

## Cómo usar:
1. Copia tus archivos de audio a este directorio
2. Ejecuta `AudioSignalApp.java`
3. Selecciona la opción "2. Cargar archivo de audio"
4. Ingresa la ruta completa al archivo (ejemplo: `/ruta/completa/al/archivo.mp3`)

## Notas:
- Para mejores resultados, usa archivos con frecuencia de muestreo de 8000 Hz
- El programa puede convertir automáticamente otras frecuencias de muestreo
- Archivos muy largos pueden generar muchos datos - considera limitar la duración