package interfacee;

import logic.*;
import models.Signal;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * Aplicación simple para probar la lectura de archivos de audio sin interacción
 */
public class SimpleAudioTest {
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE LECTURA DE ARCHIVOS DE AUDIO ===");
        
        // Archivos de prueba
        String[] testFiles = {
            "audio-samples/tono_300Hz_5seg.wav",
            "audio-samples/tono_440Hz_2seg.wav", 
            "audio-samples/tono_1000Hz_3seg.wav"
        };
        
        for (String filePath : testFiles) {
            System.out.println("\n--- Probando archivo: " + filePath + " ---");
            
            try {
                // Cargar archivo de audio
                Signal audioSignal = AudioFileReader.readAudioFile(filePath, 2.0); // Limitar a 2 segundos
                
                // Procesar la señal
                processAndShowSignal(audioSignal);
                
            } catch (IOException | UnsupportedAudioFileException e) {
                System.err.println("Error al cargar " + filePath + ": " + e.getMessage());
            }
        }
        
        System.out.println("\n=== COMPARACIÓN CON SEÑAL MATEMÁTICA ===");
        try {
            // Comparar con señal matemática (como en el código original)
            Signal mathSignal = Sampling.sample(t -> Math.sin(2 * Math.PI * 300 * t), 8000, 0.01);
            System.out.println("\nSeñal matemática (300Hz):");
            processAndShowSignal(mathSignal);
        } catch (Exception e) {
            System.err.println("Error con señal matemática: " + e.getMessage());
        }
    }
    
    /**
     * Procesa una señal y muestra información básica
     */
    private static void processAndShowSignal(Signal signal) {
        System.out.println("Información de la señal:");
        System.out.println("- Muestras: " + signal.getSamples().length);
        System.out.println("- Frecuencia de muestreo: " + signal.getFs() + " Hz");
        System.out.println("- Duración: " + (signal.getSamples().length / signal.getFs()) + " segundos");
        
        // Mostrar primeras muestras
        System.out.println("Primeras 10 muestras:");
        double[] samples = signal.getSamples();
        for (int i = 0; i < 10 && i < samples.length; i++) {
            System.out.printf("  [%d]: %.4f%n", i, samples[i]);
        }
        
        // Proceso PCM básico
        PCMEncoder pcm = new PCMEncoder(8, -1.0, 1.0);
        int[] levels = pcm.quantizeLevels(signal.getSamples());
        boolean[][] bitsPerSample = pcm.levelsToBits(levels);
        boolean[] pcmBits = pcm.flatten(bitsPerSample);
        
        System.out.println("Codificación PCM:");
        System.out.println("- Bits generados: " + pcmBits.length);
        System.out.println("- Primeros 16 bits: ");
        for (int i = 0; i < 16 && i < pcmBits.length; i++) {
            System.out.print(pcmBits[i] ? "1" : "0");
        }
        System.out.println();
    }
}