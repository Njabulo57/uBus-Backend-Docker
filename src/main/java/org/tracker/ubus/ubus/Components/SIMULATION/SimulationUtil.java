package org.tracker.ubus.ubus.Components.SIMULATION;

public abstract class SimulationUtil {


    public static boolean isAtEndOfLeg(int currentCoIndex, int lastCoIndex) {
        return currentCoIndex == lastCoIndex;
    }
}
