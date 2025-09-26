package util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class IconGenerator {
    public static void main(String[] args) {
        try {
            // Crear imagen de 64x64 píxeles
            BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            // Activar antialiasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Fondo circular azul
            g2d.setColor(new Color(33, 150, 243));
            g2d.fillOval(2, 2, 60, 60);
            
            // Borde más oscuro
            g2d.setColor(new Color(25, 118, 210));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(2, 2, 60, 60);
            
            // Onda de señal en blanco
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(3));
            
            // Dibujar onda senoidal
            int[] x = {8, 16, 24, 32, 40, 48, 56};
            int[] y = {32, 20, 32, 44, 32, 20, 32};
            for (int i = 0; i < x.length - 1; i++) {
                g2d.drawLine(x[i], y[i], x[i + 1], y[i + 1]);
            }
            
            // Puntos de muestreo
            g2d.setColor(Color.WHITE);
            for (int i = 0; i < x.length; i++) {
                g2d.fillOval(x[i] - 2, y[i] - 2, 4, 4);
            }
            
            // Antena pequeña
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(52, 8, 52, 20);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(48, 12, 56, 12);
            g2d.drawLine(49, 10, 55, 10);
            
            // Texto "ASK"
            g2d.setFont(new Font("Arial", Font.BOLD, 8));
            FontMetrics fm = g2d.getFontMetrics();
            String text = "ASK";
            int textWidth = fm.stringWidth(text);
            g2d.drawString(text, (64 - textWidth) / 2, 52);
            
            g2d.dispose();
            
            // Guardar imagen
            File outputFile = new File("icon.png");
            ImageIO.write(image, "png", outputFile);
            System.out.println("Icono creado: " + outputFile.getAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Error creando icono: " + e.getMessage());
        }
    }
}