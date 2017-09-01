package com.boxtrotstudio.ghost.game.team;

import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import com.boxtrotstudio.ghost.game.match.world.timeline.EntitySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private PlayableEntity[] members;
    private int teamNumber;
    private int score;

    public Team(int teamNumber, PlayableEntity... players) {
        members = players;
        this.teamNumber = teamNumber;
    }

    //DEPRECATED
    /*public Team(int teamNumber, UUID... players) {
        PlayableEntity[] p = new PlayableEntity[players.length];
        for (int i = 0; i < p.length; i++) {
            BaseNetworkPlayer player;
            if ((player = PlayerFactory.findPlayerByUUID(players[i])) == null) {
                throw new IllegalArgumentException("Invalid UUID!");
            }

            p[i] = player;
        }

        this.members = p;
        this.teamNumber = teamNumber;
    }*/

    public Team(int teamNumber, List<BaseNetworkPlayer> players) {
        members = players.toArray(new BaseNetworkPlayer[players.size()]);
        this.teamNumber = teamNumber;
    }

    public boolean isTeamDead() {
        for (PlayableEntity p : members) {
            if (p.getLives() > 0)
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
     * Get a list of {@link BaseNetworkPlayer} objects on this team
     * @deprecated It is discouraging to think of a {@link Team} as a team of
     * {@link BaseNetworkPlayer} objects, as a team may not include any at all!
     * @return A list of {@link BaseNetworkPlayer} objects that are on this team
     */
    @Deprecated
    public List<BaseNetworkPlayer> getPlayers() {
        ArrayList<BaseNetworkPlayer> players = new ArrayList<>();
        for (PlayableEntity m : members) {
            if (m instanceof BaseNetworkPlayer)
                players.add((BaseNetworkPlayer)m);
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

    public boolean isAlly(EntitySnapshot snapshot) {
        for (PlayableEntity member : members) {
            if (snapshot.getID() == member.getID())
                return true;
        }
        return false;
    }

    public boolean isAlly(short id) {
        for (PlayableEntity member : members) {
            if (id == member.getID())
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

    public int totalLives() {
        int count = 0;
        for (PlayableEntity member : members) {
            count += member.getLives();
        }

        return count;
    }

    public void dispose() {
        members = null;
    }

    public String getTeamName() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < members.length; i++) {
            result.append(members[i].getName());
            if (i + 1 < members.length)
                result.append(", ");
        }

        return result.toString();
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore() {
        score++;
    }

    public void subtractScore() {
        score--;
    }

    public void setLives(int lives) {
        for (PlayableEntity p : members) {
            p.setLives((byte) lives);
        }
    }

    public void unready() {
        for (PlayableEntity p : members) {
            p.setReady(false);
            p.setVisible(false);
        }
    }

    public void ready() {
        for (PlayableEntity p : members) {
            p.setReady(true);
        }
    }

    public void resetLives() {
        for (PlayableEntity p : members) {
            p.resetLives();
        }
    }

    public void triggerEvent(Event event, double v) {
        for (PlayableEntity p : members) {
            p.triggerEvent(event, v);
        }
    }


}
