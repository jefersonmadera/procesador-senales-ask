package logic;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utilidad para generar archivos de audio de ejemplo para pruebas
 */
public class AudioGenerator {
    
    /**
     * Genera un archivo WAV con un tono de prueba
     * 
     * @param outputPath Ruta donde guardar el archivo
     * @param frequency Frecuencia del tono en Hz
     * @param durationSeconds Duración en segundos
     * @param sampleRate Frecuencia de muestreo
     * @throws IOException Si hay error al escribir el archivo
     */
    public static void generateToneWAV(String outputPath, double frequency, 
                                     double durationSeconds, float sampleRate) throws IOException {
        
        // Calcular número de muestras
        int numSamples = (int) (durationSeconds * sampleRate);
        
        // Generar las muestras de audio
        byte[] audioData = new byte[numSamples * 2]; // 2 bytes por muestra (16-bit)
        ByteBuffer buffer = ByteBuffer.wrap(audioData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Generar tono senoidal
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            double sample = Math.sin(2 * Math.PI * frequency * time);
            
            // Convertir a 16-bit signed integer
            short sample16 = (short) (sample * Short.MAX_VALUE * 0.8); // 0.8 para evitar clipping
            buffer.putShort(sample16);
        }
        
        // Crear formato de audio
        AudioFormat format = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sampleRate,
            16, // 16 bits por muestra
            1,  // mono
            2,  // 2 bytes por frame
            sampleRate,
            false // little endian
        );
        
        // Crear stream de audio
        ByteArrayInputStream byteStream = new ByteArrayInputStream(audioData);
        AudioInputStream audioStream = new AudioInputStream(byteStream, format, numSamples);
        
        // Escribir archivo WAV
        File outputFile = new File(outputPath);
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile);
        
        System.out.println("Archivo de audio generado: " + outputPath);
        System.out.println("- Frecuencia: " + frequency + " Hz");
        System.out.println("- Duración: " + durationSeconds + " segundos");
        System.out.println("- Frecuencia de muestreo: " + sampleRate + " Hz");
        
        // Cerrar streams
        audioStream.close();
        byteStream.close();
    }
    
    /**
     * Genera varios archivos de ejemplo para pruebas
     */
    public static void generateSampleFiles(String directory) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // Generar varios tonos de ejemplo
        generateToneWAV(directory + "/tono_440Hz_2seg.wav", 440.0, 2.0, 8000); // La musical
        generateToneWAV(directory + "/tono_1000Hz_3seg.wav", 1000.0, 3.0, 8000); // 1 kHz
        generateToneWAV(directory + "/tono_300Hz_5seg.wav", 300.0, 5.0, 8000); // 300 Hz (como en el ejemplo)
        
        System.out.println("\nArchivos de ejemplo generados en: " + directory);
    }
    
    /**
     * Aplicación para generar archivos de ejemplo
     */
    public static void main(String[] args) {
        try {
            String sampleDir = "audio-samples";
            generateSampleFiles(sampleDir);
        } catch (IOException e) {
            System.err.println("Error al generar archivos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}