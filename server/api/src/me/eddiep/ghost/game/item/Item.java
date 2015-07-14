package me.eddiep.ghost.game.item;

import me.eddiep.ghost.game.Entity;
import me.eddiep.ghost.game.LiveMatch;
import me.eddiep.ghost.game.entities.PlayableEntity;
import me.eddiep.ghost.game.entities.items.ItemEntity;
import me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.Vector2f;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public abstract class Item {

    protected LiveMatch match;

    private ItemEntity entity;
    private long spawnTime = -1;

    private boolean active; //Whether the item's effect is active.
    protected BaseNetworkPlayer activator; //Player who activated the item.
    protected long activationTime; //Time since activation.

    /**
     * The duration (in milliseconds) this item should stay on the screen before despawning if
     * it is not activated in the meantime.
     */
    public abstract long getDuration();

    /**
     * Class of the {@link Entity} that this item should spawn when it's created.
     */
    protected abstract Class<? extends ItemEntity> getEntityClass();

    /**
     * Called when the item is first activated.
     */
    protected abstract void onActivated();

    /**
     * Handles the logic for this item. When the item is activated, this method will be called
     * every tick.
     */
    protected abstract void handleLogic();

    public Item(LiveMatch match) {

        this.match = match;

        try {
            entity = getEntityClass().getDeclaredConstructor(LiveMatch.class).newInstance(match);
            entity.setPosition(calculatePosition());
            System.out.println("Set entity position to " + entity.getPosition());

            match.spawnEntity(entity);
        } catch (InstantiationException | IllegalAccessException | IOException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        spawnTime = System.currentTimeMillis();
    }

    public boolean isActive() {
        return active;
    }

    public void tick() {
        if (active) {
            handleLogic();
        } else if (System.currentTimeMillis() - spawnTime >= getDuration()) {
            match.despawnItem(this);
            try {
                match.despawnEntity(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkIntersection(BaseNetworkPlayer player) {
        if (entity.intersects(player)) {
            try {
                match.despawnEntity(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }

            active = true;
            activator = player;
            activationTime = System.currentTimeMillis();

            onActivated();
        }
    }

    private Vector2f calculatePosition() {
        PlayableEntity[] players = match.getPlayers();

        if (players.length == 2) { //line; determine point in the middle
            Vector2f p1 = players[0].getPosition();
            Vector2f p2 = players[1].getPosition();

            if (p1.y != p2.y) { //avoid dividing by zero
                float min = p1.x < p2.x ? p1.x : p2.x;
                float max = p1.x > p2.x ? p1.x : p2.x;
                float x = Global.random(min, max);

                float y = (p1.x * p1.x - 2 * p1.x * x + p1.y * p1.y - p2.x * p2.x +2 * p2.x * x - p2.y * p2.y) /
                        (2 * (p1.y - p2.y));

                return new Vector2f(x, y).clip(0, 1024, 0, 720); //change these to constants or something?
            } else {
                float x = (p1.x + p2.x) / 2;
                float y = p1.y + Global.random(-10.0f, 10.0f);

                return new Vector2f(x, y).clip(0, 1024, 0, 720);
            }

        } else { //quadriliteral; determine center of mass as a 'close enough' approximation
            Vector2f center = new Vector2f(0, 0);
            float area = 0f;
            float x1, y1, x2, y2, a;

            for (int i = 0; i < players.length - 1; i++) {
                Vector2f point1 = players[i].getPosition();
                Vector2f point2 = players[i + 1].getPosition();
                x1 = point1.x;
                y1 = point1.y;
                x2 = point2.x;
                y2 = point2.y;

                a = (x1 * y2) - (x2 * y1);
                area += a;
                center.x += (x1 + x2) * a;
                center.y += (y1 + y2) * a;
            }
            Vector2f point1 = players[players.length - 1].getPosition();
            Vector2f point2 = players[0].getPosition();

            x1 = point1.x;
            y1 = point1.y;
            x2 = point2.x;
            y2 = point2.y;

            a = (x1 * y2) - (x2 * y1);
            area += a;
            center.x += (x1 + x2) * a;
            center.y += (y1 + y2) * a;

            area /= 2f;
            center.x /= (6f * area);
            center.y /= (6f * area);

            center.x += Global.random(-3, 3);
            center.y += Global.random(-3, 3);

            return center.clip(0, 1024, 0, 720);
        }
    }
}
