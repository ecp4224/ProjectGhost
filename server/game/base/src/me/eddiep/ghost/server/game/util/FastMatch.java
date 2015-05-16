package me.eddiep.ghost.server.game.util;

public class FastMatch {

    public static double pow(final double a, final double b) {
        final long tmp = Double.doubleToLongBits(a);
        final long tmp2 = (long)(b * (tmp - 4606921280493453312L))
                + 4606921280493453312L;
        return Double.longBitsToDouble(tmp2);
    }

}
