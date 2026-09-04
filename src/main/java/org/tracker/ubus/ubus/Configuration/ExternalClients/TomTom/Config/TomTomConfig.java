package org.tracker.ubus.ubus.Configuration.ExternalClients.TomTom.Config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;
import org.tracker.ubus.ubus.Configuration.ExternalClients.TomTom.API.TomTomAPI;

@ImportHttpServices({TomTomAPI.class})
@Configuration(proxyBeanMethods = false)
public class TomTomConfig {
}
