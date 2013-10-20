package com.timepath.ffonline;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.timepath.ffonline.util.Map;
import com.timepath.ffonline.util.Player;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public class GameClient extends MyApplication implements ActionListener, AnalogListener {

    private static final Logger LOG = Logger.getLogger(GameClient.class.getName());

    public static void main(String... args) {
        SimpleApplication a = new GameClient(new StatsAppState(), new DebugKeysAppState());
        a.setShowSettings(true);
        a.setPauseOnLostFocus(false);
        a.start();
    }

    private Client client;

    private float frustumSize = 1;

    private final Player[] p = {new Player("char/blackmage_m.png"), new Player("char/blackmage_f.png")};
    private Geometry playerGeom;
    public Map world = new Map();

    public GameClient(AppState... states) {
        super(states);
    }

    @Override
    public void onAction(String name, boolean pressed, float tpf) {
        client.send(new MovementMessage(cam.getLocation()));
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (name.equals("Size-")) {
            frustumSize += 0.3f * tpf;
        } else if (name.equals("Size+")) {
            frustumSize -= 0.3f * tpf;
        }
        zoom(frustumSize);
    }

    @Override
    public void simpleInitApp() {
        try {
            client = Network.connectToServer("localhost", 5110);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        client.start();

        client.addMessageListener(new ClientMessageListener());

        this.inputManager.setCursorVisible(true);

//        cam.setParallelProjection(true);
//        zoom(frustumSize);
        Mesh m = new Box(0.5f, 0.75f, 0.5f);
//        Mesh m = new Quad(32, 48);
        playerGeom = new Geometry("Player", m);
//        playerGeom.setQueueBucket(RenderQueue.Bucket.Gui);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/ColoredTextured.j3md");
        Texture2D tex = convert(p[0].getImg(0));
        mat.setTexture("ColorMap", tex);
        playerGeom.setMaterial(mat);

        playerGeom.setLocalTranslation(0, 0, 0);

        Picture p = new Picture("Picture");
        p.move(0, 0, 1); // make it appear above stats view
        p.setPosition(0, 0);
        p.setWidth(tex.getImage().getWidth());
        p.setHeight(tex.getImage().getHeight());
        p.setTexture(assetManager, tex, true);

        guiNode.attachChild(p);

        inputManager.addMapping("My Action", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "My Action");

        inputManager.addListener(this, "Size+", "Size-");
        inputManager.addMapping("Size+", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Size-", new KeyTrigger(KeyInput.KEY_S));
    }

    @Override
    public void simpleUpdate(float tpf) {
        playerGeom.rotate(0, 0, tpf * -.3f);
    }

    @Override
    public void destroy() {
        super.destroy();
        client.close();
    }

    private class ClientMessageListener implements MessageListener<Client> {

        @Override
        public void messageReceived(Client source, Message message) {
            if (message instanceof PongMessage) {
            } else if (message instanceof MovementMessage) {
                final MovementMessage mm = (MovementMessage) message;
                GameClient.this.enqueue(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        playerGeom.setLocalTranslation(mm.v);
                        return null;
                    }
                });
            }
        }
    }
}
