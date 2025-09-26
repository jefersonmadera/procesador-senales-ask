
package interfacee;
import java.util.function.DoubleUnaryOperator;
import logic.*;
import models.Signal;
public class SignalApp {

    public static void main(String[] args) {
         // 1. Generar señal moduladora (analógica)
        double fs = 8000; // Frecuencia de muestreo tomamos 8000 muestras por segundo de la señal analógica
        double duration = 0.01; // Duración de la señal 10 milisegundos → pequeña señal para ejemplo
        
        DoubleUnaryOperator modulatingSignal = t -> Math.sin(2 * Math.PI * 300 * t); // Definición de la señal moduladora como una función matemática
        // Esto representa una señal senoidal de 300 Hz
        // t → tiempo en segundos
        // 2 * π * 300 * t → ángulo de la función seno en radianes
        // Math.sin(...) → devuelve el valor de la onda en ese instante t
        
        Signal modSignal = Sampling.sample(modulatingSignal, fs, duration); // Muestreo de la señal analógica
        // Aquí se convierte la señal continua (analógica) en una serie de valores discretos
        // fs → frecuencia de muestreo
        // duration → tiempo total de la señal
        // modSignal.samples contendrá un arreglo de valores dobles representando la señal digitalizada
        
        // 2. Codificación PCM de la señal moduladora
        PCMEncoder pcm = new PCMEncoder(8, -1.0, 1.0); // 8 bits, rango [-1,1]
        int[] levels = pcm.quantizeLevels(modSignal.getSamples());
        boolean[][] bitsPerSample = pcm.levelsToBits(levels);
        boolean[] pcmBits = pcm.flatten(bitsPerSample);

        // 3. Codificación Polar
        double[] polarSignal = Polar.encode(pcmBits);

        // 4. Generar portadora para ASK
        Signal carrier = ASKModulator.carrierSine(2000, fs, duration, 1.0); // 2 kHz

        // 5. Modulación ASK con la señal PCM polar
        Signal askSignal = ASKModulator.modulate(pcmBits, carrier, 1000, 0.1, 1.0); // bitRate=1 kHz

        // 6. Mostrar resultados
        System.out.println("Señal moduladora (primeros 10 valores):");
        for (int i = 0; i < 10 && i < modSignal.getSamples().length; i++) {
            System.out.printf("%.4f ", modSignal.getSamples()[i]);
        }
        System.out.println("\nBits PCM (primeros 16 bits):");
        for (int i = 0; i < 16 && i < pcmBits.length; i++) {
            System.out.print((pcmBits[i] ? "1" : "0") + " ");
        }
        System.out.println("\nSeñal ASK (primeros 10 valores):");
        for (int i = 0; i < 10 && i < askSignal.getSamples().length; i++) {
            System.out.printf("%.4f ", askSignal.getSamples()[i]);
        }
    }
}
