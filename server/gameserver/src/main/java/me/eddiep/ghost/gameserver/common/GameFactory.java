package me.eddiep.ghost.gameserver.common;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.gameserver.api.game.Game;

import java.util.HashMap;

public class GameFactory {
    private static final HashMap<Queues, Game> games = new HashMap<>();

    public static void addGame(Queues queue, Game game) {
        games.put(queue, game);
    }

    public static Game getGameFor(Queues queue) {
        return games.get(queue);
    }
}
