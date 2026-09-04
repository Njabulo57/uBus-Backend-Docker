package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SplittingFeature {

    CAMPUS(Integer.MIN_VALUE, Integer.MAX_VALUE, 0),

    MORNING_PEAK(5,9, 10),
    WEEKDAY(1, 5, 5),
    WEEKEND(6,6, 6),
    AFTERNOON_PEAK(3, 6, 7),

    PEAK_HOUR(5, 18, 10),
    LOW_AVERAGE_SPEED(0, 20, 20),

    ACCIDENT_OCCURRED(0, 0,  0);


    private final double start;
    private final double end;
    private final double threshold;


}
