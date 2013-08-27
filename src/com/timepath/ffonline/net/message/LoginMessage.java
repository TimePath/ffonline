package com.timepath.ffonline.net.message;

import com.timepath.ffonline.net.Packet;
import java.nio.ByteBuffer;

/**
 *
 * @author timepath
 */
public class LoginMessage extends Packet {

    public String name;

    public LoginMessage(String user, byte[] pass) {
        super(user.length(), user, pass);
    }

    @Override
    public void read() {
        int nameLen = payload.getInt();
        byte[] nameBytes = new byte[nameLen];
        payload.get(nameBytes);
        name = new String(nameBytes);
    }
}
