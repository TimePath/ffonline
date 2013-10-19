package com.timepath.ffonline.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public class Tile {

    private static final Logger LOG = Logger.getLogger(Tile.class.getName());

    public static GraphicsConfiguration graphicsConfig() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }
    Image tex;
    Color bg = Color.BLACK;

    Tile(Image img) {
        if (img == null) {
            return;
        }
        BufferedImage bi = graphicsConfig().createCompatibleImage( // createCompatibleVolatileImage
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TRANSLUCENT);
        Graphics2D g2d = (Graphics2D) bi.getGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        tex = bi;
        tex.setAccelerationPriority(1);
    }
}
