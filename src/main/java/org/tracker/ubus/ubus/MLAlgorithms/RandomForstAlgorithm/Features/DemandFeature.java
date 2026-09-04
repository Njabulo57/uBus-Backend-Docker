package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;



@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DemandFeature extends AbstractFeature {

    private int semesterWeek;
    private Destination campus;

    private boolean hasAccident;
    private boolean hasRoadworks;

    private int delayInMinutes;
    private int distanceInMeters;
    private int passengerCount;

    private int staffCount;
    private int studentCount;

}
