package com.timepath.ffonline;

import com.timepath.ffonline.util.BimgUtils;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public class ImageImporter {

    private static final Logger LOG = Logger.getLogger(ImageImporter.class.getName());

    public final int TS;

    public final LinkedHashMap<Integer, Tile> map = new LinkedHashMap<Integer, Tile>();

    public ImageImporter(int size) {
        TS = size;
    }

    public ImageImporter() {
        this(16);
    }
    
    public Tile[][] arr;

    public void load(BufferedImage bi) throws IOException {
        int width = (bi.getWidth() / TS) * TS, height = (bi.getHeight()) / TS * TS;
        arr = new Tile[height][width];
        for (int y = 0; y < height; y += TS) {
            for (int x = 0; x < width; x += TS) {
                BufferedImage sub = bi.getSubimage(x, y, TS, TS);
                int[] hashes = {BimgUtils.hash(sub),
                    BimgUtils.hash(BimgUtils.flip(sub, false, true)),
                    BimgUtils.hash(BimgUtils.flip(sub, true, false)),
                    BimgUtils.hash(BimgUtils.flip(sub, true, true))
                };

                Tile t = null;
                for (int hash : hashes) {
                    if (map.containsKey(hash)) {
                        t = map.get(hash);
                    }
                }
                if (t == null) {
                    t = new Tile(sub);
                    map.put(hashes[0], t);
                }
                arr[y][x] = t;
                t.rects.add(new Rectangle(x, y, TS, TS));
            }
        }
        LOG.log(Level.INFO, "Loaded {0} unique tiles", map.size());
    }

    public void load(File f) throws IOException {
        load(BimgUtils.load(f));
    }

    public void load(String s) throws IOException {
        load(BimgUtils.load(s));
    }

    public static class Tile {

        public ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
        public final BufferedImage img;

        Tile(BufferedImage img) {
            this.img = img;
        }
    }

}
