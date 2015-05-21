package me.eddiep.ghost.central.network.gameserv;

import com.google.gson.Gson;
import me.eddiep.ghost.central.Main;
import me.eddiep.ghost.central.network.Client;
import me.eddiep.ghost.central.network.packet.impl.GameServerLoginPacket;
import me.eddiep.ghost.central.network.packet.impl.InterServerPacket;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameServerFactory {
    private static final String CONFIG_PATH = "gservers.json";

    // <Category, Map<Id, listOfGameServers>>
    private HashMap<Byte, HashMap<Byte, List<GameServer>>> gameServers = new HashMap<>();
    private ArrayList<GameServerConnection> connections = new ArrayList<>();

    public void load() throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(CONFIG_PATH)));

        GameServer[] servers = Main.GSON.fromJson(json, GameServer[].class);

        for (GameServer g : servers) {
            if (!gameServers.containsKey(g.getCategoryId())) {
                gameServers.put(g.getCategoryId(), new HashMap<Byte, List<GameServer>>());
            }
            if (!gameServers.get(g.getCategoryId()).containsKey(g.getId())) {
                gameServers.get(g.getCategoryId()).put(g.getId(), new ArrayList<GameServer>());
            }
            gameServers.get(g.getCategoryId()).get(g.getId()).add(g);
        }
    }

    public void queryQueueInfo() {
        for (GameServerConnection connection : connections) {
            InterServerPacket packet = new InterServerPacket(connection);
            try {
                packet.writePacket("queueInfoRequest", connection.getGameServer().getSecret());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connectToServers() {
        for (Byte cid : gameServers.keySet()) {
            for (Byte id : gameServers.get(cid).keySet()) {
                for (GameServer server : gameServers.get(cid).get(id)) {
                    try {
                        GameServerConnection connection = connectToServer(server);
                        if (connection == null) {
                            System.err.println("!! Error connecting to game server " + server.getIp() + ":" + server.getPort() + " !!");
                            continue;
                        }
                        connections.add(connection);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("!! Error connecting to game server " + server.getIp() + ":" + server.getPort() + " !!");
                    }
                }
            }
        }
    }

    private GameServerConnection connectToServer(GameServer server) throws IOException {
        Socket connection = new Socket(server.getIp(), server.getPort());

        connection.setSoTimeout(0); //Never timeout a gameserver connection

        GameServerConnection gconnection = new GameServerConnection(connection, Main.TCP_UDP_SERVER, server);

        server.setConnection(gconnection);

        GameServerLoginPacket packet = new GameServerLoginPacket(gconnection);
        packet.writePacket(server.getSecret());

        try {
            boolean result = gconnection.waitForOk();
            if (!result) {
                gconnection.disconnect();

                System.err.println("!! Invalid secret for " + server.getIp() + ":" + server.getPort() + " !!");

                return null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        return gconnection;
    }

    public GameServer findServerFor(byte toJoin) {
       return null;
    }

    public List<QueueInfo> getAllRankedGames() {
        List<QueueInfo> info = new ArrayList<QueueInfo>();
        for (GameServerConnection con : connections) {
            if (con.getGameServer().isRanked()) {
                info.add(con.getGameServer().getQueueInfo());
            }
        }
    }
}
