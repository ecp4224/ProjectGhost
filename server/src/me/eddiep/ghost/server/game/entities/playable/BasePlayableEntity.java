package me.eddiep.ghost.server.game.entities.playable;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.entities.abilities.Ability;
import me.eddiep.ghost.server.game.entities.abilities.Gun;
import me.eddiep.ghost.server.game.team.Team;
import me.eddiep.ghost.server.game.entities.TypeableEntity;
import me.eddiep.ghost.server.network.packet.impl.DespawnEntityPacket;
import me.eddiep.ghost.server.network.packet.impl.PlayerStatePacket;
import me.eddiep.ghost.server.network.packet.impl.SpawnEntityPacket;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;

public abstract class BasePlayableEntity extends Entity implements Playable {
    private static final byte MAX_LIVES = 3;

    protected byte lives;
    protected boolean isDead;
    protected boolean frozen;
    protected boolean isReady;
    protected boolean canFire = true;
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
                this.updateStateFor(opp);
            }

            oldVisibleState = alpha != 0;
        }

        for (Playable ally : getTeam().getTeamMembers()) { //This loop will include all allies and this playable
            if (ally.getEntity() == null)
                continue;
            ally.getEntity().updateStateFor(this);
        }
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
}
