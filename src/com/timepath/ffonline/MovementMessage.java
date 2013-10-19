package com.timepath.ffonline;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
@Serializable
public class MovementMessage extends AbstractMessage {

    public int id;
    public Vector3f v;

    public MovementMessage() {
    }
    
    public MovementMessage(Vector3f v) {
        this(0, v);
    }

    public MovementMessage(int id, Vector3f v) {
        this.id = id;
        this.v = v;
    }
    private static final Logger LOG = Logger.getLogger(MovementMessage.class.getName());

}
