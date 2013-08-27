package com.timepath.ffonline;

import com.timepath.ffonline.input.InputAdapter;
import com.timepath.ffonline.render.GameCanvas;
import com.timepath.ffonline.net.Connection;
import com.timepath.ffonline.net.Packet;
import com.timepath.ffonline.net.PacketHandler;
import com.timepath.ffonline.net.message.MovementMessage;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author timepath
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new Main();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    private final GameCanvas canvas;
    private final JFrame frame;
    private final Connection client;

    public Main() throws IOException {
        canvas = new GameCanvas();
        frame = new JFrame() {
            @Override
            public Dimension getMinimumSize() {
                return canvas.getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return canvas.getPreferredSize();
            }

            @Override
            public Dimension getPreferredSize() {
                return canvas.getPreferredSize();
            }

            @Override
            public boolean isResizable() {
                return false;
            }
        };
        frame.add(canvas);
        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.quit();
                System.exit(0);
            }
        });

        canvas.bind(new GameInput());

        client = new Connection();
        PacketHandler h = new PacketHandler() {
            @Override
            public void handle(Packet p) {
                LOG.log(Level.FINE, "Message received: {0}", p);
                if (p instanceof MovementMessage) {
                    MovementMessage m = (MovementMessage) p;
                    m.id = 1;
                    if (m.id != 0) {
                        canvas.player(m.id).loc.set(m.x, m.y);
                    }
                }
            }
        };
        client.register(h);
        client.connect(new InetSocketAddress(Server.PORT));

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    client.step();
                }
            }
        }).start();

        frame.setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                canvas.start();
            }
        }).start();
    }

    class GameInput extends InputAdapter {

        final int VK_UP = KeyEvent.VK_UP;
        final int VK_DOWN = KeyEvent.VK_DOWN;
        final int VK_LEFT = KeyEvent.VK_LEFT;
        final int VK_RIGHT = KeyEvent.VK_RIGHT;

        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            canvas.target = p;
            canvas.pressed = false;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            canvas.target = p;
            canvas.pressed = true;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            Point p = e.getPoint();
            canvas.target = p;
            canvas.pressed = false;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point p = e.getPoint();
            canvas.target = p;
            canvas.pressed = true;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int x = 0, y = 0, rot = 0;
            switch (e.getKeyCode()) {
                case VK_UP:
                    y--;
                    rot = 3;
                    break;
                case VK_DOWN:
                    y++;
                    rot = 0;
                    break;
                case VK_LEFT:
                    x--;
                    rot = 1;
                    break;
                case VK_RIGHT:
                    x--;
                    rot = 2;
                    break;
                default:
                    break;
            }
            canvas.vel = new Point(x * 32 * 2, y * 32 * 2);
            canvas.rot = rot;
            client.send(new MovementMessage(0, canvas.cam.x / 32, canvas.cam.y / 32));
        }
    }
}
