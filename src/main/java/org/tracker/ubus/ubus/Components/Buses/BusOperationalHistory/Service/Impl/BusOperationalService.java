package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Internal.TimeFilterer;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Request.BusConcernRequest;

import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.AllBusOperationalHistoriesWrapper;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.BusAllOperationHistoryResponse;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.BusOperationHistoryResponse;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Entity.BusOperationalHistory;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Repository.BusOperationalHistoryRepository;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Service.Interface.IBusOperationalService;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Mapper.BusOperationalMapper;
import org.tracker.ubus.ubus.Components.Users.Driver.Abstract.BaseDriverService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BusOperationalService extends BaseDriverService implements IBusOperationalService {

    private final BusRepository busRepository;
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
    public AllBusOperationalHistoriesWrapper getBusConcerns(TimeFilterer timeFilterer,
                                                            PageRequest page, boolean resolved) {

        var from = timeFilterer.start();
        var to = timeFilterer.end();

        Page<BusOperationalHistory> histories;

        if(!resolved)
            histories = this.busOperationalHistoryRepository
                .findByDateOperatedBetweenNotResolved(from, to, page);
        else
            histories = this.busOperationalHistoryRepository.findByDateOperatedBetweenResolved(from, to, page);

        return this.busOperationalMapper.toDTOs(histories);
    }


    @Override
    @Transactional
    public void resolveIssue(UUID busOperationalId) {
        var busConcern = this.busOperationalHistoryRepository
                .findByIdOrThrow(busOperationalId);

        var today = LocalDate.now();
        busConcern.setDateResolved(today); //resolved today

        var bus = busConcern.getBus();
        bus.setOperationalStatus(BusOperationalStatus.OPERATIONAL);

        this.busRepository.save(bus);
        this.busOperationalHistoryRepository.save(busConcern);
    }

}
