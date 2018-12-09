package fr.themode.server;

import fr.themode.Callback;
import fr.themode.GameConnection;
import fr.themode.packet.Packet;
import fr.themode.packet.ReconciliationPacket;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ServerUpdate {

    private Server server;
    private LinkedList<PacketHandler> packets;
    private Map<Class<?>, Callback.PacketCallBack> callbacks;

    private Map<GameConnection, Long> requiredRequestId;

    private int ups;

    private long delay;
    private long lastUpdate;

    public ServerUpdate(Server server, LinkedList<PacketHandler> packets, int ups) {
        this.server = server;
        this.packets = packets;
        setUps(ups);

        this.callbacks = new HashMap<>();

        this.requiredRequestId = new HashMap<>();
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
                    if (!packets.isEmpty()) {
                        int size = packets.size();
                        for (int i = 0; i < size; i++) {
                            PacketHandler packetHandler = packets.poll();
                            GameConnection connection = packetHandler.connection;
                            Packet packet = packetHandler.packet;
                            Class<?> clazz = packet.getClass();
                            Callback.PacketCallBack callback = this.callbacks.getOrDefault(clazz, null);
                            if (callback != null && shouldBeKeep(connection, packet)) {
                                connection.setLastRequestId(packet.requestId);
                                PacketResult result = callback.apply(connection, packet);
                                switch (result) {
                                    case SUCCESS:
                                        // TODO send validation ?
                                        break;
                                    case RECONCILIATION:
                                        sendReconciliation(connection, packet);
                                        break;
                                }
                            }
                        }
                        //System.out.println("delay: " + (System.currentTimeMillis() - time));
                        // TODO send all packets
                    }
                }
            }
        }).start();
    }

    protected void sendReconciliation(GameConnection connection, Packet packet) {
        long requestId = packet.requestId;
        ReconciliationPacket reconciliationPacket = new ReconciliationPacket();
        reconciliationPacket.requestId = requestId;
        this.requiredRequestId.put(connection, requestId);
        server.sendToTCP(connection, reconciliationPacket);
    }

    private boolean shouldBeKeep(GameConnection connection, Packet packet) {
        // Check if packet should be keep
        long lastRequestId = connection.getLastRequestId();
        long requiredRequestId = this.requiredRequestId.getOrDefault(connection, -1L);
        long requestId = packet.requestId;
        if (requestId == requiredRequestId && requestId != -1) {
            this.requiredRequestId.remove(connection);
        } else if (requiredRequestId != -1) {
            //System.out.println("FALSE: "+lastRequestId+" : "+requiredRequestId+" : "+requestId);
            return false;
        }
        return true;
    }

}
