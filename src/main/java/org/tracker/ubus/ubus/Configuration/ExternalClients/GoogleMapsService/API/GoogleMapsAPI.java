package org.tracker.ubus.ubus.Configuration.ExternalClients.GoogleMapsService.API;


import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "https://maps.googleapis.com/maps/api/directions/json", accept = "application/json")
public interface GoogleMapsAPI {

}
