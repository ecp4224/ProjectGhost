package com.boxtrotstudio.ghost.client.core.physics;

import com.boxtrotstudio.ghost.client.core.game.Entity;
import com.boxtrotstudio.ghost.client.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicsImpl implements Physics {
    private Map<Integer, PhysicsObject> cache = new HashMap<>();
    private ArrayList<Integer> ids = new ArrayList<>();

    @Override
    public void clear() {
        cache.clear();
        ids.clear();
    }

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
        obj.id = id;
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
    public void checkEntity(Entity entity) {
        if (entity instanceof PhysicsEntity) {
            PhysicsEntity pentity = (PhysicsEntity) entity;
            if (pentity.getHitbox() == null)
                return;

            for (Integer id : ids) {
                PhysicsObject obj = cache.get(id);
                CollisionResult result;
                if ((result = obj.hitbox.isHitboxInside(pentity.getHitbox())).didHit()) {
                    result.setContacter(pentity);
                    if (obj.onHitboxHit != null)
                        obj.onHitboxHit.run(result);
                    else
                        obj.onBasicHit.run((Entity)pentity);
                }
            }
        } else {
            for (Integer id : ids) {
                PhysicsObject obj = cache.get(id);

                if (!obj.hitbox.isPointInside(entity.getPosition()))
                    continue;

                obj.onBasicHit.run(entity);
                return;
            }
        }
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
        ids.remove(Integer.valueOf(id));
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

    private boolean willIntersect(Entity entity, Hitbox hitbox) {
        if (hitbox.hasPolygon())
            return false;

        Vector2f startPoint = entity.getPosition();
        Vector2f endPoint = new Vector2f(entity.getCenterX() + entity.getVelocity().x, entity.getCenterY() + entity.getVelocity().y);
        int numOfIntersections = 0;

        for (Face face : hitbox.getPolygon().getFaces()) {
            Vector2f intersect = VectorUtils.pointOfIntersection(startPoint, endPoint, face.getPointA(), face.getPointB());
            if (intersect == Vector2f.ZERO)
                continue;
            numOfIntersections++;
        }

        return numOfIntersections != 0 && numOfIntersections % 2 == 0;
    }

    private class PhysicsObject {
        public PRunnable<Entity> onBasicHit;
        public PRunnable<CollisionResult> onHitboxHit;
        public Hitbox hitbox;
        public int id;
    }
}
