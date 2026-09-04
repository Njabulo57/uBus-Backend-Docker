package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Request.BusConcernRequest;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.AllBusOperationalHistoriesWrapper;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.BusOperationHistoryResponse;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Entity.BusOperationalHistory;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.Priority;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class BusOperationalMapper {

    //formatter for date and Time. eg
    private static final String defaultMessage = "Reported: ";
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofPattern("EEEE, MMMM d, yyyy");

    private static final int LIMIT = 6;

    public BusOperationalHistory toEntity(BusConcernRequest busConcernRequest, Bus bus) {

        var today = LocalDate.now();
        return BusOperationalHistory.builder()
                .bus(bus)
                .maintenanceIssue(busConcernRequest.issue())
                .priority(busConcernRequest.priority())
                .description(busConcernRequest.description())
                .dateOperated(today)
                .build();
    };

    public AllBusOperationalHistoriesWrapper toDTOs(Page<BusOperationalHistory> mostRecentHistories) {

        var busIssuesGroupedByBus = getHistoriesGroupedByBuses(mostRecentHistories.getContent());

        List<BusOperationHistoryResponse> results = new ArrayList<>();

        for (var entry : busIssuesGroupedByBus.entrySet()) {
            var bus = entry.getKey();
            var busIssues = entry.getValue();
            busIssues.sort(Comparator.comparing(BusOperationalHistory::getDateOperated)
                    .reversed());
            for (var busIssue : busIssues) {
                if (busIssue.getPriority() == null) continue;
                if (busIssue.getDescription() == null) continue;
                if (busIssue.getMaintenanceIssue() == null) continue;

                var formattedDate = getFormattedDateAsLocalDate(busIssue);
                var message = defaultMessage + formattedDate + " For Bus " + bus.getName();
                var description = "Issue: " + busIssue.getDescription();

                var response = BusOperationHistoryResponse.builder()
                        .message(message)
                        .description(description)
                        .busName(bus.getName())
                        .date(formattedDate)
                        .busId(bus.getId())
                        .priority(busIssue.getPriority())
                        .issue(busIssue.getMaintenanceIssue())
                        .build();

                results.add(response);
            }
        }

        return AllBusOperationalHistoriesWrapper.builder()
                .busOperationHistoryResponses(results)
                .totalPages(mostRecentHistories.getTotalPages())
                .currentPage(mostRecentHistories.getNumber())
                .pageSize(mostRecentHistories.getSize())
                .totalElements((int) mostRecentHistories.getTotalElements())
                .build();
    }

    public Collection<BusOperationHistoryResponse> toDTOs(List<BusOperationalHistory> mostRecentHistories) {

        var busIssuesGroupedByBus = getHistoriesGroupedByBuses(mostRecentHistories);

        List<BusOperationHistoryResponse> results = new ArrayList<>();

        for (var entry : busIssuesGroupedByBus.entrySet()) {
            var bus = entry.getKey();
            var busIssues = entry.getValue();
            busIssues.sort(Comparator.comparing(BusOperationalHistory::getDateOperated)
                    .reversed());
            for (var busIssue : busIssues) {
                if (busIssue.getPriority() == null) continue;
                if (busIssue.getDescription() == null) continue;
                if (busIssue.getMaintenanceIssue() == null) continue;

                var formattedDate = getFormattedDateAsLocalDate(busIssue);
                var message = defaultMessage + formattedDate + " For Bus " + bus.getName();
                var description = "Issue: " + busIssue.getDescription();

                var response = BusOperationHistoryResponse.builder()
                        .message(message)
                        .description(description)
                        .busName(bus.getName())
                        .date(formattedDate)
                        .busId(bus.getId())
                        .priority(busIssue.getPriority())
                        .issue(busIssue.getMaintenanceIssue())
                        .build();

                results.add(response);
            }
        }

        results = sortHistoriesByPriority(results);

        return results;
    }


    private List<BusOperationHistoryResponse> sortHistoriesByPriority(List<BusOperationHistoryResponse> results) {


        var priorityOrder = new HashMap<Priority, Integer>();
        priorityOrder.put(Priority.MINOR, 3);
        priorityOrder.put(Priority.MAJOR, 2);
        priorityOrder.put(Priority.CRITICAL, 1);

        results.sort((r1, r2) -> {
            int p1 = priorityOrder.getOrDefault(r1.priority(), 3);
            int p2 = priorityOrder.getOrDefault(r2.priority(), 3);
            return Integer.compare(p1, p2);
        });

        //we return only that number of entries
        if (results.size() > LIMIT)
            results = results.subList(0, LIMIT);

        return results;
    }

    private String getFormattedDateAsString(BusOperationalHistory busOperationalHistory) {
        return busOperationalHistory.getDateOperated()
                .format(dateFormatter);
    }

    private Map<Bus, List<BusOperationalHistory>> getHistoriesGroupedByBuses(Collection<BusOperationalHistory> histories) {
        return histories.stream()
                .collect(Collectors.groupingBy(BusOperationalHistory::getBus));
    }

    private LocalDate getFormattedDateAsLocalDate(BusOperationalHistory busOperationalHistory) {
        return dateFormatter.parse(getFormattedDateAsString(busOperationalHistory),
                LocalDate::from);
    }

}
