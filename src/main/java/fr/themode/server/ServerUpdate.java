package fr.themode.server;

import fr.themode.Callback;
import fr.themode.GameConnection;
import fr.themode.packet.Packet;
import fr.themode.packet.ReconciliationPacket;
import fr.themode.packet.StateSuccessPacket;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ServerUpdate {

    private Server server;
    private LinkedList<PacketHandler> packets;
    private Map<Class<? extends Packet>, Callback.PacketCallBack> callbacks;

    private Map<GameConnection, Long> requiredRequestId;

    // Update cache
    private Map<GameConnection, Long> lastSuccessfulPacket;

    private int ups;

    private long delay;
    private long lastUpdate;

    private Runnable runnable;

    public ServerUpdate(Server server, LinkedList<PacketHandler> packets, int ups) {
        this.server = server;
        this.packets = packets;
        setUps(ups);

        this.callbacks = new HashMap<>();

        this.requiredRequestId = new HashMap<>();

        this.lastSuccessfulPacket = new HashMap<>();
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
                            Class<? extends Packet> clazz = packet.getClass();
                            Callback.PacketCallBack callback = this.callbacks.getOrDefault(clazz, null);
                            if (callback != null && shouldBeKeep(connection, packet)) {
                                connection.setLastRequestId(packet.requestId);
                                PacketResult result = callback.apply(connection, packet);
                                switch (result) {
                                    case NEUTRAL:
                                        break;
                                    case SUCCESS:
                                        this.lastSuccessfulPacket.put(connection, connection.getLastRequestId());
                                        // TODO send validation ?
                                        break;
                                    case RECONCILIATION:
                                        sendReconciliation(connection, packet);
                                        break;
                                }
                            }
                        }
                        //System.out.println("delay: " + (System.currentTimeMillis() - time));

                        // Send success packet
                        if (!lastSuccessfulPacket.isEmpty()) {
                            for (Map.Entry<GameConnection, Long> entry : lastSuccessfulPacket.entrySet()) {
                                GameConnection connection = entry.getKey();
                                long requestId = entry.getValue();
                                StateSuccessPacket successPacket = new StateSuccessPacket();
                                successPacket.requestId = requestId;
                                connection.getKryoConnection().sendTCP(successPacket);
                            }
                            lastSuccessfulPacket.clear();
                        }

                        // Server update callback
                        if (runnable != null) {
                            runnable.run();
                        }
                    }
                }
            }
        }).start();
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
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
        boolean contains = this.requiredRequestId.containsKey(connection);
        if (!contains)
            return true;

        //long lastRequestId = connection.getLastRequestId();
        long requiredRequestId = this.requiredRequestId.get(connection);
        long requestId = packet.requestId;
        //System.out.println("FALSE: "+lastRequestId+" : "+requiredRequestId+" : "+requestId);
        if (requestId == requiredRequestId && requestId != -1) {
            this.requiredRequestId.remove(connection);
            return true;
        } else {
            return requiredRequestId == -1;
        }
    }

}
