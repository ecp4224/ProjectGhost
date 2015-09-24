package me.eddiep.ghost.matchmaking.network.database;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.player.ranking.*;
import me.eddiep.jconfig.JConfig;
import org.bson.Document;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.GZIPOutputStream;

import static me.eddiep.ghost.utils.Global.GSON;

public class Database {
    public static int Season = 0;

    private static final File MATCHES = new File("match_history");

    private static MongoCollection<Document> playerRankingCollection;
    private static MongoCollection<Document> gamesRankingCollection;
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
        gamesRankingCollection = ghostDB.getCollection("games");

        playerRankingCollection.createIndex(new Document("pID", 1).append("season", -1));
        gamesRankingCollection.createIndex(new Document("pID", 1).append("season", -1));

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
