package com.timepath.ffonline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author TimePath
 */
@SuppressWarnings("serial")
public class TileRipper extends JFrame {

    private static final int TS = 16;
    private static final Logger LOG = Logger.getLogger(TileRipper.class.getName());

    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TileRipper t = new TileRipper();
                t.pack();
                t.setLocationRelativeTo(null);
                t.setVisible(true);
            }
        });

    }
    private final JMenuBar menubar;
    private final JMenuItem open;
    private final ImagePanel canvas;
    private final JList<JLabel> list;
    private ArrayList<Tile> tiles;
    private LinkedHashMap<Integer, Tile> map = new LinkedHashMap<Integer, Tile>();
    private final HashMap<Integer, Integer> picks = new HashMap<Integer, Integer>();
    private final Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA};

    FileFilter imageFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String suffix = f.getName().substring(f.getName().lastIndexOf('.') + 1);
            Iterator<ImageReader> i = ImageIO.getImageReadersBySuffix(suffix);
            if (i.hasNext()) {
                if (i.next() != null) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "Supported images";
        }
    };

    ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == open) {
                JFileChooser c = new JFileChooser();
                c.setFileFilter(imageFilter);
                c.showOpenDialog(TileRipper.this);
                File f = c.getSelectedFile();
                if (f == null) {
                    return;
                }
                try {
                    load(f);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
    };

    TileRipper() {
        menubar = new JMenuBar();
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JMenu file = new JMenu("File");
        menubar.add(file);
        open = new JMenuItem("Open");
        open.setAccelerator(KeyStroke.getKeyStroke("control O"));
        open.addActionListener(al);
        file.add(open);
        this.setJMenuBar(menubar);
        canvas = new ImagePanel();
        JScrollPane sp = new JScrollPane(canvas);
        sp.getVerticalScrollBar().setBlockIncrement(TS * TS);
        this.add(sp, BorderLayout.CENTER);
        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                JLabel l2 = (JLabel) list.getModel().getElementAt(index);
                l.setIcon(l2.getIcon());
                l.setText(l2.getText());
                return l;
            }
        };
        list = new JList<JLabel>();

        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent me) {
                select(me);
            }

            @Override
            public void mouseClicked(MouseEvent me) {
                select(me);
            }

            @Override
            public void mouseDragged(MouseEvent me) {
                select(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                select(me);
            }

            public void select(MouseEvent e) {
                if (e.getButton() == 0) {
                    return;
                }
                int element = list.locationToIndex(e.getPoint());
                picks.put(e.getButton(), element);

                Collection<Integer> vs = picks.values();
                int[] ret = new int[vs.size() + 1];
                int i = 0;
                for (Integer in : vs) {
                    ret[i++] = in.intValue();
                }
                ret[i] = element;
                list.setSelectedIndices(ret);
                canvas.repaint();
            }
        });
        list.setCellRenderer(renderer);
        this.add(new JScrollPane(list), BorderLayout.WEST);
    }

    private void load(File f) throws IOException {
        BufferedImage bi = ImageIO.read(f);
        canvas.setImage(bi);
        canvas.repaint();
        for (int y = 0; y < (bi.getHeight()) / TS * TS; y += TS) {
            for (int x = 0; x < (bi.getWidth() / TS) * TS; x += TS) {
                BufferedImage sub = bi.getSubimage(x, y, TS, TS);
//                AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
//                tx.translate(-sub.getWidth(), 0);
//                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
//                BufferedImage flip = op.filter(sub, null);
//                int[] flipData = flip.getRGB(0, 0, flip.getWidth(), flip.getHeight(), null, 0, flip.getWidth());
//                if (!map.containsKey(Arrays.hashCode(flipData))) {
                Tile t;
                int hash = hashImage(sub);
                if (map.containsKey(hash)) {
                    t = map.get(hash);
                } else {
                    t = new Tile(sub);
                }
                t.rects.add(new Rectangle(x, y, TS, TS));
                map.put(hash, t);
//                }
            }
        }
        System.out.println(map.size());
        final DefaultListModel<JLabel> m = new DefaultListModel<JLabel>();
        Set<Entry<Integer, Tile>> s = map.entrySet();
        Iterator<Entry<Integer, Tile>> i = s.iterator();
        tiles = new ArrayList<Tile>();
        while (i.hasNext()) {
            Entry<Integer, Tile> e = i.next();
            JLabel lab = new JLabel("Image");
            lab.setIcon(new ImageIcon(e.getValue().img.getScaledInstance(TS * 2, TS * 2, 0)));
            m.addElement(lab);
            tiles.add(e.getValue());
        }
        list.setModel(m);
    }

    private int hashImage(BufferedImage i) {
        return Arrays.hashCode(i.getRGB(0, 0, i.getWidth(), i.getHeight(), null, 0, i.getWidth()));
    }

    private static class Tile {

        private ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
        private final BufferedImage img;

        Tile(BufferedImage img) {
            this.img = img;
        }
    }

    private class ImagePanel extends JPanel {

        private BufferedImage img;

        ImagePanel() {
            this.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent me) {
                    select(me);
                }

                private void select(MouseEvent me) {
                    if(me.getButton() == 0) {
                        return;
                    }
                    Point p = me.getPoint();
                    int x = p.x / TS, y = p.y / TS;
                    int h = hashImage(img.getSubimage(x * TS, y * TS, TS, TS));
                    ArrayList<Tile> t = new ArrayList<Tile>();
                    t.addAll(map.values());
                    picks.put(me.getButton(), t.indexOf(map.get(h)));
                    repaint();
                }

            });
        }

                public void setImage(BufferedImage i) {
                    img = i;
                }

        @Override
        public Dimension getPreferredSize() {
            if (img == null) {
                return super.getPreferredSize();
            } else {
                return new Dimension(img.getWidth(), img.getHeight());
            }
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.drawImage(img, 0, 0, null);
            int i = 0;
            for (int index : picks.values()) {
                Tile search = tiles.get(index);
                if (search != null) {
                    g.setColor(colors[(i++) % colors.length]);
                    for (Rectangle r : search.rects) {
                        g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
                    }
                }
            }
        }
    }
}
