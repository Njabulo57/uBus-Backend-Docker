package org.tracker.ubus.ubus.Configuration.HttpClient;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;
import org.tracker.ubus.ubus.Configuration.HttpClient.API.RouteAPI;


@ImportHttpServices({RouteAPI.class})
@Configuration(proxyBeanMethods = false)
public class HttpClientConfig {



}
