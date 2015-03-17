package me.eddiep.ghost.server.network.sql;

import me.eddiep.ghost.server.game.queue.QueueType;
import org.bson.Document;

import javax.print.Doc;
import java.util.HashMap;

public class PlayerData {
    protected String displayname;
    protected String username;
    protected HashMap<Byte, Integer> winHash = new HashMap<>();
    protected HashMap<Byte, Integer> loseHash = new HashMap<>();

    protected long id;
    protected String hash;
    
    public PlayerData(String username, String displayname) {
        this(username, displayname, new HashMap<Byte, Integer>(), new HashMap<Byte, Integer>());
    }
    
    public PlayerData(String username, String displayname, HashMap<Byte, Integer> winHash, HashMap<Byte, Integer> loseHash) {
        this.username = username;
        this.displayname = displayname;
        this.winHash = winHash;
        this.loseHash = loseHash;
    }

    public int getLosesFor(QueueType type) {
        return loseHash.get(type.asByte());
    }

    public int getWinsFor(QueueType type) {
        return winHash.get(type.asByte());
    }
    
    public int getTotalWins() {
        int i = 0;
        for (Byte t : winHash.keySet()) {
            i += winHash.get(t);
        }
        return i;
    }
    
    public int getTotalLoses() {
        int i = 0;
        for (Byte t : loseHash.keySet()) {
            i += loseHash.get(t);
        }
        return i;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayname() {
        return displayname;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Document asDocument() {
        Document temp = new Document("username", username)
                .append("displayName", displayname)
                .append("id", id)
                .append("hash", hash);

        Document wins = new Document();
        for (Byte t : winHash.keySet()) {
            wins.append(t.toString(), winHash.get(t));
        }

        temp.append("wins", wins);

        Document loses = new Document();
        for (Byte t : loseHash.keySet()) {
            wins.append(t.toString(), loseHash.get(t));
        }

        temp.append("loses", loses);

        return temp;
    }

    public static PlayerData fromDocument(Document document) {
        String username = document.getString("username");
        String displayName = document.getString("displayName");
        long id = document.getLong("id");

        HashMap<Byte, Integer> wins = new HashMap<>();
        HashMap<Byte, Integer> loses = new HashMap<>();

        Document winDoc = document.get("wins", Document.class);
        Document loseDoc = document.get("loses", Document.class);
        for (QueueType type : QueueType.values()) {
            if (winDoc.get("" + type.asByte()) != null) {
                wins.put(type.asByte(), winDoc.getInteger("" + type.asByte()));
            }
            if (loseDoc.get("" + type.asByte()) != null) {
                loses.put(type.asByte(), loseDoc.getInteger("" + type.asByte()));
            }
        }

        PlayerData data = new PlayerData(username, displayName, wins, loses);
        data.setId(id);

        return data;
    }
}
