package gui;

import logic.*;
import models.Signal;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.function.DoubleUnaryOperator;

/**
 * Interfaz gráfica principal para el procesador de señales digitales
 */
public class SignalProcessorGUI extends JFrame {
    
    // Componentes de la interfaz
    private JTextField filePathField;
    private JButton browseButton;
    private JButton processFileButton;
    private JButton processMathButton;
    private JButton generateSamplesButton;
    private JTextArea resultsArea;
    private JScrollPane scrollPane;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    // Componentes para configuración
    private JSpinner durationSpinner;
    private JSpinner frequencySpinner;
    private JCheckBox limitDurationCheckBox;
    
    // Variables para el procesamiento
    private Signal currentSignal;
    private DecimalFormat df = new DecimalFormat("#.####");
    
    public SignalProcessorGUI() {
        initializeGUI();
    }
    
    /**
     * Inicializa todos los componentes de la interfaz gráfica
     */
    private void initializeGUI() {
        // Configuración básica de la ventana
        setTitle("Procesador de Señales Digitales - Modulación ASK");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Configurar el layout principal
        setLayout(new BorderLayout(10, 10));
        
        // Crear paneles
        JPanel topPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel bottomPanel = createBottomPanel();
        
        // Agregar paneles a la ventana
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Configurar el área de texto
        setupResultsArea();
        
        // Mostrar mensaje inicial
        showWelcomeMessage();
    }
    
    /**
     * Crea el panel superior con controles de archivo
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder("Selección de Archivo de Audio")
        ));
        
        // Panel para selección de archivo
        JPanel filePanel = new JPanel(new BorderLayout(5, 5));
        
        filePathField = new JTextField();
        filePathField.setEditable(false);
        filePathField.setBackground(Color.WHITE);
        filePathField.setToolTipText("Ruta del archivo de audio seleccionado");
        
        browseButton = new JButton("Explorar...");
        browseButton.setToolTipText("Buscar archivo de audio");
        browseButton.addActionListener(new BrowseActionListener());
        
        filePanel.add(new JLabel("Archivo: "), BorderLayout.WEST);
        filePanel.add(filePathField, BorderLayout.CENTER);
        filePanel.add(browseButton, BorderLayout.EAST);
        
        // Panel para configuración
        JPanel configPanel = createConfigPanel();
        
        topPanel.add(filePanel, BorderLayout.NORTH);
        topPanel.add(configPanel, BorderLayout.CENTER);
        
        return topPanel;
    }
    
    /**
     * Crea el panel de configuración
     */
    private JPanel createConfigPanel() {
        JPanel configPanel = new JPanel(new GridLayout(2, 3, 10, 5));
        configPanel.setBorder(BorderFactory.createTitledBorder("Configuración"));
        
        // Límite de duración
        limitDurationCheckBox = new JCheckBox("Limitar duración");
        limitDurationCheckBox.setSelected(true);
        
        durationSpinner = new JSpinner(new SpinnerNumberModel(3.0, 0.1, 60.0, 0.1));
        JSpinner.NumberEditor durationEditor = new JSpinner.NumberEditor(durationSpinner, "#.#");
        durationSpinner.setEditor(durationEditor);
        
        // Frecuencia para señal matemática
        frequencySpinner = new JSpinner(new SpinnerNumberModel(300, 50, 5000, 50));
        
        configPanel.add(limitDurationCheckBox);
        configPanel.add(new JLabel("Duración (seg):"));
        configPanel.add(durationSpinner);
        configPanel.add(new JLabel("Frecuencia matemática (Hz):"));
        configPanel.add(frequencySpinner);
        configPanel.add(new JLabel(""));
        
        return configPanel;
    }
    
    /**
     * Crea el panel central con botones de acción
     */
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de botones
        JPanel buttonPanel = createButtonPanel();
        centerPanel.add(buttonPanel, BorderLayout.NORTH);
        
        // Área de resultados
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultsArea.setBackground(new Color(248, 248, 248));
        resultsArea.setForeground(new Color(33, 37, 41)); // Texto oscuro para mejor contraste
        
        scrollPane = new JScrollPane(resultsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Resultados del Procesamiento"));
        scrollPane.setPreferredSize(new Dimension(800, 400));
        
        // Mejorar contraste del scroll pane
        scrollPane.getViewport().setBackground(new Color(248, 248, 248));
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        return centerPanel;
    }
    
    /**
     * Crea el panel de botones de acción
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Acciones"));
        
        // Botón para procesar archivo de audio
        processFileButton = new JButton("🎵 Procesar Archivo de Audio");
        processFileButton.setToolTipText("Procesar el archivo de audio seleccionado");
        processFileButton.addActionListener(new ProcessFileActionListener());
        processFileButton.setEnabled(false);
        
        // Botón para procesar señal matemática
        processMathButton = new JButton("📐 Procesar Señal Matemática");
        processMathButton.setToolTipText("Procesar una señal senoidal generada matemáticamente");
        processMathButton.addActionListener(new ProcessMathActionListener());
        
        // Botón para generar archivos de ejemplo
        generateSamplesButton = new JButton("🔧 Generar Archivos de Ejemplo");
        generateSamplesButton.setToolTipText("Crear archivos WAV de ejemplo para pruebas");
        generateSamplesButton.addActionListener(new GenerateSamplesActionListener());
        
        // Botón para limpiar resultados
        JButton clearButton = new JButton("🧹 Limpiar Resultados");
        clearButton.setToolTipText("Limpiar el área de resultados");
        clearButton.addActionListener(e -> clearResults());
        
        buttonPanel.add(processFileButton);
        buttonPanel.add(processMathButton);
        buttonPanel.add(generateSamplesButton);
    // Button to open visualizer
    JButton visualizeButton = new JButton("👁️ Ver Visualizador");
    visualizeButton.setToolTipText("Abrir visualizador de señales");
    visualizeButton.addActionListener(e -> openVisualizer());
    buttonPanel.add(visualizeButton);
        buttonPanel.add(clearButton);
        
        return buttonPanel;
    }
    
    /**
     * Crea el panel inferior con estado y progreso
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        statusLabel = new JLabel("Listo");
        statusLabel.setForeground(new Color(0, 120, 0));
        
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        
        return bottomPanel;
    }
    
    /**
     * Configura el área de resultados
     */
    private void setupResultsArea() {
        resultsArea.setMargin(new Insets(10, 10, 10, 10));
    }
    
    /**
     * Muestra mensaje de bienvenida
     */
    private void showWelcomeMessage() {
        StringBuilder welcome = new StringBuilder();
        welcome.append("=== PROCESADOR DE SEÑALES DIGITALES ===\n\n");
        welcome.append("Bienvenido al simulador de procesamiento de señales digitales.\n");
        welcome.append("Este programa implementa una cadena completa de transmisión:\n\n");
        welcome.append("1. 📊 Muestreo de señal analógica\n");
        welcome.append("2. 💻 Codificación PCM (Pulse Code Modulation)\n");
        welcome.append("3. ⚡ Codificación Polar NRZ\n");
        welcome.append("4. 📡 Modulación ASK (Amplitude Shift Keying)\n\n");
        welcome.append("Opciones disponibles:\n");
        welcome.append("• Procesar archivos de audio reales (WAV, MP3, AU, AIFF)\n");
        welcome.append("• Generar señales matemáticas (senoidales)\n");
        welcome.append("• Crear archivos de ejemplo para pruebas\n\n");
        welcome.append("¡Selecciona una opción para comenzar!\n");
        welcome.append("═══════════════════════════════════════════════════════════════\n\n");
        
        resultsArea.setText(welcome.toString());
    }
    
    /**
     * Listener para el botón de explorar archivos
     */
    private class BrowseActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            browseForAudioFile();
        }
    }
    
    /**
     * Listener para procesar archivo de audio
     */
    private class ProcessFileActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            processAudioFile();
        }
    }
    
    /**
     * Listener para procesar señal matemática
     */
    private class ProcessMathActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            processMathematicalSignal();
        }
    }
    
    /**
     * Listener para generar archivos de ejemplo
     */
    private class GenerateSamplesActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            generateSampleFiles();
        }
    }
    
    /**
     * Abre un diálogo para seleccionar archivo de audio
     */
    private void browseForAudioFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Archivo de Audio");
        
        // Configurar filtros de archivo
        FileNameExtensionFilter audioFilter = new FileNameExtensionFilter(
            "Archivos de Audio (*.wav, *.mp3, *.au, *.aiff)", 
            "wav", "mp3", "au", "aiff"
        );
        fileChooser.setFileFilter(audioFilter);
        
        // Configurar directorio inicial
        File audioSamplesDir = new File("audio-samples");
        if (audioSamplesDir.exists()) {
            fileChooser.setCurrentDirectory(audioSamplesDir);
        }
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            processFileButton.setEnabled(true);
            updateStatus("Archivo seleccionado: " + selectedFile.getName());
        }
    }
    
    /**
     * Procesa el archivo de audio seleccionado
     */
    private void processAudioFile() {
        String filePath = filePathField.getText();
        
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor selecciona un archivo de audio primero.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Ejecutar procesamiento en un hilo separado
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Iniciando procesamiento de archivo de audio...\n");
                
                try {
                    showProgress(true);
                    updateStatus("Cargando archivo de audio...");
                    
                    // Obtener duración límite
                    double maxDuration = 0;
                    if (limitDurationCheckBox.isSelected()) {
                        maxDuration = (Double) durationSpinner.getValue();
                    }
                    
                    // Cargar archivo de audio
                    Signal audioSignal;
                    if (maxDuration > 0) {
                        audioSignal = AudioFileReader.readAudioFile(filePath, maxDuration);
                    } else {
                        audioSignal = AudioFileReader.readAudioFile(filePath);
                    }
                    
                    currentSignal = audioSignal;
                    updateStatus("Procesando señal...");
                    
                    // Procesar la señal
                    String results = processSignalComplete(audioSignal, "ARCHIVO DE AUDIO");
                    publish(results);
                    
                    updateStatus("Procesamiento completado");
                    
                } catch (IOException | UnsupportedAudioFileException ex) {
                    publish("ERROR: No se pudo cargar el archivo: " + ex.getMessage() + "\n");
                    updateStatus("Error en el procesamiento");
                }
                
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    resultsArea.append(chunk);
                }
                resultsArea.setCaretPosition(resultsArea.getDocument().getLength());
            }
            
            @Override
            protected void done() {
                showProgress(false);
            }
        };
        
        worker.execute();
    }
    
    /**
     * Procesa una señal matemática
     */
    private void processMathematicalSignal() {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Iniciando procesamiento de señal matemática...\n");
                
                try {
                    showProgress(true);
                    updateStatus("Generando señal matemática...");
                    
                    // Obtener parámetros
                    int frequency = (Integer) frequencySpinner.getValue();
                    double fs = 8000;
                    double duration = 0.01;
                    
                    // Generar señal matemática
                    DoubleUnaryOperator signalFunction = t -> Math.sin(2 * Math.PI * frequency * t);
                    Signal mathSignal = Sampling.sample(signalFunction, fs, duration);
                    
                    currentSignal = mathSignal;
                    updateStatus("Procesando señal...");
                    
                    // Procesar la señal
                    String results = processSignalComplete(mathSignal, 
                        "SEÑAL MATEMÁTICA (" + frequency + " Hz)");
                    publish(results);
                    
                    updateStatus("Procesamiento completado");
                    
                } catch (Exception ex) {
                    publish("ERROR: " + ex.getMessage() + "\n");
                    updateStatus("Error en el procesamiento");
                }
                
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    resultsArea.append(chunk);
                }
                resultsArea.setCaretPosition(resultsArea.getDocument().getLength());
            }
            
            @Override
            protected void done() {
                showProgress(false);
            }
        };
        
        worker.execute();
    }
    
    /**
     * Genera archivos de ejemplo
     */
    private void generateSampleFiles() {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Generando archivos de ejemplo...\n");
                
                try {
                    showProgress(true);
                    updateStatus("Generando archivos WAV...");
                    
                    AudioGenerator.generateSampleFiles("audio-samples");
                    
                    publish("✅ Archivos de ejemplo generados exitosamente en 'audio-samples/'\n");
                    publish("Archivos creados:\n");
                    publish("• tono_300Hz_5seg.wav - Tono de 300 Hz, 5 segundos\n");
                    publish("• tono_440Hz_2seg.wav - La musical (440 Hz), 2 segundos\n");
                    publish("• tono_1000Hz_3seg.wav - Tono agudo (1000 Hz), 3 segundos\n\n");
                    
                    updateStatus("Archivos de ejemplo generados");
                    
                } catch (IOException ex) {
                    publish("ERROR: No se pudieron generar los archivos: " + ex.getMessage() + "\n");
                    updateStatus("Error al generar archivos");
                }
                
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    resultsArea.append(chunk);
                }
                resultsArea.setCaretPosition(resultsArea.getDocument().getLength());
            }
            
            @Override
            protected void done() {
                showProgress(false);
            }
        };
        
        worker.execute();
    }
    
    /**
     * Procesa completamente una señal y retorna los resultados formateados
     */
    private String processSignalComplete(Signal signal, String signalType) {
        StringBuilder results = new StringBuilder();
        
        results.append("═══════════════════════════════════════════════════════════════\n");
        results.append("🔬 ANÁLISIS DE ").append(signalType).append("\n");
        results.append("═══════════════════════════════════════════════════════════════\n\n");
        
        // Información de la señal original
        results.append("📊 INFORMACIÓN DE LA SEÑAL ORIGINAL:\n");
        results.append("  • Muestras: ").append(signal.getSamples().length).append("\n");
        results.append("  • Frecuencia de muestreo: ").append(df.format(signal.getFs())).append(" Hz\n");
        results.append("  • Duración: ").append(df.format(signal.getSamples().length / signal.getFs())).append(" segundos\n\n");
        
        // Mostrar primeras muestras
        results.append("🔢 PRIMERAS 10 MUESTRAS:\n  ");
        double[] samples = signal.getSamples();
        for (int i = 0; i < 10 && i < samples.length; i++) {
            results.append(df.format(samples[i])).append(" ");
        }
        results.append("\n\n");
        
        // 1. Codificación PCM
        results.append("💻 PASO 1: CODIFICACIÓN PCM\n");
        PCMEncoder pcm = new PCMEncoder(8, -1.0, 1.0);
        int[] levels = pcm.quantizeLevels(signal.getSamples());
        boolean[][] bitsPerSample = pcm.levelsToBits(levels);
        boolean[] pcmBits = pcm.flatten(bitsPerSample);
        
        results.append("  • Bits por muestra: 8\n");
        results.append("  • Rango de cuantización: [-1.0, 1.0]\n");
        results.append("  • Total de bits PCM: ").append(pcmBits.length).append("\n");
        results.append("  • Primeros 32 bits: ");
        for (int i = 0; i < 32 && i < pcmBits.length; i++) {
            results.append(pcmBits[i] ? "1" : "0");
            if ((i + 1) % 8 == 0) results.append(" ");
        }
        results.append("\n\n");
        
        // 2. Codificación Polar
        results.append("⚡ PASO 2: CODIFICACIÓN POLAR NRZ\n");
        double[] polarSignal = Polar.encode(pcmBits);
        results.append("  • Símbolos generados: ").append(polarSignal.length).append("\n");
        results.append("  • Codificación: bit 1 → +1V, bit 0 → -1V\n");
        results.append("  • Primeros 16 símbolos: ");
        for (int i = 0; i < 16 && i < polarSignal.length; i++) {
            results.append(polarSignal[i] > 0 ? "+1 " : "-1 ");
        }
        results.append("\n\n");
        
        // 3. Modulación ASK
        results.append("📡 PASO 3: MODULACIÓN ASK\n");
        double duration = signal.getSamples().length / signal.getFs();
        Signal carrier = ASKModulator.carrierSine(2000, signal.getFs(), duration, 1.0);
        Signal askSignal = ASKModulator.modulate(pcmBits, carrier, 1000, 0.1, 1.0);
        
        results.append("  • Frecuencia portadora: 2000 Hz\n");
        results.append("  • Tasa de bits: 1000 bps\n");
        results.append("  • Amplitud bit 0: 0.1V\n");
        results.append("  • Amplitud bit 1: 1.0V\n");
        results.append("  • Muestras moduladas: ").append(askSignal.getSamples().length).append("\n");
        results.append("  • Primeras 10 muestras ASK: ");
        double[] askSamples = askSignal.getSamples();
        for (int i = 0; i < 10 && i < askSamples.length; i++) {
            results.append(df.format(askSamples[i])).append(" ");
        }
        results.append("\n\n");
        
        // Resumen final
        results.append("✅ PROCESAMIENTO COMPLETADO\n");
        results.append("La señal está lista para transmisión por radiofrecuencia.\n\n");
        
        return results.toString();
    }
    
    /**
     * Limpia el área de resultados
     */
    private void clearResults() {
        resultsArea.setText("");
        showWelcomeMessage();
        updateStatus("Resultados limpiados");
    }

    /**
     * Abre la ventana del visualizador con la señal actual o genera una señal de ejemplo
     */
    private void openVisualizer() {
        SwingUtilities.invokeLater(() -> {
            if (currentSignal != null) {
                new gui.SignalVisualizerGUI(currentSignal).setVisible(true);
            } else {
                // Generar señal por defecto
                int frequency = (Integer) frequencySpinner.getValue();
                double fs = 8000;
                double duration = 0.05; // breve muestra para visualización
                DoubleUnaryOperator signalFunction = t -> Math.sin(2 * Math.PI * frequency * t);
                Signal mathSignal = Sampling.sample(signalFunction, fs, duration);
                new gui.SignalVisualizerGUI(mathSignal).setVisible(true);
            }
        });
    }
    
    /**
     * Actualiza el estado en la barra inferior
     */
    private void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }
    
    /**
     * Muestra u oculta la barra de progreso
     */
    private void showProgress(boolean show) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(show);
            if (show) {
                progressBar.setIndeterminate(true);
            } else {
                progressBar.setIndeterminate(false);
            }
        });
    }
    
    /**
     * Método principal para ejecutar la aplicación
     */
    public static void main(String[] args) {
        // Configurar Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Usar Look and Feel por defecto si hay error
            System.out.println("No se pudo configurar Look and Feel, usando el predeterminado");
        }
        
        // Crear y mostrar la ventana
        SwingUtilities.invokeLater(() -> {
            new SignalProcessorGUI().setVisible(true);
        });
    }
}