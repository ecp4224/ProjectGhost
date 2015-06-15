package me.eddiep.ghost.server.game.entities.playable;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.entities.TypeableEntity;
import me.eddiep.ghost.server.game.entities.abilities.Ability;
import me.eddiep.ghost.server.game.entities.abilities.Gun;
import me.eddiep.ghost.server.game.team.Team;
import me.eddiep.ghost.server.game.util.VisibleFunction;
import me.eddiep.ghost.server.network.packet.impl.DespawnEntityPacket;
import me.eddiep.ghost.server.network.packet.impl.EntityStatePacket;
import me.eddiep.ghost.server.network.packet.impl.PlayerStatePacket;
import me.eddiep.ghost.server.network.packet.impl.SpawnEntityPacket;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;

import static me.eddiep.ghost.server.utils.Constants.*;

public abstract class BasePlayableEntity extends Entity implements Playable {
    private static final byte MAX_LIVES = 3;
    private static final float VISIBLE_TIMER = 800f;

    protected byte lives;
    protected boolean isDead;
    protected boolean frozen;
    protected boolean isReady;
    protected float speed = 6f;
    protected long lastFire;
    protected boolean wasHit;
    protected long lastHit;
    protected boolean didFire = false;

    protected boolean canFire = true;
    protected VisibleFunction function = VisibleFunction.ORGINAL; //Always default to original style
    private Ability<Playable> ability = new Gun(this);

    @Override
    public Team getTeam() {
        return containingMatch == null ? null : containingMatch.getTeamFor(this);
    }

    @Override
    public void spawnEntity(Entity entity) throws IOException {
        if (!isConnected())
            return;

        if (entity.getID() != getID()) {
            SpawnEntityPacket packet = new SpawnEntityPacket(getClient());
            byte type;
            if (entity instanceof Playable) {
                Playable p = (Playable)entity;
                if (getTeam().isAlly(p)) {
                    type = 0;
                } else {
                    type = 1;
                }
            } else if (entity instanceof TypeableEntity) {
                type = ((TypeableEntity)entity).getType();
            } else {
                return;
            }

            packet.writePacket(entity, type);
        }
    }

    @Override
    public void prepareForMatch() {
        oldVisibleState = true;
        setVisible(false);
        resetUpdateTimer();
    }

    @Override
    public void onFire() {
        lastFire = System.currentTimeMillis();
        didFire = true;
        switch (function) {
            case ORGINAL:
                if (!isVisible())
                    setVisible(true);
                break;
            case TIMER:
                if (visibleIndicator < VISIBLE_COUNTER_DEFAULT_LENGTH) {
                    visibleIndicator = VISIBLE_COUNTER_DEFAULT_LENGTH;
                }
                break;
        }
    }

    protected void handleVisible() {
        switch (function) {
            case ORGINAL:
                if (didFire) {
                    if (isVisible() && System.currentTimeMillis() - lastFire >= VISIBLE_TIMER) {
                        fadeOut(FADE_SPEED);
                        didFire = false;
                    }
                } else if (wasHit) {
                    if (isVisible() && System.currentTimeMillis() - lastHit >= VISIBLE_TIMER) {
                        fadeOut(FADE_SPEED);
                        wasHit = false;
                    }
                }
                break;

            case TIMER:
                if (getMatch().hasMatchStarted()) {
                    handleVisibleState();
                }
                break;

        }
    }


    public int getVisibleIndicatorPosition() {
        return visibleIndicator;
    }


    int visibleIndicator;
    private void handleVisibleState() {
        if (didFire || wasHit) {
            visibleIndicator -= VISIBLE_COUNTER_DECREASE_RATE;
            if (visibleIndicator <= 0) {
                visibleIndicator = 0;
                alpha = 0;
                didFire = false;
                wasHit = false;
            }
        } else {
            visibleIndicator += VISIBLE_COUNTER_INCREASE_RATE;
        }

        if (visibleIndicator < VISIBLE_COUNTER_START_FADE) {
            alpha = 0;
        } else if (visibleIndicator > VISIBLE_COUNTER_START_FADE && visibleIndicator < VISIBLE_COUNTER_FULLY_VISIBLE) {
            int totalDistance = VISIBLE_COUNTER_FADE_DISTANCE;
            int curDistance = visibleIndicator - VISIBLE_COUNTER_START_FADE;

            alpha = Math.max(Math.min((int) (((double)curDistance / (double)totalDistance) * 255.0), 255), 0);

        } else if (visibleIndicator > VISIBLE_COUNTER_FULLY_VISIBLE) {
            alpha = 255;
        }
    }

    @Override
    public void onDamage(Playable damager) {
        wasHit = true;

        lastHit = System.currentTimeMillis();
        switch (function) {
            case ORGINAL:
                if (!isVisible())
                    setVisible(true);
                break;
            case TIMER:
                if (visibleIndicator < VISIBLE_COUNTER_DEFAULT_LENGTH) {
                    visibleIndicator = VISIBLE_COUNTER_DEFAULT_LENGTH;
                }
                break;
        }
    }

    @Override
    public void tick() {
        position.x += velocity.x;
        position.y += velocity.y;

        handleVisible();

        super.tick();
    }

    @Override
    public void despawnEntity(Entity e) throws IOException {
        if (!isConnected())
            return;

        DespawnEntityPacket packet = new DespawnEntityPacket(getClient());
        packet.writePacket(e);
    }

    @Override
    public Entity getEntity() {
        return this;
    }

    @Override
    public void subtractLife() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        lives--;
        if (lives <= 0) {
            isDead = true;
            frozen = true;
            setVisible(true);
        }
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addLife() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        lives++;
        if (isDead) {
            isDead = false;
            frozen = false;
            setVisible(false);
        }
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resetLives() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        lives = MAX_LIVES;
        if (isDead) {
            isDead = false;
            frozen = false;
            setVisible(false);
        }
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setVisibleFunction(VisibleFunction function) {
        this.function = function;
    }

    @Override
    public VisibleFunction getVisibleFunction() {
        return function;
    }

    @Override
    public void setLives(byte value) {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");
        if (value <= 0)
            throw new InvalidParameterException("Invalid argument!\nTo set the playable's lives to 0, please use the kill() function!");

        lives = value;
        if (isDead) {
            isDead = false;
            frozen = false;
            setVisible(false);
        }
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void kill() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        lives = 0;
        isDead = true;
        frozen = true;
        setVisible(true);
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void freeze() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        frozen = true;
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unfreeze() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        frozen = false;
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        return getClient() != null;
    }

    @Override
    public void updateState() throws IOException {
        if (alpha > 0 || (alpha == 0 && oldVisibleState)) {

            for (Playable opp : getOpponents()) {
                opp.updateEntity(this);
            }

            oldVisibleState = alpha != 0;
        }

        for (Playable ally : getTeam().getTeamMembers()) { //This loop will include all allies and this playable
            ally.updateEntity(this);
        }
    }

    /*private ArrayList<Entity> bufferedUpdates = new ArrayList<>();*/
    @Override
    public void updateEntity(Entity e) throws IOException {
        //DEFAULT BEHAVIOR

        if (!isConnected())
            return;

        EntityStatePacket packet = new EntityStatePacket(getClient());
        packet.writePacket(e);
    }

    @Override
    public byte getLives() {
        return lives;
    }

    @Override
    public boolean isDead() {
        return isDead;
    }

    @Override
    public boolean isFrozen() {
        return frozen;
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public void setReady(boolean ready) {
        this.isReady = ready;
    }

    @Override
    public void updatePlayerState() throws IOException {
        if (!isInMatch())
            return;

        for (Playable oop  : getOpponents()) {
            oop.playableUpdated(this);
        }

        for (Playable ally : getAllies()) {
            ally.playableUpdated(this);
        }
    }

    @Override
    public Playable[] getOpponents() {
        if (!isInMatch())
            return new Playable[0];

        if (getMatch().getTeam1().isAlly(this))
            return getMatch().getTeam2().getTeamMembers();
        else if (getMatch().getTeam2().isAlly(this))
            return getMatch().getTeam1().getTeamMembers();
        else
            return new Playable[0];
    }

    @Override
    public Playable[] getAllies() {
        if (getTeam() == null)
            return new Playable[0];

        return getTeam().getTeamMembers();
    }

    @Override
    public Ability<Playable> currentAbility() {
        return ability;
    }

    @Override
    public void setCurrentAbility(Class<? extends Ability<Playable>> class_) {
        try {
            this.ability = class_.getConstructor(Playable.class).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("This ability is not compatible!");
        }
    }

    public void useAbility(float targetX, float targetY, int action) {
        if (!canFire)
            return; //This playable can't use abilities

        if (ability != null)
            ability.use(targetX, targetY, action);
    }

    @Override
    public void playableUpdated(Playable p) throws IOException {
        //DEFAULT BEHAVIOR

        if (!isConnected())
            return;

        PlayerStatePacket packet = new PlayerStatePacket(getClient());
        packet.writePacket(p);
    }

    @Override
    public boolean canFire() {
        return canFire;
    }

    @Override
    public void setCanFire(boolean val) {
        this.canFire = val;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
