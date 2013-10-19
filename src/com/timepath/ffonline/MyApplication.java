package com.timepath.ffonline;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.math.FastMath;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 *
 * @author TimePath
 */
public abstract class MyApplication extends SimpleApplication {

    public MyApplication(AppState... states) {
        super(states);

        Serializer.registerClass(PingMessage.class);
        Serializer.registerClass(PongMessage.class);
        Serializer.registerClass(MovementMessage.class);
    }

    public MyApplication() {
        this(null);
    }

    protected void zoom(float frustumSize) {
        float aspect = cam.getWidth() / cam.getHeight();
        cam.setFrustum(-1000, 1000, -aspect * frustumSize, aspect * frustumSize, frustumSize, -frustumSize);
    }

    protected float getFov() {
        float h = cam.getFrustumTop();
        float near = cam.getFrustumNear();

        float fovY = FastMath.atan(h / near) / (FastMath.DEG_TO_RAD * .5f);
        return fovY;
    }

    protected void setFov(float fovY) {
        float near = cam.getFrustumNear();

        float h = FastMath.tan(fovY * .5f * FastMath.DEG_TO_RAD) * near;
        float w = h * (cam.getFrustumRight() / cam.getFrustumTop());

        cam.setFrustumTop(h);
        cam.setFrustumBottom(-h);
        cam.setFrustumLeft(-w);
        cam.setFrustumRight(w);
    }
    
    protected Texture2D convert(BufferedImage src) {
        BufferedImage i = src;
        boolean scale = false;
        if (scale) {
            int newWidth = src.getWidth() * 2;
            int newHeight = src.getWidth() * 2;
            i = new BufferedImage(newWidth, newHeight, src.getType());
            Graphics2D g = i.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(src, 0, 0, newWidth, newHeight, 0, 0, src.getWidth(), src.getHeight(), null);
            g.dispose();
        }

        Image textureImage = new AWTLoader().load(i, true);
        Texture2D tex = new Texture2D(textureImage);
        tex.setMagFilter(Texture.MagFilter.Nearest);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        return tex;
    }

    @Serializable
    public static class PingMessage extends AbstractMessage {
    }

    @Serializable
    public static class PongMessage extends AbstractMessage {
    }

}
