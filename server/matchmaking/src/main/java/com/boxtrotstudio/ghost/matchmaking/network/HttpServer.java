package com.boxtrotstudio.ghost.matchmaking.network;

import com.boxtrotstudio.ghost.matchmaking.network.gameserver.GameServerFactory;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.OfflineGameServer;
import com.boxtrotstudio.ghost.matchmaking.Main;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.GameServer;
import com.boxtrotstudio.ghost.network.Server;
import com.boxtrotstudio.ghost.utils.Global;
import me.eddiep.tinyhttp.TinyHttpServer;
import me.eddiep.tinyhttp.TinyListener;
import me.eddiep.tinyhttp.annotations.GetHandler;
import me.eddiep.tinyhttp.annotations.PostHandler;
import me.eddiep.tinyhttp.net.Request;
import me.eddiep.tinyhttp.net.Response;
import me.eddiep.tinyhttp.net.http.StatusCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class HttpServer extends Server implements TinyListener {

    private TinyHttpServer server;

    @Override
    public void onStart() {
        super.onStart();

        server = new TinyHttpServer(8080, this, false);

        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean validate(Request request, Response response) {
        if (request.hasHeader("X-AdminKey")) {
            String key = request.getHeaderValue("X-AdminKey");
            if (key.equals(Main.getServer().getConfig().getAdminSecret()))
                return true;
        }

        response.setStatusCode(StatusCode.Unauthorized);
        response.echo("<b>Unauthorized Access</b>");
        return false;
    }

    @GetHandler(requestPath = "/admin/info")
    public void info(Request request, Response response) {
        if (!validate(request, response))
            return;

        List<GameServer> servers = GameServerFactory.getConnectedServers();

        ServerInfo info = new ServerInfo();
        info.playersInQueue = Main.getServer().getConnectedClients().size();

        for (GameServer server : servers) {
            info.matchCount += server.getMatchCount();
        }

        info.connectedServers = servers.size();

        response.echo(Global.GSON.toJson(info));
    }

    @GetHandler(requestPath = "/admin/servers")
    public void getServers(Request request, Response response) {
        if (!validate(request, response))
            return;

        String json = Global.GSON.toJson(GameServerFactory.getAllServers());

        response.echo(json);
    }

    @GetHandler(requestPath = "/admin/servers/[0-9]+")
    public void getServer(Request request, Response response) {
        if (!validate(request, response))
            return;

        String serverReq = request.getFileRequest();

        try {
            long id = Long.parseLong(serverReq);

            OfflineGameServer server = GameServerFactory.findServer(id);

            if (server == null) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("{\"error\":\"true\", \"message\":\"Server not found!\"}");
            }

            String json = Global.GSON.toJson(server);

            response.echo(json);
        } catch (Throwable t) {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("{\"error\":\"true\", \"message\":\"Invalid ID!\"}");
        }
    }

    @PostHandler(requestPath = "/admin/servers/[0-9]+")
    public void updateServer(Request request, Response response) {
        if (!validate(request, response))
            return;

        try {
            String serverReq = request.getFileRequest();
            long id = Long.parseLong(serverReq);
            String updated = request.getContentAsString();
            GameServerFactory.updateServer(id, updated);
            response.echo("{\"error\":\"false\", \"message\":\"Config saved!\"}");
        } catch (IOException e) {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("{\"error\":\"true\", \"message\":\"" + e.getMessage() + "\"}");
        }  catch (Throwable t) {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("{\"error\":\"true\", \"message\":\"Invalid ID!\"}");
        }
    }

    @PostHandler(requestPath = "/admin/servers/add")
    public void addServer(Request request, Response response) {
        if (!validate(request, response))
            return;

        try {
            long newID = GameServerFactory.getNextID();
            String config = request.getContentAsString();
            File file = new File("servers", newID + ".gserver");
            Files.write(file.toPath(), config.getBytes(), StandardOpenOption.CREATE);
            response.echo("{\"error\":\"false\", \"message\":\"Config saved!\"}");
        } catch (IOException e) {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("{\"error\":\"true\", \"message\":\"" + e.getMessage() + "\"}");
        }  catch (Throwable t) {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("{\"error\":\"true\", \"message\":\"Invalid ID!\"}");
        }
    }

    @PostHandler(requestPath = "/admin/servers/remove")
    public void removeServer(Request request, Response response) {
        if (!validate(request, response))
            return;

        try {
            long id = Long.parseLong(request.getContentAsString());
            File file = new File("servers", id + ".gserver");

            boolean result = file.delete();
            String message = result ? "Server deleted" : "Server not deleted";

            OfflineGameServer offlineGServer = GameServerFactory.findServer(id);
            if (offlineGServer != null) {
                offlineGServer.getServer().disconnect();
                response.echo("{\"error\":\"" + result + "\", \"message\":\"" + message + "\"}");
            } else {
                response.echo("{\"error\":\"true\", \"message\":\"Server not found!\"}");
            }
        } catch (IOException e) {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("{\"error\":\"true\", \"message\":\"" + e.getMessage() + "\"}");
        }  catch (Throwable t) {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("{\"error\":\"true\", \"message\":\"Invalid ID!\"}");
        }
    }

    public class ServerInfo {
        public int playersInQueue;
        public int matchCount;
        public int connectedServers;
    }
}
