package me.eddiep.ghost.server;

import me.eddiep.ghost.server.game.impl.Player;
import me.eddiep.ghost.server.game.impl.PlayerFactory;
import me.eddiep.tinyhttp.TinyHttpServer;
import me.eddiep.tinyhttp.TinyListener;
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

    @PostHandler(requestPath = "/api/validate")
    public void validateUser(Request request, Response response) {
        try {
            String content = request.getContentAsString();
            String username = content.split("&")[0];
            String session = content.split("&")[1];

            Player p;
            if ((p = PlayerFactory.findPlayerByUsername(username)) == null) {
                response.setStatusCode(StatusCode.NotFound);
                response.echo("Username not found!");
                return;
            }

            if (p.getSession().toString().equals(session)) {
                response.setStatusCode(StatusCode.Accepted);
            } else {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Session ID does not match!");
            }
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
                return;
            } else if (username == null || username.trim().equalsIgnoreCase("")) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Bad username!");
                return;
            }

            Player player = PlayerFactory.registerPlayer(username);
            response.setStatusCode(StatusCode.Accepted);
            response.addHeader("Set-Cookie", "session=" + player.getSession().toString() + ";");

            log("Created session for " + username + " with session-id " + player.getSession());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
