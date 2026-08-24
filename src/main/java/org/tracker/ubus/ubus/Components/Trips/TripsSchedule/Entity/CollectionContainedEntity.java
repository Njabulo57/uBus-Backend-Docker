package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity;

public interface CollectionContainedEntity<T> {

    void add(T entity);

    void addAll(Iterable<T> entities);

    void remove(T entity);

    void removeAll();
}
