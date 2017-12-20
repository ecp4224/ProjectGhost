package com.boxtrotstudio.ghost.game.match.entities.playable;

import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.abilities.Ability;
import com.boxtrotstudio.ghost.game.match.abilities.Gun;
import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.map.FlagEntity;
import com.boxtrotstudio.ghost.game.match.item.Inventory;
import com.boxtrotstudio.ghost.game.match.item.Item;
import com.boxtrotstudio.ghost.game.match.stats.BuffType;
import com.boxtrotstudio.ghost.game.match.stats.Stat;
import com.boxtrotstudio.ghost.game.match.stats.TemporaryStats;
import com.boxtrotstudio.ghost.game.match.world.map.Light;
import com.boxtrotstudio.ghost.game.match.world.physics.BasePhysicsEntity;
import com.boxtrotstudio.ghost.game.match.world.physics.CollisionResult;
import com.boxtrotstudio.ghost.game.match.world.physics.Hitbox;
import com.boxtrotstudio.ghost.game.match.world.physics.PolygonHitbox;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.game.util.VisibleFunction;
import com.boxtrotstudio.ghost.utils.ArrayHelper;
import com.boxtrotstudio.ghost.utils.TimeUtils;
import com.boxtrotstudio.ghost.utils.Vector2f;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;

import static com.boxtrotstudio.ghost.utils.Constants.*;

public abstract class BasePlayableEntity extends BasePhysicsEntity implements PlayableEntity {
    private static final byte MAX_LIVES = 3;
    //private static final float VISIBLE_TIMER = 800f;

    protected byte lives;
    protected boolean isDead;
    protected boolean frozen;
    protected boolean isReady;
    protected Stat speed = new Stat("mspd", 5.0);
    protected long lastFire;
    protected boolean wasHit;
    protected long lastHit;
    protected boolean didFire;
    protected Vector2f target;
    protected Vector2f direction;
    protected Stat fireRate = new Stat("frte", 5.0); //In percent
    protected boolean isVisibleToAllies = true;
    protected Inventory inventory = new Inventory(2);
    protected boolean canChangeAbility = true;

    protected int preferredItem = -1;

    protected boolean canFire = true;
    protected VisibleFunction function = VisibleFunction.ORIGINAL; //Always default to original style
    protected Stat visibleLength = new Stat("vlen", 800.0); //In ms
    protected Stat visibleStrength = new Stat("vstr", 255.0);

    protected int invincibilityStack;

    private Ability<PlayableEntity> ability = new Gun(this);

    private TemporaryStats stats;
    private boolean tempWasHit;
    private boolean respawn;
    private boolean showLives = true;

    //Respawn info
    private long deathTime;
    private boolean carryingFlag;

    @Override
    public boolean isStaticPhysicsObject() {
        return false;
    }

    @Override
    public Team getTeam() {
        return containingMatch == null ? null : containingMatch.getTeamFor(this);
    }

    @Override
    public void prepareForMatch() {
        oldVisibleState = true;
        setVisible(false);

        //Alert playables of current stat values
        onStatUpdate(speed);
        onStatUpdate(fireRate);
        onStatUpdate(visibleLength);
        onStatUpdate(visibleStrength);

        stats = new TemporaryStats();
        tempWasHit = false;

        stats.set(TemporaryStats.WEAPON, currentAbility().id());

        super.hitbox = PolygonHitbox.createCircleHitbox(24.0, 5, "PLAYER");
        super.hitbox.getPolygon().translate(getPosition());

        if (world.isCaptureTheFlag()) { //If we are playing capture the flag
            respawn = true;
            showLives = false;
            lives = 1;
        }
    }

    @Override
    public int getPreferredItem() {
        return preferredItem;
    }

    @Override
    public void setPreferredItem(int item) {
        this.preferredItem = item;
    }

    @Override
    public void onStatUpdate(Stat stat) {
        if (stat == speed && this.velocity.length() != 0f) {
            this.velocity.normalise().scale((float) speed.getValue());
        }
    }

    @Override
    public void onFire() {
        stats.plusOne(TemporaryStats.SHOTS_FIRED);

        lastFire = System.currentTimeMillis();
        didFire = true;
        isFiring = false;
        switch (function) {
            case ORIGINAL:
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

    public long calculateFireRate(long base) {
        return base - (long)((fireRate.getValue() / 100.0) * base);
    }

    @Override
    public boolean isFiring() {
        return isFiring;
    }

    protected void handleVisible() {
        if (isFiring || carryingFlag)
            return;

        switch (function) {
            case ORIGINAL:
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

    @Override
    public boolean shouldRespawn() {
        return respawn;
    }

    @Override
    public boolean showLives() {
        return showLives;
    }

    private boolean hasStartedFade;
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

    private void doDeathCheck() {
        if (lives > 0) { //They're not dead
            if (isDead) { //but if they were
                onRevive();
            }
        } else { //They're dead
            if (!isDead || !frozen) { //but if they weren't dead or frozen
                onDeath();
            }
        }
    }

    protected void onDeath() {
        isDead = true;
        frozen = true;
        getMatch().playableUpdated(this);

        if (respawn) {
            deathTime = System.currentTimeMillis();
        }
    }

    protected void onRevive() {
        isDead = false;
        frozen = false;
        getMatch().playableUpdated(this);
    }

    @Override
    public void onKilledPlayable(PlayableEntity killed) {
        double xdif = killed.getX() - getX();
        double ydif = killed.getY() - getY();
        double angle = Math.atan2(ydif, xdif);

        killed.triggerEvent(Event.PlayerDeath, angle);

        killed.setTarget(null);
        killed.setVelocity(new Vector2f(0f, 0f));
    }

    @Override
    public void onDamagePlayable(PlayableEntity hit) {
       stats.plusOne(TemporaryStats.SHOTS_HIT);
    }

    @Override
    public TemporaryStats getCurrentMatchStats() {
        return stats;
    }

    @Override
    public void onDamage(PlayableEntity damager) {
        double xdif = damager.getX() - getX();
        double ydif = damager.getY() - getY();
        double angle = Math.atan2(ydif, xdif);

        triggerEvent(Event.PlayerHit, angle);

        wasHit = true;
        tempWasHit = true;

        lastHit = System.currentTimeMillis();
        switch (function) {
            case ORIGINAL:
                if (!isVisible())
                    setVisible(true);
                break;
            case TIMER:
                if (visibleIndicator < VISIBLE_COUNTER_DEFAULT_LENGTH) {
                    visibleIndicator = VISIBLE_COUNTER_DEFAULT_LENGTH;
                }
                break;
        }

        doDeathCheck();
    }

    @Override
    public void tick() {
        //Check the player's death stat
        doDeathCheck();

        doIdleCheck();

        if (respawn && isDead) {
            if (System.currentTimeMillis() - deathTime >= 5000) {
                onRespawn();
            }
        }

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

        if (super.hitbox != null) {
            super.hitbox.getPolygon().translate(velocity);
        }

        handleVisible();

        fadePlayerOut();

        checkBounds();

        this.speed.tick();
        this.fireRate.tick();
        this.visibleStrength.tick();
        this.visibleLength.tick();

        super.tick();
    }

    protected void onRespawn() {
        lives = 1;
        isDead = false;
        frozen = false;

        if (!world.isCaptureTheFlag()) {
            int map_xmin = (int) containingMatch.getLowerBounds().x, map_xmax = (int)containingMatch.getUpperBounds().x;
            int map_ymin = (int) containingMatch.getLowerBounds().y, map_ymax = (int)containingMatch.getUpperBounds().y;
            int map_xmiddle = map_xmin + ((map_xmax - map_xmin) / 2);

            Vector2f start = world.randomLocation(map_xmin, map_ymin, map_xmiddle, map_ymax);

            setPosition(start);
            setVisible(false);
        } else {
            FlagEntity flag = world.getTeamFlag(getTeam());

            int xmin = (int)flag.getX() - FLAG_RESPAWN_RANGE, ymin = (int)flag.getY() - FLAG_RESPAWN_RANGE;
            int xmax = (int)flag.getX() + FLAG_RESPAWN_RANGE, ymax = (int)flag.getY() + FLAG_RESPAWN_RANGE;

            xmin = Math.max(xmin, 0);
            ymin = Math.max(ymin, 0);
            xmax = Math.min(xmax, (int)containingMatch.getUpperBounds().x);
            ymax = Math.min(ymax, (int)containingMatch.getUpperBounds().y);

            Vector2f start = world.randomLocation(xmin, ymin, xmax, ymax);

            setPosition(start);
            setVisible(false);
        }

        getMatch().playableUpdated(this);
    }

    private void checkBounds() {
        position.x = Math.max(Math.min(position.x, 1280), 0);
        position.y = Math.max(Math.min(position.y, 720), 0);
    }

    private boolean isIdle;
    private long idleStart;
    private void doIdleCheck() {
        if (velocity.length() != 0f) {
            if (isIdle && isVisible()) {
                isIdle = false;
                idleStart = 0L;
                startPlayerFadeOut();
            } else if (isIdle) {
                isIdle = false;
                idleStart = 0L;
            }
            return;
        }

        if (!isIdle) {
            isIdle = true;
            idleStart = System.currentTimeMillis();
        } else {
            if (System.currentTimeMillis() - idleStart >= getMatch().getMaxIdleTime()) {
                setVisible(true);
            }
        }
    }

    @Override
    public boolean isIdle() {
        return isIdle && isVisible();
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
        if (isInvincible())
            return;

        lives--;
        getMatch().playableUpdated(this);
    }

    @Override
    public void addLife() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        lives++;
        getMatch().playableUpdated(this);
    }

    @Override
    public void resetLives() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        lives = MAX_LIVES;
        isDead = false;
        frozen = false;
        invincibilityStack = 0;
        if (inventory != null) {
            inventory.clear();
        }
        speed.clearBuffs();

        triggerEvent(Event.LivesReset, lives);

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

        getMatch().playableUpdated(this);
    }

    @Override
    public void onWin(Match match) {
        if (!tempWasHit) {
            stats.set(TemporaryStats.HAT_TRICK, 1);
        } else {
            stats.set(TemporaryStats.HAT_TRICK, 0);
        }
    }

    @Override
    public void kill() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        lives = 0;

        getMatch().playableUpdated(this);
    }

    @Override
    public void freeze() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        frozen = true;
        setTarget(null);
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
        if (getMatch().getTimeElapsed() < 10000)
            return true; //Send all updates within the first 10 seconds

        for (Light light : world.getLights()) {
            float xmin = light.getX() - (light.getRadius() + 48f);
            float ymin = light.getY() - (light.getRadius() + 48f);
            float xmax = light.getX() + (light.getRadius() + 48f);
            float ymax = light.getY() + (light.getRadius() + 48f);

            if (position.x > xmin && position.y > ymin && position.x < xmax && position.y < ymax)
                return true; //Send all updates when inside a light
        }

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

        if (isReady && containingMatch != null) {
            containingMatch.onReady(this);
        }
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
    public void _packet_setCurrentAbility(Class<? extends Ability<PlayableEntity>> class_) {
        if (!canChangeAbility)
            return;

        try {
            this.ability = class_.getConstructor(PlayableEntity.class).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("This ability is not compatible!");
        }
    }

    @Override
    public void setCurrentAbility(Ability<PlayableEntity> ability) {
        this.ability = ability;
    }

    @Override
    public boolean canChangeAbility() {
        return canChangeAbility;
    }

    @Override
    public void setCanChangeAbility(boolean value) {
        this.canChangeAbility = value;
    }

    private boolean isFiring;
    public void useAbility(float targetX, float targetY, int action) {
        if (!canFire || isDead)
            return; //This playable can't use abilities

        if (ability != null) {
            hasStartedFade = false;
            isFiring = true;

            ability.use(targetX, targetY);
        }
    }

    @Override
    public boolean isCarryingFlag() {
        return carryingFlag;
    }

    @Override
    public void setCarryingFlag(boolean value) {
        this.carryingFlag = value;
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

    @Override
    public Stat getVisibleLengthStat() {
        return visibleLength;
    }

    @Override
    public Stat getVisibleStrengthStat() {
        return visibleStrength;
    }

    @Override
    public void onItemActivated(Item item, PlayableEntity owner) {
        stats.plusOne(TemporaryStats.ITEM_USAGE);
    }

    @Override
    public void onItemDeactivated(Item item, PlayableEntity owner) { }

    @Override
    public Hitbox getHitbox() {
        return super.hitbox;
    }

    @Override
    public final Vector2f[] generateHitboxPoints() {
        throw new IllegalStateException("This PlayableEntity generates it's own hitbox!");
    }

    @Override
    public final void onHit(Entity entity) {
        throw new IllegalStateException("PlayableEntities should not act on other physics objects.\nOther physics objects should act on this PlayableObject");
    }

    @Override
    public final void onHit(CollisionResult entity) {
        throw new IllegalStateException("PlayableEntities should not act on other physics objects.\nOther physics objects should act on this PlayableObject");
    }

    @Override
    public boolean isInvincible() {
        return invincibilityStack > 0;
    }

    @Override
    public void addInvincibilityStack() {
        invincibilityStack++;
    }

    @Override
    public void removeInvincibilityStack() {
        invincibilityStack--;
        if (invincibilityStack < 0)
            invincibilityStack = 0;
    }

    @Override
    public boolean visibleToAllies() {
        return isVisibleToAllies;
    }

    @Override
    public void isVisibleToAllies(boolean val) {
        this.isVisibleToAllies = val;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
