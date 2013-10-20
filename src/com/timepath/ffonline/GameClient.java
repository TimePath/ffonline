package com.timepath.ffonline;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.timepath.ffonline.ImageImporter.Tile;
import com.timepath.ffonline.util.BimgUtils;
import com.timepath.ffonline.util.Map;
import com.timepath.ffonline.util.Player;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public class GameClient extends MyApplication implements ActionListener, AnalogListener {

    private static final Logger LOG = Logger.getLogger(GameClient.class.getName());
    public static final String FORWARD = "forward";
    public static final String CUSTOM_ACTION = "My Action";
    public static final String MOVERIGHT = "moveright";
    public static final String MOVELEFT = "moveleft";
    public static final String BACK = "back";

    public static void main(String... args) {
        SimpleApplication a = new GameClient(new StatsAppState(), new DebugKeysAppState());
        a.setShowSettings(false);
        a.setPauseOnLostFocus(false);
        a.start();
    }

    private Client client;

    private final ImageImporter ii = new ImageImporter();

    private final Player[] p = {new Player("char/blackmage_m.png"), new Player("char/blackmage_f.png")};
    private Geometry playerGeom;
    public Map world = new Map();

    Geometry mark;

    public GameClient(AppState... states) {
        super(states);
    }

    @Override
    public void onAction(String name, boolean pressed, float tpf) {

    }

    float movespeed = 320.0f;

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (name.equals(FORWARD)) {
            playerGeom.setLocalTranslation(playerGeom.getLocalTranslation().add(0, movespeed * tpf, 0));
        } else if (name.equals(BACK)) {
            playerGeom.setLocalTranslation(playerGeom.getLocalTranslation().add(0, -movespeed * tpf, 0));
        } else if (name.equals(MOVERIGHT)) {
            playerGeom.setLocalTranslation(playerGeom.getLocalTranslation().add(movespeed * tpf, 0, 0));
        } else if (name.equals(MOVELEFT)) {
            playerGeom.setLocalTranslation(playerGeom.getLocalTranslation().add(-movespeed * tpf, 0, 0));
        }
        client.send(new MovementMessage(playerGeom.getLocalTranslation()));
    }

    @Override
    public void simpleInitApp() {
        Arrow arrow = new Arrow(Vector3f.UNIT_Z.mult(2f));
        arrow.setLineWidth(3);
        mark = new Geometry("Mark", arrow);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);

        this.inputManager.setCursorVisible(true);

        rootNode.setQueueBucket(RenderQueue.Bucket.Gui);
        try {
            ii.load("res/SNES - Final Fantasy 5 - Town of Mirage.png");
        } catch (IOException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            client = Network.connectToServer("localhost", 5110);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        client.addMessageListener(new ClientMessageListener());
        client.start();

        playerGeom = createSprite(p[0].getImg(0));
        rootNode.attachChild(playerGeom);

        for (int y = 0; y < ii.arr.length; y++) {
            for (int x = 0; x < ii.arr[y].length; x++) {
                Tile t = ii.arr[y][x];
                if (t == null) {
                    continue;
                }
                Geometry g = createSprite(t.img);
                g.setLocalTranslation(x, ii.arr.length - y - 1, -2);
                rootNode.attachChild(g);
            }
        }

        inputManager.addMapping(CUSTOM_ACTION, new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, CUSTOM_ACTION);

        inputManager.addListener(this, FORWARD, BACK, MOVELEFT, MOVERIGHT);
        inputManager.addMapping(FORWARD, new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(BACK, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(MOVELEFT, new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(MOVERIGHT, new KeyTrigger(KeyInput.KEY_D));
    }

    public Geometry createSprite(BufferedImage img) {
        img = BimgUtils.convert(img, BufferedImage.TYPE_INT_ARGB);
//        Mesh m = new Box(0.5f * ii.TS, 0.75f * ii.TS, 0.5f * ii.TS);
        Mesh m = new Quad(ii.TS, ii.TS);
        Geometry geom = new Geometry("Player", m);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/ColoredTextured.j3md");
        Texture2D tex = convert(img);
        mat.setTexture("ColorMap", tex);
        geom.setMaterial(mat);

        Picture p = new Picture("Picture");
        p.move(0, 0, -1);
        p.setPosition(0, 0);
        p.setWidth(tex.getImage().getWidth());
        p.setHeight(tex.getImage().getHeight());
        p.setTexture(assetManager, tex, true);
//        return p;
        return geom;
    }

    @Override
    public void simpleUpdate(float tpf) {
        rootNode.setLocalTranslation(playerGeom.getLocalTranslation().negate().add(settings.getWidth() / 2, settings.getHeight() / 2, 0));

        Vector3f origin = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);
        Vector3f direction = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.3f);
        direction.subtractLocal(origin).normalizeLocal();

        Ray ray = new Ray(origin, direction);
        CollisionResults results = new CollisionResults();
        rootNode.collideWith(ray, results);
        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            mark.setLocalTranslation(closest.getContactPoint());

            Quaternion q = new Quaternion();
            q.lookAt(closest.getContactNormal(), Vector3f.UNIT_Y);
            mark.setLocalRotation(q);

            rootNode.attachChild(mark);
        } else {
            rootNode.detachChild(mark);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        client.close();
    }

    private HashMap<Integer, Geometry> players = new HashMap<Integer, Geometry>();

    private Geometry getPlayer(int id) {
        if (!players.containsKey(id)) {
            Geometry g = createSprite(p[0].getImg(0));
            players.put(id, g);
            rootNode.attachChild(g);
            return g;
        }
        return players.get(id);
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
                        getPlayer(mm.id).setLocalTranslation(mm.v);
                        return null;
                    }
                });
            }
        }
    }
}
