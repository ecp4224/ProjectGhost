package me.eddiep.ghost.client.core.game;

public enum Direction {
    /**
     * Going away from the y origin
     */
    UP,

    /**
     * Moving towards the y origin
     */
    DOWN,

    /**
     * Moving towards the x origin
     */
    LEFT,

    /**
     * Moving away from the x origin
     */
    RIGHT,

    /**
     * A complex direction <br></br>
     * Moving away from the y origin and towards the x origin
     */
    UP_LEFT,

    /**
     * A complex direction <br></br>
     * Moving away from the y origin and away from the x origin
     */
    UP_RIGHT,

    /**
     * A complex direction <br></br>
     * Moving towards from the y origin and towards the x origin
     */
    DOWN_LEFT,

    /**
     * A complex direction <br></br>
     * Moving towards from the y origin and away from the x origin
     */
    DOWN_RIGHT,

    /**
     * Represents no direction, or not moving.
     */
    NONE,

    /**
     * Represents any direction, or moving.
     */
    MOVING;

    /**
     * Converts a complex direction into a simple direction.
     * @return The only 2 results this can return is either {@link Direction#UP} or {@link Direction#DOWN} if this direction is a complex direction, otherwise it will return itself
     */
    public Direction simple() {
        switch (this) {
            case UP_RIGHT:
            case UP_LEFT:
                return UP;
            case DOWN_LEFT:
            case DOWN_RIGHT:
                return DOWN;
            default:
                return this;
        }
    }

    public Direction add(Direction dir) {
        if (this == Direction.UP && dir == Direction.LEFT)
            return Direction.UP_LEFT;
        else if (this == Direction.UP && dir == Direction.RIGHT)
            return Direction.UP_RIGHT;
        else if (this == Direction.DOWN && dir == Direction.LEFT)
            return Direction.DOWN_LEFT;
        else if (this == Direction.DOWN && dir == Direction.RIGHT)
            return Direction.DOWN_RIGHT;
        else if (this == Direction.LEFT && dir == Direction.UP)
            return Direction.UP_LEFT;
        else if (this == Direction.RIGHT && dir == Direction.UP)
            return Direction.UP_RIGHT;
        else if (this == Direction.LEFT && dir == Direction.DOWN)
            return Direction.DOWN_LEFT;
        else if (this == Direction.RIGHT && dir == Direction.DOWN)
            return Direction.DOWN_RIGHT;
        else
            return this;
    }

    /**
     * Get the opposite direction of this direction. For example, if this direction is {@link Direction#RIGHT}, then this method will return {@link Direction#LEFT}. <br></br>
     * If a complex direction is given, then a complex direction is returned. The complex direction returned will always be the opposite of what was given.
     * @return The opposite direction of this direction.
     */
    public Direction opposite() {
        switch (this) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case RIGHT:
                return LEFT;
            case LEFT:
                return RIGHT;
            case UP_LEFT:
                return DOWN_RIGHT;
            case UP_RIGHT:
                return DOWN_LEFT;
            case DOWN_LEFT:
                return UP_RIGHT;
            case DOWN_RIGHT:
                return UP_LEFT;
            case NONE:
                return MOVING;
            case MOVING:
                return NONE;
            default:
                return this;
        }
    }

    /**
     * Rotate this direction 90 degrees clockwise
     * @return The resulting direction
     */
    public Direction rotateNegitive90() {
        return opposite().rotate90();
    }

    /**
     * Rotate this direction 90 degrees counter-clockwise
     * @return The resulting direction
     */
    public Direction rotate90() {
        switch (this) {
            case UP:
                return LEFT;
            case DOWN:
                return RIGHT;
            case LEFT:
                return DOWN;
            case RIGHT:
                return UP;
            case UP_LEFT:
                return DOWN_LEFT;
            case UP_RIGHT:
                return UP_LEFT;
            case DOWN_LEFT:
                return DOWN_RIGHT;
            case DOWN_RIGHT:
                return UP_LEFT;
            case NONE:
            case MOVING:
            default:
                return this;
        }
    }

    public static Direction fromDegrees(double value) {
        double degrees = validateDegree(value);

        if (degrees <= 30) {
            return RIGHT;
        } else if (degrees > 30 && degrees <= 60) {
            return UP_RIGHT;
        } else if (degrees > 60 && degrees <= 120) {
            return UP;
        } else if (degrees > 120 && degrees <= 150) {
            return UP_LEFT;
        } else if (degrees > 150 && degrees <= 210) {
            return LEFT;
        } else if (degrees > 210 && degrees <= 240) {
            return DOWN_LEFT;
        } else if (degrees > 240 && degrees <= 300) {
            return DOWN;
        } else if (degrees > 300 && degrees <= 330) {
            return DOWN_RIGHT;
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
