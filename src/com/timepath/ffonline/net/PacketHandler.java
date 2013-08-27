package com.timepath.ffonline.net;

import com.timepath.util.ClassScanner;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.reflect.ReflectionFactory;

/**
 *
 * @author timepath
 */
public abstract class PacketHandler {
    
    private static final Logger LOG = Logger.getLogger(PacketHandler.class.getName());

    public PacketHandler() {
    }
    
    public static final HashMap<Integer, Packet> packetMap = findPackets();

    public static HashMap<Integer, Packet> findPackets() {
        final HashMap<Integer, Packet> m = new HashMap<>();
        try {
            final Class classTarget = Packet.class;
            ClassScanner.scan(new ClassScanner.ClassFoundCallback() {
                @Override
                public void found(Class<?> c) {
                    if (c == classTarget) {
                        return;
                    }

                    if (classTarget.isAssignableFrom(c)) { // c instanceof classTarget
                        try {
                            Class parent = classTarget;
//                            PacketHandler p = ((Class<PacketHandler>) c).newInstance();
                            Constructor intConstr = ReflectionFactory.getReflectionFactory()
                                    .newConstructorForSerialization(c, parent.getConstructor());
                            Packet p = ((Class<Packet>) c).cast(intConstr.newInstance());
                            m.put(p.opCode(), p);
                        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        LOG.log(Level.INFO, "Loaded {0} packets", m.size());
        Set<Entry<Integer, Packet>> s = m.entrySet();
        Iterator<Entry<Integer, Packet>> i = s.iterator();
        while(i.hasNext()) {
            Entry e = i.next();
            LOG.log(Level.INFO, "{0} == {1}", new Object[]{e.getKey().toString(), e.getValue().getClass().getSimpleName()});
        }
        return m;
    }

    public static final ArrayList<PacketHandler> handlers = loadHandlers();

    public static ArrayList<PacketHandler> loadHandlers() {
        final ArrayList<PacketHandler> m = new ArrayList<>();
        try {
            final Class classTarget = PacketHandler.class;
            ClassScanner.scan(new ClassScanner.ClassFoundCallback() {
                @Override
                public void found(Class<?> c) {
                    if (c == classTarget) {
                        return;
                    }
                    if (classTarget.isAssignableFrom(c)) {
                        try {
                            Class parent = classTarget;
//                            PacketHandler p = ((Class<PacketHandler>) c).newInstance();
                            Constructor intConstr = ReflectionFactory.getReflectionFactory()
                                    .newConstructorForSerialization(c, parent.getConstructor());
                            PacketHandler p = ((Class<PacketHandler>) c).cast(intConstr.newInstance());
                            m.add(p);
                        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return m;
    }
    
    public abstract void handle(Packet p);
    
}