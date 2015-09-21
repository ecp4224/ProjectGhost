package me.eddiep.ghost.matchmaking.network;

import com.google.gson.reflect.TypeToken;
import me.eddiep.ghost.matchmaking.Main;
import me.eddiep.ghost.matchmaking.network.gameserver.GameServer;
import me.eddiep.ghost.matchmaking.network.gameserver.GameServerFactory;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.utils.Global;
import me.eddiep.tinyhttp.TinyHttpServer;
import me.eddiep.tinyhttp.TinyListener;
import me.eddiep.tinyhttp.annotations.GetHandler;
import me.eddiep.tinyhttp.annotations.PostHandler;
import me.eddiep.tinyhttp.net.Request;
import me.eddiep.tinyhttp.net.Response;
import me.eddiep.tinyhttp.net.http.StatusCode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HttpServer extends Server implements TinyListener {

    private TinyHttpServer server;
    private ArrayList<String> verifiedMacs = new ArrayList<>();

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
            if (key.equals(Main.getServer().getConfig().getAdminSecret())) {
                if (request.hasHeader("X-MAC")) {
                    String val = request.getHeaderValue("X-MAC");
                    if (verifiedMacs.contains(val))
                        return true;
                    else {
                        updateMacs(); //update and check again
                        if (verifiedMacs.contains(val))
                            return true;
                        else {
                            //Log access
                            try {
                                Files.write(Paths.get("MAC-access-log.txt"), val.getBytes(), StandardOpenOption.APPEND);
                                //TODO Post to slack?
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        response.echo("<b>Unauthorized Access</b>");
        return false;
    }

    private void updateMacs() {
        File file = new File("adminMAC.json");
        if (!file.exists())
            return;
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            return;
        }
        scanner.useDelimiter("\\Z");
        String content = scanner.next();
        scanner.close();

        Type type = new TypeToken<List<String>>(){}.getType();
        verifiedMacs = Global.GSON.fromJson(content, type);
    }

    @GetHandler(requestPath = "/admin/servers")
    public void getServers(Request request, Response response) {
        if (!validate(request, response))
            return;

        String json = Global.GSON.toJson(GameServerFactory.getConnectedServers());

        response.echo(json);
    }

    @GetHandler(requestPath = "/admin/servers/[0-9]+")
    public void getServer(Request request, Response response) {
        if (!validate(request, response))
            return;

        String serverReq = request.getFileRequest();

        try {
            long id = Long.parseLong(serverReq);

            GameServer server = GameServerFactory.findServer(id);

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
}
