package org.tracker.ubus.ubus.Components.Audit.Enum;


import lombok.AllArgsConstructor;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

@AllArgsConstructor
public enum AuditType {


    ADMIN_DRIVER_APPROVAL("Driver %s %s account approved by %s %s on %s"),

    ADMIN_DRIVER_BUS_ASSIGNMENT("Driver %s was assigned bus %s by %s"),
    ADMIN_DRIVER_BUS_UNASSIGNMENT("Driver %s was unassigned from bus %s by %s"),


    STUDENT_EMAIL_APPROVAL("");










    private final String message;


    public String adminDriverApprovalMsg(User createdBy, User createdOn, String formattedDateTime) {
        String creatorFistName = createdBy.getFirstname();
        String creatorLastName = createdBy.getLastname();

        String createdOnFistName = createdOn != null ?  createdOn.getFirstname() : "";
        String createdOnLastName = createdOn != null ?  createdOn.getLastname() : "";

        return String.format(message,
                createdOnFistName,
                createdOnLastName,

                creatorFistName,
                creatorLastName,

                formattedDateTime);
    }

    public String adminDriverBusAssignmentMsg(User driver, String busName, User admin) {

        String adminFullName = admin.getFirstname() + " " + admin.getLastname();
        String driverFullName = driver.getFirstname() + " " + driver.getLastname();

        return String.format(message, driverFullName
                ,busName,
                adminFullName);
    }

    public String adminDriverBusUnAssignmentMsg(User driver, String busName, User admin) {

        String adminFullName = admin.getFirstname() + " " + admin.getLastname();
        String driverFullName = driver.getFirstname() + " " + driver.getLastname();
        return String.format(message,
                driverFullName,
                busName,
                adminFullName);
    }


}
