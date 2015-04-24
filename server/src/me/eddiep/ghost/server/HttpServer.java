package me.eddiep.ghost.server;

import com.google.gson.Gson;
import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.MatchFactory;
import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.game.entities.PlayerFactory;
import me.eddiep.ghost.server.game.queue.PlayerQueue;
import me.eddiep.ghost.server.game.queue.QueueInfo;
import me.eddiep.ghost.server.game.queue.QueueType;
import me.eddiep.ghost.server.game.queue.Queues;
import me.eddiep.ghost.server.network.sql.PlayerData;
import me.eddiep.tinyhttp.TinyHttpServer;
import me.eddiep.tinyhttp.TinyListener;
import me.eddiep.tinyhttp.annotations.GetHandler;
import me.eddiep.tinyhttp.annotations.PostHandler;
import me.eddiep.tinyhttp.net.Request;
import me.eddiep.tinyhttp.net.Response;
import me.eddiep.tinyhttp.net.http.StatusCode;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer extends Server implements TinyListener {
    private static final Gson GSON = new Gson();
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
                response.setStatusCode(StatusCode.Accepted);
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
            String post = request.getContentAsString();
            if (post.split("&").length < 2 || post.split("&")[0].split("=").length != 2 || post.split("&")[1].split("=").length != 2) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Invalid request!");
                return;
            }

            String username = post.split("&")[0].split("=")[1];
            String session = post.split("&")[1].split("=")[1];

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
            response.echo(
                    GSON.toJson(playerData)
            );

            log("Created session for " + username + " with session-id " + player.getSession());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostHandler(requestPath = "/api/accounts/logout")
    public void logoutUser(Request request, Response response) {
        try {
            String[] cookies = request.getHeaderValue("Cookie").split(";");

            String session = null;
            for (String s : cookies) {
                s = s.trim();
                if (s.split("=")[0].equalsIgnoreCase("session")) {
                    session = s.split("=")[1].trim();
                    break;
                }
            }

            if (session == null) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("No session specified!");
                return;
            }

            Player p;
            if ((p = PlayerFactory.findPlayerByUUID(session)) == null) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Bad session!");
                return;
            }

            p.logout();

            response.setStatusCode(StatusCode.Accepted);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetHandler(requestPath = "/api/accounts/stats/.*/onlineFriends")
    public void getPlayerFriends(Request request, Response respose) {
        String accountReq = request.getRequestPath().split("/")[4];
        try {
            long id = Long.parseLong(accountReq);
            Player p;
            if ((p = PlayerFactory.findPlayerById(id)) != null) {
                List<PlayerData> friends = p.getOnlineFriendsStats();

                respose.setStatusCode(StatusCode.Accepted);
                respose.echo(
                        GSON.toJson(friends)
                );
                return;
            }
            respose.setStatusCode(StatusCode.BadRequest);
            respose.echo("Requested user is not online!");
        } catch (Throwable t) {
            respose.setStatusCode(StatusCode.BadRequest);
            respose.echo("Invalid ID!");
        }
    }

    @GetHandler(requestPath = "/api/accounts/stats/.*")
    public void getPlayerStats(Request request, Response respose) {
        String accountReq = request.getFileRequest();
        String[] accounts = accountReq.split(",");
        if (accounts.length == 1) {
            try {
                long id = Long.parseLong(accounts[0]);
                PlayerData data = Main.SQL.fetchPlayerStat(id);
                if (data != null) {
                    respose.setStatusCode(StatusCode.Accepted);
                    respose.echo(
                            GSON.toJson(new PlayerData[] { data })
                    );

                    return;
                }

                respose.setStatusCode(StatusCode.NotFound);
                respose.echo("ID Not Found!");
            } catch (Throwable t) {
                respose.setStatusCode(StatusCode.BadRequest);
                respose.echo("Invalid ID!");
            }
        } else if (accounts.length > 1) {
            long[] ids = new long[accounts.length];
            for (int i = 0; i < ids.length; i++) {
                try {
                    ids[i] = Long.parseLong(accounts[i]);
                } catch (Throwable t) {
                    t.printStackTrace();
                    respose.setStatusCode(StatusCode.BadRequest);
                    respose.echo("Invalid ID!");
                    return;
                }
            }

            PlayerData[] datas = Main.SQL.fetchPlayerStats(ids);

            respose.setStatusCode(StatusCode.Accepted);
            respose.echo(
                    GSON.toJson(datas)
            );
        } else {
            respose.setStatusCode(StatusCode.BadRequest);
            respose.echo("Invalid ID!");
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

    public void currentMatch(Request request, Response response) {
        String[] cookies = request.getHeaderValue("Cookie").split(";");

        String session = null;
        for (String s : cookies) {
            s = s.trim();
            if (s.split("=")[0].equalsIgnoreCase("session")) {
                session = s.split("=")[1].trim();
                break;
            }
        }

        if (session == null) {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("No session specified!");
            return;
        }

        Player p;
        if ((p = PlayerFactory.findPlayerByUUID(session)) == null) {
            response.setStatusCode(StatusCode.NotFound);
            response.echo("No such session!");
            return;
        }

        if (p.isInMatch()) {
            response.echo(
                    GSON.toJson(p.getMatch().matchHistory())
            );
        } else {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("Not in match!");
        }
    }

    @GetHandler(requestPath = "/api/matches/.*")
    public void queryMatch(Request request, Response response) {
        if (request.getFileRequest().equalsIgnoreCase("currentMatch")) {
            currentMatch(request, response);
            return;
        }
        String requestString = request.getFileRequest();
        String[] matchIds = requestString.split(",");

        if (matchIds.length == 1) {
            try {
                long id = Long.parseLong(matchIds[0].trim());
                Match m = MatchFactory.findMatch(id);

                response.echo(
                        GSON.toJson(m)
                );
            } catch (Throwable t) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Invalid ID!");
            }
        } else if (matchIds.length > 1) {
            int max = Math.min(matchIds.length, 15);
            Match[] matches = new Match[max];
            for (int i = 0; i < max; i++) {
                try {
                    long id = Long.parseLong(matchIds[i].trim());
                    Match m = MatchFactory.findMatch(id);

                    matches[i] = m;
                } catch (Throwable t) {
                    response.setStatusCode(StatusCode.BadRequest);
                    response.echo("Invalid ID!");
                    return;
                }
            }

            response.echo(
                    GSON.toJson(matches)
            );
        } else {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("Invalid ID!");
        }
    }

    @GetHandler(requestPath = "/api/queues/.*")
     public void getQueueStatus(Request request, Response response) {
        String queue = request.getFileRequest();
        Queues type;
        try {
            byte id = Byte.parseByte(queue);
            type = Queues.byteToType(id);
        } catch (Throwable t) {
            type = Queues.nameToType(queue);
        }

        if (type == Queues.UNKNOWN) {
            response.setStatusCode(StatusCode.NotFound);
            return;
        }

        PlayerQueue queueObj = type.getQueue();

        response.setStatusCode(StatusCode.OK);
        response.echo(
                GSON.toJson(queueObj.getInfo())
        );
    }

    @GetHandler(requestPath = "/api/queues")
    public void getAllQueues(Request request, Response response) {
        Map<String, QueueInfo[]> jsonMap = new HashMap<>();
        for (int i = 0; i < QueueType.values().length; i++) {
            List<Queues> queues = QueueType.values()[i].getQueues();
            QueueInfo[] infos = new QueueInfo[queues.size()];

            for (int z = 0; z < infos.length; z++) {
                PlayerQueue queueObj = queues.get(z).getQueue();
                if (queueObj == null)
                    continue;
                infos[z] = queueObj.getInfo();
            }

            jsonMap.put(QueueType.values()[i].name().toLowerCase(), infos);
        }

        response.setStatusCode(StatusCode.OK);
        response.echo(
                GSON.toJson(jsonMap)
        );
    }
}
