package me.eddiep.ghost.server.network.sql;

import static me.eddiep.ghost.server.utils.Constants.*;

import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.game.queue.Queues;
import me.eddiep.ghost.server.game.rating.Glicko2;
import me.eddiep.ghost.server.game.rating.Rank;
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
    protected transient Rank rank;
    protected int hatTricks;

    public PlayerData(Player p) {
        this.displayname = p.getDisplayName();
        this.username = p.getUsername();
        this.winHash = p.getWinHash();
        this.loseHash = p.getLoseHash();
        this.shotsHit = p.getShotsHit();
        this.shotsMissed = p.getShotsMissed();
        this.playersKilled = p.getPlayersKilled();
        this.hatTricks = p.getHatTrickCount();
        this.rank = p.getRanking();
    }
    
    public PlayerData(String username, String displayname) {
        this(username, displayname, new HashMap<Byte, Integer>(), new HashMap<Byte, Integer>(), 0, 0, new HashSet<Long>(), 0, Glicko2.getInstance().defaultRank());
    }
    
    public PlayerData(String username, String displayname, HashMap<Byte, Integer> winHash,
                      HashMap<Byte, Integer> loseHash, long shotsHit, long shotsMissed,
                      Set<Long> playersKilled, int hatTricks, Rank rank) {
        this.username = username;
        this.displayname = displayname;
        this.winHash = winHash;
        this.loseHash = loseHash;
        this.shotsHit = shotsHit;
        this.shotsMissed = shotsMissed;
        this.playersKilled = playersKilled;
        this.rank = rank;
    }

    public int getLosesFor(Queues type) {
        return loseHash.get(type.asByte());
    }

    public int getWinsFor(Queues type) {
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

    public Rank getRank() {
        return rank;
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Document asDocument() {
        Document temp = new Document(USERNAME, username)
                .append(DISPLAY_NAME, displayname)
                .append(ID, id)
                .append(HASH, hash)
                .append(SHOTS_HIT, shotsHit)
                .append(SHOTS_MISSED, shotsMissed)
                .append(PLAYERS_KILLED, new ArrayList<>(playersKilled))
                .append(HAT_TRICK, hatTricks)
                .append(RANK, rank.asDocument());

        Document wins = new Document();
        for (Byte t : winHash.keySet()) {
            wins.append(t.toString(), winHash.get(t));
        }

        temp.append(WINS, wins);

        Document loses = new Document();
        for (Byte t : loseHash.keySet()) {
            wins.append(t.toString(), loseHash.get(t));
        }

        temp.append(LOSES, loses);

        return temp;
    }

    public static PlayerData fromDocument(Document document) {
        String username = document.getString(USERNAME);

        String displayName = document.getString(DISPLAY_NAME);

        long id = document.getLong(ID);

        long shotsHit = document.getLong(SHOTS_HIT) == null ? 0 : document.getLong(SHOTS_HIT);

        long shotsMissed = document.getLong(SHOTS_MISSED)  == null ? 0 : document.getLong(SHOTS_MISSED);

        List playersKilledList = document.get(PLAYERS_KILLED, List.class);
        HashSet<Long> playersKilled = new HashSet<Long>(playersKilledList);

        int hatTricks = document.getInteger(HAT_TRICK) == null ? 0 : document.getInteger(HAT_TRICK);

        Document rankDoc = document.get("rank", Document.class);
        Rank rank;
        if (rankDoc == null)
            rank = Glicko2.getInstance().defaultRank();
        else
            rank = Rank.fromDocument(rankDoc);

        HashMap<Byte, Integer> wins = new HashMap<>();
        HashMap<Byte, Integer> loses = new HashMap<>();

        Document winDoc = document.get(WINS, Document.class);
        Document loseDoc = document.get(LOSES, Document.class);
        for (Queues type : Queues.values()) {
            if (winDoc.get("" + type.asByte()) != null) {
                wins.put(type.asByte(), winDoc.getInteger("" + type.asByte()));
            }
            if (loseDoc.get("" + type.asByte()) != null) {
                loses.put(type.asByte(), loseDoc.getInteger("" + type.asByte()));
            }
        }

        PlayerData data = new PlayerData(username, displayName, wins, loses, shotsHit, shotsMissed, playersKilled, hatTricks, rank);
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
