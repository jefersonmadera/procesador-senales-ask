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
