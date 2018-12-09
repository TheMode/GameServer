package fr.themode.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;

public class PingListener implements Listener {

    private Client kryoClient;

    private int ping;

    protected PingListener(Client kryoClient) {
        this.kryoClient = kryoClient;
    }

    @Override
    public void connected(Connection connection) {
        kryoClient.updateReturnTripTime();
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof FrameworkMessage.Ping) {
            FrameworkMessage.Ping ping = (FrameworkMessage.Ping) object;
            if (ping.isReply)
                this.ping = connection.getReturnTripTime();
            kryoClient.updateReturnTripTime();
        }
    }

    public int getPing() {
        return ping;
    }
}
