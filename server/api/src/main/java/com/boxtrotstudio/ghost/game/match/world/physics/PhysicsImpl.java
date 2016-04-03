package com.boxtrotstudio.ghost.game.match.world.physics;

import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.PFunction;
import com.boxtrotstudio.ghost.utils.PRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicsImpl implements Physics {
    private Map<Integer, PhysicsObject> cache = new HashMap<>();
    private ArrayList<Integer> ids = new ArrayList<>();

    @Override
    public int addPhysicsEntity(PRunnable<Entity> onHit, Hitbox hitbox) {
        return addPhysicsEntity(onHit, null, hitbox);
    }

    @Override
    public int addPhysicsEntity(PRunnable<Entity> onHit, PRunnable<CollisionResult> onHit2, Hitbox hitbox) {
        int id;
        do {
            id = Global.RANDOM.nextInt();
        } while (cache.containsKey(id));

        PhysicsObject obj = new PhysicsObject();
        obj.hitbox = hitbox;
        obj.onBasicHit = onHit;
        obj.onHitboxHit = onHit2;

        cache.put(id, obj);
        ids.add(id);

        if (hitboxCache != null) {
            hitboxCache.clear();
            hitboxCache = null;
        }

        return id;
    }

    @Override
    public boolean checkEntity(Entity entity) {
        boolean found = false;
        if (entity instanceof PhysicsEntity) {
            PhysicsEntity pentity = (PhysicsEntity) entity;
            if (pentity.getHitbox() == null)
                return false;

            for (Integer id : ids) {
                PhysicsObject obj = cache.get(id);
                CollisionResult result;
                if ((result = obj.hitbox.isHitboxInside(pentity.getHitbox())).didHit()) {
                    result.setContacter(pentity);
                    if (obj.onHitboxHit != null) {
                        obj.onHitboxHit.run(result);
                        found = true;
                    }
                    else {
                        obj.onBasicHit.run(pentity);
                        found = true;
                    }
                }
            }

            return found;
        } else {
            for (Integer id : ids) {
                PhysicsObject obj = cache.get(id);

                if (obj.hitbox.isPointInside(entity.getPosition())) {
                    obj.onBasicHit.run(entity);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean foreach(PFunction<Hitbox, Boolean> onHit) {
        for (Integer id : ids) {
            PhysicsObject obj = cache.get(id);
            boolean test = onHit.run(obj.hitbox);
            if (test)
                return true;
        }
        return false;
    }

    @Override
    public boolean removePhysicsEntity(int id) {
        if (!cache.containsKey(id))
            return false;
        cache.remove(id);
        ids.remove(new Integer(id));
        return true;
    }

    private List<Hitbox> hitboxCache;
    @Override
    public List<Hitbox> allHitboxes() {
        if (hitboxCache != null)
            return hitboxCache;

        hitboxCache = new ArrayList<>();
        for (Integer id : ids) {
            PhysicsObject obj = cache.get(id);
            hitboxCache.add(obj.hitbox);
        }
        return hitboxCache;
    }

    private class PhysicsObject {
        public PRunnable<Entity> onBasicHit;
        public PRunnable<CollisionResult> onHitboxHit;
        public Hitbox hitbox;
    }
}