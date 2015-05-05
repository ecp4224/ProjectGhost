package me.eddiep.ghost.server.network.sql;

import static me.eddiep.ghost.server.utils.Constants.*;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.game.queue.Queues;
import me.eddiep.ghost.server.game.ranking.Rank;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlayerUpdate extends PlayerData {

    public PlayerUpdate(Player p) {
        super(p);
        setId(p.getPlayerID());
    }

    public PlayerUpdate(PlayerData data) {
        super(data);
    }

    public void updateDisplayName(String name) {
        super.displayname = name;
        update(DISPLAY_NAME, name);
    }

    public void updateShotsMade(long newValue) {
        super.shotsHit = newValue;
        update(SHOTS_HIT, newValue);
    }

    public void updateShotsMissed(long newValue) {
        super.shotsMissed = newValue;
        update(SHOTS_MISSED, newValue);
    }

    public void updateRank(Rank rank) {
        super._rank = rank;
        super.rank = super._rank.getRating();
        super.lastRankUpdate = super._rank.getLastUpdate();
        update(RANK, rank.asDocument());
    }

    public void updatePlayersKilled(Set<Long> playersKilled) {
        super.playersKilled = playersKilled;
        update(PLAYERS_KILLED, new ArrayList<>(playersKilled));
    }

    public void updateHatTricks(int hatTricks) {
        super.hatTricks = hatTricks;
        update(HAT_TRICK, hatTricks);
    }

    public void updateFriendList(Set<Long> friends) {
        super.friends = friends;
        update(FRIENDS, friends);
    }

    public void updateWinsFor(Queues type, int wins) {
        super.winHash.put(type.asByte(), wins);

        Document w = new Document();
        for (Byte t : super.winHash.keySet()) {
            w.append(t.toString(), super.winHash.get(t));
        }
        update(WINS, w);
    }

    public void updateLosesFor(Queues type, int loses) {
        super.loseHash.put(type.asByte(), loses);

        Document w = new Document();
        for (Byte t : super.loseHash.keySet()) {
            w.append(t.toString(), super.loseHash.get(t));
        }
        update(LOSES, w);
    }


    private void update(String key, Object value) {
        construct.append(key, value);
    }

    private Document construct = new Document();
    @Override
    public Document asDocument() {
        return new Document("$set", construct);
    }

    public void push() {
        Main.SQL.updatePlayerData(this);
    }
}
