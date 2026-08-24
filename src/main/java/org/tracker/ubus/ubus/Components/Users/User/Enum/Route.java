package org.tracker.ubus.ubus.Components.Users.User.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.DestinationUserNameProvider;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPrefView;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination.*;

@Getter
@RequiredArgsConstructor
public enum Route {

    ROUTE_1("DFC-APK",
            List.of(DFC, APB, APK, APB, DFC), false, "Route 1"),
    ROUTE_2("SWC-APB",
            List.of(SWC, APK, APB, APK, SWC), false, "Route 2"),

    ROUTE_3("SWC-DFC",
            List.of(SWC, DFC), true, "Route 3"),

    ROUTE_JBS("APK-JBS",
              List.of(APK, APB, JBS, APB, APK), false, "Route JBS");



    private final String label;
    private final List<Destination> destinations;
    private final boolean isDirect;
    private final String formattedName;

    public static final List<Route> ALL_ROUTES = List.of(ROUTE_1, ROUTE_2, ROUTE_3, ROUTE_JBS);
    public static final List<Route> DIRECT_ROUTES = Collections.singletonList(ROUTE_3);
    public static final List<Route> NON_DIRECT_ROUTES = List.of(ROUTE_1, ROUTE_2, ROUTE_JBS);


    public List<Destination> getUniqueStops() {
        return this.destinations.stream()
                .distinct()
                .toList();
    }

    /**
     * Finds and retrieves all routes that contain the specified collection of destinations.
     * Throws an IllegalArgumentException if no such route exists.
     *
     * @param destinations the collection of destinations to find within the route.
     *                     Each destination in the specified collection must be present in the route.
     * @return a collection of routes that match the specified destinations.
     * @throws IllegalArgumentException if no route is found containing all specified destinations.
     */
    public static Collection<Route> findRouteByDestinations(Collection<Destination> destinations) throws IllegalArgumentException{
        Predicate<Route> isContained = route -> new HashSet<>(route.getDestinations())
                .containsAll(destinations);

        var list = Stream.of(values())
                .filter(isContained)
                .toList();

        if(list.isEmpty())
            throw new IllegalArgumentException("No route found for destinations " + destinations);
        return list;
    }

    public static Collection<Route> findByDestinations(Destination to, Destination from) throws IllegalArgumentException{
        return findRouteByDestinations(List.of(to, from));
    }

    public static Collection<Route> findRouteByDestinations(Destination... destinations) throws IllegalArgumentException{
        return findRouteByDestinations(List.of(destinations));
    }


    public static int getFromDestinationIndex(Route route, Destination from, Destination to ) {

        var destinations = route.getDestinations(); //getting the destinations of the route

        for(int toIndex = destinations.size() - 1; toIndex >= 0; toIndex--) {
            if(destinations.get(toIndex) == to) //found a occurrence
                for(int i = toIndex -1; i >= 0; i--)
                    if(destinations.get(i).equals(from))
                        return i; //we return the index of the from destination

        }

        return -1;
    }


    public static int getNextDestinationIndex(int currentDestIndex, Route route) {
        return (currentDestIndex + 1) % route.getDestinations().size();
    }

    public static Collection<BusPrefView> getAllValidDestinationCombinations() {
        var straightLineRoutes = List.of(ROUTE_1, ROUTE_2, ROUTE_JBS);

        return straightLineRoutes.stream()
                .flatMap(route -> route.getUniqueStops().stream()
                        .flatMap(from -> route.getUniqueStops().stream()
                                .filter(to -> !to.equals(from))
                                .map(to -> new BusPrefView(from, to))
                        )
                )
                .collect(Collectors.toList());
    }


    public static Route findRouteByLabel(String label) {
        return ALL_ROUTES.stream()
                .filter(route -> route.getLabel().equalsIgnoreCase(label))
                .findFirst()
                .orElse(null);
    }


}