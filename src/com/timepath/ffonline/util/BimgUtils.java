
package com.timepath.ffonline.util;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author TimePath
 */
public class BimgUtils {
    private static final Logger LOG = Logger.getLogger(BimgUtils.class.getName());
    
    public static BufferedImage convert(BufferedImage img, int format) {
        BufferedImage nu = new BufferedImage(img.getWidth(), img.getHeight(), format);
        Graphics g = nu.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return nu;
    }
    
    public static BufferedImage load(File f) throws IOException {
        return ImageIO.read(f);
    }

    public static BufferedImage load(String s) throws IOException {
        return load(new File(s));
    }

    public static BufferedImage flip(BufferedImage bi, boolean x, boolean y) {
        AffineTransform tx = AffineTransform.getScaleInstance(x ? -1 : 1, y ? -1 : 1);
        if (x || y) {
            tx.translate(x ? -bi.getWidth() : 0, y ? -bi.getHeight() : 0);
        }
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(bi, null);
    }

    public static int hash(BufferedImage i) {
        return Arrays.hashCode(i.getRGB(0, 0, i.getWidth(), i.getHeight(), null, 0, i.getWidth()));
    }

    private BimgUtils() {
    }
    
}
