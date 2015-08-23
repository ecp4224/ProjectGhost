package me.eddiep.ghost.matchmaking.network.gameserver;

import com.google.gson.Gson;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.matchmaking.network.GameServerClient;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GameServerFactory {
    private static final Gson GSON = new Gson();
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

    public static GameServer findServer(long id) {
        return connectedGameServers.get(id);
    }

    public static GameServer createFromConfig(GameServerClient client, GameServerConfiguration config) {
        GameServer server = new GameServer(client, config);
        connectedGameServers.put(config.getID(), server);
        if (queueCache.containsKey(config.getQueueServing()))
            queueCache.get(config.getQueueServing()).add(config.getID());
        else {
            List<Long> temp = new ArrayList<>();
            temp.add(config.getID());
            queueCache.put(config.getQueueServing(), temp);
        }

        return server;
    }

    static void disconnect(GameServer server) {
        connectedGameServers.remove(server.getConfig().getID());
        queueCache.get(server.getQueueServing().asByte()).remove(server.getConfig().getID());
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
