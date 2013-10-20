package com.timepath.ffonline;

import com.timepath.ffonline.ImageImporter.Tile;
import com.timepath.ffonline.util.BimgUtils;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

    ImageImporter ii = new ImageImporter(16);
    private final JMenuBar menubar;
    private final JMenuItem open;
    private final ImagePanel canvas;
    private final JList<JLabel> list;
    private ArrayList<ImageImporter.Tile> tiles;

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
                    BufferedImage bi = BimgUtils.load(f);
                    canvas.setImage(bi);
                    canvas.repaint();
                    ii.load(bi);

                    final DefaultListModel<JLabel> m = new DefaultListModel<JLabel>();
                    Set<Map.Entry<Integer, ImageImporter.Tile>> s = ii.map.entrySet();
                    Iterator<Map.Entry<Integer, ImageImporter.Tile>> i = s.iterator();
                    tiles = new ArrayList<ImageImporter.Tile>();
                    while (i.hasNext()) {
                        Map.Entry<Integer, ImageImporter.Tile> entry = i.next();
                        JLabel lab = new JLabel("Image");
                        lab.setIcon(new ImageIcon(entry.getValue().img.getScaledInstance(ii.TS * 2, ii.TS * 2, 0)));
                        m.addElement(lab);
                        tiles.add(entry.getValue());
                    }
                    list.setModel(m);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
    };

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

    MouseAdapter ma = new MouseAdapter() {

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
    };

    public TileRipper() {
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
        sp.getVerticalScrollBar().setBlockIncrement(ii.TS * ii.TS);
        this.add(sp, BorderLayout.CENTER);

        list = new JList<JLabel>();

        list.addMouseListener(ma);
        list.setCellRenderer(renderer);
        this.add(new JScrollPane(list), BorderLayout.WEST);
    }

    private class ImagePanel extends JPanel {

        private BufferedImage img;

        MouseAdapter panelma = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent me) {
                select(me);
            }

            private void select(MouseEvent me) {
                if (me.getButton() == 0) {
                    return;
                }
                Point p = me.getPoint();
                int x = p.x / ii.TS, y = p.y / ii.TS;
                int h = BimgUtils.hash(img.getSubimage(x * ii.TS, y * ii.TS, ii.TS, ii.TS));
                ArrayList<Tile> t = new ArrayList<Tile>();
                t.addAll(ii.map.values());
                picks.put(me.getButton(), t.indexOf(ii.map.get(h)));
                repaint();
            }

        };

        ImagePanel() {
            this.addMouseListener(panelma);
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
