package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Service.Interface;

import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Request.BusConcernRequest;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.BusAllOperationHistoryResponse;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.BusOperationHistoryResponse;

import java.util.Collection;

public interface IBusOperationalService {

    void addConcern(BusConcernRequest busConcernRequest);


    Collection<BusOperationHistoryResponse> getMostRecentConcerns();
    Collection<BusAllOperationHistoryResponse> getBusConcerns();

}


