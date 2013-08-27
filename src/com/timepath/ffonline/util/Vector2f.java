package com.timepath.ffonline.util;

import java.awt.Point;

/**
 *
 * @author timepath
 */
public class Vector2f {
    
    public float x, y;
    
    public Vector2f(Number _x, Number _y) {
        set(_x, _y);
    }
    
    public Vector2f set(Number _x, Number _y) {
        this.x = _x.floatValue();
        this.y = _y.floatValue();
        return this;
    }
    
    public Vector2f translate(Number _x, Number _y) {
        this.x += _x.floatValue();
        this.y += _y.floatValue();
        return this;
    }
    
    public Point point() {
        return new Point(Math.round(x), Math.round(y));
    }
    
}
