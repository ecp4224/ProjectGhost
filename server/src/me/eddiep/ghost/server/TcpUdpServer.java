package me.eddiep.ghost.server;

import me.eddiep.ghost.server.network.Client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;

public class TcpUdpServer extends Server {
    private static final int PORT = 2546;

    private DatagramSocket udpServerSocket;
    private ServerSocket tcpServerSocket;
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
        try {
            udpServerSocket = new DatagramSocket(PORT);
            tcpServerSocket = new ServerSocket(PORT + 1);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {

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

    public void sendUdpPacket(DatagramPacket packet) throws IOException {
        udpServerSocket.send(packet);
    }

    private void validateSession(Socket connection) throws IOException {
        DataInputStream reader = new DataInputStream(connection.getInputStream());
        byte firstByte = (byte)reader.read();
        if (firstByte != 0x00)
            return;
        byte[] sessionBytes = new byte[36];
        int read = reader.read(sessionBytes, 0, sessionBytes.length);
        if (read == -1)
            return;
        String session = new String(sessionBytes, 0, read, Charset.forName("ASCII"));
    }

    private final Runnable TCP_SERVER_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            Socket connection = null;
            while (isRunning()) {
                try {
                    connection = tcpServerSocket.accept();
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
            DatagramPacket recievePacket;
            byte[] receiveData;
            while (isRunning()) {
                try {
                    receiveData = new byte[1024];
                    recievePacket = new DatagramPacket(receiveData, receiveData.length);
                    udpServerSocket.receive(recievePacket);

                    UdpClientInfo info = new UdpClientInfo(recievePacket.getAddress(), recievePacket.getPort());
                    Client client;
                    if ((client = connectedUdpClients.get(info)) != null) {
                        client.processUdpPacket(recievePacket);
                    } else {
                        //TODO Handle session packet
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
                validateSession(connection);
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
    }
}
