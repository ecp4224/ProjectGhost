package me.eddiep.ghost.game;

import me.eddiep.ghost.game.entities.PlayableEntity;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.game.util.Vector2f;

import java.io.IOException;

public interface LiveMatch extends Match {
    Team getTeam1();

    Team getTeam2();

    Team getWinningTeam();

    Team getLosingTeam();

    Team getTeamFor(PlayableEntity p);

    Vector2f getLowerBounds();

    Vector2f getUpperBounds();

    long getTimeElapsed();

    void despawnEntity(Entity e) throws IOException;

    void spawnEntity(Entity e) throws IOException;

    void updateEntityState();

    void playableUpdated(PlayableEntity p);

    boolean hasMatchStarted();

    boolean hasMatchEnded();
}
