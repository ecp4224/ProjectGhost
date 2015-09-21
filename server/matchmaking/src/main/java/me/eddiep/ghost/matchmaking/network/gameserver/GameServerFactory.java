package me.eddiep.ghost.matchmaking.network.gameserver;

import com.google.gson.Gson;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.utils.Global;

import java.io.*;
import java.util.*;

public class GameServerFactory {
    private static final Gson GSON = Global.GSON;
    private static HashMap<Long, GameServer> connectedGameServers = new HashMap<>();
    private static HashMap<Byte, List<Long>> queueCache = new HashMap<>();

    public static boolean isConnected(long id) {
        return connectedGameServers.containsKey(id);
    }

    public static GameServerConfiguration findServerConfig(long id) {
        File file = new File("servers", id + ".gserver");
        if (!file.exists())
            return null;

        try {
            Scanner scanner = new Scanner(file);
            scanner.useDelimiter("\\Z");
            String content = scanner.next();
            scanner.close();

            return GSON.fromJson(content, GameServerConfiguration.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static void updateServer(long id, String newConfig) {
        //Update file
        File file = new File("servers", id + ".gserver");
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            out.println(newConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (connectedGameServers.containsKey(id)) {
            GameServerConfiguration config = GSON.fromJson(newConfig, GameServerConfiguration.class);
            GameServer server = connectedGameServers.get(id);
            server.setConfig(config);
        }
    }

    public static GameServer findServer(long id) {
        return connectedGameServers.get(id);
    }

    public static GameServer createFromConfig(GameServerClient client, GameServerConfiguration config, long id) {
        GameServer server = new GameServer(client, config, id);
        connectedGameServers.put(server.getID(), server);
        if (queueCache.containsKey(config.getQueueServing()))
            queueCache.get(config.getQueueServing()).add(server.getID());
        else {
            List<Long> temp = new ArrayList<>();
            temp.add(server.getID());
            queueCache.put(config.getQueueServing(), temp);
        }

        return server;
    }

    static void disconnect(GameServer server) {
        connectedGameServers.remove(server.getID());
        queueCache.get(server.getQueueServing().asByte()).remove(server.getID());
    }

    public static List<GameServer> getConnectedServers() {
        ArrayList<GameServer> servers = new ArrayList<>();
        for (Long id : connectedGameServers.keySet()) {
            servers.add(connectedGameServers.get(id));
        }

        return Collections.unmodifiableList(servers);
    }

    public static List<GameServer> getServersWithQueue(Queues queues) {
        if (!queueCache.containsKey(queues.asByte()))
            return new ArrayList<>();

        ArrayList<GameServer> servers = new ArrayList<>();
        for (Long id : queueCache.get(queues.asByte())) {
            servers.add(connectedGameServers.get(id));
        }

        return Collections.unmodifiableList(servers);
    }

    public static GameServer findLeastFullFor(Queues queues) {
        List<GameServer> servers = getServersWithQueue(queues);

        GameServer smallest = null;
        for (GameServer server : servers) {
            if (smallest == null) {
                smallest = server;
                continue;
            }
            if (server.getPlayerCount() < smallest.getPlayerCount())
                smallest = server;
        }

        return smallest;
    }
}
