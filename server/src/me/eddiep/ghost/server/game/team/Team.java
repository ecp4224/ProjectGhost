package me.eddiep.ghost.server.game.team;

import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import me.eddiep.ghost.server.game.entities.playable.impl.PlayerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {
    private Playable[] members;
    private int teamNumber;

    public Team(int teamNumber, Playable... players) {
        members = players;
        this.teamNumber = teamNumber;
    }

    public Team(int teamNumber, UUID... players) {
        Playable[] p = new Playable[players.length];
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
        for (Playable p : members) {
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

    public Playable[] getTeamMembers() {
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
        for (Playable m : members) {
            if (m instanceof Player)
                players.add((Player)m);
        }

        return players;
    }

    public boolean isAlly(Playable p) {
        for (Playable member : members) {
            if (p.getEntity().getID() == member.getEntity().getID())
                return true;
        }
        return false;
    }

    public boolean isTeamReady() {
        for (Playable p : members) {
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
        for (Playable member : members) {
            member.onWin(match);
        }
    }

    public void onLose(Match match) {
        for (Playable member : members) {
            member.onLose(match);
        }
    }
}
