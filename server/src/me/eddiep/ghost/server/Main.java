package me.eddiep.ghost.server;

import java.util.Random;
import java.util.UUID;

public class Main {
    public static final Random RANDOM = new Random();
    public static final HttpServer HTTP_SERVER = new HttpServer();
    public static final TcpUdpServer TCP_UDP_SERVER = new TcpUdpServer();
    public static void main(String[] args) {
        System.out.println("Starting http server..");
        HTTP_SERVER.start();
        System.out.println("Started!");
        System.out.println("Starting tcp/udp server..");
        TCP_UDP_SERVER.start();
        System.out.println("Started!");
    }
}
