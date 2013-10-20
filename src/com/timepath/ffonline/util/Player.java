package com.timepath.ffonline.util;

import com.jme3.math.Vector3f;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author TimePath
 */
public class Player {
    private static final Logger LOG = Logger.getLogger(Player.class.getName());

    public String sheet;
    private BufferedImage[][] img;
    public Vector3f loc = new Vector3f(0, 0, 0);

    public Player(String sheet) {
        this.sheet = sheet;
        sheet = "res/" + sheet;
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
     * @param set Which set of animations to use
     * @return The first frame
     */
    public BufferedImage getImg(int set) {
        return img[set][0];
    }
}
