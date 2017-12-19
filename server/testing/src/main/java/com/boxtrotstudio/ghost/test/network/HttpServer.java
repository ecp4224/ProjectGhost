package com.boxtrotstudio.ghost.test.network;

import com.boxtrotstudio.ghost.common.game.MatchFactory;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.common.game.PlayerFactory;
import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.queue.QueueType;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.network.Server;
import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.test.Main;
import com.boxtrotstudio.ghost.test.game.queue.PlayerQueue;
import com.boxtrotstudio.ghost.test.game.queue.QueueInfo;
import com.boxtrotstudio.ghost.utils.Global;
import me.eddiep.tinyhttp.TinyHttpServer;
import me.eddiep.tinyhttp.TinyListener;
import me.eddiep.tinyhttp.annotations.GetHandler;
import me.eddiep.tinyhttp.annotations.PostHandler;
import me.eddiep.tinyhttp.net.Request;
import me.eddiep.tinyhttp.net.Response;
import me.eddiep.tinyhttp.net.http.StatusCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer extends Server implements TinyListener {
    private TinyHttpServer server;

    @Override
    public void onStart() {
        super.onStart();

        server = new TinyHttpServer(8080, this, false);

        runInBackground(() -> {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
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

            if (Global.SQL.usernameExists(username)) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Username already taken!");
                return;
            }

            if (Global.SQL.createAccount(username, password)) {
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
            if ((p = PlayerFactory.getCreator().findPlayerByUsername(username)) == null) {
                response.setStatusCode(StatusCode.NotFound);
                response.echo("User logged out!");
                return;
            } else if (!PlayerFactory.getCreator().checkSession(session)) {
                response.setStatusCode(StatusCode.RequestTimeout);
                response.echo("Session timeout!");
                return;
            }

            if (p.getSession().equals(session)) {
                response.setStatusCode(StatusCode.Accepted);
            } else {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Session ID does not match!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostHandler(requestPath = "/api/v1/auth/login")
    public void loginUser2(Request request, Response response) {
        try {
            String post = request.getContentAsString();
            if (post.split("&").length < 2 || post.split("&")[0].split("=").length != 2 || post.split("&")[1].split("=").length != 2) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("{\"success\": false,\"message\": \"Invalid Request\"}");
                return;
            }

            String username = post.split("&")[0].split("=")[1];
            String password = post.split("&")[1].split("=")[1];
            if (username == null || username.trim().equalsIgnoreCase("") || password == null || password.trim().equalsIgnoreCase("")) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("{\"success\": false,\"message\": \"Bad Username or Password\"}");
                return;
            }

            PlayerData playerData = Global.SQL.fetchPlayerData(username, password);
            if (playerData == null) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("{\"success\": false,\"message\": \"Bad Username or Password\"}");
                return;
            }

            if (PlayerFactory.getCreator().findPlayerByUsername(username) != null) {
                PlayerFactory.getCreator().invalidateSession(username);
            }

            Player player = PlayerFactory.getCreator().registerPlayer(username, playerData);
            response.echo("{\"success\": true,\"session_id\": \"" + player.getSession() + "\"}");

            log.debug("Created session for " + username + " with session-id " + player.getSession());
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

            PlayerData playerData = Global.SQL.fetchPlayerData(username, password);
            if (playerData == null) {
                response.setStatusCode(StatusCode.Unauthorized);
                response.echo("Invalid username or password!");
                return;
            }

            if (PlayerFactory.getCreator().findPlayerByUsername(username) != null) {
                PlayerFactory.getCreator().invalidateSession(username);
            }

            Player player = PlayerFactory.getCreator().registerPlayer(username, playerData);
            response.setStatusCode(StatusCode.Accepted);
            response.addHeader("Set-Cookie", "session=" + player.getSession() + "; Path=/;");
            response.echo(
                    Global.GSON.toJson(playerData)
            );

            log.debug("Created session for " + username + " with session-id " + player.getSession());
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
            if ((p = PlayerFactory.getCreator().findPlayerByUUID(session)) == null) {
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
    public void getPlayerFriends(Request request, Response response) {
        String accountReq = request.getRequestPath().split("/")[4];
        try {
            long id = Long.parseLong(accountReq);
            Player p;
            if ((p = PlayerFactory.getCreator().findPlayerById(id)) != null) {
                List<PlayerData> friends = p.getOnlineFriendsStats();

                response.setStatusCode(StatusCode.Accepted);
                response.echo(Global.GSON.toJson(friends));
                return;
            }
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("Requested user is not online!");
        } catch (Throwable t) {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("Invalid ID!");
        }
    }

    @GetHandler(requestPath = "/api/accounts/stats/.*")
    public void getPlayerStats(Request request, Response response) {
        String accountReq = request.getFileRequest();
        String[] accounts = accountReq.split(",");
        if (accounts.length == 1) {
            try {
                long id = Long.parseLong(accounts[0]);
                PlayerData data = Global.SQL.fetchPlayerStat(id);
                if (data != null) {
                    response.setStatusCode(StatusCode.Accepted);
                    response.echo(Global.GSON.toJson(new PlayerData[] { data }));

                    return;
                }

                response.setStatusCode(StatusCode.NotFound);
                response.echo("ID Not Found!");
            } catch (Throwable t) {
                response.setStatusCode(StatusCode.BadRequest);
                response.echo("Invalid ID!");
            }
        } else if (accounts.length > 1) {
            long[] ids = new long[accounts.length];
            for (int i = 0; i < ids.length; i++) {
                try {
                    ids[i] = Long.parseLong(accounts[i]);
                } catch (Throwable t) {
                    t.printStackTrace();
                    response.setStatusCode(StatusCode.BadRequest);
                    response.echo("Invalid ID!");
                    return;
                }
            }

            PlayerData[] datas = Global.SQL.fetchPlayerStats(ids);

            response.setStatusCode(StatusCode.Accepted);
            response.echo(
                    Global.GSON.toJson(datas)
            );
        } else {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("Invalid ID!");
        }
    }

    @GetHandler(requestPath = "/api/accounts/usernameExists/.*")
    public void doesUsernameExist(Request request, Response response) {
        String username = request.getFileRequest();

        response.echo(Global.SQL.usernameExists(username) ? "true" : "false");
    }

    @GetHandler(requestPath = "/api/accounts/displayNameExists/.*")
    public void doesDisplayNameExist(Request request, Response response) {
        String displayName = request.getFileRequest();

        response.echo(Global.SQL.displayNameExist(displayName) ? "true" : "false");
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
        if ((p = PlayerFactory.getCreator().findPlayerByUUID(session)) == null) {
            response.setStatusCode(StatusCode.NotFound);
            response.echo("No such session!");
            return;
        }

        if (p.isInMatch()) {
            response.echo(Global.GSON.toJson(p.getMatch().matchHistory()));
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
                Match m = MatchFactory.getCreator().findMatch(id);

                response.echo(Global.GSON.toJson(m));
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
                    Match m = MatchFactory.getCreator().findMatch(id);

                    matches[i] = m;
                } catch (Throwable t) {
                    response.setStatusCode(StatusCode.BadRequest);
                    response.echo("Invalid ID!");
                    return;
                }
            }

            response.echo(
                    Global.GSON.toJson(matches)
            );
        } else {
            response.setStatusCode(StatusCode.BadRequest);
            response.echo("Invalid ID!");
        }
    }

    @GetHandler(requestPath = "/api/activematches")
    public void queryActiveMatch(Request request, Response response) {

        List<NetworkMatch> matches = MatchFactory.getCreator().getAllActiveMatches();
        List<Long> matchIds = new ArrayList<>();
        for (NetworkMatch match : matches) {
            matchIds.add(match.getID());
        }

        response.echo(
                Global.GSON.toJson(matchIds)
        );
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

        PlayerQueue queueObj = Main.playerQueueHashMap.get(type);

        response.setStatusCode(StatusCode.OK);
        response.echo(
                Global.GSON.toJson(queueObj.getInfo())
        );
    }

    @GetHandler(requestPath = "/api/queues")
    public void getAllQueues(Request request, Response response) {
        Map<String, QueueInfo[]> jsonMap = new HashMap<>();
        for (int i = 0; i < QueueType.values().length; i++) {
            List<Queues> queues = QueueType.values()[i].getQueues();
            QueueInfo[] infos = new QueueInfo[queues.size()];

            for (int z = 0; z < infos.length; z++) {
                PlayerQueue queueObj = Main.playerQueueHashMap.get(queues.get(z));
                if (queueObj == null)
                    continue;
                infos[z] = queueObj.getInfo();
            }

            jsonMap.put(QueueType.values()[i].name().toLowerCase(), infos);
        }

        response.setStatusCode(StatusCode.OK);
        response.echo(
                Global.GSON.toJson(jsonMap)
        );
    }
}
