package fr.themode.server;

import fr.themode.Callback;
import fr.themode.packet.Packet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ServerUpdate {

    private LinkedList<PacketHandler> packets;
    private Map<Class<?>, Callback.PacketCallBack> callbacks;

    private int ups;

    private long delay;
    private long lastUpdate;

    public ServerUpdate(LinkedList<PacketHandler> packets, int ups) {
        this.packets = packets;
        setUps(ups);

        this.callbacks = new HashMap<>();
    }

    public <T extends Packet> void setCallbacks(Class<T> clazz, Callback.PacketCallBack<T> callBack) {
        this.callbacks.put(clazz, callBack);
    }

    public void setUps(int ups) {
        this.ups = ups;

        this.delay = 1000 / ups;
        this.lastUpdate = System.currentTimeMillis();
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                long time = System.currentTimeMillis();
                if (time - lastUpdate >= delay) {
                    lastUpdate = System.currentTimeMillis();
                    if(packets.isEmpty())
                        continue;
                    int size = packets.size();
                    for (int i = 0; i < size; i++) {
                        PacketHandler packetHandler = packets.poll();
                        Packet packet = packetHandler.packet;
                        Class<?> clazz = packet.getClass();
                        Callback.PacketCallBack callback = this.callbacks.getOrDefault(clazz, null);
                        if (callback != null) {
                            callback.apply(packetHandler.connection, packet);
                        }
                    }
                    // TODO send all packets
                }
            }
        }).start();
    }

}
