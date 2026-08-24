package org.tracker.ubus.ubus.Configuration.ExternalClients.GoogleMapsService.Service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Configuration.ExternalClients.GoogleMapsService.API.GoogleMapsAPI;

@Component
@RequiredArgsConstructor
public class GoogleMapsService {

    private final GoogleMapsAPI googleMapsAPI;
}
