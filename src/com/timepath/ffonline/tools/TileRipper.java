package com.timepath.ffonline.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author timepath
 */
public class TileRipper extends JFrame implements ActionListener {

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
    private final JList list;
    private final JList list2;
    private ArrayList<Tile> tiles;

    TileRipper() {
        menubar = new JMenuBar();
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JMenu file = new JMenu("File");
        menubar.add(file);
        open = new JMenuItem("Open");
        open.setAccelerator(KeyStroke.getKeyStroke("control O"));
        open.addActionListener(this);
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
        list = new JList();
        list.setCellRenderer(renderer);
        list.addListSelectionListener(new SharedListSelectionHandler());
        list2 = new JList();
        list2.setCellRenderer(renderer);
        list2.addListSelectionListener(new SharedListSelectionHandler());
        this.add(new JScrollPane(list), BorderLayout.WEST);
        this.add(new JScrollPane(list2), BorderLayout.EAST);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (open == src) {
            JFileChooser c = new JFileChooser();
            c.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    String suffix = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                    Iterator i = ImageIO.getImageReadersBySuffix(suffix);
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
            });
            c.showOpenDialog(this);
            File f = c.getSelectedFile();
            if(f == null) {
                return;
            }
            try {
                load(f);
            } catch (IOException ex) {
                Logger.getLogger(TileRipper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private static int TS = 16;

    private void load(File f) throws IOException {
        LinkedHashMap<Integer, Tile> map = new LinkedHashMap<>();
        BufferedImage bi = ImageIO.read(f);
        canvas.setImage(bi);
        canvas.repaint();
        for (int y = 0; y < (bi.getHeight()) / TS * TS; y += TS) {
            for (int x = 0; x < (bi.getWidth() / TS) * TS; x += TS) {
                BufferedImage sub = bi.getSubimage(x, y, TS, TS);
                int[] data = sub.getRGB(0, 0, sub.getWidth(), sub.getHeight(), null, 0, sub.getWidth());
//                AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
//                tx.translate(-sub.getWidth(), 0);
//                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
//                BufferedImage flip = op.filter(sub, null);
//                int[] flipData = flip.getRGB(0, 0, flip.getWidth(), flip.getHeight(), null, 0, flip.getWidth());
//                if (!map.containsKey(Arrays.hashCode(flipData))) {
                Tile t;
                int hash = Arrays.hashCode(data);
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
        final DefaultListModel<JLabel> m = new DefaultListModel<>();
        Set<Entry<Integer, Tile>> s = map.entrySet();
        Iterator<Entry<Integer, Tile>> i = s.iterator();
        tiles = new ArrayList();
        while (i.hasNext()) {
            Entry<Integer, Tile> e = i.next();
            JLabel lab = new JLabel("Image");
            lab.setIcon(new ImageIcon(e.getValue().img.getScaledInstance(TS * 2, TS * 2, 0)));
            m.addElement(lab);
            tiles.add(e.getValue());
        }
        list.setModel(m);
        list2.setModel(m);
    }

    private static class Tile {

        private ArrayList<Rectangle> rects = new ArrayList<>();
        private final BufferedImage img;

        public Tile(BufferedImage img) {
            this.img = img;
        }
    }

    class SharedListSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) ((JList) e.getSource()).getSelectionModel();
            int firstIndex = e.getFirstIndex();
            int lastIndex = e.getLastIndex();
            boolean isAdjusting = e.getValueIsAdjusting();
            if(isAdjusting) {
                return;
            }
//            output.append("Event for indexes "
//                    + firstIndex + " - " + lastIndex
//                    + "; isAdjusting is " + isAdjusting
//                    + "; selected indexes:");

            if (lsm.isSelectionEmpty()) {
//                output.append(" <none>");
            } else {
                // Find out which indexes are selected.
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        System.out.println(i);
                        Tile t = tiles.get(i);
                        if(e.getSource() == list) {
                            canvas.search1 = t;
                        } else 
                        if(e.getSource() == list2) {
                            canvas.search2 = t;
                        }
                        canvas.repaint();
//                        output.append(" " + i);
                    }
                }
            }
//            output.append(newline);
        }
    }

    private class ImagePanel extends JPanel {

        public ImagePanel() {
        }
        private BufferedImage img;

        public void setImage(BufferedImage i) {
            img = i;
        }
        Tile search1;
        Tile search2;

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
            if (search1 != null) {
                g.setColor(Color.RED);
                for (Rectangle r : search1.rects) {
                    g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
                }
            }
            if (search2 != null) {
                g.setColor(Color.BLUE);
                for (Rectangle r : search2.rects) {
                    g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
                }
            }
        }
    }
}
