package me.eddiep.ghost.server;

import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.game.entities.PlayerFactory;
import me.eddiep.ghost.server.network.sql.PlayerData;
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

    @PostHandler(requestPath = "/api/accounts/register")
    public void registerUser(Request request, Response response) {
        try {
            String post = request.getContentAsString();
            if (post.split("&").length < 2 || post.split("&")[0].split("=").length != 2 || post.split("&")[1].split("=").length != 2) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Invalid request!");
                return;
            }

            String username = post.split("&")[0].split("=")[1];
            String password = post.split("&")[1].split("=")[1];
            if (username == null || username.trim().equalsIgnoreCase("") || password == null || password.trim().equalsIgnoreCase("")) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Bad username or password!");
                return;
            }

            if (Main.SQL.usernameExists(username)) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Username already taken!");
                return;
            }

            if (Main.SQL.createAccount(username, password)) {
                response.echo("success");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.setStatusCode(StatusCode.InternalServerError);
        response.echo("Error creating account..");
    }

    @PostHandler(requestPath = "/api/accounts/validate")
    public void validateUser(Request request, Response response) {
        try {
            String content = request.getContentAsString();
            String username = content.split("&")[0];
            String session = content.split("&")[1];

            Player p;
            if ((p = PlayerFactory.findPlayerByUsername(username)) == null) {
                response.setStatusCode(StatusCode.NotFound);
                response.echo("User logged out!");
                return;
            } else if (!PlayerFactory.checkSession(session)) {
                response.setStatusCode(StatusCode.RequestTimeout);
                response.echo("Session timeout!");
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

    @PostHandler(requestPath = "/api/accounts/login")
    public void loginUser(Request request, Response response) {
        try {
            String post = request.getContentAsString();
            if (post.split("&").length < 2 || post.split("&")[0].split("=").length != 2 || post.split("&")[1].split("=").length != 2) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Invalid request!");
                return;
            }

            String username = post.split("&")[0].split("=")[1];
            String password = post.split("&")[1].split("=")[1];
            if (username == null || username.trim().equalsIgnoreCase("") || password == null || password.trim().equalsIgnoreCase("")) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Bad username or password!");
                return;
            }

            PlayerData playerData = Main.SQL.fetchPlayerData(username, password);
            if (playerData == null) {
                response.setStatusCode(StatusCode.Unauthorized);
                response.echo("Invalid username or password!");
                return;
            }

            if (PlayerFactory.findPlayerByUsername(username) != null) {
                PlayerFactory.invalidateSession(username);
            }

            Player player = PlayerFactory.registerPlayer(username, playerData);
            response.setStatusCode(StatusCode.Accepted);
            response.addHeader("Set-Cookie", "session=" + player.getSession().toString() + "; Path=/;");

            log("Created session for " + username + " with session-id " + player.getSession());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetHandler(requestPath = "/api/accounts/usernameExists/.*")
    public void doesUsernameExist(Request request, Response response) {
        String username = request.getFileRequest();

        response.echo(Main.SQL.usernameExists(username) ? "true" : "false");
    }

    @GetHandler(requestPath = "/api/accounts/displayNameExists/.*")
    public void doesDisplayNameExist(Request request, Response response) {
        String displayName = request.getFileRequest();

        response.echo(Main.SQL.displayNameExist(displayName) ? "true" : "false");
    }

    @GetHandler(requestPath = "/api/matches/isInMatch")
    public void isInMatch(Request request, Response response) {
        try {
            String session = request.getContentAsString();

            Player p;
            if ((p = PlayerFactory.findPlayerByUUID(session)) == null) {
                response.setStatusCode(StatusCode.NotFound);
                response.echo("No such session!");
                return;
            }

            response.echo(p.isInMatch() ? "true" : "false");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
