package me.eddiep.ghost.game;

import me.eddiep.ghost.game.entities.PlayableEntity;
import me.eddiep.ghost.game.item.Item;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.utils.Vector2f;

import java.io.IOException;

public interface LiveMatch extends Match {
    Team getTeam1();

    Team getTeam2();

    Team getWinningTeam();

    Team getLosingTeam();

    Team getTeamFor(PlayableEntity p);

    PlayableEntity[] getPlayers();

    Vector2f getLowerBounds();

    Vector2f getUpperBounds();

    long getTimeElapsed();

    void despawnEntity(Entity e) throws IOException;

    void spawnEntity(Entity e) throws IOException;

    void spawnItem(Item item);

    void despawnItem(Item item);

    void updateEntityState();

    void playableUpdated(PlayableEntity p);

    boolean hasMatchStarted();

    boolean hasMatchEnded();

    MatchHistory matchHistory();
}
