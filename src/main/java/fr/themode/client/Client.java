package fr.themode.client;

import com.esotericsoftware.kryo.Kryo;
import fr.themode.GameListener;
import fr.themode.packet.Packet;
import fr.themode.packet.ReconciliationPacket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Client {

    private String address;
    private int tcp, udp;
    private int timeout;

    private com.esotericsoftware.kryonet.Client kryoClient;
    private GameListener listener;
    private PingListener pingListener;
    private Kryo serverKryo;
    private Kryo kryo;

    private LocalState localState;

    private Map<Long, LocalState> states;
    private long stateCounter;

    public Client(String address, int tcp, int udp) {
        this.address = address;
        this.tcp = tcp;
        this.udp = udp;

        this.timeout = 5000;

        this.kryoClient = new com.esotericsoftware.kryonet.Client();
        this.listener = new GameListener();
        this.pingListener = new PingListener(kryoClient);
        this.serverKryo = this.kryoClient.getKryo();
        this.kryo = new Kryo();

        this.localState = new LocalState();
        this.states = new HashMap<>();
        this.states.put(0L, localState);

        this.kryoClient.addListener(listener);
        this.kryoClient.addListener(pingListener);
        registerPacket(Packet.class);
        registerPacket(ReconciliationPacket.class);

        registerObject(LocalState.class);
        registerObject(HashMap.class);
    }

    public void connect() {
        this.kryoClient.start();
        try {
            this.kryoClient.connect(timeout, address, tcp, udp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }


    public void registerObject(Class<?> clazz) {
        this.kryo.register(clazz);
    }

    public void registerPacket(Class<? extends Packet> clazz) {
        this.serverKryo.register(clazz);
    }

    public void sendTCP(Packet packet) {
        packet.requestId = stateCounter;
        this.kryoClient.sendTCP(packet);
    }

    public void sendUDP(Packet packet) {
        this.kryoClient.sendUDP(packet);
    }

    public int getPing() {
        return pingListener.getPing();
    }

    public void onReconciliation(Consumer<Long> consumer) {
        this.listener.addTypeHandler(ReconciliationPacket.class, ((connection, packet) -> {
            long requestId = packet.requestId;
            restoreState(requestId);
            consumer.accept(requestId);
        }));
    }

    public void saveCurrentState() {
        LocalState copiedState = kryo.copy(localState);
        this.states.put(++stateCounter, copiedState);
    }

    public void restoreState(long id) {
        if (!states.containsKey(id))
            throw new NullPointerException("There isn't any state with the id " + id);


        LocalState newState = this.states.get(id);
        this.localState.update(newState);
        this.stateCounter = id - 1;

        // Remove all other states
        //System.out.println("SIZE BEFORE: " + states.size());
        this.states.keySet().removeIf(stateId -> stateId > id - 1);
        //System.out.println("SIZE AFTER: " + states.size());
    }

    public LocalState getLocalState() {
        return localState;
    }
}
