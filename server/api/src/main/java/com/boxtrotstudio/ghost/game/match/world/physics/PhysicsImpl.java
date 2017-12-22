package com.boxtrotstudio.ghost.game.match.world.physics;

import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.utils.*;

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
    public CollisionResult checkEntity(Entity entity) {
        boolean found = false;
        if (entity instanceof PhysicsEntity) {
            PhysicsEntity pEntity = (PhysicsEntity) entity;
            if (pEntity.getHitbox() == null)
                return CollisionResult.NO_HIT;

            CollisionResult result = CollisionResult.NO_HIT;
            for (Integer id : ids) {
                PhysicsObject obj = cache.get(id);

                if ((result = obj.hitbox.isHitboxInside(pEntity.getHitbox())).didHit()) {
                    result.setContacter(pEntity);
                    result.setCollideWith(obj.hitbox);
                    if (obj.onHitboxHit != null) {
                        obj.onHitboxHit.run(result);
                    }
                    else {
                        obj.onBasicHit.run(pEntity);
                    }
                }
            }

            return result;
        } else {
            for (Integer id : ids) {
                PhysicsObject obj = cache.get(id);

                if (obj.hitbox.isPointInside(entity.getPosition())) {
                    obj.onBasicHit.run(entity);
                    CollisionResult result = new CollisionResult(true, entity.getPosition());
                    result.setCollideWith(obj.hitbox);
                    return result;
                }
            }
        }

        return CollisionResult.NO_HIT;
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
    public boolean projectLine(Vector2f startPoint, Vector2f endPoint) {
        List<Hitbox> hitboxes = allHitboxes();

        for (Hitbox hitbox : hitboxes) {
            if (!hitbox.hasPolygon() || !hitbox.isCollideable())
                continue;
            for (Face face : hitbox.getPolygon().getFaces()) {
                Vector2f pointOfIntersection = VectorUtils.pointOfIntersection(startPoint, endPoint, face.getPointA(), face.getPointB());
                if (pointOfIntersection == null)
                    continue;

                double d = Vector2f.distance(pointOfIntersection, startPoint);
                if (d == 0f)
                    continue; //This starting point is this face

                return true;
            }
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