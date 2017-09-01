package com.boxtrotstudio.ghost.game.match.world.map;

import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.LiveMatchImpl;
import com.boxtrotstudio.ghost.game.match.item.Item;
import com.boxtrotstudio.ghost.utils.Constants;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.Vector2f;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
            random = itemsToSpawn[random];

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

    public void spawnOnly(List<Integer> items) {
        this.itemsToSpawn = new int[items.size()];

        for (int i = 0; i < items.size(); i++) {
            this.itemsToSpawn[i] = items.get(i);
        }
    }

    private void calculateNextItemTime() {
        int div = maxItems + Global.random(-3, 3);
        if (div == 0) {
            div = 1;
        }

        nextItemTime = Constants.AVERAGE_MATCH_TIME / div;

        if (nextItemTime < 0) {
            nextItemTime = 5_000;
        }

        nextItemTime += System.currentTimeMillis();
    }

}
