package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.PRunnable;
import me.eddiep.ghost.utils.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class PhysicsImpl implements Physics {
    private Map<Integer, PhysicsObject> cache = new HashMap<>();

    @Override
    public int addPhysicsEntity(PRunnable<Entity> onHit, Vector2f... hitbox) {
        int id;
        do {
            id = Global.RANDOM.nextInt();
        } while (cache.containsKey(id));

        PhysicsObject obj = new PhysicsObject();
        obj.id = id;
        obj.hitBox = hitbox;
        obj.onHit = onHit;

        cache.put(id, obj);

        return id;
    }

    @Override
    public void checkEntity(Entity entity) {

    }

    @Override
    public void checkEntity(PhysicsEntity entity) {

    }

    @Override
    public boolean removePhysicsEntity(int id) {
        if (!cache.containsKey(id))
            return false;
        cache.remove(id);
        return true;
    }

    private class PhysicsObject {
        public PRunnable<Entity> onHit;
        public Vector2f[] hitBox;
        public int id;
    }
}
