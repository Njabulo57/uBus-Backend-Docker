package org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response;


import lombok.Builder;

import java.util.Collection;

/**
 * this containerizes past trips that can be
 * requested by different actors in the system
 */
@Builder
public record PastTripsPageResponse( Collection<AbstractPastTrip> pastTrips, int totalPages,
                                     int pageNumber, int pageSize) {
}
