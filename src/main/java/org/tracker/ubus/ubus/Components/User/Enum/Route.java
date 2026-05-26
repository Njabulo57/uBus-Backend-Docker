package org.tracker.ubus.ubus.Components.User.Enum;

import lombok.Getter;

@Getter
public enum Route {


    SWC_DFC("Soweto Campus to Doornfontein Campus"),
    DFC_SWC("Doornfontein Campus to Soweto Campus"),
    APK_APB("Auckland Park Kingsway Campus to Auckland Park Bunting Road Campus"),
    APB_APK("Auckland Park Bunting Road Campus to Auckland Park Kingsway Campus"),
    APB_DFC("Auckland Park Bunting Road Campus to Doornfontein Campus"),
    DFC_APB("Doornfontein Campus to Auckland Park Bunting Road Campus"),
    APK_SWC("Auckland Park Kingsway Campus to Soweto Campus"),
    SWC_APK("Soweto Campus to Auckland Park Kingsway Campus");

    private final String label;

    Route(String label) {
        this.label = label;
    }

}