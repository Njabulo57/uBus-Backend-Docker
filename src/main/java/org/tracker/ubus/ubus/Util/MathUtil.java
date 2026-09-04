package org.tracker.ubus.ubus.Util;

public abstract class MathUtil {

    /**
     * Rounds the given decimal value to the specified number of decimal places.
     *
     * @param value the decimal value to be rounded
     * @param places the number of decimal places to round to; must be non-negative
     * @return the rounded decimal value
     * @throws IllegalArgumentException if the specified number of decimal places is negative
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        return (double) Math.round(value * factor) / factor;
    }
}
