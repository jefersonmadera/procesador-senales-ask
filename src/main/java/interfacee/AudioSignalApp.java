package interfacee;

import logic.*;
import models.Signal;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Aplicación principal que puede procesar tanto señales generadas 
 * matemáticamente como archivos de audio reales
 */
public class AudioSignalApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== PROCESADOR DE SEÑALES DIGITALES ===");
        System.out.println("1. Usar señal matemática (seno 300Hz)");
        System.out.println("2. Cargar archivo de audio");
        System.out.print("Selecciona una opción (1 o 2): ");
        
        String option = scanner.nextLine().trim();
        Signal modSignal = null;
        
        try {
            if ("2".equals(option)) {
                modSignal = loadAudioFile(scanner);
            } else {
                modSignal = generateMathematicalSignal();
            }
            
            if (modSignal != null) {
                processSignal(modSignal);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    /**
     * Carga un archivo de audio desde el disco
     */
    private static Signal loadAudioFile(Scanner scanner) throws IOException, UnsupportedAudioFileException {
        System.out.println("\n=== CARGAR ARCHIVO DE AUDIO ===");
        System.out.println("Formatos soportados: WAV, MP3, AU, AIFF");
        System.out.println("Ejemplo de ruta: /home/usuario/musica/cancion.mp3");
        System.out.print("Ingresa la ruta del archivo: ");
        
        String filePath = scanner.nextLine().trim();
        
        System.out.print("¿Limitar duración? (s/n): ");
        String limitDuration = scanner.nextLine().trim().toLowerCase();
        
        Signal audioSignal;
        if ("s".equals(limitDuration) || "si".equals(limitDuration) || "sí".equals(limitDuration)) {
            System.out.print("Duración en segundos (ej: 5.0): ");
            double duration = Double.parseDouble(scanner.nextLine().trim());
            audioSignal = AudioFileReader.readAudioFile(filePath, duration);
        } else {
            audioSignal = AudioFileReader.readAudioFile(filePath);
        }
        
        // Verificar si necesitamos resampling
        double currentFs = audioSignal.getFs();
        System.out.println("Frecuencia actual: " + currentFs + " Hz");
        
        if (currentFs != 8000) {
            System.out.print("¿Convertir a 8000 Hz para compatibilidad? (s/n): ");
            String resample = scanner.nextLine().trim().toLowerCase();
            
            if ("s".equals(resample) || "si".equals(resample) || "sí".equals(resample)) {
                System.out.println("Convirtiendo a 8000 Hz...");
                audioSignal = AudioFileReader.resample(audioSignal, 8000);
                System.out.println("Conversión completada.");
            }
        }
        
        return audioSignal;
    }
    
    /**
     * Genera una señal matemática (código original)
     */
    private static Signal generateMathematicalSignal() {
        System.out.println("\n=== GENERANDO SEÑAL MATEMÁTICA ===");
        double fs = 8000;
        double duration = 0.01;
        
        // Señal seno de 300 Hz
        return Sampling.sample(t -> Math.sin(2 * Math.PI * 300 * t), fs, duration);
    }
    
    /**
     * Procesa la señal (PCM, Polar, ASK) - código original adaptado
     */
    private static void processSignal(Signal modSignal) {
        System.out.println("\n=== PROCESANDO SEÑAL ===");
        
        // Mostrar información de la señal de entrada
        System.out.println("Señal de entrada:");
        System.out.println("- Muestras: " + modSignal.getSamples().length);
        System.out.println("- Frecuencia de muestreo: " + modSignal.getFs() + " Hz");
        System.out.println("- Duración: " + (modSignal.getSamples().length / modSignal.getFs()) + " segundos");
        
        // 1. Codificación PCM
        System.out.println("\n--- Codificación PCM ---");
        PCMEncoder pcm = new PCMEncoder(8, -1.0, 1.0); // 8 bits, rango [-1,1]
        int[] levels = pcm.quantizeLevels(modSignal.getSamples());
        boolean[][] bitsPerSample = pcm.levelsToBits(levels);
        boolean[] pcmBits = pcm.flatten(bitsPerSample);
        System.out.println("Bits PCM generados: " + pcmBits.length);

        // 2. Codificación Polar
        System.out.println("\n--- Codificación Polar ---");
        double[] polarSignal = Polar.encode(pcmBits);
        System.out.println("Señal polar generada: " + polarSignal.length + " símbolos");

        // 3. Generar portadora para ASK
        System.out.println("\n--- Modulación ASK ---");
        double duration = modSignal.getSamples().length / modSignal.getFs();
        Signal carrier = ASKModulator.carrierSine(2000, modSignal.getFs(), duration, 1.0); // 2 kHz
        
        // 4. Modulación ASK
        Signal askSignal = ASKModulator.modulate(pcmBits, carrier, 1000, 0.1, 1.0); // bitRate=1 kHz
        System.out.println("Señal ASK generada: " + askSignal.getSamples().length + " muestras");

        // 5. Mostrar resultados
        showResults(modSignal, pcmBits, askSignal);
    }
    
    /**
     * Muestra los resultados del procesamiento
     */
    private static void showResults(Signal modSignal, boolean[] pcmBits, Signal askSignal) {
        System.out.println("\n=== RESULTADOS ===");
        
        System.out.println("Señal original (primeros 10 valores):");
        double[] samples = modSignal.getSamples();
        for (int i = 0; i < 10 && i < samples.length; i++) {
            System.out.printf("%.4f ", samples[i]);
        }
        
        System.out.println("\n\nBits PCM (primeros 32 bits):");
        for (int i = 0; i < 32 && i < pcmBits.length; i++) {
            System.out.print((pcmBits[i] ? "1" : "0"));
            if ((i + 1) % 8 == 0) System.out.print(" "); // Espacio cada byte
        }
        
        System.out.println("\n\nSeñal ASK (primeros 10 valores):");
        double[] askSamples = askSignal.getSamples();
        for (int i = 0; i < 10 && i < askSamples.length; i++) {
            System.out.printf("%.4f ", askSamples[i]);
        }
        
        System.out.println("\n\n=== PROCESAMIENTO COMPLETADO ===");
    }
}