package me.eddiep.ghost.matchmaking.network;

import me.eddiep.ghost.matchmaking.Main;
import me.eddiep.ghost.matchmaking.ServerConfig;
import me.eddiep.ghost.matchmaking.network.packets.UpdateSessionPacket;
import me.eddiep.ghost.matchmaking.player.PlayerFactory;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.network.Client;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.network.sql.PlayerData;
import me.eddiep.jconfig.JConfig;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.*;

public class TcpServer extends Server {
    private static final int PORT = 2546;

    private ServerSocket tcpServerSocket;
    private Thread tcpThread;

    private List<Client> connectedClients = new ArrayList<>();

    private final List<Runnable> toTick = Collections.synchronizedList(new LinkedList<Runnable>());
    private List<Runnable> tempTick = new LinkedList<>();
    private boolean ticking = false;
    private ServerConfig config;

    @Override
    public boolean requiresTick() {
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        setTickRate(16);
        setTickNanos(666667);

        config = JConfig.newConfigObject(ServerConfig.class);
        File file = new File("server.json");
        if (!file.exists())
            config.save(file);
        else
            config.load(file);

        try {
            if (config.getServerIP().equals("")) {
                tcpServerSocket = new ServerSocket(config.getServerPort(), config.getServerMaxBacklog());
            } else {
                tcpServerSocket = new ServerSocket(config.getServerPort(), config.getServerMaxBacklog(), InetAddress.getByName(config.getServerIP()));
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tcpThread = new Thread(TCP_SERVER_RUNNABLE);
        tcpThread.start();
    }

    public ServerConfig getConfig() {
        return config;
    }

    @Override
    protected void onStop() {
        super.onStop();

        tcpThread.interrupt();
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

    @Override
    public void disconnect(Client client) throws IOException {
        System.out.println("[SERVER] " + client.getIpAddress() + " disconnected..");
        connectedClients.remove(client);

        client.disconnect();
    }

    public List<Client> getConnectedClients() {
        return Collections.unmodifiableList(connectedClients);
    }

    private void validateTcpSession(Socket connection) throws IOException {
        DataInputStream reader = new DataInputStream(connection.getInputStream());
        byte firstByte = (byte)reader.read();
        if (firstByte == 0x00) {
            byte[] sessionBytes = new byte[32];
            int read = reader.read(sessionBytes, 0, sessionBytes.length);
            if (read == -1)
                return;
            String session = new String(sessionBytes, 0, read, Charset.forName("ASCII"));
            PlayerData data = Main.SESSION_VALIDATOR.validate(session);
            if (data == null) {
                reader.close();
                connection.close();
                return;
            }
            final Player player = PlayerFactory.registerPlayer(data.getUsername(), data);
            //final Player player = PlayerFactory.findPlayerByUUID(session);
            //if (player == null)
            //    return;
            PlayerClient client = new PlayerClient(player, connection, this);
            client.listen();
            client.sendOk();
            connectedClients.add(client);

            UpdateSessionPacket packet = new UpdateSessionPacket(client);
            packet.writePacket();

            log("TCP connection made with client " + connection.getInetAddress().toString() + " using session " + session);
        } else if (firstByte == 0x23) {
            log("GameServer attempting to connect from " + connection.getInetAddress().toString() + "...");

            GameServerClient tempClient = new GameServerClient(connection, this);
            tempClient.validateConnection();
        } else if (firstByte == 0x34) {
            log("Admin attempting to connect from " + connection.getInetAddress().toString() + "...");

            AdminClient client = new AdminClient(connection, this);
            client.verifyConnection();
        } else {
            connection.close();
        }
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
}
