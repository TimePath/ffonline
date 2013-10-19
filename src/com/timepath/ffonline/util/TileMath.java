package com.timepath.ffonline.util;

import java.awt.Dimension;
import java.awt.Point;

/**
 *
 * When converting from local view, add camera When converting to local view,
 * subtract camera
 *
 * @author TimePath
 */
public class TileMath {
    public static final int TS = 32;
    public static final Dimension gridRes = new Dimension(10, 9);
    public static final Dimension res = new Dimension(gridRes.width * TS, gridRes.height * TS);

    public static Point viewToWorld(int x, int y) {
        int _x = (x - (res.width / 2));
        int _y = (y - (res.height / 2));
        return new Point(
                (_x + (int) Math.signum(_x) * (TS / 2)) / TS,
                (_y + (int) Math.signum(_y) * (TS / 2)) / TS);
    }

    public static Point viewToWorld(Point v) {
        return viewToWorld(v.x, v.y);
    }

    public static Point worldToView(int x, int y) {
        return new Point(
                (x * TS) + (res.width / 2) - (TS / 2),
                (y * TS) + (res.height / 2) - (TS / 2));
    }

    public static Point worldToView(Point w) {
        return worldToView(w.x, w.y);
    }

    public static Point worldToRegion(int x, int y) {
        Dimension region = regionSize();
        return new Point(
                (x + (int) Math.signum(x) * (region.width / 2)) / region.width,
                (y + (int) Math.signum(y) * (region.height / 2)) / region.height);
    }

    public static Point worldToRegion(Point w) {
        return worldToRegion(w.x, w.y);
    }

    public static Point regionToView(int x, int y) {
        Dimension region = regionSize();
        return new Point(
                x * (region.width * TS) - (even(gridRes.width) * (TS / 2)),
                y * (region.height * TS) - (even(gridRes.height) * (TS / 2)));
    }

    public static Point regionToView(Point r) {
        return regionToView(r.x, r.y);
    }

    /**
     * Checks if a number is even
     *
     * @param i the integer to check
     * @return 1 if even, 0 if odd
     */
    public static int even(int i) {
        return (i + 1) % 2;
    }

    /**
     * Maximum displayable tiles in a region
     *
     * @return Region Dimension in tiles
     */
    public static Dimension regionSize() {
        return new Dimension(
                gridRes.width + even(gridRes.width),
                gridRes.height + even(gridRes.height));
    }

    public Point cam = new Point(0, 0);
}
