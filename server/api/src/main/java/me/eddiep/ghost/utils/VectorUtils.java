package me.eddiep.ghost.utils;

/**
 * A utility class for handling vector math
 */
public class VectorUtils {

    /**
     * Checks to see if a point is inside a closed polygon
     * @param point The point to check
     * @param polygon A closed polygon
     * @return True if the point is inside the closed polygon, otherwise false
     */
    public static boolean isPointInside(Vector2f point, Vector2f... polygon) {
        int i, j;
        boolean c = false;
        int nvert = polygon.length;

        for (i = 0, j = nvert - 1; i < nvert; j = i++) {
            if ( ((polygon[i].y > point.y ) != (polygon[j].y > point.y)) &&
                    (point.x < (polygon[j].x-polygon[i].x) * (point.y-polygon[i].y) / (polygon[j].y-polygon[i].y) + polygon[i].x)   )
                c = !c;
        }

        return c;
    }

    /**
     * Rotate a point
     * @param point The point to rotate
     * @param angle an angle, in radians.
     * @return The new, rotated point
     */
    public static Vector2f rotate(Vector2f point, double angle) {
        float oldx = point.x;

        Vector2f vector2f = new Vector2f();

        vector2f.x = (float) (point.x * Math.cos(angle) - point.y * Math.sin(angle));
        vector2f.y = (float) (point.x * Math.sin(angle) + point.y * Math.cos(angle));

        return vector2f;
    }

    /**
     * Rotate a closed polygon around a center
     * @param angle an angle, in radians.
     * @param center The center to rotate around
     * @param points The closed polygon
     * @return A new array of points that make up the rotated polygon
     */
    public static Vector2f[] rotatePoints(double angle, Vector2f center, Vector2f... points) {
        for (int i = 0; i < points.length; i++) {
            points[i] = center.add(rotate(points[i].sub(center), angle));
        }

        return points;
    }
}
