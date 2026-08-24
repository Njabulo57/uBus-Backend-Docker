package org.tracker.ubus.ubus.Configuration.ExternalClients.GoogleMapsService.Config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;
import org.tracker.ubus.ubus.Configuration.ExternalClients.GoogleMapsService.API.GoogleMapsAPI;


@Configuration(proxyBeanMethods = false)
@ImportHttpServices({GoogleMapsAPI.class})
public class GoogleMapsClientConfig {



}
