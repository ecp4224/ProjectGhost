package me.eddiep.ghost.client.core;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class EntityFactory {
    private static HashMap<Short, Class<? extends Entity>> ENTITIES = new HashMap<Short, Class<? extends Entity>>();

    static {
        //TODO Add typeable entities here
    }

    public static Entity createEntity(short type, short id, float x, float y) {
        Class class_ = ENTITIES.get(type);
        if (class_ != null) {
            try {
                Entity entity = (Entity) class_.getConstructor(short.class).newInstance(id);
                entity.setCenter(x, y);
                return entity;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
