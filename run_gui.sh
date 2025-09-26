#!/bin/bash

# Script para compilar y empaquetar el proyecto, copiar el JAR ejecutable a dist/
# y arrancar la aplicación preservando las variables necesarias (LANG, DISPLAY, etc.)

set -euo pipefail

echo "═══════════════════════════════════════════"
echo "  PROCESADOR DE SEÑALES DIGITALES - GUI  "
echo "═══════════════════════════════════════════"
echo ""

# Verificar dependencias mínimas
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java no está instalado. Por favor instala Java 11 o superior."
    exit 1
fi
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven no está instalado. Por favor instala Apache Maven."
    exit 1
fi

echo "🔧 Compilando proyecto con Maven..."
mvn -DskipTests package

echo "✅ Empaquetado exitoso"
mkdir -p dist

# Localizar JAR (preferir shaded jar generado por shade, fallback a cualquier jar en target)
JAR_SHADDED="$(ls target/*-shaded.jar 2>/dev/null | head -n 1 || true)"
JAR_SIMPLE="$(ls target/*.jar 2>/dev/null | grep -v '\-sources\|original' | head -n 1 || true)"
JAR_TO_USE="${JAR_SHADDED:-$JAR_SIMPLE}"

if [ -z "${JAR_TO_USE}" ] || [ ! -f "${JAR_TO_USE}" ]; then
    echo "❌ No se encontró JAR en target/. Revisa la compilación."; exit 2
fi

echo "Usando JAR: $JAR_TO_USE"
DEST_JAR="dist/projectSignals.jar"
cp -f "$JAR_TO_USE" "$DEST_JAR"
echo "JAR copiado a: $DEST_JAR"

echo "Creando wrapper de lanzamiento en dist/run.sh"
cat > dist/run.sh << 'EOF'
#!/bin/sh
# Lanzador para projectSignals.jar — ejecuta con entorno reducido y preserva LANG y variables X
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"
JAR="projectSignals.jar"
if [ ! -f "$JAR" ]; then
  echo "No se encontró $JAR en $DIR"; exit 1
fi
# Ejecuta con entorno limpio, preservando las variables necesarias
env -i LANG="$LANG" PATH=/usr/bin HOME="$HOME" DISPLAY="$DISPLAY" XAUTHORITY="$XAUTHORITY" XDG_RUNTIME_DIR="$XDG_RUNTIME_DIR" \
  java -Djava.awt.headless=false -jar "$JAR"
EOF
chmod +x dist/run.sh || true

echo "Puedes distribuir el archivo 'dist/projectSignals.jar' y usar 'dist/run.sh' para lanzarlo."

echo "🚀 Iniciando aplicación (en background) y registrando salida en /tmp/projectSignals_gui.log"
nohup env -i LANG="$LANG" PATH=/usr/bin HOME="$HOME" DISPLAY="$DISPLAY" XAUTHORITY="$XAUTHORITY" XDG_RUNTIME_DIR="$XDG_RUNTIME_DIR" \
  java -Djava.awt.headless=false -jar "$DEST_JAR" > /tmp/projectSignals_gui.log 2>&1 &
PID=$!
echo "PID: $PID"
echo "Log: /tmp/projectSignals_gui.log"
echo "Hecho. Si necesitas que lo pare, usa: kill $PID"