package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Request.BusConcernRequest;

import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.BusAllOperationHistoryResponse;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.BusOperationHistoryResponse;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Repository.BusOperationalHistoryRepository;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Service.Interface.IBusOperationalService;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Mapper.BusOperationalMapper;
import org.tracker.ubus.ubus.Components.Users.Driver.Abstract.BaseDriverService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BusOperationalService extends BaseDriverService implements IBusOperationalService {

    private final BusOperationalMapper busOperationalMapper;
    private final BusOperationalHistoryRepository busOperationalHistoryRepository;


    @Override
    public void addConcern(BusConcernRequest busConcernRequest) {
        var driverBus =  this.getDriverBus();

        var busConcern = this.busOperationalMapper.toEntity(busConcernRequest, driverBus);
        this.busOperationalHistoryRepository.save(busConcern);
    }

    @Override
    public Collection<BusOperationHistoryResponse> getMostRecentConcerns() {

        var today = LocalDate.now();
        var yesterday = today.minusDays(1);

        var mostRecentHistories = this.busOperationalHistoryRepository.findByDateOperatedBetween(yesterday,
                today.plusDays(1));

        return this.busOperationalMapper.toDTOs(mostRecentHistories);
    }

    @Override
    public Collection<BusAllOperationHistoryResponse> getBusConcerns() {
        return List.of();
    }

}
