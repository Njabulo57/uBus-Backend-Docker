package org.tracker.ubus.ubus.Configuration.ExternalClients.OpenRouteService.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;
import org.tracker.ubus.ubus.Configuration.ExternalClients.OpenRouteService.API.OpenRouteAPI;


@ImportHttpServices({OpenRouteAPI.class})
@Configuration(proxyBeanMethods = false)
public class OpenRouteServiceClientConfig {

}
