package fr.themode;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.HashMap;
import java.util.Map;

public class GameListener extends Listener.TypeListener {

    private Callback.ConnectionCallBack connectionCallBack;
    private Callback.DisconnectionCallBack disconnectionCallBack;

    private Map<Connection, GameConnection> connections;

    public GameListener() {
        this.connections = new HashMap<>();
    }

    @Override
    public void connected(Connection connection) {
        GameConnection gameConnection = new GameConnection(connection);
        this.connections.put(connection, gameConnection);
        if (connectionCallBack != null)
            connectionCallBack.apply(gameConnection);
    }

    @Override
    public void disconnected(Connection connection) {
        if (disconnectionCallBack != null)
            disconnectionCallBack.apply(getConnection(connection));
    }

    public GameConnection getConnection(Connection connection) {
        return connections.get(connection);
    }

    public void setConnectionCallBack(Callback.ConnectionCallBack connectionCallBack) {
        this.connectionCallBack = connectionCallBack;
    }

    public void setDisconnectionCallBack(Callback.DisconnectionCallBack disconnectionCallBack) {
        this.disconnectionCallBack = disconnectionCallBack;
    }
}
