package com.boxtrotstudio.ghost.game.match.entities.map;

import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.game.match.entities.TypeableEntity;
import com.boxtrotstudio.ghost.game.match.world.physics.BasePhysicsEntity;
import com.boxtrotstudio.ghost.game.match.world.physics.CollisionResult;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.utils.Vector2f;

public class FlagEntity extends BasePhysicsEntity implements TypeableEntity {

    private int team;
    FlagEntity(int team) {
        super();
        sendUpdates(true);
        requestTicks(true);
        this.team = team;
    }

    @Override
    public short getType() {
        return 89;
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
    public void onHit(Entity entity) { }

    @Override
    public void onHit(CollisionResult entity) {

    }
}
