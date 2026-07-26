package org.tracker.ubus.ubus.Components.Trips.TripCalendarRule.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trips.TripCalendarRule.Entity.CalendarRule;

import java.util.UUID;

@Repository
public interface CalendarRuleRepository extends JpaRepository<CalendarRule, UUID> {
}
