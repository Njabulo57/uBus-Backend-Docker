package org.tracker.ubus.ubus.Components.User.Enum;

import lombok.Getter;

@Getter
public enum UserBusPreference {


    SWC_DFC("Soweto Campus to Doornfontein Campus", "SWC", "DFC"),
    DFC_SWC("Doornfontein Campus to Soweto Campus", "DFC", "SWC"),
    APK_APB("Auckland Park Kingsway Campus to Auckland Park Bunting Road Campus", "APK", "APB"),
    APB_APK("Auckland Park Bunting Road Campus to Auckland Park Kingsway Campus", "APB", "APK"),
    APB_DFC("Auckland Park Bunting Road Campus to Doornfontein Campus", "APB", "DFC"),
    DFC_APB("Doornfontein Campus to Auckland Park Bunting Road Campus", "DFC", "APB"),
    APK_SWC("Auckland Park Kingsway Campus to Soweto Campus", "APK", "SWC"),
    SWC_APK("Soweto Campus to Auckland Park Kingsway Campus", "SWC", "APK");

    private final String label;
    private final String fromAbbr;
    private final String toAbbr;

    UserBusPreference(String label, String fromAbbr, String toAbbr) {
        this.label = label;
        this.fromAbbr = fromAbbr;
        this.toAbbr = toAbbr;
    }

}