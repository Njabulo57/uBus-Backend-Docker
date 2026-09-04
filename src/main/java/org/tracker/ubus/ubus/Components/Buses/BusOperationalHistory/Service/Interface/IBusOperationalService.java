package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Service.Interface;

import org.springframework.data.domain.PageRequest;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Internal.TimeFilterer;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Request.BusConcernRequest;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.AllBusOperationalHistoriesWrapper;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.BusOperationHistoryResponse;

import java.util.Collection;
import java.util.UUID;

public interface IBusOperationalService {

    void addConcern(BusConcernRequest busConcernRequest);


    Collection<BusOperationHistoryResponse> getMostRecentConcerns();
    AllBusOperationalHistoriesWrapper getBusConcerns(TimeFilterer timeFilterer, PageRequest page, boolean resolved);

    void resolveIssue(UUID busOperationalId);
}


