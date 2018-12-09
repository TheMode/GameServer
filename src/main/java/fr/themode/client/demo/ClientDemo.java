package fr.themode.client.demo;

import fr.themode.client.Client;
import fr.themode.client.LocalState;
import fr.themode.server.demo.PlayerMovePacket;

public class ClientDemo {

    public static void main(String[] args) {
        new ClientDemo();
    }

    public ClientDemo() {
        int tcp = 25565;
        int udp = 25565;
        Client client = new Client("localhost", tcp, udp);

        client.registerObject(Player.class);

        client.registerPacket(PlayerMovePacket.class);

        client.onReconciliation((requestId) -> {
            System.out.println("An error occurred with the requestId: " + requestId);
        });

        // Used for objects which change over time that are sent to server and then to current client by other clients (such as players, pets...)
        // Check "Entity interpolation" for more informations
        // EntityState entityState = client.getEntityState();

        // Used for everything local where changes need server authorization (ex: local player movements)
        LocalState localState = client.getLocalState();
        localState.setComponent("player", new Player());

        client.connect();

        long time = System.currentTimeMillis();
        long maxTime = 2000;

        while (true) {
            // Save current local state before changing it (in order to backup it with server reconciliation)
            // Should be used each time we modify a state component and send a packet related to this
            client.saveCurrentState();

            Player player = localState.getComponent("player");

            player.move(1, 0);
            PlayerMovePacket movePacket = new PlayerMovePacket();
            movePacket.x = 1;

            System.out.println("Position: " + player.getX());

            // Before being sent, movePacket get an unique requestId
            client.sendTCP(movePacket);


            if (System.currentTimeMillis() - time >= maxTime) {
                System.out.println("END CLIENT");
                break;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while(true){

        }
    }
}
