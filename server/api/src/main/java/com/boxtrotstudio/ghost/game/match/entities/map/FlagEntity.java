package com.boxtrotstudio.ghost.game.match.entities.map;

import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.TypeableEntity;
import com.boxtrotstudio.ghost.game.match.world.World;
import com.boxtrotstudio.ghost.game.match.world.physics.BasePhysicsEntity;
import com.boxtrotstudio.ghost.game.match.world.physics.CollisionResult;
import com.boxtrotstudio.ghost.utils.Vector2f;

public class FlagEntity extends BasePhysicsEntity implements TypeableEntity {

    private int team;
    private boolean isAtSpawn = true;
    private boolean isHeld = true;
    private PlayableEntity owner;
    private Vector2f spawnPoint;
    FlagEntity(int team) {
        super();
        setAlpha(1f);
        sendUpdates(true);
        requestTicks(true);
        this.team = team;
    }

    @Override
    public short getType() {
        return team == 1 ? (short)89 : (short)90;
    }

    @Override
    public boolean isStaticPhysicsObject() {
        return true;
    }

    @Override
    public Vector2f[] generateHitboxPoints() {
        float x1 = getX() - (width / 2f), x2 = getX() + (width / 2f);
        float y1 = getY() - (height / 2f), y2 = getY() + (height / 2f);

        return new Vector2f[] {
                new Vector2f(x1, y1),
                new Vector2f(x1, y2),
                new Vector2f(x2, y2),
                new Vector2f(x2, y1)
        };
    }

    @Override
    public void tick() {
        super.tick();

        if (owner != null) {
            if (owner.isDead()) {
                owner.setCarryingFlag(false);
                owner = null;
                isHeld = false;
                System.out.println("OWNER DEAD");
            } else {
                setPosition(new Vector2f(owner.getX(), owner.getY() + 64f));
                setVelocity(owner.getVelocity());
                owner.setVisible(true); //Ensure they are still visible
            }
        }
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if (world == null)
            return;

        spawnPoint = getPosition().cloneVector();
    }

    @Override
    public void onHit(Entity entity) { }

    @Override
    public void onHit(CollisionResult result) {
        if (result.didHit() && result.getContacter() instanceof PlayableEntity) {
            PlayableEntity player = (PlayableEntity)result.getContacter();

            if (player.isDead())
                return;

            if (player.getTeam().getTeamNumber() == team) {
                if (!isAtSpawn && !isHeld) {
                    easeTo(spawnPoint, 900);
                    isAtSpawn = true;
                    System.out.println("RESPAWN");
                } else if (player.isCarryingFlag()) {
                    player.getTeam().addScore();
                    player.setCarryingFlag(false);

                    FlagEntity otherFlag = world.getTeamFlag(team == 1 ? 2 : 1);

                    otherFlag.setPosition(spawnPoint);
                    otherFlag.setVelocity(new Vector2f(0f, 0f));
                    otherFlag.isHeld = false;
                    otherFlag.isAtSpawn = true;
                    System.out.println("SCORE!");
                }
            } else if (!player.isCarryingFlag()) {
                isAtSpawn = false;
                isHeld = true;
                owner = player;
                owner.setCarryingFlag(true);
                System.out.println("CAP");

            }
        }
    }

    public int getTeam() {
        return team;
    }
}
