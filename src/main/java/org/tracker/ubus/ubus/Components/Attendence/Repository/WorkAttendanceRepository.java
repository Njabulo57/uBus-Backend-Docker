package org.tracker.ubus.ubus.Components.Attendence.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Attendence.Entity.WorkAttendance;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkAttendanceRepository extends JpaRepository<WorkAttendance, UUID> {


    @Query("""
        SELECT CASE WHEN COUNT(wa) > 0 THEN true ELSE false END
        FROM WorkAttendance  wa
        WHERE wa.driver =:driver
        AND DATE(wa.createdAt) =:date
    """)
    boolean existsByDriverAtDate(@Param("driver") User driver,
                                 @Param("date") LocalDate date);


    boolean existsByDriverAndSignedAtBetween(User user, LocalDateTime rangeStart, LocalDateTime rangeEnd);

    @Query("""
        SELECT wa FROM WorkAttendance wa
        LEFT JOIN FETCH wa.driver
        WHERE wa.driver = :driver
        AND wa.signedAt BETWEEN :from AND :to
""")
    List<WorkAttendance> findByDriverAndSignedAtBetween(
            @Param("driver") User driver,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT wa FROM WorkAttendance wa
            LEFT JOIN FETCH wa.driver
        WHERE wa.signedAt BETWEEN :from AND :to
        """)
    List<WorkAttendance> findSignedAtBetween(LocalDateTime from, LocalDateTime to);
}

