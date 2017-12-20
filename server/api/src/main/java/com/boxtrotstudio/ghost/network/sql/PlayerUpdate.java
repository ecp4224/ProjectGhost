package com.boxtrotstudio.ghost.network.sql;

import com.boxtrotstudio.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import com.boxtrotstudio.ghost.utils.Constants;
import com.boxtrotstudio.ghost.utils.Global;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Set;

public class PlayerUpdate extends PlayerData {

    public PlayerUpdate(BaseNetworkPlayer p) {
        super(p);
        setId(p.getPlayerID());
    }

    public PlayerUpdate(PlayerData data) {
        super(data);
    }

    public void updateDisplayName(String name) {
        super.displayName = name;
        update(Constants.DISPLAY_NAME, name);
    }

    public void updateShotsMade(long newValue) {
        super.shotsHit = newValue;
        update(Constants.SHOTS_HIT, newValue);
    }

    public void updateShotsMissed(long newValue) {
        super.shotsMissed = newValue;
        update(Constants.SHOTS_MISSED, newValue);
    }

    public void updatePlayersKilled(Set<Long> playersKilled) {
        super.playersKilled = playersKilled;
        update(Constants.PLAYERS_KILLED, new ArrayList<>(playersKilled));
    }

    public void updateFriendList(Set<Long> friends) {
        super.friends = friends;
        update(Constants.FRIENDS, friends);
    }


    private void update(String key, Object value) {
        construct.append(key, value);
    }

    private transient Document construct = new Document();
    @Override
    public Document asDocument() {
        return new Document("$set", construct);
    }

    public PlayerData push() {
        Global.SQL.updatePlayerData(this);

        return this;
    }

    public PlayerData complete() {
        return this;
    }
}
