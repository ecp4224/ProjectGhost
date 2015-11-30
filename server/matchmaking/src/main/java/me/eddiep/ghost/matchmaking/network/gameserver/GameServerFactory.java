package me.eddiep.ghost.matchmaking.network.gameserver;

import com.google.gson.Gson;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.utils.Global;

import java.io.*;
import java.net.InetAddress;
import java.util.*;

public class GameServerFactory {
    private static final Gson GSON = Global.GSON;
    private static HashMap<Long, GameServer> connectedGameServers = new HashMap<>();

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

    public static OfflineGameServer findServer(long id) {
        GameServer server;
        if ((server = connectedGameServers.get(id)) != null) {
            return new OfflineGameServer(server);
        } else {
            GameServerConfiguration config = findServerConfig(id);
            if (config != null)
                return new OfflineGameServer(config, id);
        }
        return null;
    }

    public static GameServer createFromConfig(GameServerClient client, GameServerConfiguration config, long id) {
        GameServer server = new GameServer(client, config, id);
        connectedGameServers.put(server.getID(), server);
        return server;
    }

    static void disconnect(GameServer server) {
        connectedGameServers.remove(server.getID());
    }

    public static List<GameServer> getConnectedServers() {
        ArrayList<GameServer> servers = new ArrayList<>();
        for (Long id : connectedGameServers.keySet()) {
            servers.add(connectedGameServers.get(id));
        }

        return Collections.unmodifiableList(servers);
    }

    public static List<OfflineGameServer> getAllServers() {
        File dir = new File("servers");

        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".gserver");
            }
        });

        ArrayList<OfflineGameServer> servers = new ArrayList<>();
        for (File file : files) {
            String name = file.getName();
            int npos = name.lastIndexOf(".");
            if (npos > 0)
                name = name.substring(0, npos);

            long id;
            try {
                id = Long.parseLong(name);
            } catch (Exception e) {
                continue;
            }

            GameServer server;
            if ((server = connectedGameServers.get(id)) != null) {
                OfflineGameServer oserver = new OfflineGameServer(server);
                servers.add(oserver);
                continue;
            }

            Scanner scanner = null;
            try {
                scanner = new Scanner(file);
                scanner.useDelimiter("\\Z");
                String content = scanner.next();
                scanner.close();

                GameServerConfiguration config = GSON.fromJson(content, GameServerConfiguration.class);
                OfflineGameServer oserver = new OfflineGameServer(config, id);

                servers.add(oserver);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return Collections.unmodifiableList(servers);
    }

    public static List<GameServer> getServersWithStream(Stream stream) {
        ArrayList<GameServer> servers = new ArrayList<>();
        for (Long id : connectedGameServers.keySet()) {
            GameServer server = connectedGameServers.get(id);
            if (server.getConfig().getStream() != stream)
                continue;

            servers.add(server);
        }

        return Collections.unmodifiableList(servers);
    }

    public static GameServer findServerWithIP(InetAddress address) {
        for (Long id : connectedGameServers.keySet()) {
            GameServer server = connectedGameServers.get(id);
            if (server.getConfig().getIp().equalsIgnoreCase(address.getHostAddress()))
                return server;
        }
        return null;
    }

    public static GameServer findLeastFullFor(Stream stream) {
        List<GameServer> servers = getServersWithStream(stream);

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

    public static GameServer findLeastFullFor(Stream stream, List<GameServer> exclude) {
        List<GameServer> servers = getServersWithStream(stream);

        GameServer smallest = null;
        for (GameServer server : servers) {
            if (exclude.contains(server))
                continue;

            if (smallest == null) {
                smallest = server;
                continue;
            }
            if (server.getPlayerCount() < smallest.getPlayerCount())
                smallest = server;
        }

        return smallest;
    }

    public static GameServer createMatchFor(Queues queue, Player[] team1, Player[] team2, Stream stream) throws IOException {
        if (stream == Stream.BUFFERED)
            throw new IllegalAccessError("Games cant be created in buffered servers!");

        if (team1[0].getPreferedServer() != null) {
            InetAddress prefered = team1[0].getPreferedServer();
            GameServer preferedServer = findServerWithIP(prefered);
            if (preferedServer != null) {
                try {
                    preferedServer.createMatchFor(queue, team1, team2);
                    return preferedServer;
                } catch (MatchCreationExceptoin matchCreationExceptoin) {
                    matchCreationExceptoin.printStackTrace();
                    return null;
                }
            } else {
                System.err.println("Prefered server is not connected!");
                return null;
            }
        }

        List<GameServer> failed = new ArrayList<>();
        GameServer openServer = null;
        while (true) {
            openServer = findLeastFullFor(stream, failed);
            if (openServer == null)
                break;
            try {
                openServer.createMatchFor(queue, team1, team2);
            } catch (MatchCreationExceptoin matchCreationExceptoin) {
                matchCreationExceptoin.printStackTrace();
                failed.add(openServer);
                continue;
            }

            break;
        }

        if (openServer == null) {
            System.err.println("No more open servers!");
        }

        return openServer;
    }
}
