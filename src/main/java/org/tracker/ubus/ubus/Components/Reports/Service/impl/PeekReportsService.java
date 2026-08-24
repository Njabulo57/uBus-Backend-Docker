package org.tracker.ubus.ubus.Components.Reports.Service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Reports.Abstract.AbstractReportService;
import org.tracker.ubus.ubus.Components.Reports.DTO.Request.ReportingFilterRequest;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.PeekResponseWrapper;
import org.tracker.ubus.ubus.Components.Reports.Mapper.ReportsMapper;
import org.tracker.ubus.ubus.Components.Reports.Service.interfece.IPeekReportsService;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripReportsRepository;

import java.time.LocalDate;
import static org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus.COMPLETE;


@Slf4j
@Service
@RequiredArgsConstructor
public class PeekReportsService extends AbstractReportService implements IPeekReportsService {

    private static final double PEEK_HOURS_THRESHOLD = 0.85;

    private final ReportsMapper reportsMapper;
    private final TripReportsRepository tripReportsRepository;



    @Override
    public PeekResponseWrapper getHourlyPeeks(ReportingFilterRequest request) {

        var currentTIme = System.currentTimeMillis();

        var endDate = LocalDate.now();
        var timeRange = this.getTimeRangeByReportingFilter(request.timeFrame()); //getting the time range
        var timeRangeDate = timeRange.toLocalDate();

        var routeChosen = request.route();
        var completedTripsInRange = this.tripReportsRepository.findByRouteAndDateRange(routeChosen,
                timeRangeDate, endDate,
                COMPLETE);

        var endProcessTime = System.currentTimeMillis();
        var timeInSecs = (endProcessTime - currentTIme) / 1000.0;
        log.info("Time taken to fetch trips: {} s",  timeInSecs);


        return this.reportsMapper.toDTO(completedTripsInRange, PEEK_HOURS_THRESHOLD);

    }
}
