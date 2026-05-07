package org.tracker.ubus.ubus.Components.User.Enum;


import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum UserRole {

    ADMIN("Admin", "ROLE_Admin"),
    STUDENT("Student", "ROLE_Student"),
    STAFF("Staff", "ROLE_Staff"),
    DRIVER("Driver", "ROLE_Driver");

    private final String label;
    private final String securityRole;

    UserRole(String label, String securityLabel) {
        this.label = label;
        this.securityRole = securityLabel;
    }

    /**
     * converts a label to its corresponding UserRole value.
     * It looks whilst ignoring cases to provide a balance of
     * leniency and security
     * @param label the label to convert
     * @return the UserRole corresponding to the given label
     * @throws IllegalArgumentException if no UserRole with the given label exists
     */
    public static UserRole fromLabel(String label) throws IllegalArgumentException {
        return Stream.of(UserRole.values())
                .filter(role -> role.getLabel().equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No UserRole with label " + label));
    }
}
