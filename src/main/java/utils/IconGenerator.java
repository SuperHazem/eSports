package utils;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

public class IconGenerator {

    /**
     * Generates a simple icon with a letter in the center
     * @param letter The letter to display in the icon
     * @param size The size of the icon
     * @param color The background color
     * @return An Image object containing the generated icon
     */
    public static Image generateIcon(String letter, int size, Color color) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw a circular background
        gc.setFill(color);
        gc.fillOval(0, 0, size, size);

        // Draw a border
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(1, 1, size-2, size-2);

        // Draw the letter
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial Bold", size/2));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(letter, size/2, size/2 + size/6);

        // Convert canvas to image
        WritableImage image = new WritableImage(size, size);
        canvas.snapshot(null, image);

        return image;
    }
    
    /**
     * Creates a text-based icon with initials for profile pictures
     * @param text The text to display (typically initials)
     * @param backgroundColor The background color for the icon
     * @return An Image object containing the generated icon
     */
    public static Image createTextIcon(String text, Color backgroundColor) {
        // Default size for profile icons
        final int size = 100;
        
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw a circular background
        gc.setFill(backgroundColor);
        gc.fillOval(0, 0, size, size);

        // Draw a subtle border
        gc.setStroke(Color.WHITE.deriveColor(0, 1, 1, 0.3)); // Semi-transparent white
        gc.setLineWidth(2);
        gc.strokeOval(1, 1, size-2, size-2);

        // Draw the text (initials)
        gc.setFill(Color.WHITE);
        // Adjust font size based on text length
        int fontSize = text.length() > 2 ? size/3 : size/2;
        gc.setFont(new Font("Arial Bold", fontSize));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER); // Center text vertically
        gc.fillText(text, size/2, size/2);

        // Convert canvas to image
        WritableImage image = new WritableImage(size, size);
        canvas.snapshot(null, image);

        return image;
    }

    /**
     * Generates a set of icons for the application
     * @return An array of Images for different sections
     */
    public static Image[] generateAppIcons() {
        Image[] icons = new Image[5];

        // Tournament icon (T)
        icons[0] = generateIcon("T", 20, Color.rgb(0, 247, 255));

        // Team icon (E)
        icons[1] = generateIcon("E", 20, Color.rgb(0, 200, 200));

        // User icon (U)
        icons[2] = generateIcon("U", 20, Color.rgb(0, 180, 180));

        // Reward icon (R)
        icons[3] = generateIcon("R", 20, Color.rgb(0, 160, 160));

        // Sponsor icon (S)
        icons[4] = generateIcon("S", 20, Color.rgb(0, 140, 140));

        return icons;
    }
}