package me.eddiep.ghost.server.game.team;

import me.eddiep.ghost.server.game.entities.PlayableEntity;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class OfflineTeam {
    private String[] usernames;
    private Long[] playerIds;
    private int teamNumber;

    OfflineTeam(Team team) {
        this.teamNumber = team.getTeamNumber();
        usernames = new String[team.getTeamLength()];
        playerIds = new Long[team.getTeamLength()];

        PlayableEntity[] players = team.getTeamMembers();
        for (int i = 0; i < players.length; i++) {
            PlayableEntity p = players[i];

            if (p instanceof Player) {
                Player pp = (Player)p;
                usernames[i] = pp.getUsername();
                playerIds[i] = pp.getPlayerID();
            } else {
                usernames[i] = p.getName();
                playerIds[i] = 0L;
            }
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

    public boolean isAlly(Player p) {
        for (String username : usernames) {
            if (p.getUsername().equals(username))
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
