package me.eddiep.ghost.server.network.sql.impl;

import static me.eddiep.ghost.server.utils.Constants.*;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.network.sql.PlayerData;
import me.eddiep.ghost.server.network.sql.PlayerUpdate;
import me.eddiep.ghost.server.network.sql.SQL;
import me.eddiep.ghost.server.utils.PasswordHash;
import me.eddiep.jconfig.JConfig;
import org.bson.Document;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MongoDB implements SQL {
    MongoDatabase ghostDB;
    MongoCollection<Document> playerCollection;

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
    public boolean createAccount(String username, String password) {
        if (usernameExists(username))
            throw new IllegalArgumentException("Username already exists!");

        try {
            String hash = PasswordHash.createHash(password);

            String displayName = username;
            while (displayNameExist(displayName)) {
                displayName = username + Main.RANDOM.nextInt();
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
}
