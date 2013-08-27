package com.timepath.ffonline.net.message;

import com.timepath.ffonline.net.Packet;
import java.nio.ByteOrder;

/**
 *
 * @author timepath
 */
public class Word extends Packet {

    public String word;
    
    public Word(String s) {
        super(s.length(), s.getBytes());
    }

    @Override
    public void read() {
        int l = payload.getInt();
        byte[] st = new byte[l];
        payload.get(st);
        word = new String(st);
    }
}