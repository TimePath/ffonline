package com.timepath.ffonline.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 *
 * @author TimePath
 */
public class Map {

    private static final Logger LOG = Logger.getLogger(Map.class.getName());
    private final Tile[] palette = new Tile[128];
    private final HashMap<Point, Integer> map = new HashMap<Point, Integer>();

    private final HashMap<Point, Image> cache = new HashMap<Point, Image>();

    public Map() {
        palette[0] = new Tile(null);
        palette[1] = new Tile(((ImageIcon) UIManager.getIcon("OptionPane.warningIcon")).getImage());
        palette[2] = new Tile(((ImageIcon) UIManager.getIcon("OptionPane.informationIcon")).getImage());
        palette[3] = new Tile(((ImageIcon) UIManager.getIcon("OptionPane.errorIcon")).getImage());
    }

    public Tile get(int x, int y) {
        if (map.containsKey(new Point(x, y))) {
            return palette[map.get(new Point(x, y))];
        }
        if (y == x * x) {
            return palette[3];
        }
        if (y == x) {
            return palette[2];
        }
        if (x == 1 || x == -1) {
            return palette[1];
        }
        if (y == 1 || y == -1) {
            return palette[2];
        }
        return null;
    }

    public void set(int x, int y, int i) {
        map.put(new Point(x, y), i);
        cache.remove(TileMath.worldToRegion(x, y));
    }

    public Image region(int _x, int _y) {
        Point p = new Point(_x, _y);
        if (cache.containsKey(p)) {
            return cache.get(p);
        }
        Dimension d = TileMath.regionSize();
        BufferedImage bi = Tile.graphicsConfig().createCompatibleImage(d.width * TileMath.TS, d.height * TileMath.TS, BufferedImage.TRANSLUCENT);
        Graphics2D g = bi.createGraphics();
        for (int y = 0; y < d.height; y++) {
            for (int x = 0; x < d.width; x++) {
                Tile t = get((_x * d.width + x) - (d.width / 2), (_y * d.height + y) - (d.height / 2));
                if (t == null) {
                    continue;
                }
                Rectangle r = new Rectangle(x * TileMath.TS, y * TileMath.TS, TileMath.TS - 1, TileMath.TS - 1);

                g.setColor(t.bg);
                g.fill(r);

                g.drawImage(t.tex, r.x, r.y, r.width, r.height, null);

                g.setColor(Color.RED);
                g.draw(r);
            }
        }
        g.dispose();
        cache.put(p, bi);
        return bi;
    }
}
