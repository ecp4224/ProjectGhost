package me.eddiep.ghost.central.network.dataserv;

import me.eddiep.ghost.central.Main;
import me.eddiep.ghost.central.TcpServer;
import me.eddiep.ghost.central.network.Client;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

public class ClientFactory {
    private static final long SESSION_TIMEOUT = 10800000; //3 hours in ms
    private static HashMap<UUID, Client> connectedUsers = new HashMap<UUID, Client>();
    private static HashMap<String, UUID> cachedUsernames = new HashMap<>();
    private static HashMap<Long, UUID> cachedIds = new HashMap<>();

    public static Client findClientByUUID(UUID uuid) {
        return connectedUsers.get(uuid);
    }

    public static Client findClientByUUID(String uuid) {
        return connectedUsers.get(UUID.fromString(uuid));
    }

    public static Client findClientByUsername(String username) {
        return connectedUsers.get(cachedUsernames.get(username));
    }

    public static Client findClientById(long id) {
        return connectedUsers.get(cachedIds.get(id));
    }

    public static Client attemptLogin(String uuid, Socket connection, TcpServer server) throws IOException {
        LoginServerBridge bridge = Main.getLoginBridge();

        if (bridge.isValidSession(uuid)) {
            PlayerData data = bridge.fetchPlayerStats(uuid);

            Client client = new Client(connection, server, uuid, data);

            connectedUsers.put(client.getSession(), client);
            cachedUsernames.put(data.getUsername(), client.getSession());
            cachedIds.put(client.getPlayerID(), client.getSession());

            return client;
        } else {
            return null;
        }
    }

    /*public static Client registerClient(String username, ClientData sqlData) {
        if (findClientByUsername(username) != null)
            throw new InvalidParameterException("Username already taken! No check was taken!");

        Client Client = Client.createClient(username, sqlData);

        connectedUsers.put(Client.getSession(), Client);
        cachedUsernames.put(username, Client.getSession());
        cachedIds.put(Client.getClientID(), Client.getSession());

        return Client;
    }*/
}
