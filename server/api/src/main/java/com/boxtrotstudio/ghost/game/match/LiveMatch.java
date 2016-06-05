package com.boxtrotstudio.ghost.game.match;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.item.Item;
import com.boxtrotstudio.ghost.game.match.stats.MatchHistory;
import com.boxtrotstudio.ghost.game.match.world.World;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.network.Server;
import com.boxtrotstudio.ghost.utils.Vector2f;

public interface LiveMatch extends Match {
    Team getTeam1();

    Team getTeam2();

    Team getWinningTeam();

    Team getLosingTeam();

    Team getTeamFor(PlayableEntity p);

    PlayableEntity[] getPlayers();

    Vector2f getLowerBounds();

    Vector2f getUpperBounds();

    String getLastActiveReason();

    long getTimeElapsed();

    World getWorld();

    void playableUpdated(PlayableEntity updated);

    void spawnItem(Item item);

    void despawnItem(Item item);

    boolean hasMatchStarted();

    boolean hasMatchEnded();

    MatchHistory matchHistory();

    void setup();

    void tick();

    Server getServer();

    boolean isMatchActive();

    void dispose();

    void onReady(PlayableEntity playableEntity);

    int getPlayerCount();

    void disableItems();

    void enableItems();
}
