/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logic;

/**
 *
 * @author xexpl
 */
public class PCMEncoder {
    final int nBits;      // Cantidad de bits por muestra
    final double xmin;    // Valor mínimo de la señal analógica
    final double xmax;    // Valor máximo de la señal analógica
    final int L;          // Número de niveles de cuantización (2^nBits)
    final double q;       // Tamaño de cuantización

    // Constructor de la clase, recibe los parámetros necesarios
    public PCMEncoder(int nBits, double xmin, double xmax) {
        this.nBits = nBits;
        this.xmin = xmin;
        this.xmax = xmax;
        this.L = 1 << nBits;  // 2^nBits niveles
        this.q = (xmax - xmin) / L; // Calcular el tamaño de cada nivel
    }

    /**
     * Convierte una señal digital en niveles discretos de cuantización
     * 
     * @param x Arreglo de doubles que representa la señal digital muestreada
     * @return  Arreglo de enteros con los niveles de cuantización (0 a L-1)
     */
    public int[] quantizeLevels(double[] x) {
        // Crear un arreglo para almacenar los niveles cuantizados
        int[] levels = new int[x.length];

        // Iterar sobre cada muestra de la señal
        for (int i = 0; i < x.length; i++) {
            // Saturar la señal para que no se salga del rango [xmin, xmax)
            // Se resta un pequeño valor 1e-12 para evitar que xmax caiga fuera del rango
            double xi = Math.max(xmin, Math.min(xmax - 1e-12, x[i])); 

            // Calcular el nivel de cuantización
            // Fórmula: k = floor((xi - xmin) / q)
            int k = (int) Math.floor((xi - xmin) / q);  

            // Asegurar que el nivel esté dentro del rango [0, L-1]
            levels[i] = Math.max(0, Math.min(L - 1, k));  
        }

        // Devolver el arreglo de niveles cuantizados
        return levels;
    }

    /**
     * Convierte un arreglo de niveles cuantizados en un arreglo de bits
     * 
     * @param levels Arreglo de enteros con niveles cuantizados (0 a L-1)
     * @return       Arreglo bidimensional boolean[][] donde cada fila representa
     *               la codificación en bits del nivel correspondiente
     */
    public boolean[][] levelsToBits(int[] levels) {
        // Creamos un arreglo de booleanos para almacenar los bits
        // Cada fila corresponde a un nivel, cada columna a un bit
        boolean[][] bits = new boolean[levels.length][nBits];

        // Iteramos sobre cada nivel
        for (int i = 0; i < levels.length; i++) {
            int val = levels[i]; // Tomamos el valor entero del nivel

            // Iteramos sobre cada bit
            for (int b = 0; b < nBits; b++) {
                int shift = nBits - 1 - b; // Calculamos la posición del bit (de más significativo a menos)
                bits[i][b] = ((val >> shift) & 1) == 1; 
                // Explicación:
                // 1. val >> shift → desplazamos el bit deseado a la posición 0
                // 2. & 1 → obtenemos solo el bit menos significativo (0 o 1)
                // 3. == 1 → convertimos a boolean: true si es 1, false si es 0
            }
        }
        // Devolvemos el arreglo de bits
        return bits;
    }
    
    /**
     * Convierte un arreglo bidimensional de bits por muestra
     * en un arreglo unidimensional de bits continuo
     *
     * @param bitsPerSample boolean[][] donde cada fila es una muestra codificada en bits
     * @return boolean[] secuencia continua de bits lista para transmisión/modulación
     */
    public boolean[] flatten(boolean[][] bitsPerSample) {
        // Creamos el arreglo de salida con tamaño total = número de muestras * bits por muestra
        boolean[] flat = new boolean[bitsPerSample.length * nBits];

        // Índice para ir llenando el arreglo plano
        int idx = 0;

        // Recorremos cada fila (muestra) del arreglo bidimensional
        for (boolean[] row : bitsPerSample) {
            // Recorremos cada bit de la muestra
            for (boolean bit : row) {
                flat[idx++] = bit; // Lo copiamos al arreglo plano y avanzamos el índice
            }
        }
        // Retornamos la secuencia continua de bits
        return flat;
    }

    // Getters para integridad con la GUI de visualización
    public double getXmin() {
        return xmin;
    }

    public double getQ() {
        return q;
    }

    public int getL() {
        return L;
    }
}
