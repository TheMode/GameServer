package fr.themode;

import fr.themode.packet.Packet;

public class Callback {

    public interface ConnectionCallBack {
        public void apply(GameConnection connection);
    }

    public interface PacketCallBack<T extends Packet> {
        public void apply(GameConnection connection, T packet);
    }

    public interface DisconnectionCallBack {
        public void apply(GameConnection connection);
    }
}
