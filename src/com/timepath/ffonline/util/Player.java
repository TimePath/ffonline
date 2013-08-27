package com.timepath.ffonline.util;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author timepath
 */
public class Player {

    private BufferedImage[][] img;
    public Vector2f loc = new Vector2f(0, 0);

    public Player(String sheet) {
        int tsx = 16 * 2;
        int tsy = 16 * 3;
        try {
            BufferedImage src = ImageIO.read(new File(sheet));
            int framesx = src.getWidth() / tsx;
            int framesy = src.getHeight() / tsy;
            img = new BufferedImage[framesy][framesx];
            for (int y = 0; y < framesy; y++) {
                for (int x = 0; x < framesx; x++) {
                    this.img[y][x] = src.getSubimage(x * tsx, y * tsy, tsx, tsy);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the img
     */
    public BufferedImage getImg(int set) {
        return img[set][0];
    }
}
