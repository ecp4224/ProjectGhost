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
            if (!owner.isCarryingFlag()) { //If we are no longer carrying the flag, go back to spawn
                owner = null;
                isHeld = false;
                easeTo(spawnPoint, 900);
                isAtSpawn = true;
                return;
            }

            if (owner.isDead()) {
                owner = null;
                isHeld = false;
            } else {
                position.x = owner.getX();
                position.y = owner.getY() + owner.getHeight();
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

            if (player.getTeam().getTeamNumber() == team) {
                if (!isAtSpawn && !isHeld) {
                    easeTo(spawnPoint, 900);
                    isAtSpawn = true;
                } else if (player.isCarryingFlag()) {
                    player.getTeam().addScore();
                    player.setCarryingFlag(false);
                }
            } else {
                isAtSpawn = false;
                isHeld = true;
                owner = player;
                owner.setCarryingFlag(true);

            }
        }
    }

    public int getTeam() {
        return team;
    }
}
