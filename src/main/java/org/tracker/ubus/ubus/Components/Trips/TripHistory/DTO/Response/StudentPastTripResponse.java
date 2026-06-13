package org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public final class StudentPastTripResponse extends AbstractPastTrip {

    private Collection<CoOrdinate> coOrdinates;

}
