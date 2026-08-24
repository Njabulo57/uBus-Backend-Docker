package org.tracker.ubus.ubus.Components.Reports.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;

public record ReportsRequest(@NotBlank(message = "Time frame is required")
                             ReportsTime timeFrame) {
}
