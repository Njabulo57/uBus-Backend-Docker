package org.tracker.ubus.ubus.Configuration.HttpClient.DTOs.Request;


import java.util.List;

public interface GetDirectionAPIResponses {

    record Geometry(List<double[]> coordinates) {}

    record Feature(Geometry geometry) {}

    record RouteResponse(Feature[] features) {}

}
