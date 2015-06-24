package me.eddiep.ghost.matchmaking;

import me.eddiep.ghost.matchmaking.network.TcpServer;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting matchmaking server...");

        TcpServer server = new TcpServer();
        server.start();

        System.out.println("Started!");
    }
}
