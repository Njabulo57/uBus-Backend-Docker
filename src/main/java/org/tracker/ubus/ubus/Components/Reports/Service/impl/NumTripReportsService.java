package org.tracker.ubus.ubus.Components.Reports.Service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Reports.Service.interfece.INumTripReportsService;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.ReportsResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NumTripReportsService implements INumTripReportsService {


    private final TripRepository tripRepository;
    @Override
    public ReportsResponse numTrips(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        return ReportsResponse.builder().key("Number of Trips").value(tripRepository.countAllWithScheduleFetched(dateTime)).valueUnits("trips").build();
    }

    @Override
    public List<ReportsResponse> numTripsByStatus(ReportsTime reportsTime) {
       LocalDateTime dateTime = getDateTime(reportsTime);
       List<ReportsResponse> reportsResponseList = new ArrayList<>();
       for (TripStatus status : TripStatus.values()) {
           int count = tripRepository.countAllWithScheduleFetchedAndStatus(dateTime, status);
           reportsResponseList.add(ReportsResponse.builder().key(status.name()).value(count).valueUnits("trips").build());
       }
       return reportsResponseList;
    }

    @Override
    public List<ReportsResponse> numTripsByTimeIntervals(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        List<ReportsResponse> reportsResponseList = new ArrayList<>();

        switch (reportsTime) {
            case DAY: {
                LocalDateTime now = LocalDateTime.now().minusDays(1);
                for(int n = 6; n < 22; n++)
                {
                    int numTrips = Math.toIntExact(tripRepository.countByDepartureTimeBetween(
                            LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), n, 0),
                            LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), n+1, 0)));
                    reportsResponseList.add(ReportsResponse.builder().key(LocalTime.of(n, 0).toString()).value(numTrips).valueUnits("trips").build());
                }
                break;
            }
            case WEEK:{
                LocalDate now = LocalDate.now().minusWeeks(1);
                for(int n = 0; n < 7; n++)
                {
                    int numTrips = Math.toIntExact(tripRepository.countByDepartureTimeBetween(
                            now.plusDays(n).atStartOfDay(),
                            now.plusDays(n).atTime(23,59)));
                    reportsResponseList.add(ReportsResponse.builder().key(LocalDate.now().plusDays(n).getDayOfWeek().toString()).value(numTrips).valueUnits("trips").build());
                }
                break;
            }
            case MONTH: {
                LocalDate now = LocalDate.now().minusWeeks(4);
                for(int n = 0; n < 4; n++)
                {
                    int numTrips = Math.toIntExact(tripRepository.countByDepartureTimeBetween(
                            now.plusWeeks(n).atStartOfDay(),
                            now.plusWeeks(n).plusDays(6).atTime(23,59)));
                    reportsResponseList.add(ReportsResponse.builder().key("WEEK: " + (n+1)).value(numTrips).valueUnits("trips").build());
                }
                break;
            }
            case YEAR: {
                LocalDate now = LocalDate.now().minusYears(1);
                for(int n = 0; n < 12; n++)
                {
                    int numTrips = Math.toIntExact(tripRepository.countByDepartureTimeBetween(
                            now.plusMonths(n).atStartOfDay(),
                            now.plusMonths(n+1).minusDays(1).atTime(23,59)));
                    reportsResponseList.add(ReportsResponse.builder().key(now.getMonth().plus(n).toString()).value(numTrips).valueUnits("trips").build());
                }
                break;
            }
            case ALL_TIME: {
                LocalDate now = tripRepository.getFirstDepartureDate();
                now = LocalDate.of(now.getYear(), 1,1);

                for(int n = 0; n < LocalDate.now().getYear() - now.getYear() +1; n++)
                {
                    int numTrips = Math.toIntExact(tripRepository.countByDepartureTimeBetween(
                            now.plusYears(n).atStartOfDay(),
                            now.plusYears(n+1).minusDays(1).atTime(23,59)));
                    reportsResponseList.add(ReportsResponse.builder().key(String.valueOf(now.plusYears(n).getYear())).value(numTrips).valueUnits("trips").build());
                }
                break;
            }
        }
        return reportsResponseList;
    }


    @Override
    public List<ReportsResponse> numTripsByRouteAndStatus(ReportsTime reportsTime) {
        LocalDateTime dateTime = getDateTime(reportsTime);
        List<ReportsResponse> reportsResponseList = new ArrayList<>();

        for(Route route: Route.values())
        {
            for(TripStatus tripStatus: TripStatus.values())
            {
                int numTrips = tripRepository.countAllWithScheduleFetchedAndStatusAndRoute(dateTime, tripStatus, route);
                reportsResponseList.add(ReportsResponse.builder().key(route.getLabel() + " " + tripStatus.name()).value(numTrips).valueUnits("trips").build());
            }
        }
        return reportsResponseList;
    }


    private LocalDateTime getDateTime(ReportsTime reportsTime) {
        LocalDateTime dateTime = LocalDateTime.now();
        switch (reportsTime) {
            case DAY: {
                dateTime = dateTime.minusDays(1);
                break;
            }
            case MONTH: {
                dateTime = dateTime.minusMonths(1);
                break;
            }
            case YEAR: {
                dateTime = dateTime.minusYears(1);
                break;
            }
            case ALL_TIME: {
                dateTime = null;
                break;
            }
        }
        return dateTime;
    }
}
