package com.timepath.ffonline.render;

import com.timepath.ffonline.input.InputAdapter;
import com.timepath.ffonline.util.Map;
import com.timepath.ffonline.util.Player;
import com.timepath.ffonline.util.TileMath;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timepath
 */
public class GameCanvas extends java.awt.Canvas {
    
    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Win")) {
            Thread timerAccuracyThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            });
            timerAccuracyThread.setName("Timer accuracy");
            timerAccuracyThread.setDaemon(true);
            timerAccuracyThread.start();
        }
    }

    private BufferStrategy buffer;
    public int rot;
    public Point vel = new Point(0, 0);
    public boolean pressed;

    public GameCanvas() {
        setIgnoreRepaint(true);
    }

    public void start() {
        createBufferStrategy(2);
        buffer = getBufferStrategy();
        boolean running = true;
        long lastLoopTime = System.currentTimeMillis();
        int FRAME_DELAY = 20;
        while (running) {
            long delta = System.currentTimeMillis() - lastLoopTime;
            lastLoopTime = System.currentTimeMillis();
            float tpf = delta / 1000f;
            update(tpf);
            do {
                Graphics2D g = (Graphics2D) buffer.getDrawGraphics();

                g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
                g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
                g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);

                draw(g);
                g.setColor(Color.GREEN);
                String fps = delta != 0 ? (1 / tpf) + " fps" : "____ fps";
                g.drawString(fps, 0, 10);
                g.drawString("(" + delta + "ms)", 0, 20);
                g.dispose();
            } while (buffer.contentsRestored());
            buffer.show();
            long difference = lastLoopTime + FRAME_DELAY - System.currentTimeMillis();
            try {
                Thread.sleep(Math.max(0, difference));
            } catch (Exception e) {
            }
        }
    }

    public static GraphicsConfiguration graphicsConfig() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }
    private static final Logger LOG = Logger.getLogger(GameCanvas.class.getName());
    private Player p = new Player("dump/char/blackmage_m.png");
    private Player p2 = new Player("dump/char/blackmage_f.png");

    public Player player(int id) {
        if (id == 0) {
            return p;
        } else {
            p2.loc.set(0, 0);
            return p2;
        }
    }

    static {
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.d3d", "true");
        System.setProperty("sun.java2d.noddraw", "false");
        System.setProperty("sun.java2d.translaccel", "true");
        System.setProperty("sun.java2d.ddforcevram", "true");
//        System.setProperty("sun.java2d.accthreshold", "0");
    }
    public Map world = new Map();
    /**
     * In terms of pixels
     */
    public Point cam = new Point(0, 0);
    public static final int TS = 32;
    public static final Dimension gridRes = new Dimension(10, 9);
    public static final Dimension res = new Dimension(gridRes.width * TS, gridRes.height * TS);

    @Override
    public Dimension getPreferredSize() {
        return res;
    }

    public void bind(InputAdapter i) {
        this.addKeyListener(i);
        this.addMouseListener(i);
        this.addMouseMotionListener(i);
        this.addMouseWheelListener(i);
        this.requestFocusInWindow();
    }
    public Point target = new Point(0, 0);

    /**
     * Do not call this method, painting is handled on another thread
     *
     * @deprecated
     */
    @Override
    @Deprecated
    public void repaint() {
    }

    public void update(float tpf) {
        target.translate(cam.x, cam.y);
        Point tile = TileMath.viewToWorld(target);
        target.translate(-cam.x, -cam.y);
        if(pressed) {
            world.set(tile.x, tile.y, 1);
        }
            
        player(0).loc.translate(vel.x * tpf, vel.y * tpf);
        cam = player(0).loc.point();
//        cam.translate(-((res.width / 2) - (TS / 2)), -((res.height / 2) - (TS / 2)));
    }

    /**
     * Layers: BG, Walkable layer, Entities, Decoration
     */
    public void draw(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        Point r = new Point(0, 0);
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                Point v = TileMath.regionToView(x, y);
                v.translate(-cam.x, -cam.y);
                g.drawImage(world.region(r.x + x, r.y + y),
                        v.x, v.y,
                        this);
            }
        }

        g.setColor(Color.GREEN);
        target.translate(cam.x, cam.y);
        Point w = TileMath.viewToWorld(target.x, target.y);
        target.translate(-cam.x, -cam.y);
        Point sq = TileMath.worldToView(w);
        sq.translate(-cam.x, -cam.y);
        g.drawRect(sq.x, sq.y, TS - 1, TS - 1);
        class BackFirstComparator implements Comparator<Player> {

            @Override
            public int compare(Player a, Player b) {
                return a.loc.y < b.loc.y ? -1 : a.loc.y == b.loc.y ? 0 : 1;
            }
        }
        ArrayList<Player> l = new ArrayList<>();
        for (int i = 1; i >= 0; i--) {
            Player p = player(i);
            l.add(p);
        }
        Collections.sort(l, new BackFirstComparator());
        for (Player pdraw : l) {
            Point playerPos = pdraw.loc.point();
            playerPos.translate(-cam.x + (res.width / 2) - (TS / 2), -cam.y + (res.height / 2) - (TS / 2));
            BufferedImage src = pdraw.getImg(rot);
            int x = playerPos.x;
            int y = playerPos.y;
            y += TS - src.getHeight(null);
            g.drawImage(src, x, y, null);
        }
    }
}
