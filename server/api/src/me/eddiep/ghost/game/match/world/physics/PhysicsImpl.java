package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.PRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PhysicsImpl implements Physics {
    private Map<Integer, PhysicsObject> cache = new HashMap<>();
    private ArrayList<Integer> ids = new ArrayList<>();

    @Override
    public int addPhysicsEntity(PRunnable<Entity> onHit, Hitbox hitbox) {
        int id;
        do {
            id = Global.RANDOM.nextInt();
        } while (cache.containsKey(id));

        PhysicsObject obj = new PhysicsObject();
        obj.id = id;
        obj.hitbox = hitbox;
        obj.onHit = onHit;

        cache.put(id, obj);
        ids.add(id);

        return id;
    }

    @Override
    public void checkEntity(Entity entity) {
        for (Integer id : ids) {
            PhysicsObject obj = cache.get(id);
            if (obj.hitbox.isPointInside(entity.getPosition())) {
                obj.onHit.run(entity);
            }
        }
    }

    @Override
    public void checkEntity(PhysicsEntity entity) {
        for (Integer id : ids) {
            PhysicsObject obj = cache.get(id);
            if (obj.hitbox.isHitboxInside(entity.getHitbox())) {
                obj.onHit.run(entity);
            }
        }
    }

    @Override
    public boolean removePhysicsEntity(int id) {
        if (!cache.containsKey(id))
            return false;
        cache.remove(id);
        ids.remove(new Integer(id));
        return true;
    }

    private class PhysicsObject {
        public PRunnable<Entity> onHit;
        public Hitbox hitbox;
        public int id;
    }
}
