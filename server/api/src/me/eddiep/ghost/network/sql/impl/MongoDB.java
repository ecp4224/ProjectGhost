package me.eddiep.ghost.network.sql.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import me.eddiep.ghost.game.Match;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.network.sql.PlayerData;
import me.eddiep.ghost.network.sql.PlayerUpdate;
import me.eddiep.ghost.network.sql.SQL;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.PasswordHash;
import me.eddiep.jconfig.JConfig;
import org.bson.Document;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.eddiep.ghost.utils.Constants.*;

public class MongoDB implements SQL {
    MongoDatabase ghostDB;
    MongoCollection<Document> playerCollection;
    MongoCollection<Document> matchCollection;

    @Override
    public void loadAndSetup() {
        File dir = new File("sql");
        if (!dir.exists()) {
            if (!dir.mkdir())
                throw new RuntimeException("Could not create SQL directory!");
        }

        File config = new File(dir, "mongo.conf");
        MongoConfig mongoConfig = JConfig.newConfigObject(MongoConfig.class);
        if (!config.exists()) {
            mongoConfig.save(config);
        } else {
            mongoConfig.load(config);
        }

        MongoClient client = new MongoClient(mongoConfig.getIp(), mongoConfig.getPort());
        ghostDB = client.getDatabase(mongoConfig.getDatabaseName());

        playerCollection = ghostDB.getCollection("players");
        matchCollection = ghostDB.getCollection("matches");
    }

    @Override
    public void storePlayerData(PlayerData data) {
        if (playerCollection == null)
            return;

        data.setId(playerCollection.count() + 1L);
        playerCollection.insertOne(data.asDocument());
    }

    @Override
    public void updatePlayerData(PlayerUpdate data) {
        Document query = new Document().append(ID, data.getId());

        playerCollection.findOneAndUpdate(query, data.asDocument());
    }

    @Override
    public void bulkUpdate(PlayerUpdate[] updates) {
        List<WriteModel<Document>> toUpdate = new ArrayList<>();
        for (PlayerUpdate update : updates) {
            toUpdate.add(new UpdateOneModel<Document>(new Document("id", update.getId()), update.asDocument()));
        }

        playerCollection.bulkWrite(toUpdate, new BulkWriteOptions().ordered(false));
    }

    @Override
    public PlayerData fetchPlayerData(String username, String password) {
        Document doc = playerCollection.find(new Document().append(USERNAME, username)).first();
        if (doc == null)
            return null;

        String hash = doc.get(HASH, String.class);
        if (hash == null)
            return null;

        try {
            if (PasswordHash.validatePassword(password, hash)) {
                return PlayerData.fromDocument(doc);
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PlayerData[] fetchPlayerStats(long[] id) {
        List<Long> list = new ArrayList<>();
        for (Long i : id) {
            list.add(i);
        }

        MongoCursor<Document> docs = playerCollection.find(new Document(
                                                              ID, new Document(
                                                                  "$in", list)
                                                              )
                                                           ).iterator();

        List<PlayerData> results = new ArrayList<>();
        while (docs.hasNext()) {
            PlayerData data = PlayerData.fromDocument(docs.next());
            data.setHash(""); //Hide hash
            results.add(data);
        }

        return results.toArray(new PlayerData[results.size()]);
    }

    @Override
    public PlayerData fetchPlayerStat(long id) {
        Document docs = playerCollection.find(new Document(
                        ID, new Document(
                        "$in", Arrays.asList(id))
                )
        ).first();
        if (docs != null) {
            PlayerData data = PlayerData.fromDocument(docs);
            data.setHash(""); //Hide hash
            return data;
        }
        return null;
    }

    @Override
    public List<PlayerData> fetchPlayerStats(long min, long max) {
        List<Long> list = new ArrayList<>();
        for (; min <= max; min++) {
            list.add(min);
        }

        MongoCursor<Document> docs = playerCollection.find(new Document(
                        ID, new Document(
                        "$in", list)
                )
        ).iterator();

        List<PlayerData> results = new ArrayList<>();
        while (docs.hasNext()) {
            PlayerData data = PlayerData.fromDocument(docs.next());
            data.setHash(""); //Hide hash
            results.add(data);
        }

        return results;
    }

    @Override
    public long getPlayerCount() {
        return playerCollection.count();
    }

    @Override
    public boolean createAccount(String username, String password) {
        if (usernameExists(username))
            throw new IllegalArgumentException("Username already exists!");

        try {
            String hash = PasswordHash.createHash(password);

            String displayName = username;
            while (displayNameExist(displayName)) {
                displayName = username + Global.RANDOM.nextInt();
            }
            PlayerData data = new PlayerData(username, displayName);

            data.setHash(hash);

            storePlayerData(data);

            System.out.println("[SERVER] Created account for " + username + "!");

            return true;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean usernameExists(String username) {
        return playerCollection.find(new Document().append(USERNAME, username)).first() != null;
    }

    @Override
    public boolean displayNameExist(String displayName) {
        return playerCollection.find(new Document().append(DISPLAY_NAME, displayName)).first() != null;
    }

    @Override
    public void saveMatch(MatchHistory history) {
        matchCollection.insertOne(history.asDocument());
    }

    @Override
    public long getStoredMatchCount() {
        return matchCollection.count();
    }

    @Override
    public Match fetchMatch(long id) {
        Document temp = matchCollection.find(new Document().append("id", id)).first();
        if (temp == null)
            return null;

        return MatchHistory.fromDocument(temp);
    }
}
