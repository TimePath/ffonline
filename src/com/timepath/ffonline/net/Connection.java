package com.timepath.ffonline.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timepath
 */
public class Connection {

    private static final Logger LOG = Logger.getLogger(Connection.class.getName());
    public SocketChannel chan;
    public ArrayList<PacketHandler> handlers = new ArrayList<>();
    
    public Connection() throws IOException {
        this(SocketChannel.open());
    }
    
    private SelectorWrapper sw;
    
    public void step() {
        sw.step();
    }
    
    public void connect(InetSocketAddress addr) throws IOException {
        sw = new SelectorWrapper();
        sw.register(chan, SelectionKey.OP_CONNECT, this);
        chan.connect(addr);
        LOG.log(Level.INFO, "Connected to {0}", addr);
    }
    
    public Connection(SocketChannel _chan) throws IOException {
        chan = _chan;
        chan.configureBlocking(false);
        chan.socket().setTcpNoDelay(true);
    }

    /**
     * Create a Packet object from ByteBuffer input
     * @param b
     * @throws IOException 
     */
    public void recv(ByteBuffer b) throws IOException {
        byte[] data = new byte[b.limit()];
        b.get(data);
        b.flip();
        LOG.log(Level.FINE, "<<< {0}:{1}", new Object[]{data.length, Arrays.toString(data)});
        int op = b.getInt();
        if (!PacketHandler.packetMap.containsKey(op)) {
            LOG.log(Level.WARNING, "Unhandled opcode {0}", op);
        } else {
            LOG.log(Level.FINE, "Op: {0}", op);
            Packet p = PacketHandler.packetMap.get(op);
            byte[] pload = new byte[data.length - 4];
            System.arraycopy(data, 4, pload, 0, pload.length);
            p.payload = ByteBuffer.wrap(pload);
            p.read();
            for (PacketHandler h : handlers) {
                h.handle(p);
            }
        }
    }

    public boolean send(Packet packet) {
        if(!chan.isConnected()) {
            return false;
        }
        try {
            LOG.log(Level.FINE, "Sending {0}", packet);
            ByteBuffer b = ByteBuffer.allocate(1024);
            b.putInt(packet.opCode());
            ByteBuffer payload = packet.getPayload();
            if(payload == null) {
                payload = ByteBuffer.allocate(1024);
                packet.toBuffer(payload);
                payload.limit(payload.position());
            }
            payload.position(0);
            payload = payload.slice();
            b.put(payload);
            byte[] send = new byte[b.position()];
            b.flip();
            b.get(send);
            LOG.log(Level.FINE, ">>> {0}:{1}", new Object[]{send.length, Arrays.toString(send)});
            ByteBuffer sending = ByteBuffer.wrap(send);
            chan.write(sending);
            b.clear();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public void disconnect() {
        if (chan != null) {
            try {
                chan.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    public void quit() {
        LOG.info("Disconnecting...");
        disconnect();
    }

    public void register(PacketHandler h) {
        handlers.add(h);
    }
}
