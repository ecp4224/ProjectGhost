package me.eddiep.ghost.server;

import me.eddiep.ghost.server.network.Player;
import me.eddiep.ghost.server.network.PlayerFactory;
import me.eddiep.tinyhttp.TinyHttpServer;
import me.eddiep.tinyhttp.TinyListener;
import me.eddiep.tinyhttp.annotations.GetHandler;
import me.eddiep.tinyhttp.annotations.PostHandler;
import me.eddiep.tinyhttp.net.Request;
import me.eddiep.tinyhttp.net.Response;
import me.eddiep.tinyhttp.net.http.StatusCode;

import java.io.IOException;

public class HttpServer extends Server implements TinyListener {
    private TinyHttpServer server;

    @Override
    public boolean requiresTick() {
        return false;
    }

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

    @Override
    public void onStop() {
        super.onStop();

        try {
            server.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostHandler(requestPath = "/api/register")
    public void registerUser(Request request, Response response) {
        try {
            String username = request.getContentAsString();
            if (PlayerFactory.findPlayerByUsername(username) != null) {
                response.setStatusCode(StatusCode.Conflict);
                response.echo("Username already registered!");
            } else if (username == null || username.trim().equalsIgnoreCase("")) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Bad username!");
            }

            Player player = PlayerFactory.registerPlayer(username);
            response.setStatusCode(StatusCode.Accepted);
            response.addHeader("Set-Cookie", "session=" + player.getSession().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetHandler(requestPath = "/api/queue")
    public void joinQueue(Request request, Response response) {
        if (!request.hasHeader("Cookie")) {
            response.setStatusCode(StatusCode.BadRequest);
            return;
        }

        String session = "";
        String[] rawCookies = request.getHeaderValue("Cookie").split(";");
        for (String cookie : rawCookies) {
            cookie = cookie.trim();
            String name = cookie.split("=")[0];
            if (name.equals("session")) {
                session = cookie.split("=")[1];
            }
        }

        if (session.equals("")) {
            response.setStatusCode(StatusCode.BadRequest);
            return;
        }

        Player player = PlayerFactory.findPlayerByUUID(session);

        if (player == null) {
            response.setStatusCode(StatusCode.BadRequest);
            return;
        }


    }
}
