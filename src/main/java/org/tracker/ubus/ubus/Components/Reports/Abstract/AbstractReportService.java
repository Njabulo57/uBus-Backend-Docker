package org.tracker.ubus.ubus.Components.Reports.Abstract;

import lombok.RequiredArgsConstructor;
import org.tracker.ubus.ubus.Components.Reports.DTO.Request.ReportingFilterRequest;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public abstract class AbstractReportService {

    protected LocalDateTime getTimeRangeByReportingFilter(ReportsTime reportsTime) {


        var currentDate = LocalDateTime.now();
        return switch (reportsTime) {
            case NOW -> currentDate;
            case DAY -> currentDate.minusDays(1);
            case WEEK -> currentDate.minusWeeks(1);
            case MONTH -> currentDate.minusMonths(1);
            case QUARTER -> currentDate.minusMonths(4);
            case HALF_YEAR -> currentDate.minusMonths(6);
            case YEAR -> currentDate.minusYears(1);
            case ALL_TIME -> currentDate.minusYears(10); // future proof lol
        };
    }



}
