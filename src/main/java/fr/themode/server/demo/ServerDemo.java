package fr.themode.server.demo;

import fr.themode.GameConnection;
import fr.themode.server.PacketResult;
import fr.themode.server.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ServerDemo {

    public static void main(String[] args) {
        new ServerDemo();
    }

    private Map<GameConnection, Player> players = new HashMap<>();

    // Answer related stuffs
    private Map<GameConnection, Float> positions = new HashMap<>();

    public ServerDemo() {
        int tcp = 25565;
        int udp = 25565;
        Server server = new Server(tcp, udp);
        server.setUPS(10);

        server.registerPacket(PlayerMovePacket.class);

        server.onConnection(connection -> {
            System.out.println("New player !");
            players.put(connection, new Player());
        });

        server.onPacket(PlayerMovePacket.class, (connection, packet) -> {
            float x = packet.x;
            boolean success = new Random().nextBoolean(); // Player have 50% chance to walk successfully, otherwise, he will have a rollback
            if (success) {
                // Send to other players
                Player player = getPlayer(connection);
                float newX = player.getX() + x;
                player.setX(newX);
                System.out.println("The player " + connection.getID() + " position is now: " + newX);

                // the value represents the change in position compared to the last server update
                this.positions.put(connection, this.positions.getOrDefault(connection, 0f) + x);
                return PacketResult.SUCCESS;
            } else {
                // Packet is invalid (ex: moving too fast)
                // Returning reconciliation make the server warns the player, he'll then come back to his previous local state
                return PacketResult.RECONCILIATION;
            }
        });

        // After all packets being processed, this is called
        // It is better usage to send all answers here (Send one large packet instead of multiple small one
        server.onUpdateEnd(() -> {
            // Send packets here
            this.positions.clear();
        });

        server.start();
    }

    private Player getPlayer(GameConnection connection) {
        return players.get(connection);
    }

    private class Player {

        private float x, y;

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

}
