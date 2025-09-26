/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logic;

/**
 *
 * @author xexpl
 */
public class Polar {
    /**
     * Codificación Polar (NRZ)
     * Convierte un arreglo de bits booleanos en niveles +1/-1
     * 
     * NRZ (Non-Return-to-Zero) Polar:
     *  - bit 1 -> +1
     *  - bit 0 -> -1
     *
     * @param bits arreglo de bits booleanos
     * @return arreglo de doubles con valores +1/-1
     */
    public static double[] encode(boolean[] bits) {
        // Creamos un arreglo de salida de tamaño igual al número de bits
        double[] y = new double[bits.length];

        // Recorremos cada bit
        for (int i = 0; i < bits.length; i++) {
            // Si el bit es true (1) -> +1, si es false (0) -> -1
            y[i] = bits[i] ? +1.0 : -1.0;
        }

        // Retornamos la señal codificada
        return y;
    }
}
