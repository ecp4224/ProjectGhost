package me.eddiep.ghost.server.network.sql;

import me.eddiep.ghost.server.game.queue.QueueType;

import java.util.HashMap;

public class PlayerData {
    private String displayname;
    private String username;
    private HashMap<Byte, Integer> winHash = new HashMap<>();
    private HashMap<Byte, Integer> loseHash = new HashMap<>();
    
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
}
