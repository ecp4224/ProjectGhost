package com.boxtrotstudio.ghost.game.team;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import org.bson.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OfflineTeam {
    private String[] usernames;
    private Long[] playerIds;
    private HashMap<Long, Byte> weapons = new HashMap<>();
    private HashMap<Long, Integer> lives = new HashMap<>();
    private int teamNumber;

    OfflineTeam(Team team) {
        this.teamNumber = team.getTeamNumber();
        usernames = new String[team.getTeamLength()];
        playerIds = new Long[team.getTeamLength()];

        PlayableEntity[] players = team.getTeamMembers();
        for (int i = 0; i < players.length; i++) {
            PlayableEntity p = players[i];

            if (p instanceof BaseNetworkPlayer) {
                BaseNetworkPlayer pp = (BaseNetworkPlayer)p;
                usernames[i] = pp.getUsername();
                playerIds[i] = pp.getPlayerID();
            } else {
                usernames[i] = p.getName();
                playerIds[i] = (long) p.getID();
            }
            weapons.put(playerIds[i], p.currentAbility().id());
            lives.put(playerIds[i], (int) p.getLives());
        }
    }

    private OfflineTeam() { }

    public String[] getUsernames() {
        return usernames;
    }

    public Long[] getPlayerIds() {
        return playerIds;
    }

    public int getTeamLength() {
        return usernames.length;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public byte getWeaponFor(long id) {
        return weapons.get(id);
    }

    public int getLivesFor(long id) {
        return lives.get(id);
    }

    public boolean isAlly(BaseNetworkPlayer p) {
        for (String username : usernames) {
            if (p.getUsername().equals(username))
                return true;
        }
        return false;
    }

    public boolean containsName(String name) {
        for (String n : usernames) {
            if (n.equals(name))
                return true;
        }
        return false;
    }

    public boolean containsID(long id) {
        for (long pID : playerIds) {
            if (id == pID)
                return true;
        }
        return false;
    }

    public Document asDocument() {
        return new Document()
                .append("teamNumber", teamNumber)
                .append("usernames", Arrays.asList(usernames))
                .append("playerIds", Arrays.asList(playerIds));
    }

    public static OfflineTeam fromDocument(Document document) {
        int teamNumber = document.getInteger("teamNumber");
        List<String> usernames = document.get("usernames", List.class);
        List<Long> ids = document.get("playerIds", List.class);

        OfflineTeam offlineTeam = new OfflineTeam();
        offlineTeam.teamNumber = teamNumber;
        offlineTeam.playerIds = ids.toArray(new Long[ids.size()]);
        offlineTeam.usernames = usernames.toArray(new String[usernames.size()]);

        return offlineTeam;
    }
}
