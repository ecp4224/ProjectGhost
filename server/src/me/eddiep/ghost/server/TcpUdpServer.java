package me.eddiep.ghost.server;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.game.impl.Player;
import me.eddiep.ghost.server.network.PlayerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TcpUdpServer extends Server {
    private static final int PORT = 2546;

    private DatagramSocket udpServerSocket;
    private ServerSocket tcpServerSocket;
    private Thread tcpThread;
    private Thread udpThread;

    private List<Client> connectedClients = new ArrayList<>();
    private HashMap<UdpClientInfo, Client> connectedUdpClients = new HashMap<>();

    private final List<Runnable> toTick = Collections.synchronizedList(new LinkedList<Runnable>());
    private List<Runnable> tempTick = new LinkedList<>();
    private boolean ticking = false;

    @Override
    public boolean requiresTick() {
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        setTickRate(16);
        setTickNanos(666667);

        try {
            udpServerSocket = new DatagramSocket(PORT);
            tcpServerSocket = new ServerSocket(PORT + 1);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tcpThread = new Thread(TCP_SERVER_RUNNABLE);
        udpThread = new Thread(UDP_SERVER_RUNNABLE);
        tcpThread.start();
        udpThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();

        tcpThread.interrupt();
        udpThread.interrupt();
    }

    public void executeNextTick(Runnable runnable) {
        if (!ticking) {
            toTick.add(runnable);
        } else {
            tempTick.add(runnable);
        }
    }

    @Override
    protected void onTick() {
        synchronized (toTick) {
            Iterator<Runnable> runnableIterator = toTick.iterator();

            ticking = true;
            while (runnableIterator.hasNext()) {
                runnableIterator.next().run();
                runnableIterator.remove();
            }
            ticking = false;
        }
        toTick.addAll(tempTick);
        tempTick.clear();
    }

    public void disconnect(Client client) throws IOException {
        System.out.println("[SERVER] " + client.getIpAddress() + " disconnected..");

        UdpClientInfo info = new UdpClientInfo(client.getIpAddress(), client.getPort());
        if (connectedUdpClients.containsKey(info))
            connectedUdpClients.remove(info);
        connectedClients.remove(client);

        client.disconnect();
    }

    public void sendUdpPacket(DatagramPacket packet) throws IOException {
        udpServerSocket.send(packet);
    }

    private void validateTcpSession(Socket connection) throws IOException {
        DataInputStream reader = new DataInputStream(connection.getInputStream());
        byte firstByte = (byte)reader.read();
        if (firstByte != 0x00)
            return;
        byte[] sessionBytes = new byte[36];
        int read = reader.read(sessionBytes, 0, sessionBytes.length);
        if (read == -1)
            return;
        String session = new String(sessionBytes, 0, read, Charset.forName("ASCII"));
        Player player = PlayerFactory.findPlayerByUUID(session);
        if (player == null)
            return;
        Client client = new Client(player, connection, this);
        client.listen();
        client.sendOk();
        connectedClients.add(client);
        log("TCP connection made with client " + connection.getInetAddress().toString() + " using session " + session);
    }

    private void validateUdpSession(DatagramPacket packet) throws IOException {
        byte[] data = packet.getData();
        if (data[0] != 0x00)
            return;
        String session = new String(data, 1, 36, Charset.forName("ASCII"));
        Player player = PlayerFactory.findPlayerByUUID(session);
        if (player == null || player.getClient() == null || player.getClient().isLoggedIn())
            return;

        UdpClientInfo info = new UdpClientInfo(packet.getAddress(), packet.getPort());
        connectedUdpClients.put(info, player.getClient());
        player.getClient().setIpAddress(packet.getAddress());
        player.getClient().setPort(packet.getPort());

        player.getClient().sendOk();
        log("UDP connection made with client " + info + " using session " + session);
    }

    private final Runnable TCP_SERVER_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            Thread.currentThread().setName("TCP Server Listener");
            Socket connection = null;
            while (isRunning()) {
                try {
                    connection = tcpServerSocket.accept();

                    if (connection == null)
                        continue;
                    if (!isRunning())
                        break;

                    connection.setSoTimeout(300000);
                    log("Client connected " + connection.getInetAddress().toString());
                    new AcceptThread(connection).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private final Runnable UDP_SERVER_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            Thread.currentThread().setName("UDP Server Listener");
            DatagramPacket recievePacket;
            byte[] receiveData;
            while (isRunning()) {
                try {
                    receiveData = new byte[6144];

                    recievePacket = new DatagramPacket(receiveData, receiveData.length);
                    udpServerSocket.receive(recievePacket);

                    if (!isRunning())
                        break;

                    UdpClientInfo info = new UdpClientInfo(recievePacket.getAddress(), recievePacket.getPort());
                    Client client;
                    if ((client = connectedUdpClients.get(info)) != null) {
                        client.processUdpPacket(recievePacket);
                    } else {
                        new UdpAcceptThread(recievePacket).run();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private class AcceptThread extends Thread {
        private Socket connection;
        public AcceptThread(Socket connection) { this.connection = connection; }

        @Override
        public void run() {
            try {
                validateTcpSession(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class UdpAcceptThread extends Thread {
        private DatagramPacket packet;
        public UdpAcceptThread(DatagramPacket packet) { this.packet = packet; }

        @Override
        public void run() {
            try {
                validateUdpSession(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class UdpClientInfo {
        private InetAddress address;
        private int port;

        public UdpClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UdpClientInfo that = (UdpClientInfo) o;

            if (port != that.port) return false;
            if (!address.equals(that.address)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = address.hashCode();
            result = 31 * result + port;
            return result;
        }

        @Override
        public String toString() {
            return "UdpClientInfo{" +
                    "address=" + address +
                    ", port=" + port +
                    '}';
        }
    }
}
