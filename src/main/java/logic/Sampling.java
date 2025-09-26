/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logic;

import java.util.function.DoubleUnaryOperator;
import models.Signal;

/**
 *
 * @author xexpl
 */
public class Sampling {
    /**
     * Convierte una señal analógica en una señal digital muestreada.
     * 
     * @param f        La señal analógica como función matemática f(t)
     * @param fs       Frecuencia de muestreo (Hz)
     * @param duration Duración de la señal en segundos
     * @return         Objeto Signal que contiene las muestras y la frecuencia
     */
    public static Signal sample(DoubleUnaryOperator f, double fs, double duration) {
        // Calcular el número total de muestras que se van a tomar
        int N = (int) Math.round(fs * duration); 
        // Creamos un arreglo para almacenar los valores de la señal muestreada
        double[] x = new double[N]; 
        System.out.println(N+" Muestras");

        // Iteramos sobre cada muestra
        for (int n = 0; n < N; n++) {
            // Calculamos el tiempo real de esta muestra
            double t = n / fs; 
            // Evaluamos la señal analógica en el tiempo t y guardamos el valor
            x[n] = f.applyAsDouble(t);  
            // Esto transforma la señal continua en valores discretos
        }

        // Devolvemos un objeto Signal que contiene:
        // 1. El arreglo de muestras x[]
        // 2. La frecuencia de muestreo fs
        return new Signal(x, fs); 
    }
}
