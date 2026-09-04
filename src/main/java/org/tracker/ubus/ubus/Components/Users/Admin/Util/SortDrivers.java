package org.tracker.ubus.ubus.Components.Users.Admin.Util;

import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverActiveResponseDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public abstract class SortDrivers {


    public static Collection<DriverActiveResponseDTO> sort(Collection<DriverActiveResponseDTO> drivers) {

        var assignedDrivers = drivers.stream()
                .filter(DriverActiveResponseDTO::isAssigned)
                .sorted(Comparator.comparing(DriverActiveResponseDTO::firstName))
                .toList();

        var unassignedDrivers = drivers.stream()
                .filter(driver -> !driver.isAssigned())
                .sorted(Comparator.comparing(DriverActiveResponseDTO::firstName))
                .toList();

        var responses = new ArrayList<DriverActiveResponseDTO>();
        responses.addAll(assignedDrivers);
        responses.addAll(unassignedDrivers);
        return responses;
    }
}
