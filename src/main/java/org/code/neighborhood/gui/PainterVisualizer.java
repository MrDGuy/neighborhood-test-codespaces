package org.code.neighborhood.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.code.neighborhood.Painter;
import org.code.neighborhood.support.GridSquare;

public class PainterVisualizer extends JPanel {
    private final List<Painter> painters;
    private final int tileSize = 32;
    private final Map<Integer, BufferedImage> tileImages = new HashMap<>();
    private final Map<String, BufferedImage> painterImages = new HashMap<>();
    private BufferedImage backgroundImage;
    private int gridWidth;
    private int gridHeight;

    public PainterVisualizer() {
        this.painters = new ArrayList<>();
        loadPainterImages();

        try {
            backgroundImage = ImageIO.read(new File("src/main/resources/background.png"));
        } catch (IOException e) {
            System.err.println("Failed to load background image: " + e.getMessage());
        }

        try {
            BufferedImage spriteSheet = ImageIO.read(new File("src/main/resources/sprite_sheet.png"));
            int tilesPerRow = spriteSheet.getWidth() / tileSize;
            int tilesPerCol = spriteSheet.getHeight() / tileSize;

            int assetId = 0;
            for (int y = 0; y < tilesPerCol; y++) {
                for (int x = 0; x < tilesPerRow; x++) {
                    BufferedImage tile = spriteSheet.getSubimage(
                        x * tileSize, y * tileSize, tileSize, tileSize
                    );
                    tileImages.put(assetId, tile);
                    assetId++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    public void addPainter(Painter painter) {
        this.painters.add(painter);
        if (painters.size() == 1) {
            this.gridWidth = painter.getGrid().getWidth();
            this.gridHeight = painter.getGrid().getHeight();
            setPreferredSize(new Dimension(tileSize * gridWidth, tileSize * gridHeight));
        }
        repaint();
    }

    private void loadPainterImages() {
        try {
            BufferedImage base = ImageIO.read(new File("src/main/resources/painter.png"));
            painterImages.put("EAST", base);
            painterImages.put("NORTH", rotateImage(base, -90));
            painterImages.put("SOUTH", rotateImage(base, 90));
            painterImages.put("WEST", rotateImage(base, 180));
        } catch (IOException e) {
            System.err.println("Painter image load failed: " + e.getMessage());
        }
    }

    private BufferedImage rotateImage(BufferedImage img, double angleDegrees) {
        int size = tileSize;
        BufferedImage rotated = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();

        AffineTransform at = new AffineTransform();
        at.translate(size / 2.0, size / 2.0);
        at.rotate(Math.toRadians(angleDegrees));
        at.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0);

        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!painters.isEmpty()) {
            Painter ref = painters.get(0);

            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, gridWidth * tileSize, gridHeight * tileSize, null);
            }

            for (int y = 0; y < gridHeight; y++) {
                for (int x = 0; x < gridWidth; x++) {
                    try {
                        GridSquare square = ref.getGrid().getSquare(x, y);
                        BufferedImage tile = tileImages.get(square.getAssetID());
                        if (tile != null) {
                            g.drawImage(tile, x * tileSize, y * tileSize, tileSize, tileSize, null);
                        }
                        if (square.getColor() != null) {
                            g.setColor(square.getColor());
                            g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                        }
                    } catch (Exception e) {
                        System.err.println("Invalid square at (" + x + "," + y + ")");
                    }
                }
            }

            for (Painter painter : painters) {
                String dir = painter.getDirection().toString().toUpperCase();
                BufferedImage sprite = painterImages.getOrDefault(dir, painterImages.get("EAST"));
                if (sprite != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    AffineTransform at = AffineTransform.getTranslateInstance(
                        painter.getX() * tileSize, painter.getY() * tileSize);
                    g2d.drawImage(sprite, at, null);
                }
            }
        }
    }
} 
