package com.timepath.ffonline.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 *
 * @author timepath
 */
public abstract class Packet {

    private static final Logger LOG = Logger.getLogger(Packet.class.getName());

    public Packet() {
    }

    public int opCode() {
        return this.getClass().getName().hashCode();
    }
    private ArrayList<Object> data = new ArrayList<>();

    public Packet(Object... args) {
        data.addAll(Arrays.asList(args));
    }

    public void toBuffer(ByteBuffer b) throws IOException {
        for (Object o : data) {
            if (o instanceof String) {
                b.put(((String) o).getBytes("UTF-8"));
            } else if (o instanceof Byte) {
                b.put((Byte) o);
            } else if (o instanceof Integer) {
                b.putInt((Integer) o);
            } else if (o instanceof byte[]) {
                b.put((byte[]) o);
            }
        }
    }
    
    protected ByteBuffer payload;

    public abstract void read();
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        return sb.toString();
    }

    public ByteBuffer getPayload() {
        return payload;
    }
    
}
