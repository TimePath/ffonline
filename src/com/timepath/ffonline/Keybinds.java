package com.timepath.ffonline;

import com.timepath.ffonline.input.Keyboard;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * linux: p, rp, rp, rp, r
 * windows: p, p, p, p, r
 * osx: p, r, p ,r, p, r ?
 * Solution: add all to buffer Keep latest state at same time instance Ignore
 * repeated events The linux version could theoretically break if an action
 * requires a key to be held
 *
 * @author timepath
 */
public class Keybinds {

    public static void main(String... args) {
        System.out.println(Toolkit.getDefaultToolkit().getClass());
        JFrame f = new JFrame();
        f.setVisible(true);
        final Keyboard k = new Keyboard();
        f.addKeyListener(k);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean pressed = false;
                while (true) {
                    k.update();
                    LinkedList<KeyEvent> keyEvents = k.getEvents();
                    for (KeyEvent event : keyEvents) {
                        if ((event.getID() == KeyEvent.KEY_PRESSED) && (event.getKeyCode() == KeyEvent.VK_ESCAPE)) {
                            System.out.println("Esc");
                        }
                    }
                    boolean b = k.isKeyPressed(KeyEvent.VK_SPACE);
                    if(b != pressed) {
                        System.out.println("changed (" + b + ")");
                        pressed = b;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Keybinds.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }
}
