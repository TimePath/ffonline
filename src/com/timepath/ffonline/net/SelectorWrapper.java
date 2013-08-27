package com.timepath.ffonline.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timepath
 */
public class SelectorWrapper {

    private static final Logger LOG = Logger.getLogger(SelectorWrapper.class.getName());
    public final Selector selector;
    private ArrayList<Connection> connections = new ArrayList<>();

    public void broadcast(Packet p) {
        for(Connection c : connections) {
            if(!c.send(p)) {
                connections.remove(c);
            }
        }
    }

    public interface ConnectionListener {

        public void connected(final Connection c);
    }
    private ConnectionListener connectionListener;

    public void setConnectionListener(ConnectionListener cl) {
        connectionListener = cl;
    }

    public SelectorWrapper() throws IOException {
        selector = Selector.open();
    }

    public SelectorWrapper(InetSocketAddress addr) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.socket().bind(addr);
        LOG.log(Level.INFO, "Listening on {0}", addr);
        selector = Selector.open();
        register(ssc, SelectionKey.OP_ACCEPT);
    }

    public void register(SelectableChannel sc, int ops) throws ClosedChannelException {
        register(sc, ops, null);
    }

    public void register(SelectableChannel sc, int ops, Object att) throws ClosedChannelException {
        sc.register(selector, ops, att);
    }

    public void step() {
        try {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> i = keys.iterator();
            while (i.hasNext()) {
                SelectionKey key = i.next();
                i.remove();
                //<editor-fold defaultstate="collapsed" desc="accept">
                if (key.isAcceptable()) {
                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                    SocketChannel client = ssc.accept();
                    LOG.info("Client accepted");
                    final Connection c = new Connection(client);
                    connections.add(c);
                    if (connectionListener != null) {
                        connectionListener.connected(c);
                    }
                    client.register(selector, SelectionKey.OP_READ, c);
                }
                //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="read">
                if (key.isReadable()) {
                    try {
                        SocketChannel client = (SocketChannel) key.channel();
                        Connection c = (Connection) key.attachment();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        int read = client.read(buffer);
                        if (read > -1) {
                            buffer.limit(buffer.position());
                            buffer.position(0);
                            c.recv(buffer.slice());
                        } else {
                            LOG.info("Client disconnected gracefully");
                            key.interestOps(0);
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, "Client disconnected forcefully: {0}", ex.getCause());
                        key.interestOps(0);
                    }
                }
                //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="connect">
                if (key.isConnectable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    LOG.info("Server Found");
                    if (channel.isConnectionPending()) {
                        channel.finishConnect();
                    }
                    Connection c;
                    if (key.attachment() instanceof Connection) {
                        c = (Connection) key.attachment();
                    } else {
                        c = new Connection(channel);
                    }
                    channel.register(selector, SelectionKey.OP_READ, c);
                }
                //</editor-fold>
            }
        } catch (IOException ex) {
            Logger.getLogger(SelectorWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
