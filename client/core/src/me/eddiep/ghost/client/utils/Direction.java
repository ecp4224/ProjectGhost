package me.eddiep.ghost.client.utils;

public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    public static Direction fromDegrees(double value) {
        double degrees = validateDegree(value);

        if (degrees <= 45) {
            return RIGHT;
        } else if (degrees > 45 && degrees <= 135) {
            return UP;
        } else if (degrees > 135 && degrees <= 215) {
            return LEFT;
        } else if (degrees > 215 && degrees <= 315) {
            return DOWN;
        } else {
            return RIGHT;
        }
    }

    private static double validateDegree(double degree) {
        while (degree > 360) {
            degree -= 360;
        }

        while (degree < 0) {
            degree += 360;
        }

        return degree;
    }
}
