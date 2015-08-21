package me.eddiep.ghost.game.match.entities.playable;

import me.eddiep.ghost.game.match.abilities.Ability;
import me.eddiep.ghost.game.match.abilities.Gun;
import me.eddiep.ghost.game.match.entities.BaseEntity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.stats.BuffType;
import me.eddiep.ghost.game.match.stats.Stat;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.game.util.VisibleFunction;
import me.eddiep.ghost.utils.ArrayHelper;
import me.eddiep.ghost.utils.TimeUtils;
import me.eddiep.ghost.utils.Vector2f;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;

import static me.eddiep.ghost.utils.Constants.*;

public abstract class BasePlayableEntity extends BaseEntity implements PlayableEntity {
    private static final byte MAX_LIVES = 3;
    //private static final float VISIBLE_TIMER = 800f;

    protected byte lives;
    protected boolean isDead;
    protected boolean frozen;
    protected boolean isReady;
    protected Stat speed = new Stat("mspd", 6.0);
    protected long lastFire;
    protected boolean wasHit;
    protected long lastHit;
    protected boolean didFire = false;
    protected Vector2f target;
    protected Stat fireRate = new Stat("frte", 300.0); //In ms

    protected boolean canFire = true;
    protected VisibleFunction function = VisibleFunction.ORGINAL; //Always default to original style
    protected Stat visibleLength = new Stat("vlen", 800.0); //In ms
    protected Stat visibleStrength = new Stat("vstr", 255.0);
    private Ability<PlayableEntity> ability = new Gun(this);

    @Override
    public Team getTeam() {
        return containingMatch == null ? null : containingMatch.getTeamFor(this);
    }

    @Override
    public void prepareForMatch() {
        oldVisibleState = true;
        setVisible(false);
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
                    if (isVisible() && System.currentTimeMillis() - lastFire >= visibleLength.getValue()) {
                        didFire = false;
                        startPlayerFadeOut();
                    }
                } else if (wasHit) {
                    if (isVisible() && System.currentTimeMillis() - lastHit >= visibleLength.getValue()) {
                        wasHit = false;
                        startPlayerFadeOut();
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

    private boolean hasStartedFade = false;
    private long startTime;
    private void fadePlayerOut() {
        if (!hasStartedFade)
            return;

        if (didFire || wasHit) {
            alpha = (int) visibleStrength.getValue();
            hasStartedFade = false;
            return;
        }

        alpha = (int) TimeUtils.ease((float) visibleStrength.getValue(), 0f, FADE_SPEED, System.currentTimeMillis() - startTime);

        if (alpha == 0) {
            hasStartedFade = false;
        }
    }

    private void startPlayerFadeOut() {
        if (hasStartedFade)
            return;

        hasStartedFade = true;
        startTime = System.currentTimeMillis();
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

    public boolean didFire() { return didFire; }

    @Override
    public void onDamage(PlayableEntity damager) {
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
        if (hasTarget()) {
            if (Math.abs(position.x - target.x) < 8 && Math.abs(position.y - target.y) < 8) {
                setPosition(target);
                target = null;
                setVelocity(new Vector2f(0f, 0f));
                world.requestEntityUpdate();
            }
        }

        position.x += velocity.x;
        position.y += velocity.y;

        handleVisible();

        fadePlayerOut();
    }

    @Override
    public Vector2f getTarget() {
        return target;
    }

    @Override
    public boolean hasTarget() { return target != null; }

    @Override
    public void setTarget(Vector2f target) {
        if (target == null) {
            setVelocity(new Vector2f(0f, 0f));
            this.target = null;
            return;
        }

        float x = position.x;
        float y = position.y;

        float asdx = target.x - x;
        float asdy = target.y - y;
        float inv = (float) Math.atan2(asdy, asdx);


        velocity.x = (float) (Math.cos(inv) * speed.getValue());
        velocity.y = (float) (Math.sin(inv) * speed.getValue());

        this.target = target;

        getWorld().requestEntityUpdate();
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
        getMatch().playableUpdated(this);
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
        getMatch().playableUpdated(this);
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
        getMatch().playableUpdated(this);
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
        getMatch().playableUpdated(this);
    }

    @Override
    public void kill() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        lives = 0;
        isDead = true;
        frozen = true;
        setVisible(true);
        getMatch().playableUpdated(this);
    }

    @Override
    public void freeze() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        frozen = true;
        getMatch().playableUpdated(this);
    }

    @Override
    public void unfreeze() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        frozen = false;
        getMatch().playableUpdated(this);
    }

    @Override
    public boolean shouldSendUpdatesTo(PlayableEntity e) {
        if (getMatch().getTimeElapsed() < 3000)
            return true; //Send all updates within the first 3 seconds

        if (ArrayHelper.contains(getOpponents(), e)) { //e is an opponent
            if (alpha > 0 || (alpha == 0 && oldVisibleState)) {
                oldVisibleState = alpha != 0;

                return true;
            } else {
                return false;
            }
        } else return true;
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
    public boolean isReady() { return isReady; }

    @Override
    public void setReady(boolean ready) {
        this.isReady = ready;
    }

    @Override
    public PlayableEntity[] getOpponents() {
        if (!isInMatch())
            return new PlayableEntity[0];

        if (getMatch().getTeam1().isAlly(this))
            return getMatch().getTeam2().getTeamMembers();
        else if (getMatch().getTeam2().isAlly(this))
            return getMatch().getTeam1().getTeamMembers();
        else
            return new PlayableEntity[0];
    }

    @Override
    public PlayableEntity[] getAllies() {
        if (getTeam() == null)
            return new PlayableEntity[0];

        return getTeam().getTeamMembers();
    }

    @Override
    public Ability<PlayableEntity> currentAbility() {
        return ability;
    }

    @Override
    public void setCurrentAbility(Class<? extends Ability<PlayableEntity>> class_) {
        try {
            this.ability = class_.getConstructor(PlayableEntity.class).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("This ability is not compatible!");
        }
    }

    @Override
    public void setCurrentAbility(Ability<PlayableEntity> ability){
        this.ability = ability;
    }

    public void useAbility(float targetX, float targetY, int action) {
        if (!canFire)
            return; //This playable can't use abilities

        if (ability != null) {
            ability.use(targetX, targetY, action);

            if (isVisible()) {
                hasStartedFade = false;
                alpha = 255;
            }
        }
    }

    @Override
    public boolean canFire() {
        return canFire;
    }

    @Override
    public void setCanFire(boolean val) {
        this.canFire = val;
    }

    /**
     * Returns the speed stat of this entity. Buffs can then be applied to the stat to change its value.
     * For permanent changes {@link #setSpeed(float)} should be used, if this entity has a target.
     */
    @Override
    public Stat getSpeedStat() {
        return speed;
    }

    /**
     * Returns the speed of this entity. Buffs can be applied to the speed stat to change its value.
     */
    @Override
    public float getSpeed() {
        return (float) speed.getValue();
    }

    /**
     * Consider {@link Stat#addBuff(String, BuffType, double, boolean) adding a buff} to the speed stat instead, unless
     * this change is considered permanent.
     */
    @Override
    public void setSpeed(float speed) {
        this.speed.setTrueValue(speed);

        if (target != null) {
            float x = position.x;
            float y = position.y;

            float asdx = target.x - x;
            float asdy = target.y - y;
            float inv = (float) Math.atan2(asdy, asdx);


            velocity.x = (float) (Math.cos(inv)*speed);
            velocity.y = (float) (Math.sin(inv)*speed);

            getWorld().requestEntityUpdate();
        }
    }

    @Override
    public Stat getFireRateStat() {
        return fireRate;
    }
}
