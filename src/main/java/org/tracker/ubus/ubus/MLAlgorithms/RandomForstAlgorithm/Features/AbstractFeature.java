package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.DayOfWeek;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractFeature {

    private static final int MIN_MORNING_PEAK_HOUR = 6;
    private static final int MAX_MORNING_PEAK_HOUR = 8;

    private static final int MIN_AFTERNOON_PEAK_HOUR = 16;
    private static final int MAX_AFTERNOON_PEAK_HOUR = 18;

    private Route route;
    private int hourOfDay;
    private DayOfWeek dayOfWeek;


    public boolean isWeekend() {
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public boolean isMorningPeakHour() {
        return hourOfDay >= MIN_MORNING_PEAK_HOUR && hourOfDay <= MAX_MORNING_PEAK_HOUR;
    }

    public boolean isAfternoonPeakHour() {
        return hourOfDay >= MIN_AFTERNOON_PEAK_HOUR && hourOfDay <= MAX_AFTERNOON_PEAK_HOUR;
    }

    public boolean isPeakHour() {
        return hourOfDay >= 5 && hourOfDay <= 18;
    }
}
