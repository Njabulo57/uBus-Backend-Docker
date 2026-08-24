package org.tracker.ubus.ubus.Components.Reports.Service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Entity.BusOperationalHistory;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.MaintenanceIssue;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.Priority;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Repository.BusOperationalHistoryRepository;
import org.tracker.ubus.ubus.Components.Reports.Abstract.AbstractReportService;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.Bus.BusFleetWrapper;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.Bus.BusOperationalResponse;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Reports.Mapper.ReportsMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus.*;


@Service
@RequiredArgsConstructor
public class BusOperationalHistoryReportsService extends AbstractReportService {

    private static final int SCORE_OPEN_ISSUES = 3;
    private static final int SCORE_CRITICAL_ISSUES = 14;

    private final BusRepository busRepository;
    private final ReportsMapper reportsMapper;
    private final BusOperationalHistoryRepository busOperationalHistoryRepository;


    public BusFleetWrapper getBusFleetOperationalAnalytics(ReportsTime reportsTime) {

        var startTime = this.getTimeRangeByReportingFilter(reportsTime);
        var startOfDayOfStartTime = startTime
                .toLocalDate();

        var endDateTime = LocalDateTime.now();
        var endTimeDate = endDateTime.toLocalDate();

        //getting all the bus histories by the required time
        var busHistories = this.busOperationalHistoryRepository.findByDateOperatedBetween(startOfDayOfStartTime,
                endTimeDate);

        var busesFromHistory = this.getBusesFromHistoryRecords(busHistories); //getting all buses from the history records

        var mostUpToDateBusVersions = this.busRepository.findInAndIsActiveTrue(busesFromHistory);
        var busesGroupedByBus = this.getHistoriesGroupedByBuses(busHistories);


        var busesResponse = new ArrayList<BusOperationalResponse>();

        for (var busEntry : busesGroupedByBus.entrySet()) {

            var bus = busEntry.getKey();
            var mostUpToDateBusVersion = this.getBusMatchingHistoryAndUpdatedBus(bus, mostUpToDateBusVersions);
            var histories = busEntry.getValue();

            if (histories.isEmpty()) continue;


            var openIssues = this.getOpenIssuesCount(histories, OPEN_ISSUE_TYPE.ALL_ISSUES_OPEN); //getting all open issues for the bus
            var openCriticalIssues = this.getOpenIssuesCount(histories, OPEN_ISSUE_TYPE.CRITICAL_OPEN); //getting all critical issues for the bus
            var mostCommonBusIssue = this.getMostCommonMaintenanceIssueForBus(histories);
            var lastReported = histories
                    .getLast()
                    .getDateOperated();

            var healthScore = getBusHealthScore(histories);
            var operationalStatus = mostUpToDateBusVersion.getOperationalStatus();
            var daysSinceLastReport = getDaysSinceReport(lastReported);
    ;

            var action = getRecommendedAction(healthScore, openCriticalIssues,
                    daysSinceLastReport, operationalStatus);

            var response = this.reportsMapper.toBusOperationalResponse(bus, openIssues,
            openCriticalIssues, mostCommonBusIssue, lastReported,
            action, healthScore, mostUpToDateBusVersion);

            busesResponse.add(response);
        }

        var totalBuses = this.busRepository.countByIsActiveTrue();

        var operationalStatuses = List.of(OPERATIONAL, MAINTENANCE);
        var totalAvailable = this.busRepository.countByOperationalStatusInAndIsActiveTrue(operationalStatuses);
        var totalCriticalConditionBuses = getBusesCriticalCount(busesGroupedByBus);

        var mostCommonBusIssue = this.getMostCommonMaintenanceIssueForBus(busHistories);
        var top3CommonIssues = this.getTop3CommonMaintenanceIssues(busHistories);


        busesResponse
                .sort(Comparator.comparing(BusOperationalResponse::busName));
        return this.reportsMapper.toBusFleetWrapper(totalBuses, totalAvailable,
                totalCriticalConditionBuses, mostCommonBusIssue,
                top3CommonIssues,busesResponse);
    }


    private Map<Bus, List<BusOperationalHistory>> getHistoriesGroupedByBuses(Collection<BusOperationalHistory> histories) {
        return histories.stream()
                .collect(Collectors.groupingBy(BusOperationalHistory::getBus,
                        LinkedHashMap::new,
                        Collectors.toList())
                ); //preserving the order of insertion from the database
    }

    private int getOpenIssuesCount(Collection<BusOperationalHistory> historiesForBus, OPEN_ISSUE_TYPE issueType) {

        var count = 0L;

        if(issueType == OPEN_ISSUE_TYPE.ALL_ISSUES_OPEN) //we take all issues not fixed yet
          count = historiesForBus.stream()
                    .filter(boh -> boh.getDateResolved() == null)
                    .count();

        else if(issueType == OPEN_ISSUE_TYPE.CRITICAL_OPEN)
            count = historiesForBus.stream()
                    .filter(boh -> boh.getDateResolved() == null)
                    .filter(boh -> boh.getPriority() == Priority.CRITICAL)
                    .count();
        else
            count = historiesForBus.stream() //getting all critical issues for the bus regardless of fixing
                    .filter(boh -> boh.getPriority() == Priority.CRITICAL)
                    .count();

        return Math.toIntExact(count);
    }


    private int getBusHealthScore(Collection<BusOperationalHistory> historiesForBus) {

        var score = 100;

        var openCount = this.getOpenIssuesCount(historiesForBus, OPEN_ISSUE_TYPE.ALL_ISSUES_OPEN);

        var criticalCount = this.getOpenIssuesCount(historiesForBus, OPEN_ISSUE_TYPE.CRITICAL_OPEN);

        score -= openCount * SCORE_OPEN_ISSUES;
        score -= criticalCount * SCORE_CRITICAL_ISSUES;

        return Math.clamp(score, 0, 100);
    }

    public int getDaysSinceReport(LocalDate date) {
        var today = LocalDate.now();
        return (int) ChronoUnit.DAYS.between(date, today);
    }


    private int getBusesCriticalCount(Map<Bus, List<BusOperationalHistory>> busesGroupedByBus) {
        return (int) busesGroupedByBus.entrySet()
                .stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(h -> h.getPriority() == Priority.CRITICAL
                                && h.getDateResolved() == null)
                )
                .count();
    }

    private String getRecommendedAction(int score, int criticalCount, int daySinceLastReport,
                                        BusOperationalStatus operationalStatus) {

        if(criticalCount > 1 || operationalStatus == OUT_OF_SERVICE)
            return "Not Safe to Operate.Risk Factor too High.";
        if(operationalStatus == MAINTENANCE)
            return "Bus is undergoing maintenance.";

        if(criticalCount > 0)
            return "Critical Issue Not Resolved. Consider Repairing.";

        if(score >=80)
            return "Safe to Continue Operations";
        if(score >= 70) {

            if(daySinceLastReport < 7)
                return "Monitor Closely. Recently Reported";
            return "Schedule Maintenance.";
        }

        if(score >= 50)
            return "Consider Repairing.Multiple Issues";

        return "Not Safe to Operate.Risk Factor too High.";
    }

    private MaintenanceIssue getMostCommonMaintenanceIssueForBus(Collection<BusOperationalHistory> historiesForBus) {
        var maintenanceIssueCounter = historiesForBus.stream()
                .filter(boh -> boh.getMaintenanceIssue() != null)
                .collect(Collectors.groupingBy(BusOperationalHistory::getMaintenanceIssue,
                        Collectors.counting())
                );
        return maintenanceIssueCounter
                .entrySet()
                .stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);

    }

    private List<MaintenanceIssue> getTop3CommonMaintenanceIssues(Collection<BusOperationalHistory> histories) {
        return histories.stream()
                .filter(h -> h.getMaintenanceIssue() != null)
                .collect(Collectors.groupingBy(
                        BusOperationalHistory::getMaintenanceIssue,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<MaintenanceIssue, Long>comparingByValue() //arrange is ASC
                        .reversed() //reverse the order
                )
                .limit(3) //take the top 3
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Collection<UUID> getBusesFromHistoryRecords(Collection<BusOperationalHistory> histories) {
        return histories.stream()
                .map(BusOperationalHistory::getBus)
                .map(Bus::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    private Bus getBusMatchingHistoryAndUpdatedBus(Bus BusFromHistory, Collection<Bus> mostUpToDateBusVersions) {
        return mostUpToDateBusVersions.stream()
                .filter(bus -> bus.getId().equals(BusFromHistory.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Bus not found in the database"));
    }

    private enum OPEN_ISSUE_TYPE {
        ALL_ISSUES_OPEN,
        CRITICAL,
        CRITICAL_OPEN
    }

}
