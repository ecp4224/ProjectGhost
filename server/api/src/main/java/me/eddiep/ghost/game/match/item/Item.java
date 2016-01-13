package me.eddiep.ghost.game.match.item;

import me.eddiep.ghost.game.match.Event;
import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.items.ItemEntity;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.Vector2f;

import java.lang.reflect.InvocationTargetException;

public abstract class Item {

    private static final float GAME_WIDTH = 1024;
    private static final float GAME_HEIGHT = 720;
    protected LiveMatch match;

    private ItemEntity entity;
    private long spawnTime = -1;

    private boolean active; //Whether the item's effect is active.
    protected PlayableEntity activator; //Player who activated the item.
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

        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        spawnTime = System.currentTimeMillis();
    }

    public boolean isActive() {
        return active;
    }

    private boolean startFade;
    public void tick() {
        if (idle)
            return; //Don't do anything if this item is idle in the inventory..

        if (active) {
            handleLogic();
        } else if (!startFade && System.currentTimeMillis() - spawnTime >= getDuration() - 1300) {
            entity.fadeOut(true, 1300);
            startFade = true;
        }
        else if (System.currentTimeMillis() - spawnTime >= getDuration()) {
            match.despawnItem(this);
            match.getWorld().despawnEntity(entity);
        }
    }

    public ItemEntity getEntity() {
        return entity;
    }

    private boolean idle = false;
    public void checkIntersection(PlayableEntity player) {
        if (player.isDead())
            return; //Dead players can't pickup items
        if (idle)
            return;

        if (entity.intersects(player)) {
            if (player.getInventory() != null) {
                if (player.getInventory().isFull())
                    return;

                player.getInventory().addItem(this);
                match.getWorld().despawnEntity(entity);
                idle = true;
                player.triggerEvent(Event.ItemPickUp, 0); //No direction
            } else {
                activate(player);

                match.getWorld().despawnEntity(entity);
            }
        }
    }

    public boolean isIdle() {
        return idle;
    }

    public final void activate(PlayableEntity player) {
        if (active)
            return;

        active = true;
        idle = false;

        activator = player;
        activationTime = System.currentTimeMillis();

        onActivated();
        for (PlayableEntity p : player.getMatch().getPlayers()) {
            p.onItemActivated(this, player);
        }
    }

    public final void deactivate() {
        if (!active)
            return;

        match.despawnItem(this);
        active = false;
        for (PlayableEntity p : activator.getMatch().getPlayers()) {
            p.onItemDeactivated(this, activator);
        }
        onDeactivate();
    }

    protected void onDeactivate() { } //Clean up?

    private Vector2f calculatePosition() {
        PlayableEntity[] players = match.getPlayers();

        if (players.length == 2) { //line; determine point in the middle
            Vector2f p1 = players[0].getPosition();
            Vector2f p2 = players[1].getPosition();

            if (p1.y != p2.y) { //avoid dividing by zero
                float slope = (p1.y - p2.y) / (p1.x - p2.x);
                slope = Global.map(Math.abs(slope), 0f, 180f, Global.random(-200f, 0f), Global.random(0f, 200f));
                
                float x = Global.clip((p1.x + p2.x) / 2 + slope, 0, 1024);

                float y = (p1.x * p1.x - 2 * p1.x * x + p1.y * p1.y - p2.x * p2.x +2 * p2.x * x - p2.y * p2.y) /
                        (2 * (p1.y - p2.y));

                return new Vector2f(x, y).clip(32, GAME_WIDTH - 32, 32, GAME_HEIGHT - 32);
            } else {
                float x = (p1.x + p2.x) / 2;
                float y = Global.random(0f, 1024.0f);

                return new Vector2f(x, y).clip(32, GAME_WIDTH - 32, 32, GAME_HEIGHT - 32);
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

            return center.clip(32, GAME_WIDTH - 32, 32, GAME_HEIGHT - 32);
        }
    }
}
