package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Util;

import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Internal.ScheduleQueryContext;

import java.util.function.Function;

public record TripScheduleUtil() {

    public static<T> T getWithShceduleContext(Function<ScheduleQueryContext, T> queryContextTFunction,
                                       ScheduleQueryContext queryContext) {
        return queryContextTFunction.apply(queryContext);
    }
}
