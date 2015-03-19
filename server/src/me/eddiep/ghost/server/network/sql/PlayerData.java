package me.eddiep.ghost.server.network.sql;

import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.game.queue.QueueType;
import org.bson.Document;

import java.util.*;

public class PlayerData {
    protected String displayname;
    protected String username;
    protected HashMap<Byte, Integer> winHash = new HashMap<>();
    protected HashMap<Byte, Integer> loseHash = new HashMap<>();
    protected Set<Long> playersKilled = new HashSet<>();
    protected long shotsHit, shotsMissed;

    protected long id;
    protected transient String hash;

    public PlayerData(Player p) {
        this.displayname = p.getDisplayName();
        this.username = p.getUsername();
        this.winHash = p.getWinHash();
        this.loseHash = p.getLoseHash();
        this.shotsHit = p.getShotsHit();
        this.shotsMissed = p.getShotsMissed();
        this.playersKilled = p.getPlayersKilled();
    }
    
    public PlayerData(String username, String displayname) {
        this(username, displayname, new HashMap<Byte, Integer>(), new HashMap<Byte, Integer>(), 0, 0, new HashSet<Long>());
    }
    
    public PlayerData(String username, String displayname, HashMap<Byte, Integer> winHash, HashMap<Byte, Integer> loseHash, long shotsHit, long shotsMissed, Set<Long> playersKilled) {
        this.username = username;
        this.displayname = displayname;
        this.winHash = winHash;
        this.loseHash = loseHash;
        this.shotsHit = shotsHit;
        this.shotsMissed = shotsMissed;
        this.playersKilled = playersKilled;
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

    public Set<Long> getPlayersKilled() {
        return playersKilled;
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
                .append("hash", hash)
                .append("shotsHit", shotsHit)
                .append("shotsMissed", shotsMissed)
                .append("playersKilled", new ArrayList<>(playersKilled));

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
        long shotsHit = document.getLong("shotsHit") == null ? 0 : document.getLong("shotsHit");
        long shotsMissed = document.getLong("shotsMissed")  == null ? 0 : document.getLong("shotsMissed");
        List playersKilledList = document.get("playersKilled", List.class);
        HashSet<Long> playersKilled = new HashSet<Long>(playersKilledList);

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

        PlayerData data = new PlayerData(username, displayName, wins, loses, shotsHit, shotsMissed, playersKilled);
        data.setId(id);

        return data;
    }

    public HashMap<Byte, Integer> getWins() {
        return winHash;
    }

    public HashMap<Byte, Integer> getLoses() {
        return loseHash;
    }

    public long getShotsHit() {
        return shotsHit;
    }

    public long getShotsMissed() {
        return shotsMissed;
    }
}
