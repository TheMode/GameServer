package fr.themode.server;

import com.esotericsoftware.kryo.Kryo;
import fr.themode.Callback;
import fr.themode.GameConnection;
import fr.themode.GameListener;
import fr.themode.packet.Packet;
import fr.themode.packet.ReconciliationPacket;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Server {

    private int tcp, udp;

    private int update_per_seconds;

    private com.esotericsoftware.kryonet.Server kryoServer;
    private Kryo kryo;
    private GameListener listener;

    private Map<GameConnection, Long> requiredRequestId;

    private LinkedList<PacketHandler> packets;
    private ServerUpdate serverUpdate;

    public Server(int tcp, int udp) {
        this.tcp = tcp;
        this.udp = udp;

        // Default value
        this.update_per_seconds = 10;

        this.kryoServer = new com.esotericsoftware.kryonet.Server();
        this.kryo = this.kryoServer.getKryo();
        this.listener = new GameListener();

        this.requiredRequestId = new HashMap<>();

        this.packets = new LinkedList<>();
        this.serverUpdate = new ServerUpdate(this.packets, update_per_seconds);

        this.kryoServer.addListener(listener);
        registerPacket(Packet.class);
        registerPacket(ReconciliationPacket.class);
    }

    public Server(int port) {
        this(port, port);
    }

    public void start() {
        this.serverUpdate.start();

        this.kryoServer.start();
        try {
            this.kryoServer.bind(tcp, udp);
            System.out.println("Server started");
        } catch (IOException e) {
            System.err.println("Error while binding tcp: " + tcp + " and udp: " + udp + " port");
            e.printStackTrace();
        }
    }

    public void stop() {
        kryoServer.stop();
    }

    public void sendToTCP(GameConnection connection, Packet packet) {
        kryoServer.sendToTCP(connection.getKryoConnection().getID(), packet);
    }

    public void sendToUDP(GameConnection connection, Packet packet) {
        kryoServer.sendToUDP(connection.getKryoConnection().getID(), packet);
    }

    public void sendToAllTCP(Packet packet) {
        kryoServer.sendToAllTCP(packet);
    }

    public void sendToAllUDP(Packet packet) {
        kryoServer.sendToAllUDP(packet);
    }

    public void sendToAllExceptTCP(GameConnection connection, Packet packet) {
        kryoServer.sendToAllExceptTCP(connection.getKryoConnection().getID(), packet);
    }

    public void sendToAllExceptUDP(GameConnection connection, Packet packet) {
        kryoServer.sendToAllExceptUDP(connection.getKryoConnection().getID(), packet);
    }

    public void sendReconciliation(GameConnection connection, Packet packet) {
        long requestId = packet.requestId;
        ReconciliationPacket reconciliationPacket = new ReconciliationPacket();
        reconciliationPacket.requestId = requestId;
        this.requiredRequestId.put(connection, requestId);
        sendToTCP(connection, reconciliationPacket);
    }

    public void registerPacket(Class<? extends Packet> clazz) {
        this.kryo.register(clazz);
    }

    public void setUPS(int ups) {
        this.update_per_seconds = ups;
        this.serverUpdate.setUps(ups);
    }

    public void onConnection(Callback.ConnectionCallBack connectionCallBack) {
        listener.setConnectionCallBack(connectionCallBack);
    }

    public <T extends Packet> void onPacket(Class<T> clazz, Callback.PacketCallBack<T> callback) {
        this.serverUpdate.setCallbacks(clazz, callback);

        listener.addTypeHandler(clazz, ((connection, packet) -> {
            GameConnection gameConnection = listener.getConnection(connection);

            // Check if packet should be keep
            long lastRequestId = gameConnection.getLastRequestId();
            long requiredRequestId = this.requiredRequestId.getOrDefault(gameConnection, -1L);
            long requestId = packet.requestId;
            if (requestId == requiredRequestId) {
                this.requiredRequestId.remove(gameConnection);
            } else if (requiredRequestId != -1 || requestId > lastRequestId + 1) {
                return;
            }

            PacketHandler packetHandler = new PacketHandler();
            packetHandler.connection = gameConnection;
            packetHandler.packet = packet;
            this.packets.add(packetHandler);

            gameConnection.setLastRequestId(packet.requestId);
        }));
    }

    public void onDisconnection(Callback.DisconnectionCallBack disconnectionCallBack) {
        listener.setDisconnectionCallBack(disconnectionCallBack);
    }

}
