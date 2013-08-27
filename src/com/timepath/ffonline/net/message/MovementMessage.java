package com.timepath.ffonline.net.message;

import com.timepath.ffonline.net.Packet;

/**
 *
 * @author timepath
 */
public class MovementMessage extends Packet {

    public int id, x, y;

    public MovementMessage(int id, int x, int y) {
        super(id, x, y);
    }

    @Override
    public void read() {
        id = payload.getInt();
        x = payload.getInt();
        y = payload.getInt();
    }
}
