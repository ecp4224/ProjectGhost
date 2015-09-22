package me.eddiep.ghost.matchmaking.network.database;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.matchmaking.player.ranking.Glicko2;
import me.eddiep.ghost.matchmaking.player.ranking.Rank;
import me.eddiep.ghost.matchmaking.player.ranking.Rankable;
import me.eddiep.ghost.matchmaking.player.ranking.RankingPeriod;
import me.eddiep.jconfig.JConfig;
import org.bson.Document;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.GZIPOutputStream;

import static com.mongodb.client.model.Projections.*;
import static me.eddiep.ghost.utils.Global.GSON;

public class Database {
    private static final File MATCHES = new File("match_history");

    private static MongoCollection<Document> playerRankingCollection;
    private static Queue<MatchHistory> histories = new LinkedList<>();

    private static long startID;

    public static void setup() {
        File dir = new File("sql");
        if (!dir.exists()) {
            if (!dir.mkdir())
                throw new RuntimeException("Could not create SQL directory!");
        }

        if (!MATCHES.exists())
            MATCHES.mkdir();

        File config = new File(dir, "mongo.conf");
        me.eddiep.ghost.network.sql.impl.MongoConfig mongoConfig = JConfig.newConfigObject(me.eddiep.ghost.network.sql.impl.MongoConfig.class);
        if (!config.exists()) {
            mongoConfig.save(config);
        } else {
            mongoConfig.load(config);
        }

        MongoClient client = new MongoClient(mongoConfig.getIp(), mongoConfig.getPort());
        MongoDatabase ghostDB = client.getDatabase(mongoConfig.getDatabaseName());

        playerRankingCollection = ghostDB.getCollection("ranks");

        startID = MATCHES.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("mdata");
            }
        }).length;
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
        String json = GSON.toJson(history);
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

    public static void saveRank(Player player) {
        Document rankDocument = player.getRanking().asDocument();
        Document query = new Document().append("pID", player.getPlayerID());

        playerRankingCollection.findOneAndUpdate(query, new Document("$set", rankDocument));
    }

    public static void pushGameOutcome(long ID, Rankable opp, double outcome) {
        Document query = new Document("pID", ID);

        Document game = new Document()
                .append("outcome", outcome)
                .append("rank", opp.getRanking().getRawRating())
                .append("rd", opp.getRanking().getRawRd());

        Document push = new Document("$push", new Document("games", game));
        playerRankingCollection.updateOne(query, push);
    }

    public static RankingPeriod getSeason(long ID) {
        Document query = new Document("pID", ID);

        Document doc = playerRankingCollection.find(query).projection(fields(include("games"), excludeId())).first();
        if (doc == null)
            return RankingPeriod.empty();

        return RankingPeriod.fromDocument(doc);
    }

    public static Rank getRank(long ID) {
        Document query = new Document("pID", ID);

        Document doc = playerRankingCollection.find(query).projection(fields(include("rating", "rd", "vol", "lastUpdate"), excludeId())).first();
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
