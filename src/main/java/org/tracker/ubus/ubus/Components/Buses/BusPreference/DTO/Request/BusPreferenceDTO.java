package org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BusPreferenceDTO {

    @NotBlank(message = "From location cannot be blank")
    String from;

    @NotBlank(message = "To location cannot be blank")
    String to;


    String oldFrom;
    String oldTo;
}


