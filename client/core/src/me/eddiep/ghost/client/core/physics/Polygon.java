package me.eddiep.ghost.client.core.physics;


import me.eddiep.ghost.client.utils.Vector2f;

import java.util.Arrays;

public class Polygon {
    private Vector2f[] points;
    private Face[] faces;

    public Polygon(Vector2f... points) {
        this.points = points;
        this.createFaces();
        this.checkNormals();
    }

    private void createFaces() {
        faces = new Face[points.length];
        for (int i = 0; i < points.length; i++) {
            Face f;
            if (i + 1 >= points.length) {
                f = new Face(points[i], points[0]);
            } else {
                f = new Face(points[i], points[i + 1]);
            }

            faces[i] = f;
        }
    }

    private void checkNormals() {
        for (int i = 0; i < faces.length; i++) {
            Face face = faces[i];
            Face nextFace;
            if (i + 1 >= faces.length) {
                nextFace = faces[0];
            } else {
                nextFace = faces[i + 1];
            }

            if (face.getNormal().length() == 0)
                continue;

            float val = Vector2f.dot(face.getNormal(), nextFace.getFaceVector());

            if (val > 0) {
                face.getNormal().invert();
            }

            face.getNormal().normalise();
        }
    }

    public Face[] getFaces() {
        return faces;
    }

    public Vector2f[] getPoints() {
        return points;
    }

    public void translate(Vector2f add) {
        for (int i = 0; i < points.length; i++) {
            Vector2f point = points[i];
            Face face = faces[i];
            point.set(point.x + add.x, point.y + add.y);
            face.getFaceVector().set(face.getFaceVector().x + add.x, face.getFaceVector().y + add.y);
        }
    }

    public void rotate(double radiusAdd) {
        for (int i = 0; i < points.length; i++) {
            Vector2f point = points[i];
            Face face = faces[i];
            point.rotate(radiusAdd);
            face.getFaceVector().rotate(radiusAdd);
            face.getNormal().rotate(radiusAdd);
        }
    }

    public void scale(float scale) {
        for (int i = 0; i < points.length; i++) {
            Vector2f point = points[i];
            Face face = faces[i];
            point.scale(scale);
            face.getFaceVector().scale(scale);
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(points) + " " + Arrays.toString(faces);
    }
}
