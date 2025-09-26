package logic;

import models.Signal;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para leer archivos de audio (WAV, MP3) y convertirlos en señales digitales
 * 
 * @author xexpl
 */
public class AudioFileReader {
    
    /**
     * Lee un archivo de audio y lo convierte en una señal digital
     * 
     * @param filePath Ruta del archivo de audio (WAV, MP3, etc.)
     * @param maxDurationSeconds Duración máxima a leer en segundos (0 = todo el archivo)
     * @return Signal con las muestras del audio y su frecuencia de muestreo
     * @throws IOException Si hay error al leer el archivo
     * @throws UnsupportedAudioFileException Si el formato no es soportado
     */
    public static Signal readAudioFile(String filePath, double maxDurationSeconds) 
            throws IOException, UnsupportedAudioFileException {
        
        File audioFile = new File(filePath);
        
        // Verificar que el archivo existe
        if (!audioFile.exists()) {
            throw new IOException("El archivo no existe: " + filePath);
        }
        
        // Obtener el stream de audio
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = audioInputStream.getFormat();
        
        // Información del audio
        float sampleRate = format.getSampleRate();
        int channels = format.getChannels();
        int sampleSizeInBits = format.getSampleSizeInBits();
        boolean bigEndian = format.isBigEndian();
        
        System.out.println("=== INFORMACIÓN DEL ARCHIVO DE AUDIO ===");
        System.out.println("Archivo: " + audioFile.getName());
        System.out.println("Frecuencia de muestreo: " + sampleRate + " Hz");
        System.out.println("Canales: " + channels);
        System.out.println("Bits por muestra: " + sampleSizeInBits);
        System.out.println("Formato: " + format.toString());
        
        // Convertir a formato estándar si es necesario (PCM 16-bit)
        AudioFormat targetFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sampleRate,
            16,  // 16 bits por muestra
            channels,
            channels * 2,  // frame size (2 bytes por muestra por canal)
            sampleRate,
            false  // little endian
        );
        
        // Convertir si el formato no es el deseado
        if (!format.equals(targetFormat)) {
            System.out.println("Convirtiendo formato de audio...");
            audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
            format = targetFormat;
        }
        
        // Calcular cuántas muestras leer
        long totalFrames = audioInputStream.getFrameLength();
        long framesToRead = totalFrames;
        
        if (maxDurationSeconds > 0) {
            long maxFrames = (long) (maxDurationSeconds * sampleRate);
            framesToRead = Math.min(totalFrames, maxFrames);
            System.out.println("Limitando a " + maxDurationSeconds + " segundos (" + framesToRead + " frames)");
        }
        
        // Leer las muestras
        List<Double> samples = new ArrayList<>();
        byte[] buffer = new byte[4096]; // Buffer de lectura
        int bytesPerFrame = format.getFrameSize();
        long framesRead = 0;
        
        try {
            int bytesRead;
            while ((bytesRead = audioInputStream.read(buffer)) != -1 && framesRead < framesToRead) {
                
                // Procesar cada frame (muestra)
                for (int i = 0; i < bytesRead && framesRead < framesToRead; i += bytesPerFrame) {
                    
                    // Procesar cada canal
                    for (int channel = 0; channel < channels; channel++) {
                        int byteOffset = i + (channel * 2); // 2 bytes por muestra (16-bit)
                        
                        if (byteOffset + 1 < bytesRead) {
                            // Leer muestra de 16 bits (little endian)
                            int sample16 = (buffer[byteOffset + 1] << 8) | (buffer[byteOffset] & 0xFF);
                            
                            // Convertir a double normalizado [-1.0, 1.0]
                            double normalizedSample = sample16 / 32768.0;
                            samples.add(normalizedSample);
                            
                            // Solo tomar el primer canal si es estéreo
                            if (channel == 0) {
                                break;
                            }
                        }
                    }
                    framesRead++;
                }
            }
        } finally {
            audioInputStream.close();
        }
        
        // Convertir la lista a array
        double[] samplesArray = samples.stream().mapToDouble(Double::doubleValue).toArray();
        
        System.out.println("Audio cargado: " + samplesArray.length + " muestras");
        System.out.println("Duración: " + (samplesArray.length / sampleRate) + " segundos");
        System.out.println("=========================================");
        
        return new Signal(samplesArray, sampleRate);
    }
    
    /**
     * Sobrecarga para leer todo el archivo sin límite de duración
     */
    public static Signal readAudioFile(String filePath) 
            throws IOException, UnsupportedAudioFileException {
        return readAudioFile(filePath, 0); // 0 = leer todo el archivo
    }
    
    /**
     * Redimensiona una señal a una nueva frecuencia de muestreo (resampling básico)
     */
    public static Signal resample(Signal original, double newSampleRate) {
        double[] originalSamples = original.getSamples();
        double originalSampleRate = original.getFs();
        
        // Calcular factor de resampling
        double resampleFactor = newSampleRate / originalSampleRate;
        int newLength = (int) Math.round(originalSamples.length * resampleFactor);
        
        double[] newSamples = new double[newLength];
        
        // Resampling básico por interpolación lineal
        for (int i = 0; i < newLength; i++) {
            double originalIndex = i / resampleFactor;
            int index1 = (int) Math.floor(originalIndex);
            int index2 = Math.min(index1 + 1, originalSamples.length - 1);
            
            if (index1 < originalSamples.length) {
                double weight = originalIndex - index1;
                newSamples[i] = originalSamples[index1] * (1 - weight) + 
                               originalSamples[index2] * weight;
            }
        }
        
        return new Signal(newSamples, newSampleRate);
    }
}