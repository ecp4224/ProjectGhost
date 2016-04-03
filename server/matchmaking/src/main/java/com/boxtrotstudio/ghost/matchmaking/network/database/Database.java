package com.boxtrotstudio.ghost.matchmaking.network.database;

import com.boxtrotstudio.ghost.game.match.stats.MatchHistory;
import com.boxtrotstudio.ghost.matchmaking.player.ranking.*;
import com.boxtrotstudio.ghost.network.sql.impl.MongoConfig;
import com.boxtrotstudio.ghost.utils.Global;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import me.eddiep.jconfig.JConfig;
import org.bson.Document;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.zip.GZIPOutputStream;

public class Database {
    public static int Season = 0;

    private static final File MATCHES = new File("match_history");

    private static MongoCollection<Document> playerRankingCollection;
    private static MongoCollection<Document> gamesRankingCollection;
    private static Queue<MatchHistory> histories = new LinkedList<>();

    private static long startID;
    private static boolean init;

    public static void setup() {
        File dir = new File("sql");
        if (!dir.exists()) {
            if (!dir.mkdir())
                throw new RuntimeException("Could not create SQL directory!");
        }

        if (!MATCHES.exists())
            MATCHES.mkdir();

        File config = new File(dir, "mongo.conf");
        com.boxtrotstudio.ghost.network.sql.impl.MongoConfig mongoConfig = JConfig.newConfigObject(MongoConfig.class);
        if (!config.exists()) {
            mongoConfig.save(config);
        } else {
            mongoConfig.load(config);
        }

        MongoClient client = new MongoClient(mongoConfig.getIp(), mongoConfig.getPort());
        MongoDatabase ghostDB = client.getDatabase(mongoConfig.getDatabaseName());

        playerRankingCollection = ghostDB.getCollection("ranks");
        gamesRankingCollection = ghostDB.getCollection("games");

        playerRankingCollection.createIndex(new Document("pID", 1).append("season", -1));
        gamesRankingCollection.createIndex(new Document("pID", 1).append("season", -1));

        try {
            loadConfig(Files.readAllLines(Paths.get("meta.conf"), Charset.defaultCharset()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown();
            }
        }));
        init = true;
    }

    public static boolean isSetup() {
        return init;
    }

    public static void shutdown() {
        saveConfig();
    }

    private static void saveConfig() {
        List<String> lines = Arrays.asList(
                "#This file contains metadata for the server",
                "#DO NOT MODIFY THE INFORMATION FOUND IN THIS FILE",
                "",
                "last_mid=" + startID
        );

        try {
            Files.write(Paths.get("meta.conf"), lines, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig(List<String> lines) {
        for (String s : lines) {
            if (s.startsWith("#"))
                continue;
            String[] split = s.split("=");
            if (split.length != 2)
                continue;

            String key = split[0].trim();
            String value = split[1].trim();

            switch (key) {
                case "last_mid":
                    startID = Long.parseLong(value);
                    break;
            }
        }
    }

    public static void processTimelineQueue(TcpServer server) {
        while (server.isRunning()) {
            int c = 0;
            while (!histories.isEmpty()) {
                MatchHistory history = histories.poll();
                saveTimeline(history);
                c++;

                //If the server is not running, finish saving all
                //the matches before stopping
                if (c > 20 && server.isRunning()) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!server.isRunning())
                break;

            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void queueTimeline(MatchHistory history) {
        histories.offer(history);
    }

    public static void saveTimeline(MatchHistory history) {
        String json = Global.GSON.toJson(history);
        String fileName = history.getID() + ".mdata";

        File file = new File(MATCHES, fileName);

        writeToFile(file, json);
    }

    private static void writeToFile(File file, String contents) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), "UTF-8"));
            writer.write(contents);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveRank(Rank rank) {
        Document rankDocument = rank.asDocument();
        Document query = new Document().append("pID", rank.getOwnerID());

        playerRankingCollection.findOneAndUpdate(query, new Document("$set", rankDocument));
    }

    public static void pushGameOutcome(long ID, Rankable opp, double outcome) {
        Document document = new Document()
                .append("pID", ID)
                .append("outcome", outcome)
                .append("rank", opp.getRanking().getRawRating())
                .append("rd", opp.getRanking().getRawRd())
                .append("season", Season);

        gamesRankingCollection.insertOne(document);
    }

    public static RankingPeriod getGames(long ID) {
        Document query = new Document()
                .append("pID", ID)
                .append("season", Season);

        MongoCursor<Document> docs = gamesRankingCollection.find(query).iterator();

        RankingPeriod period = RankingPeriod.empty();
        while (docs.hasNext()) {
            RankedGame game = RankedGame.fromDocument(docs.next());
            period.addGame(game);
        }

        return period;
    }

    public static Rank getRank(long ID) {
        Document query = new Document()
                .append("pID", ID)
                .append("season", Season);

        Document doc = playerRankingCollection.find(query).first();
        if (doc == null)
            return Glicko2.getInstance().defaultRank();

        return Rank.fromDocument(doc);
    }

    public static long getPlayerCount() {
        return playerRankingCollection.count();
    }

    public static long getNextID() {
        startID++;
        return startID;
    }
}
