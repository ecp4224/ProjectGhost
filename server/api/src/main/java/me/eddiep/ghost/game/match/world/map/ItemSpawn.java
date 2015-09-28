package me.eddiep.ghost.game.match.world.map;

import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.LiveMatchImpl;
import me.eddiep.ghost.game.match.item.Item;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.Vector2f;

import java.lang.reflect.InvocationTargetException;

import static me.eddiep.ghost.utils.Constants.AVERAGE_MATCH_TIME;

public class ItemSpawn {
    private float x, y;
    private int itemsToSpawn[];
    private long nextItemTime;
    private int maxItems = -1;
    private int itemsSpawned;

    public ItemSpawn(float x, float y) {
        this.x = x;
        this.y = y;
        this.itemsToSpawn = new int[0];
    }

    public ItemSpawn(float x, float y, int... items) {
        this.x = x;
        this.y = y;
        this.itemsToSpawn = items;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Item getRandomItem(LiveMatch match) {
        int random;
        if (itemsToSpawn.length > 0) {
            random = Global.RANDOM.nextInt(itemsToSpawn.length);

        } else {
            random = Global.RANDOM.nextInt(LiveMatchImpl.ITEMS.length);
        }

        Class class_ = LiveMatchImpl.ITEMS[random];
        try {
            Item item = (Item) class_.getConstructor(LiveMatch.class).newInstance(match);
            item.getEntity().setPosition(new Vector2f(x, y));
            return item;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void tick(LiveMatch match) {
        if (maxItems == -1) {
            maxItems = Global.random(match.getPlayerCount(), 4 * match.getPlayerCount());
            calculateNextItemTime();
        }

        if (nextItemTime != 0 && System.currentTimeMillis() - nextItemTime >= 0) {
            match.spawnItem(getRandomItem(match));

            if (++itemsSpawned < maxItems) {
                calculateNextItemTime();
            } else {
                nextItemTime = 0;
            }
        }
    }

    private void calculateNextItemTime() {
        int div = maxItems + Global.random(-3, 3);
        if (div == 0) {
            div = 1;
        }

        nextItemTime = AVERAGE_MATCH_TIME / div;

        if (nextItemTime < 0) {
            nextItemTime = 5_000;
        }

        nextItemTime += System.currentTimeMillis();
    }

}
