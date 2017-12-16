package com.boxtrotstudio.ghost.network.sql;

import com.boxtrotstudio.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import com.boxtrotstudio.ghost.game.queue.Queues;
import org.bson.Document;

import java.util.*;

import static com.boxtrotstudio.ghost.utils.Constants.*;

public class PlayerData {
    protected String displayname;
    protected String username;
    protected Set<Long> playersKilled = new HashSet<>();
    protected long shotsHit, shotsMissed;

    protected long id;
    protected byte stream;
    protected transient String hash;
    protected double rank;
    protected long lastRankUpdate;
    Set<Long> friends = new HashSet<>();

    private PlayerData() {
        this("", "", new HashMap<>(), new HashMap<>(), 0, 0, new HashSet<>(), 0, new HashSet<>(), (byte)4);
    }

    public PlayerData(BaseNetworkPlayer p) {
        this.displayname = p.getDisplayName();
        this.username = p.getUsername();
        this.shotsHit = p.getShotsHit();
        this.shotsMissed = p.getShotsMissed();
        this.playersKilled = p.getPlayersKilled();
        this.friends = p.getFriendIds();
    }

    public PlayerData(PlayerData data) {
        this.displayname = data.displayname;
        this.username = data.username;
        this.shotsHit = data.shotsHit;
        this.shotsMissed = data.shotsMissed;
        this.playersKilled = data.playersKilled;
        this.id = data.id;
        this.friends = data.friends;
        this.lastRankUpdate = data.lastRankUpdate;
        this.stream = data.stream;
    }
    
    public PlayerData(String username, String displayname) {
        this(username, displayname, new HashMap<>(), new HashMap<>(), 0, 0, new HashSet<>(), 0, new HashSet<>(), (byte)0);
    }
    
    public PlayerData(String username, String displayname, HashMap<Byte, Integer> winHash,
                      HashMap<Byte, Integer> loseHash, long shotsHit, long shotsMissed,
                      Set<Long> playersKilled, int hatTricks, Set<Long> friends, byte stream) {
        this.username = username;
        this.displayname = displayname;
        this.shotsHit = shotsHit;
        this.shotsMissed = shotsMissed;
        this.playersKilled = playersKilled;
        this.friends = friends;
        this.stream = stream;
    }

    public byte getStreamPermission() {
        return stream;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayname() {
        if (displayname == null) {
            return username;
        }
        return displayname;
    }

    public void normalizeStream() {
        stream = (byte) (4 - stream);
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

    public long getLastRankUpdate() {
        return lastRankUpdate;
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
                .append(FRIENDS, new ArrayList<>(friends));

        /*Document wins = new Document();
        for (Byte t : winHash.keySet()) {
            wins.append(t.toString(), winHash.get(t));
        }

        temp.append(WINS, wins);

        Document loses = new Document();
        for (Byte t : loseHash.keySet()) {
            wins.append(t.toString(), loseHash.get(t));
        }

        temp.append(LOSES, loses);*/

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

        List friendList = document.get(FRIENDS, List.class);
        if (friendList == null)
            friendList = new ArrayList();

        HashMap<Byte, Integer> wins = new HashMap<>();
        HashMap<Byte, Integer> loses = new HashMap<>();

        Document winDoc = document.get(WINS, Document.class);
        Document loseDoc = document.get(LOSES, Document.class);
        for (Queues type : Queues.values()) {
            if (winDoc.get(Byte.toString(type.asByte())) != null) {
                wins.put(type.asByte(), winDoc.getInteger(Byte.toString(type.asByte())));
            }
            if (loseDoc.get(Byte.toString(type.asByte())) != null) {
                loses.put(type.asByte(), loseDoc.getInteger(Byte.toString(type.asByte())));
            }
        }

        PlayerData data = new PlayerData(username, displayName, wins, loses, shotsHit, shotsMissed, playersKilled, hatTricks, new HashSet<Long>(friendList), (byte)4);
        data.setId(id);

        return data;
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

    public void setDisplayName(String displayName) {
        this.displayname = displayName;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
