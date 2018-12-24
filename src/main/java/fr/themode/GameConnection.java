package fr.themode;

import com.esotericsoftware.kryonet.Connection;
import fr.themode.packet.Packet;

public class GameConnection {

    private Connection kryoConnection;
    private long lastRequestId;

    public GameConnection(Connection kryoConnection) {
        this.kryoConnection = kryoConnection;
    }

    public int getID() {
        return getKryoConnection().getID();
    }

    public Connection getKryoConnection() {
        return kryoConnection;
    }

    public void sendPacketTCP(Packet packet) {
        getKryoConnection().sendTCP(packet);
    }

    public void sendPacketUDP(Packet packet) {
        getKryoConnection().sendUDP(packet);
    }

    public long getLastRequestId() {
        return lastRequestId;
    }

    public void setLastRequestId(long lastRequestId) {
        this.lastRequestId = lastRequestId;
    }
}
