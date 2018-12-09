package fr.themode.server;

import fr.themode.GameConnection;
import fr.themode.server.demo.PlayerMovePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ServerDemo {

    public static void main(String[] args) {
        new ServerDemo();
    }

    private Map<GameConnection, Player> players = new HashMap<>();

    public ServerDemo() {
        int tcp = 25565;
        int udp = 25565;
        Server server = new Server(tcp, udp);
        server.setUPS(10);

        server.registerPacket(PlayerMovePacket.class);

        // In number of server update
        // server.setEntityInterpolationDelay(1);

        server.onConnection(connection -> {
            System.out.println("New player !");
            players.put(connection, new Player());
        });

        server.onPacket(PlayerMovePacket.class, (connection, packet) -> {
            // System.out.println("RECEIVE MOVE PACKET");
            float x = packet.x;
            System.out.println("ID: "+packet.requestId);
            boolean success = new Random().nextBoolean(); // Player have 50% chance to walk successfully, otherwise, he will have a rollback
            if (success) {
                // Send to other players
                Player player = getPlayer(connection);
                player.setX(player.getX()+x);
                System.out.println("X: " + player.getX());
                // TODO entity interpolation
                /*EntityMovePacket entityMovePacket = new EntityMovePacket();
                entityMovePacket.id = client.getID();
                entityMovePacket.x = x;
                entityMovePacket.y = player.getY();

                // Send position change to all clients except the one which moved (client object)
                server.sendToAllExceptTCP(connection, entityMovePacket);*/
                return PacketResult.SUCCESS;
            } else {
                // Packet is invalid (ex: moving too fast)
                return PacketResult.RECONCILIATION;
                //server.sendReconciliation(connection, packet);
            }
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
