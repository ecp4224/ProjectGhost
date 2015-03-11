package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.game.impl.Player;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.network.packet.impl.EntityStatePacket;
import me.eddiep.ghost.server.utils.events.EventEmitter;

import java.io.IOException;

public abstract class Entity extends EventEmitter {
    private static final long UPDATE_STATE_INTERVAL = 50;

    protected Vector2f position;
    protected Vector2f velocity;
    protected Entity parent;
    protected Match containingMatch;
    protected String name;
    protected boolean visible;
    protected boolean oldVisibleState;
    private short ID = -1;
    private long lastUpdate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Match getMatch() {
        return containingMatch;
    }

    public void setMatch(Match containingMatch) {
        this.containingMatch = containingMatch;
    }

    public boolean isInMatch() {
        return containingMatch != null;
    }

    public Entity getParent() {
        return parent;
    }

    public void setParent(Entity parent) {
        this.parent = parent;
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getXVelocity() {
        return velocity.x;
    }

    public float getYVelocity() {
        return velocity.y;
    }

    public void setVelocity(float xvel, float yvel) {
        setVelocity(new Vector2f(xvel, yvel));
    }

    public void resetUpdateTimer() {
        lastUpdate = 0;
    }

    public void tick() {
        if (getMatch().getTimeElapsed() - lastUpdate >= UPDATE_STATE_INTERVAL) {
            lastUpdate = getMatch().getTimeElapsed();
            try {
                updateState();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void setID(short ID) {
        this.ID = ID;
    }

    public short getID() {
        return ID;
    }

    public boolean isInside(float xmin, float ymin, float xmax, float ymax) {
        return position.x >= xmin && position.y >= ymin && position.x <= xmax && position.x <= ymax;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Update this entity for the specified player
     * @param player The player this update is for
     * @throws IOException If there was an error sending the packet
     */
    public void updateStateFor(Player player) throws IOException {
        if (player == null)
            return;
        EntityStatePacket packet = new EntityStatePacket(player.getClient());
        packet.writePacket(this);
    }

    public abstract void updateState() throws IOException;
}
