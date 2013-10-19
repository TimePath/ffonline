package com.timepath.ffonline;

import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.system.AppSettings;
import com.timepath.ffonline.MyApplication.PingMessage;
import com.timepath.ffonline.MyApplication.PongMessage;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public class GameServer {
    private static final Logger LOG = Logger.getLogger(GameServer.class.getName());

    public static void main(String... args) throws IOException {
        Server server = Network.createServer(5110);
        server.addMessageListener(new MessageListener<HostedConnection>() {

            @Override
            public void messageReceived(HostedConnection source, Message message) {
                if (message instanceof PingMessage) {
                    source.send(new PongMessage());
                } else if (message instanceof MovementMessage) {
                    Message repeat = new MovementMessage(source.getId(), ((MovementMessage) message).v);
                    source.getServer().broadcast(Filters.notEqualTo(source), repeat);
                }
            }
        });
        server.start();

        MyApplication s = new MyApplication() {
            @Override
            public void simpleInitApp() { }
        };
        AppSettings settings = new AppSettings(true);
        settings.setRenderer(null);
        s.setSettings(settings);
        s.setShowSettings(false);
        s.start();
    }

}
