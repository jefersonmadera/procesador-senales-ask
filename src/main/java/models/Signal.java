/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author xexpl
 */
public class Signal {
    
    private final double[] samples;
    private final double fs;

    public Signal(double[] samples, double fs) {
        this.samples = samples;
        this.fs = fs;
    }

    public double[] getSamples() {
        return samples;
    }

    public double getFs() {
        return fs;
    }
}
