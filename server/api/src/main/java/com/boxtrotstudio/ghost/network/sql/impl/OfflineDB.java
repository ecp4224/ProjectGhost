package com.boxtrotstudio.ghost.network.sql.impl;

import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.stats.MatchHistory;
import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.network.sql.PlayerUpdate;
import com.boxtrotstudio.ghost.network.sql.SQL;
import com.boxtrotstudio.ghost.utils.Global;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;

/**
 * Represents an offline database instance.
 *
 * This database stores all data in json in the directory "datastore". Player data is stored in pdata files and match
 * history is stored in mdata files. This database cannot look up users via ID
 */
public class OfflineDB implements SQL {
    private static File jsonDir;

    @Override
    public void loadAndSetup() {
        jsonDir = new File("datastore");
        if (jsonDir.exists())
            return;

        boolean b = jsonDir.mkdir();

        if (!b)
            throw new RuntimeException("Failed to make data-store!");
    }

    @Override
    public void storePlayerData(PlayerData data) {
        String json = Global.GSON.toJson(data);
        String fileName = data.getUsername() + ".pdata";

        File file = new File(jsonDir, fileName);

        writeToFile(file, json);
    }

    @Override
    public void updatePlayerData(PlayerUpdate data) {
        String json = Global.GSON.toJson(data);
        String fileName = data.getUsername() + ".pdata";

        File file = new File(jsonDir, fileName);

        writeToFile(file, json);
    }

    @Override
    public void bulkUpdate(PlayerUpdate[] updates) {
        for (PlayerUpdate update : updates) {
            updatePlayerData(update);
        }
    }

    @Override
    public PlayerData fetchPlayerData(String username, String password) {
        File file = new File(jsonDir, username + ".pdata");

        if (file.exists()) {
            try {
                Scanner scanner = new Scanner(file);
                scanner.useDelimiter("\\Z");
                String content = scanner.next();
                scanner.close();
                return Global.GSON.fromJson(content, PlayerData.class);
            } catch (FileNotFoundException e) {
                return new PlayerData(username, username);
            }
        } else {
            return new PlayerData(username, username);
        }
    }

    @Override
    public PlayerData[] fetchPlayerStats(long... id) {
        return new PlayerData[0];
    }

    @Override
    public PlayerData fetchPlayerStat(long id) {
        return null;
    }

    @Override
    public List<PlayerData> fetchPlayerStats(long min, long max) {
        return new ArrayList<>();
    }

    @Override
    public long getPlayerCount() {
        return jsonDir.listFiles((dir, name) -> name.endsWith(".pdata")).length;
    }

    @Override
    public boolean createAccount(String username, String password) {
        return true;
    }

    @Override
    public boolean usernameExists(String username) {
        return false;
    }

    @Override
    public boolean displayNameExist(String displayName) {
        return false;
    }

    @Override
    public void saveMatch(MatchHistory history) {
        String json = Global.GSON.toJson(history);
        String fileName = history.getID() + ".mdata";

        File file = new File(jsonDir, fileName);

        writeToFile(file, json);
    }

    @Override
    public long getStoredMatchCount() {
        return jsonDir.listFiles((dir, name) -> name.endsWith(".mdata")).length;
    }

    @Override
    public Match fetchMatch(long id) {
        return null;
    }

    private void writeToFile(File file, String contents) {
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
}
