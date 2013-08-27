package com.timepath.ffonline;

import com.timepath.ffonline.net.Connection;
import com.timepath.ffonline.net.Packet;
import com.timepath.ffonline.net.PacketHandler;
import com.timepath.ffonline.net.SelectorWrapper;
import com.timepath.ffonline.net.SelectorWrapper.ConnectionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timepath
 */
public class Server {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());
    public static final int PORT = 12345;

    public static void main(String... args) {
        PacketHandler.packetMap.size();
        try {
            InetSocketAddress addr = new InetSocketAddress(PORT);
            final SelectorWrapper sw = new SelectorWrapper(addr);
            sw.setConnectionListener(new ConnectionListener() {

                @Override
                public void connected(final Connection c) {
                    PacketHandler h = new PacketHandler() {
                        @Override
                        public void handle(Packet p) {
                            LOG.log(Level.FINE, "<<< {0}", p);
                            sw.broadcast(p);
                        }
                    };
                    c.register(h);
                }
            });
            while (true) {
                sw.step();
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
