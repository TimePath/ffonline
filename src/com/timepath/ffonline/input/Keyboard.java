package com.timepath.ffonline.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Handles key input events.
 *
 * The only commonly needed public method is {@link #isKeyPressed(int)}. This
 * simply returns if a key is currently pressed (i.e. the key has been pressed,
 * but not yet released).
 *
 * @author Eric
 */
public class Keyboard extends EventQueue<KeyEvent> implements KeyListener {

    private final int KEY_CODE_MAX = 256;
    private boolean[] keys = new boolean[KEY_CODE_MAX];

    /**
     * Returns whether a given key is currently pressed or not.
     *
     * @param keyCode the code for the given key
     * @return true if the key is pressed, false otherwise
     */
    public boolean isKeyPressed(int keyCode) {
        if ((keyCode < 0) || (keyCode >= KEY_CODE_MAX)) {
            return false;
        }
        return keys[keyCode];
    }

    /**
     * Process a key event.
     *
     * @param event
     */
    @Override
    protected void processEvent(KeyEvent event) {
        if ((event.getKeyCode() < 0) || (event.getKeyCode() >= KEY_CODE_MAX)) {
            return;
        }
        switch (event.getID()) {
            case KeyEvent.KEY_PRESSED:
                keys[event.getKeyCode()] = true;
                break;
            case KeyEvent.KEY_RELEASED:
                keys[event.getKeyCode()] = false;
                break;
        }
    }

    /**
     * Adds a key pressed event to the queue.
     *
     * @param event
     */
    @Override
    public void keyPressed(KeyEvent event) {
        addEvent(event);
    }

    /**
     * Adds a key released event to the queue.
     *
     * @param event
     */
    @Override
    public void keyReleased(KeyEvent event) {
        addEvent(event);
    }

    /**
     * Adds a key typed event to the queue.
     *
     * @param event
     */
    @Override
    public void keyTyped(KeyEvent event) {
        addEvent(event);
    }
}