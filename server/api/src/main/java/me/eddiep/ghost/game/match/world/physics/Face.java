package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.utils.Vector2f;

public class Face {
    private Vector2f pointA, pointB, normal, face;
    Polygon parent;

    public Face(Vector2f pointA, Vector2f pointB) {
        this.pointA = pointA;
        this.pointB = pointB;

        this.face = this.pointB.sub(this.pointA);

        calculateNormal();
    }

    private void calculateNormal() {
        this.normal = new Vector2f(-(pointB.y - pointA.y), (pointB.x - pointA.x));
    }

    public Vector2f getFaceVector() {
        return face;
    }

    public Vector2f getPointA() {
        return pointA;
    }

    public Vector2f getPointB() {
        return pointB;
    }

    public Vector2f getNormal() {
        return normal;
    }

    public Polygon getParentPolygon() {
        return parent;
    }

    @Override
    public String toString() {
        return "Face{" +
                "pointA=" + pointA +
                ", pointB=" + pointB +
                ", normal=" + normal +
                ", face=" + face +
                '}';
    }
}
