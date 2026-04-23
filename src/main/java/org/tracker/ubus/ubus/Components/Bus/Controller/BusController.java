package org.tracker.ubus.ubus.Components.Bus.Controller;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

@RestController
@RequestMapping("/busses")
public class BusController {


    @GetMapping("/view")
    public String view(Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return "I'm looking at a bus. " +authentication.getName() + ". Role: " + principal.getRole() ;
    }

}
