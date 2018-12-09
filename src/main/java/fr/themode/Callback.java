package fr.themode;

import fr.themode.packet.Packet;
import fr.themode.server.PacketResult;

public class Callback {

    public interface ConnectionCallBack {
        public void apply(GameConnection connection);
    }

    public interface PacketCallBack<T extends Packet> {
        public PacketResult apply(GameConnection connection, T packet);
    }

    public interface DisconnectionCallBack {
        public void apply(GameConnection connection);
    }
}
