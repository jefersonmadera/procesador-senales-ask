package gui;

import logic.ASKModulator;
import logic.PCMEncoder;
import logic.Polar;
import logic.Sampling;
import models.Signal;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.DoubleUnaryOperator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Visualizador de señales con pestañas: original, cuantizada, comparación, bits, polar, ASK, espectro y micrófono.
 * Mejora: downsampling para render rápido, zoom/pan, guardar imagen y reproducción de audio.
 */
public class SignalVisualizerGUI extends JFrame {
    private JTabbedPane tabbedPane;
    private Signal originalSignal;
    private Signal askSignal;
    private int[] quantizedLevels;
    private boolean[] pcmBits;
    private double[] polarSignal;
    private PCMEncoder pcmEncoder;

    // Parámetros configurables
    private JSpinner freqSpinner;
    private JSpinner samplingRateSpinner;
    private JSpinner durationSpinner;
    private JSpinner bitsSpinner;
    private JSpinner carrierFreqSpinner;
    private JComboBox<String> signalTypeCombo;

    // View controls
    private JScrollBar hScroll;
    private int viewStart = 0; // percent 0..100
    private int zoomLevel = 1; // 1..100 (1 = full view)

    private AudioPlayer audioPlayer = new AudioPlayer();

    public SignalVisualizerGUI() {
        this(null);
    }

    public SignalVisualizerGUI(Signal externalSignal) {
        setTitle("Visualizador de Señales - PCM y ASK");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.SOUTH);

        if (externalSignal != null) {
            this.originalSignal = externalSignal;
            setupPipelineFromSignal();
            updateVisualizations();
            updateInfoPanel();
        } else {
            generateSignals();
        }

        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private void setupPipelineFromSignal() {
        int nBits = 8;
        double carrierFreq = 2000;
        pcmEncoder = new PCMEncoder(nBits, -1.0, 1.0);
        quantizedLevels = pcmEncoder.quantizeLevels(originalSignal.getSamples());
        boolean[][] bitsPerSample = pcmEncoder.levelsToBits(quantizedLevels);
        pcmBits = pcmEncoder.flatten(bitsPerSample);
        polarSignal = Polar.encode(pcmBits);
        double duration = originalSignal.getSamples().length / originalSignal.getFs();
        askSignal = ASKModulator.modulate(pcmBits, ASKModulator.carrierSine(carrierFreq, originalSignal.getFs(), duration, 1.0), 1000, 0.1, 1.0);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Parámetros de la Señal"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Tipo de Señal:"), gbc);
        gbc.gridx = 1;
        signalTypeCombo = new JComboBox<>(new String[]{"Seno", "Coseno", "Cuadrada", "Triangular", "Diente de Sierra"});
        panel.add(signalTypeCombo, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Frecuencia (Hz):"), gbc);
        gbc.gridx = 3;
        freqSpinner = new JSpinner(new SpinnerNumberModel(300, 10, 1000, 10));
        panel.add(freqSpinner, gbc);

        gbc.gridx = 4;
        panel.add(new JLabel("Frecuencia Muestreo (Hz):"), gbc);
        gbc.gridx = 5;
        samplingRateSpinner = new JSpinner(new SpinnerNumberModel(8000, 1000, 44100, 1000));
        panel.add(samplingRateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Duración (s):"), gbc);
        gbc.gridx = 1;
        durationSpinner = new JSpinner(new SpinnerNumberModel(0.01, 0.001, 10.0, 0.001));
        panel.add(durationSpinner, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Bits PCM:"), gbc);
        gbc.gridx = 3;
        bitsSpinner = new JSpinner(new SpinnerNumberModel(8, 2, 16, 1));
        panel.add(bitsSpinner, gbc);

        gbc.gridx = 4;
        panel.add(new JLabel("Frecuencia Portadora (Hz):"), gbc);
        gbc.gridx = 5;
        carrierFreqSpinner = new JSpinner(new SpinnerNumberModel(2000, 500, 20000, 100));
        panel.add(carrierFreqSpinner, gbc);

        gbc.gridx = 6; gbc.gridy = 0; gbc.gridheight = 2;
        JButton updateButton = new JButton("Actualizar Señal");
        updateButton.setBackground(new Color(70, 130, 180));
        updateButton.setForeground(Color.WHITE);
        updateButton.setFont(new Font("Arial", Font.BOLD, 12));
        updateButton.addActionListener(e -> generateSignals());
        panel.add(updateButton, gbc);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Información"));
        panel.setBackground(new Color(240, 240, 240));
        return panel;
    }

    private JPanel createToolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveBtn = new JButton("Guardar Imagen");
        saveBtn.addActionListener(e -> saveCurrentViewAsImage());
        p.add(saveBtn);

        JButton playBtn = new JButton("▶ Reproducir");
        JButton stopBtn = new JButton("■ Detener");
        playBtn.addActionListener(e -> { if (askSignal != null) audioPlayer.play(askSignal.getSamples(), (float) askSignal.getFs()); });
        stopBtn.addActionListener(e -> audioPlayer.stop());
        p.add(playBtn); p.add(stopBtn);

        p.add(new JLabel(" Zoom:"));
        JSlider zoomSlider = new JSlider(1, 50, 1);
        zoomSlider.setPreferredSize(new Dimension(120, 24));
        zoomSlider.addChangeListener(ev -> { setZoomLevel(zoomSlider.getValue()); });
        p.add(zoomSlider);

        hScroll = new JScrollBar(JScrollBar.HORIZONTAL, 0, 10, 0, 100);
        hScroll.setPreferredSize(new Dimension(300, 16));
        hScroll.addAdjustmentListener(ev -> { viewStart = hScroll.getValue(); repaintAllPanels(); });
        p.add(hScroll);

        return p;
    }

    private void setZoomLevel(int level) {
        this.zoomLevel = Math.max(1, level);
        // adjust scrollbar thumb size (not precise but gives feedback)
        int visible = Math.max(1, 100 / zoomLevel);
        hScroll.setVisibleAmount(Math.min(visible, 100));
        repaintAllPanels();
    }

    private void repaintAllPanels() { for (int i=0;i<tabbedPane.getTabCount();i++) tabbedPane.getComponentAt(i).repaint(); }

    private void saveCurrentViewAsImage() {
        Component comp = tabbedPane.getSelectedComponent();
        if (comp == null) return;
        BufferedImage img = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics(); comp.paint(g2); g2.dispose();
        JFileChooser chooser = new JFileChooser(); chooser.setSelectedFile(new File("signal.png"));
        if (chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
            try { ImageIO.write(img, "png", chooser.getSelectedFile()); JOptionPane.showMessageDialog(this, "Imagen guardada"); }
            catch (IOException ex){ JOptionPane.showMessageDialog(this, "Error: "+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void generateSignals() {
        double freq = (double)(int)freqSpinner.getValue();
        double fs = (double)(int)samplingRateSpinner.getValue();
        double duration = (double)durationSpinner.getValue();
        int nBits = (int)bitsSpinner.getValue();
        double carrierFreq = (double)(int)carrierFreqSpinner.getValue();
        String signalType = (String)signalTypeCombo.getSelectedItem();

        DoubleUnaryOperator modulatingSignal = createSignalFunction(signalType, freq);
        originalSignal = Sampling.sample(modulatingSignal, fs, duration);

        pcmEncoder = new PCMEncoder(nBits, -1.0, 1.0);
        quantizedLevels = pcmEncoder.quantizeLevels(originalSignal.getSamples());
        boolean[][] bitsPerSample = pcmEncoder.levelsToBits(quantizedLevels);
        pcmBits = pcmEncoder.flatten(bitsPerSample);

        polarSignal = Polar.encode(pcmBits);

        Signal carrier = ASKModulator.carrierSine(carrierFreq, fs, duration, 1.0);
        askSignal = ASKModulator.modulate(pcmBits, carrier, 1000, 0.1, 1.0);

        updateVisualizations();
        updateInfoPanel();
    }

    private DoubleUnaryOperator createSignalFunction(String type, double freq) {
        switch(type) {
            case "Seno": return t -> Math.sin(2 * Math.PI * freq * t);
            case "Coseno": return t -> Math.cos(2 * Math.PI * freq * t);
            case "Cuadrada": return t -> Math.signum(Math.sin(2 * Math.PI * freq * t));
            case "Triangular": return t -> 2 * Math.asin(Math.sin(2 * Math.PI * freq * t)) / Math.PI;
            case "Diente de Sierra": return t -> 2 * ((freq * t) % 1) - 1;
            default: return t -> Math.sin(2 * Math.PI * freq * t);
        }
    }

    private void updateVisualizations() {
        tabbedPane.removeAll();

        JPanel wrapOriginal = new JPanel(new BorderLayout());
        SignalPanel originalPanel = new SignalPanel(originalSignal.getSamples(), "Señal Original Muestreada", Color.BLUE, true);
        wrapOriginal.add(createToolbar(), BorderLayout.NORTH);
        wrapOriginal.add(originalPanel, BorderLayout.CENTER);
        tabbedPane.addTab("Señal Original", wrapOriginal);

        double[] quantizedValues = new double[quantizedLevels.length];
        for (int i = 0; i < quantizedLevels.length; i++) {
            quantizedValues[i] = pcmEncoder.getXmin() + quantizedLevels[i] * pcmEncoder.getQ() + pcmEncoder.getQ() / 2;
        }
        SignalPanel quantizedPanel = new SignalPanel(quantizedValues, "Señal Cuantizada (Niveles PCM)", Color.RED, true);
        tabbedPane.addTab("Señal Cuantizada", quantizedPanel);

        ComparisonPanel compPanel = new ComparisonPanel(originalSignal.getSamples(), quantizedValues);
        tabbedPane.addTab("Comparación", compPanel);

        BitsPanel bitsPanel = new BitsPanel(pcmBits, "Bits PCM");
        tabbedPane.addTab("Bits PCM", bitsPanel);

        SignalPanel polarPanel = new SignalPanel(polarSignal, "Codificación Polar NRZ", Color.MAGENTA, false);
        tabbedPane.addTab("Señal Polar", polarPanel);

        SignalPanel askPanel = new SignalPanel(askSignal.getSamples(), "Señal ASK Modulada", Color.GREEN, true);
        tabbedPane.addTab("Señal ASK", askPanel);

        MicrophonePanel micPanel = new MicrophonePanel();
        tabbedPane.addTab("Mic (Realtime)", micPanel);

        SpectrumPanel spectrumPanel = new SpectrumPanel(askSignal.getSamples(), askSignal.getFs());
        tabbedPane.addTab("Espectro", spectrumPanel);
    }

    private void updateInfoPanel() {
        JPanel infoPanel = (JPanel) getContentPane().getComponent(2);
        infoPanel.removeAll();

        infoPanel.add(new JLabel("Muestras totales: " + originalSignal.getSamples().length));
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(new JLabel("Bits totales: " + pcmBits.length));
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(new JLabel("Niveles de cuantización: " + pcmEncoder.getL()));
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(new JLabel("Paso de cuantización: " + String.format("%.6f", pcmEncoder.getQ())));

        infoPanel.revalidate();
        infoPanel.repaint();
    }

    // --- Panels (inner classes) ---
    class SignalPanel extends JPanel {
        private double[] samples;
        private String title;
        private Color color;
        private boolean smooth;

        public SignalPanel(double[] samples, String title, Color color, boolean smooth) {
            this.samples = samples;
            this.title = title;
            this.color = color;
            this.smooth = smooth;
            setBackground(Color.WHITE);

            MouseAdapter ma = new MouseAdapter() {
                private int lastX = -1;
                @Override public void mousePressed(MouseEvent e) { lastX = e.getX(); }
                @Override public void mouseReleased(MouseEvent e) { lastX = -1; }
                @Override public void mouseDragged(MouseEvent e) {
                    if (lastX >= 0) {
                        int dx = e.getX() - lastX;
                        int w = getWidth();
                        if (w > 0) {
                            int deltaPercent = -dx * 100 / Math.max(1, w);
                            viewStart = Math.max(0, Math.min(100, viewStart + deltaPercent));
                            if (hScroll != null) hScroll.setValue(viewStart);
                            repaintAllPanels();
                        }
                        lastX = e.getX();
                    }
                }
            };
            addMouseListener(ma); addMouseMotionListener(ma);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth(); int height = getHeight(); int margin = 50;

            g2.setColor(Color.BLACK);
            g2.drawLine(margin, height/2, width - margin, height/2);
            g2.drawLine(margin, margin, margin, height - margin);

            g2.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            int titleWidth = fm.stringWidth(title);
            g2.drawString(title, (width - titleWidth) / 2, 30);

            double max = Double.NEGATIVE_INFINITY; double min = Double.POSITIVE_INFINITY;
            for (double s : samples) { if (s > max) max = s; if (s < min) min = s; }
            if (max == min) { max = min + 1e-6; }

            int total = samples.length;
            int viewSamples = Math.max(1, total / Math.max(1, zoomLevel));
            int startIdx = (int) ((viewStart / 100.0) * Math.max(0, total - viewSamples));
            startIdx = Math.max(0, Math.min(total - viewSamples, startIdx));
            int visible = viewSamples;
            double scaleY = (double)(height - 2 * margin) / (max - min);

            g2.setColor(color); g2.setStroke(new BasicStroke(1));

            if (visible <= width - 2 * margin) {
                double sx = (double)(width - 2 * margin) / visible;
                int prevX = margin;
                int prevY = height/2 - (int)((samples[startIdx] - min) * scaleY - (height - 2 * margin) / 2);
                for (int i = 1; i < visible; i++) {
                    int x = margin + (int)(i * sx);
                    int y = height/2 - (int)((samples[startIdx + i] - min) * scaleY - (height - 2 * margin) / 2);
                    if (smooth) g2.drawLine(prevX, prevY, x, y);
                    else { g2.drawLine(prevX, prevY, x, prevY); g2.drawLine(x, prevY, x, y); }
                    prevX = x; prevY = y;
                }
            } else {
                int plotWidth = width - 2 * margin;
                for (int px = 0; px < plotWidth; px++) {
                    int idx0 = startIdx + (int) ((long) px * visible / plotWidth);
                    int idx1 = startIdx + (int) ((long) (px + 1) * visible / plotWidth);
                    idx0 = Math.max(0, Math.min(total - 1, idx0));
                    idx1 = Math.max(0, Math.min(total, idx1));
                    double minv = Double.POSITIVE_INFINITY, maxv = Double.NEGATIVE_INFINITY;
                    for (int k = idx0; k < idx1 && k < total; k++) { double v = samples[k]; if (v < minv) minv = v; if (v > maxv) maxv = v; }
                    if (minv==Double.POSITIVE_INFINITY) minv = 0; if (maxv==Double.NEGATIVE_INFINITY) maxv = 0;
                    int x = margin + px;
                    int y1p = height/2 - (int)((maxv - min) * scaleY - (height - 2 * margin) / 2);
                    int y2p = height/2 - (int)((minv - min) * scaleY - (height - 2 * margin) / 2);
                    g2.drawLine(x, y1p, x, y2p);
                }
            }

            g2.setColor(Color.BLACK); g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.drawString("Tiempo", width - margin - 30, height/2 + 20);
            g2.drawString("Amplitud", margin - 40, margin - 10);
            g2.drawString(String.format("%.2f", max), margin - 35, margin);
            g2.drawString(String.format("%.2f", min), margin - 35, height - margin);
        }
    }

    class ComparisonPanel extends JPanel {
        private double[] original; private double[] quantized;
        public ComparisonPanel(double[] original, double[] quantized) { this.original = original; this.quantized = quantized; setBackground(Color.WHITE); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth(); int height = getHeight(); int margin = 50;
            g2.setColor(Color.BLACK); g2.drawLine(margin, height/2, width - margin, height/2); g2.drawLine(margin, margin, margin, height - margin);
            g2.setFont(new Font("Arial", Font.BOLD, 14)); g2.drawString("Comparación: Original vs Cuantizada", width/2 - 120, 30);
            double scaleX = (double)(width - 2 * margin) / original.length; double scaleY = (height - 2 * margin) / 2.0;
            g2.setColor(new Color(0, 0, 255, 128)); g2.setStroke(new BasicStroke(2));
            for (int i = 0; i < original.length - 1; i++) { int x1 = margin + (int)(i * scaleX); int y1 = height/2 - (int)(original[i] * scaleY); int x2 = margin + (int)((i + 1) * scaleX); int y2 = height/2 - (int)(original[i + 1] * scaleY); g2.drawLine(x1, y1, x2, y2); }
            g2.setColor(new Color(255, 0, 0, 200)); g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            for (int i = 0; i < quantized.length - 1; i++) { int x1 = margin + (int)(i * scaleX); int y1 = height/2 - (int)(quantized[i] * scaleY); int x2 = margin + (int)((i + 1) * scaleX); int y2 = height/2 - (int)(quantized[i + 1] * scaleY); g2.drawLine(x1, y1, x2, y1); g2.drawLine(x2, y1, x2, y2); }
            g2.setFont(new Font("Arial", Font.PLAIN, 12)); g2.setColor(Color.BLUE); g2.fillRect(width - 150, 50, 15, 15); g2.setColor(Color.BLACK); g2.drawString("Original", width - 130, 62);
            g2.setColor(Color.RED); g2.fillRect(width - 150, 70, 15, 15); g2.setColor(Color.BLACK); g2.drawString("Cuantizada", width - 130, 82);
            double mse = 0; for (int i = 0; i < original.length; i++) { mse += Math.pow(original[i] - quantized[i], 2); } mse /= original.length; g2.setColor(Color.BLACK); g2.drawString(String.format("Error cuadrático medio: %.6f", mse), margin, height - 20);
        }
    }

    class BitsPanel extends JPanel { private boolean[] bits; private String title; public BitsPanel(boolean[] bits, String title) { this.bits = bits; this.title = title; setBackground(Color.WHITE); }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth(); int height = getHeight(); int margin = 50; g2.setFont(new Font("Arial", Font.BOLD, 14)); g2.drawString(title, width/2 - 40, 30);
            int maxBitsToShow = Math.min(bits.length, 64); int bitWidth = (width - 2 * margin) / maxBitsToShow; int bitHeight = 40; int y = height/2 - bitHeight/2;
            for (int i = 0; i < maxBitsToShow; i++) { int x = margin + i * bitWidth; if (bits[i]) { g2.setColor(new Color(0, 150, 0)); g2.fillRect(x, y - bitHeight, bitWidth - 2, bitHeight); } else { g2.setColor(new Color(150, 0, 0)); g2.fillRect(x, y, bitWidth - 2, bitHeight); } g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 12)); String bitValue = bits[i] ? "1" : "0"; g2.drawString(bitValue, x + bitWidth/2 - 4, y + 5); }
            g2.setColor(Color.BLACK); g2.setStroke(new BasicStroke(2)); g2.drawLine(margin, y, width - margin, y); g2.setFont(new Font("Arial", Font.PLAIN, 12)); g2.drawString("Total de bits: " + bits.length, margin, height - 30); if (bits.length > maxBitsToShow) { g2.drawString("(Mostrando primeros " + maxBitsToShow + " bits)", margin, height - 15); }
        }
    }

    class SpectrumPanel extends JPanel {
        private double[] samples; private double fs; public SpectrumPanel(double[] samples, double fs) { this.samples = samples; this.fs = fs; setBackground(Color.WHITE); }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth(); int height = getHeight(); int margin = 50; g2.setFont(new Font("Arial", Font.BOLD, 14)); g2.drawString("Espectro de Frecuencia (Aproximado)", width/2 - 120, 30);
            int N = Math.min(samples.length, 256); double[] magnitude = new double[N/2]; for (int k = 0; k < N/2; k++) { double real = 0; double imag = 0; for (int n = 0; n < N; n++) { double angle = -2 * Math.PI * k * n / N; real += samples[n] * Math.cos(angle); imag += samples[n] * Math.sin(angle); } magnitude[k] = Math.sqrt(real * real + imag * imag); }
            double maxMag = 0; for (double m : magnitude) if (m > maxMag) maxMag = m; if (maxMag > 0) for (int i = 0; i < magnitude.length; i++) magnitude[i] /= maxMag;
            g2.setColor(Color.BLACK); g2.drawLine(margin, height - margin, width - margin, height - margin); g2.drawLine(margin, margin, margin, height - margin);
            int barWidth = (width - 2 * margin) / Math.max(1, magnitude.length); for (int i = 0; i < magnitude.length; i++) { int x = margin + i * barWidth; int barHeight = (int)(magnitude[i] * (height - 2 * margin)); float hue = (float)i / magnitude.length; g2.setColor(Color.getHSBColor(hue, 0.8f, 0.9f)); g2.fillRect(x, height - margin - barHeight, barWidth - 1, barHeight); }
            g2.setColor(Color.BLACK); g2.setFont(new Font("Arial", Font.PLAIN, 10)); g2.drawString("Frecuencia (Hz)", width/2 - 40, height - 10); g2.drawString("Magnitud", 5, margin - 5); g2.drawString("0", margin - 10, height - margin + 15); g2.drawString(String.format("%.0f", fs/2), width - margin - 20, height - margin + 15);
        }
    }

    // Simple audio player for double[] samples (assumes normalized -1..1)
    class AudioPlayer {
        private Thread playThread;
        private final AtomicBoolean running = new AtomicBoolean(false);
        public void play(double[] samples, float fs) {
            stop();
            running.set(true);
            playThread = new Thread(() -> {
                AudioFormat fmt = new AudioFormat(fs, 16, 1, true, false);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
                    line.open(fmt, 4096);
                    line.start();
                    byte[] buffer = new byte[4096];
                    int idx = 0; int n = samples.length;
                    while (running.get() && idx < n) {
                        int toWrite = Math.min((buffer.length/2), n - idx);
                        int bi = 0;
                        for (int i = 0; i < toWrite; i++) {
                            int val = (int) Math.max(Math.min(32767, samples[idx+i]*32767), -32768);
                            buffer[bi++] = (byte)(val & 0xff);
                            buffer[bi++] = (byte)((val>>8) & 0xff);
                        }
                        line.write(buffer, 0, bi);
                        idx += toWrite;
                    }
                    line.drain();
                } catch (LineUnavailableException ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(SignalVisualizerGUI.this, "Audio line error: "+ex.getMessage()));
                }
            }, "audio-play");
            playThread.start();
        }
        public void stop() { running.set(false); if (playThread!=null) { try { playThread.join(100); } catch (InterruptedException ignored) {} playThread=null; } }
    }

    // Microphone realtime panel (uses a small buffer and paints it)
    class MicrophonePanel extends JPanel {
        private volatile double[] buffer = new double[0];
        private TargetDataLine line;
        private Thread captureThread;
        private final AtomicBoolean capturing = new AtomicBoolean(false);

        public MicrophonePanel() {
            setBackground(Color.WHITE);
            JButton start = new JButton("Iniciar captura");
            JButton stop = new JButton("Detener captura");
            start.addActionListener(e -> startCapture());
            stop.addActionListener(e -> stopCapture());
            JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT)); ctrl.add(start); ctrl.add(stop);
            setLayout(new BorderLayout()); add(ctrl, BorderLayout.NORTH);
        }

        private void startCapture() {
            if (capturing.get()) return;
            AudioFormat fmt = new AudioFormat(8000f, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, fmt);
            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(fmt, 4096);
                line.start();
                capturing.set(true);
                captureThread = new Thread(() -> {
                    byte[] buf = new byte[2048];
                    while (capturing.get()) {
                        int r = line.read(buf, 0, buf.length);
                        int samplesRead = r/2;
                        double[] s = new double[samplesRead];
                        for (int i=0;i<samplesRead;i++) {
                            int lo = buf[2*i] & 0xff; int hi = buf[2*i+1];
                            int val = (hi<<8) | lo;
                            s[i] = val / 32768.0;
                        }
                        buffer = s;
                        repaint();
                        try { Thread.sleep(40); } catch (InterruptedException ignored) {}
                    }
                }, "mic-capture");
                captureThread.start();
            } catch (LineUnavailableException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo abrir el micrófono: "+ex.getMessage());
            }
        }

        private void stopCapture() {
            capturing.set(false);
            if (line!=null) { line.stop(); line.close(); line=null; }
            if (captureThread!=null) { try { captureThread.join(100); } catch (InterruptedException ignored) {} captureThread=null; }
        }

        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight(); int margin=20; g2.setColor(Color.BLACK); g2.drawString("Micrófono (último buffer)", margin, 20);
            double[] s = buffer; if (s==null || s.length==0) return; int plotH=h-40; int plotW=w-2*margin; int N = s.length; double max=Double.NEGATIVE_INFINITY,min=Double.POSITIVE_INFINITY; for (double v: s){ if (v>max) max=v; if (v<min) min=v;} if (max==min){max=min+1e-6;} double scaleY=(double)plotH/(max-min);
            int prevX=margin; int prevY=margin+plotH/2; for (int i=0;i<N;i+=Math.max(1,N/plotW)) { int x = margin + i*plotW/N; int y = margin + plotH/2 - (int)((s[i]- (min+max)/2)*scaleY); g2.drawLine(prevX, prevY, x, y); prevX=x; prevY=y; }
        }
    }
}
