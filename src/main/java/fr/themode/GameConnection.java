package fr.themode;

import com.esotericsoftware.kryonet.Connection;

public class GameConnection {

    private Connection kryoConnection;
    private long lastRequestId;

    public GameConnection(Connection kryoConnection) {
        this.kryoConnection = kryoConnection;
    }

    public Connection getKryoConnection() {
        return kryoConnection;
    }

    public long getLastRequestId() {
        return lastRequestId;
    }

    public void setLastRequestId(long lastRequestId) {
        this.lastRequestId = lastRequestId;
    }
}
