package org.tracker.ubus.ubus.Components.Buses.Bus.Util;

import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Responses.BusAdminViewResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus.OPERATIONAL;

public abstract class BusSorting {



    public static List<BusAdminViewResponse> sort(List<BusAdminViewResponse> buses) {

        var operationalBuses = buses.stream()
                .filter(bus -> bus.eBusOperationalStatus()
                        .equals(OPERATIONAL))
                .sorted(sortByBusName())
                .toList();

        var nonOperationalBuses = buses.stream()
                .filter( bus -> !bus.eBusOperationalStatus().equals(OPERATIONAL))
                .sorted(sortByBusName())
                .toList();

        var result= new ArrayList<BusAdminViewResponse>();
        result.addAll(operationalBuses);
        result.addAll(nonOperationalBuses);

        return result;
    }


    private static Comparator<BusAdminViewResponse> sortByBusName() {
        return Comparator.comparing(BusAdminViewResponse::busName);
    }

}
