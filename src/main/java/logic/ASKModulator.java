/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logic;

import models.Signal;

/**
 *
 * @author xexpl
 */
public class ASKModulator {
    /**
     * Genera una portadora seno
     *
     * @param fc        Frecuencia de la portadora (Hz)
     * @param fs        Frecuencia de muestreo (Hz)
     * @param duration  Duración de la señal (s)
     * @param amplitude Amplitud de la portadora
     * @return          Objeto Signal con la onda seno generada
     */
    public static Signal carrierSine(double fc, double fs, double duration, double amplitude) {
        int N = (int) Math.round(duration * fs);  // Número de muestras
        double[] s = new double[N];               // Arreglo para almacenar la señal

        // Generamos la señal seno muestra a muestra
        for (int n = 0; n < N; n++) {
            double t = n / fs;                    // Tiempo de la muestra
            s[n] = amplitude * Math.sin(2 * Math.PI * fc * t);  // Onda seno
        }

        // Devolvemos la señal encapsulada en un objeto Signal
        return new Signal(s, fs);
    }

    /**
     * Modulación ASK (Amplitude Shift Keying)
     *
     * @param bits      Arreglo de bits boolean[] a modular
     * @param carrier   Señal portadora
     * @param bitRate   Tasa de bits (bits por segundo)
     * @param A0        Amplitud para bit 0
     * @param A1        Amplitud para bit 1
     * @return          Señal modulada ASK
     */
    public static Signal modulate(boolean[] bits, Signal carrier, double bitRate, double A0, double A1) {
        // Número de muestras de la portadora que corresponden a un bit
        int samplesPerBit = (int) Math.max(1, Math.round(carrier.getFs() / bitRate));

        // Total de muestras de la señal modulada
        int N = samplesPerBit * bits.length;
        double[] y = new double[N]; // Arreglo de la señal modulada

        // Iteramos sobre cada bit
        for (int i = 0; i < bits.length; i++) {
            double A = bits[i] ? A1 : A0; // Seleccionamos amplitud según el bit

            // Iteramos sobre cada muestra que corresponde a ese bit
            for (int k = 0; k < samplesPerBit; k++) {
                int n = i * samplesPerBit + k; // Índice de la muestra en la señal final
                double c = (n < carrier.getSamples().length) ? carrier.getSamples()[n] : 0.0; 
                // Tomamos la muestra de la portadora si está disponible, sino cero

                y[n] = A * c; // Aplicamos la modulación ASK
            }
        }

        // Devolvemos la señal modulada encapsulada en un objeto Signal
        return new Signal(y, carrier.getFs());
    }
}

