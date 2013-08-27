package com.timepath.ffonline.test;

import com.timepath.ffonline.net.Connection;
import com.timepath.ffonline.net.Packet;
import com.timepath.ffonline.net.PacketHandler;
import com.timepath.ffonline.net.SelectorWrapper;
import com.timepath.ffonline.Server;
import com.timepath.ffonline.net.message.Word;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author timepath
 */
public class Client {

    private static final Logger LOG = Logger.getLogger(Client.class.getName());

    public static void main(String... args) {
        PacketHandler.packetMap.size();
        try {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            final JLabel l = new JLabel("Message");
            JButton b = new JButton("Send");
            f.add(l, BorderLayout.NORTH);
            f.add(b, BorderLayout.SOUTH);
            f.pack();
            f.setLocationRelativeTo(null);

            final Connection c = new Connection();
            final SelectorWrapper sw = new SelectorWrapper();
            sw.register(c.chan, SelectionKey.OP_CONNECT, c);
            c.connect(new InetSocketAddress(Server.PORT));
            PacketHandler h = new PacketHandler() {
                @Override
                public void handle(Packet p) {
                    Logger.getLogger(Client.class.getName()).log(Level.INFO, "Setting label  {0}", p);
                    if (p instanceof Word) {
                        l.setText(((Word) p).word);
                    }
                }
            };
            c.register(h);
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    double r = Math.random() * 1000000;
                    Word w = new Word("" + r);
                    c.send(w);
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        sw.step();
                    }
                }
            }).start();

            f.setVisible(true);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
