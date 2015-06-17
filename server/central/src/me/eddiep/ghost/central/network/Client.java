package me.eddiep.ghost.central.network;

import me.eddiep.ghost.central.Main;
import me.eddiep.ghost.central.TcpServer;
import me.eddiep.ghost.central.network.dataserv.PlayerData;
import me.eddiep.ghost.central.network.gameserv.GameServer;
import me.eddiep.ghost.central.network.packet.Packet;
import me.eddiep.ghost.central.network.packet.impl.DeleteRequestPacket;
import me.eddiep.ghost.central.network.packet.impl.NewNotificationPacket;
import me.eddiep.ghost.central.network.packet.impl.OkPacket;
import me.eddiep.ghost.central.utils.Notification;
import me.eddiep.ghost.central.utils.NotificationBuilder;
import me.eddiep.ghost.central.utils.PRunnable;
import me.eddiep.ghost.central.utils.Request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class Client {
    //private Player playable;
    private PlayerData stats;
    private UUID session;
    private InetAddress IpAddress;
    private int port = -1;
    private TcpServer socketServer;
    private boolean connected = true;
    private Thread writerThread;
    private Thread readerThread;
    private Socket socket;
    private int lastReadPacket = 0;

    private OutputStream writer;
    private InputStream reader;

    protected List<byte[]> tcp_packet_queue = Collections.synchronizedList(new LinkedList<byte[]>());
    private int lastWritePacket;
    private GameServer currentGameServer;

    public Client(Socket socket, TcpServer server, String session, PlayerData stats) throws IOException {
        this.session = UUID.fromString(session);
        this.stats = stats;
        this.socket = socket;
        this.IpAddress = socket.getInetAddress();
        this.socketServer = server;

        this.writer = socket.getOutputStream();
        this.reader = socket.getInputStream();
    }

    public UUID getSession() {
        return session;
    }

    public void listen() {
        if (reader == null)
            return;

        writerThread = new Writer();
        writerThread.start();
        readerThread = new Reader();
        readerThread.start();
    }

    public InetAddress getIpAddress() {
        if (socket != null && IpAddress == null)
            return socket.getInetAddress();
        return IpAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (this.port != -1)
            throw new IllegalStateException("This client already has a UDP Port!");

        this.port = port;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.IpAddress = ipAddress;
    }

    public boolean isLoggedIn() {
        return port != -1 && IpAddress != null;
    }

    public void disconnect() throws IOException {
        connected = false;
        if (writerThread != null) {
            writerThread.interrupt();
            try {
                writerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (readerThread != null) {
            readerThread.interrupt();
        }

        readerThread = null;
        writerThread = null;

        if (socket != null && !socket.isClosed())
            socket.close();
        socket = null;
    }

    public Socket getSocket() {
        return socket;
    }

    public Client sendOk() throws IOException {
        return sendOk(true);
    }

    public Client sendOk(boolean value) throws IOException {
        OkPacket packet = new OkPacket(this);
        packet.writePacket(value);
        return this;
    }

    public void sendTCPPacket(byte[] data) {
        tcp_packet_queue.add(data);
    }

    protected boolean sendTCPNextPacket() throws IOException {
        if (tcp_packet_queue.isEmpty())
            return false;
        byte[] packet = tcp_packet_queue.remove(0);
        if (packet == null)
            return false;
        writer.write(packet);
        return true;
    }

    public void processUdpPacket(DatagramPacket recievePacket) throws IOException {
        byte[] rawData = recievePacket.getData();
        byte opCode = rawData[0];
        byte[] data = new byte[recievePacket.getLength() - 1];

        System.arraycopy(rawData, 1, data, 0, data.length);

        Packet packet = Packet.get(opCode, this, data);

        packet.handlePacket();
    }

    public int getLastReadPacket() {
        return lastReadPacket;
    }

    public void setLastReadPacket(int number) {
        this.lastReadPacket = number;
    }

    public InputStream getInputStream() {
        return reader;
    }

    public OutputStream getOutputStream() {
        return writer;
    }

    public TcpServer getServer() {
        return socketServer;
    }

    public int getLastWritePacket() {
        return lastWritePacket;
    }

    public void setLastWritePacket(int lastWritePacket) {
        this.lastWritePacket = lastWritePacket;
    }

    private long latency;
    private long pingStart;
    private int pingNumber;
    public void startPingTimer(int pingCount) {
        pingStart = System.currentTimeMillis();
    }

    public void endPingTimer(int pingRecieved) {
        if (pingRecieved > pingNumber) {
            latency = (System.currentTimeMillis() - pingStart) / 2;
        }
    }

    public long getLatency() {
        return latency;
    }

    /**
     * Get the stats of this playable
     * @return The stats of this playable represented as a {@link me.eddiep.ghost.central.network.dataserv.PlayerData} object
     */
    public PlayerData getStats() {
        return stats;
    }

    /**
     * Send a notification to this playable
     * @param title The title of the notification
     * @param description The description of the notification
     */
    public void sendNotification(String title, String description) {
        NotificationBuilder.newNotification(this)
                .title(title)
                .description(description)
                .build()
                .send();
    }


    private HashMap<Integer, Request> requests = new HashMap<>();
    /**
     * Send a request to this playable
     * @param title The title of the request
     * @param description The description of the request
     * @param result The callback for when the client responds
     */
    public void sendRequest(String title, String description, final PRunnable<Boolean> result) {
        NotificationBuilder.newNotification(this)
                .title(title)
                .description(description)
                .buildRequest()
                .onResponse(new PRunnable<Request>() {
                    @Override
                    public void run(Request p) {
                        result.run(p.accepted());
                    }
                })
                .send();
    }


    /**
     * Create a request from <b>p</b> to be friends with this playable
     * @param p The playable where the request came from
     */
    public void requestFriend(Client p) {
        if (stats.getFriends().contains(p.getPlayerID()))
            return;

        final Request request = NotificationBuilder.newNotification(p)
                .title("Friend Request")
                .description(stats.getDisplayname() + " would like to add you as a friend!")
                .buildRequest();

        request.onResponse(new PRunnable<Request>() {
            @Override
            public void run(Request p) {
                if (request.accepted()) {
                    stats.getFriends().add(p.getTarget().getPlayerID());
                    updateStats();

                    p.getTarget().getStats().getFriends().add(getPlayerID());
                    p.getTarget().updateStats();
                }
            }
        }).send();
    }

    public void updateStats() {
        Main.getLoginBridge().updatePlayerStats(getSession().toString(), getStats());
    }

    /**
     * Send a notification to this playable
     * @param notification The notification object to send
     */
    public void sendNewNotification(Notification notification) {
        while (requests.containsKey(notification.getId())) {
            notification.regenerateId();
        }

        if (notification instanceof Request) {
            requests.put(notification.getId(), (Request) notification);
        }

        NewNotificationPacket packet = new NewNotificationPacket(this);
        try {
            packet.writePacket(notification);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Respond to a request and update the client
     * @param id The ID of the request to respond to
     * @param value The response to the request
     */
    public void respondToRequest(int id, boolean value) {
        Request request = requests.get(id);
        if (request.expired())
            return;

        request.respond(value);

        requests.remove(id);
    }

    private boolean gotOk = false;
    private boolean okRespose;
    /**
     * Wait for an OK Packet
     * @return Wait for an OK Packet
     */
    public synchronized boolean waitForOk() throws InterruptedException {
        while (true) {
            if (gotOk)
                break;

            super.wait(0L);
        }
        gotOk = false;
        return okRespose;
    }

    /**
     * Remove a request from the client
     * @param request The request object to remove
     */
    public void removeRequest(Request request) {
        requests.remove(request.getId());

        DeleteRequestPacket packet = new DeleteRequestPacket(this);
        try {
            packet.writePacket(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDisplayName() {
        return stats.getDisplayname();
    }

    public void setDisplayName(String displayName) {
        this.stats.setDisplayName(displayName);
    }

    public long getPlayerID() {
        return stats.getId();
    }

    public void setCurrentGameServer(GameServer currentGameServer) {
        this.currentGameServer = currentGameServer;
    }

    public GameServer getCurrentGameServer() {
        return currentGameServer;
    }

    private class Writer extends Thread {

        @Override
        public void run() {
            Thread.currentThread().setName("Client-" + getSession() + "-Writer");
            while (socketServer.isRunning() && connected) {
                try {
                    while (sendTCPNextPacket());

                    Thread.sleep(2);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException ignored) {
                }
            }

            try {
                while (sendTCPNextPacket()); //Be sure all packets get sent
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Reader extends Thread {

        @Override
        public void run() {
            Thread.currentThread().setName("Client-" + getSession() + "-Reader");
            try {
                while (socketServer.isRunning() && connected) {
                    int readValue = reader.read();

                    if (readValue == -1) {
                        disconnect();
                        return;
                    }

                    byte opCode = (byte) readValue;

                    if (opCode == 0x01) {
                        readValue = reader.read();
                        if (readValue == -1) {
                            disconnect();
                            return;
                        }

                        okRespose = readValue == 1;
                        gotOk = true;
                    } else {
                        Packet.get(opCode, Client.this).handlePacket().endTCP();
                    }

                }
            } catch (SocketException e) {
                if (!e.getMessage().contains("Connection reset")) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socketServer.disconnect(Client.this);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
