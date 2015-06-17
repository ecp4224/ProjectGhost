package me.eddiep.ghost.server.network.dataserv;

import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import me.eddiep.ghost.server.game.ranking.Glicko2;
import me.eddiep.ghost.server.game.ranking.Rank;

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
    protected transient Rank _rank;
    protected double rank;
    protected long lastRankUpdate;
    protected int hatTricks;
    Set<Long> friends = new HashSet<>();

    public PlayerData(Player p) {
        this.displayname = p.getDisplayName();
        this.username = p.getUsername();
        this.winHash = p.getWinHash();
        this.loseHash = p.getLoseHash();
        this.shotsHit = p.getShotsHit();
        this.shotsMissed = p.getShotsMissed();
        this.playersKilled = p.getPlayersKilled();
        this.hatTricks = p.getHatTrickCount();
        this._rank = p.getRanking();
        this.rank = _rank.getRating();
        this.lastRankUpdate = _rank.getLastUpdate();
        this.friends = p.getFriendIds();
    }

    public PlayerData(PlayerData data) {
        this.displayname = data.displayname;
        this.username = data.username;
        this.winHash = data.winHash;
        this.loseHash = data.loseHash;
        this.shotsHit = data.shotsHit;
        this.shotsMissed = data.shotsMissed;
        this.playersKilled = data.playersKilled;
        this.hatTricks = data.hatTricks;
        this._rank = data._rank;
        this.id = data.id;
        this.rank = _rank.getRating();
        this.friends = data.friends;
        this.lastRankUpdate = data.lastRankUpdate;
    }
    
    public PlayerData(String username, String displayname) {
        this(username, displayname, new HashMap<Byte, Integer>(), new HashMap<Byte, Integer>(), 0, 0, new HashSet<Long>(), 0, Glicko2.getInstance().defaultRank(), new HashSet<Long>());
    }
    
    public PlayerData(String username, String displayname, HashMap<Byte, Integer> winHash,
                      HashMap<Byte, Integer> loseHash, long shotsHit, long shotsMissed,
                      Set<Long> playersKilled, int hatTricks, Rank rank, Set<Long> friends) {
        this.username = username;
        this.displayname = displayname;
        this.winHash = winHash;
        this.loseHash = loseHash;
        this.shotsHit = shotsHit;
        this.shotsMissed = shotsMissed;
        this.playersKilled = playersKilled;
        this.hatTricks = hatTricks;
        this.friends = friends;
        this._rank = rank;
        this.rank = _rank.getRating();
        this.lastRankUpdate = _rank.getLastUpdate();
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

    public Rank getRank() {
        return _rank;
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

    public int getHatTrickCount() {
        return hatTricks;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLastRankUpdate() {
        return lastRankUpdate;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
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

    public Set<Long> getFriends() {
        return friends;
    }
}
