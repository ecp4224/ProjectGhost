package me.eddiep.ghost.server.game.team;

import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.entities.PlayableEntity;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import me.eddiep.ghost.server.game.entities.playable.impl.PlayerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {
    private PlayableEntity[] members;
    private int teamNumber;

    public Team(int teamNumber, PlayableEntity... players) {
        members = players;
        this.teamNumber = teamNumber;
    }

    public Team(int teamNumber, UUID... players) {
        PlayableEntity[] p = new PlayableEntity[players.length];
        for (int i = 0; i < p.length; i++) {
            Player player;
            if ((player = PlayerFactory.findPlayerByUUID(players[i])) == null) {
                throw new IllegalArgumentException("Invalid UUID!");
            }

            p[i] = player;
        }

        this.members = p;
        this.teamNumber = teamNumber;
    }

    public Team(int teamNumber, List<Player> players) {
        members = players.toArray(new Player[players.size()]);
        this.teamNumber = teamNumber;
    }

    public boolean isTeamDead() {
        for (PlayableEntity p : members) {
            if (!p.isDead())
                return false;
        }
        return true;
    }

    public boolean isTeamAlive() {
        return !isTeamDead();
    }

    public int getTeamLength() {
        return members.length;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public PlayableEntity[] getTeamMembers() {
        return members;
    }

    /**
     * Get a list of {@link me.eddiep.ghost.server.game.entities.playable.impl.Player} objects on this team
     * @deprecated It is discouraging to think of a {@link Team} as a team of
     * {@link me.eddiep.ghost.server.game.entities.playable.impl.Player} objects, as a team may not include any at all!
     * @return A list of {@link me.eddiep.ghost.server.game.entities.playable.impl.Player} objects that are on this team
     */
    @Deprecated
    public List<Player> getPlayers() {
        ArrayList<Player> players = new ArrayList<>();
        for (PlayableEntity m : members) {
            if (m instanceof Player)
                players.add((Player)m);
        }

        return players;
    }

    public boolean isAlly(PlayableEntity p) {
        for (PlayableEntity member : members) {
            if (p.getID() == member.getID())
                return true;
        }
        return false;
    }

    public boolean isTeamReady() {
        for (PlayableEntity p : members) {
            if (!p.isReady())
                return false;
        }
        return true;
    }

    private OfflineTeam offlineTeam;
    public OfflineTeam offlineTeam() {
        if (offlineTeam == null)
            offlineTeam = new OfflineTeam(this);
        return offlineTeam;
    }

    public void onWin(Match match) {
        for (PlayableEntity member : members) {
            member.onWin(match);
        }
    }

    public void onLose(Match match) {
        for (PlayableEntity member : members) {
            member.onLose(match);
        }
    }
}
