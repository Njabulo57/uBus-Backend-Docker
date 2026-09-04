package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;


@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TrafficFeature extends AbstractFeature {

    private int delayInMinutes;
    private double averageSpeed;
    private double currentSpeed;
    private boolean hasAccident;
    private boolean hasRoadworks;

    private Destination to;
    private Destination from;



}
