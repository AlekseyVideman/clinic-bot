package nelbiasoftware.clinic_bot.core.db;

import nelbiasoftware.clinic_bot.core.Entity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AppointmentMapper {

    String APPOINTMENT = "appointment";

    @Select("""
            SELECT * FROM appointment
            WHERE patient_phone_number = #{phoneNumber}
            ORDER BY start_datetime ASC
            """)
    @Results(id = APPOINTMENT, value = {
            @Result(property = Entity.Appointment.PATIENT_PHONE_NUMBER, column = "patient_phone_number", typeHandler = org.apache.ibatis.type.LongTypeHandler.class),
            @Result(property = Entity.Appointment.START_DATE_TIME, column = "start_datetime", typeHandler = OffsetDateTimeTypeHandler.class),
            @Result(property = Entity.Appointment.END_DATE_TIME, column = "end_datetime", typeHandler = OffsetDateTimeTypeHandler.class),
            @Result(property = Entity.Appointment.GOAL, column = "goal", typeHandler = org.apache.ibatis.type.StringTypeHandler.class),
            @Result(property = Entity.Appointment.MESSAGE, column = "message", typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    })
    Map<String, Object> selectByPhoneNumber(@Param("phoneNumber") Long phoneNumber);

    /**
     * Retrieves upcoming appointments scheduled within the next 14 days.
     *
     * @return list of appointments that have not yet occurred
     */
    @Select("""
            SELECT * FROM appointment
            WHERE end_datetime >= CAST(strftime('%s', 'now') AS INTEGER)
              AND end_datetime <= CAST(strftime('%s', 'now', '+${days} days') AS INTEGER)
            ORDER BY start_datetime ASC
            """)
    @ResultMap(APPOINTMENT)
    List<Map<String, Object>> selectNextAppointments(@Param("days") int days);

    /**
     * Retrieves appointments scheduled for a specific date (from start to end of the day).
     *
     * @param date the date to query (date portion extracted from OffsetDateTime)
     * @return list of appointments occurring on that date
     */
    @Select("""
            SELECT * FROM appointment
            WHERE start_datetime >= #{date, typeHandler = nelbiasoftware.clinic_bot.core.db.OffsetDateTimeTypeHandler}
              AND end_datetime <= unixepoch(datetime(#{date, typeHandler = nelbiasoftware.clinic_bot.core.db.OffsetDateTimeTypeHandler}, 'unixepoch', '+1 days'))
            ORDER BY start_datetime ASC
            """)
    @ResultMap(APPOINTMENT)
    List<Map<String, Object>> selectAppointmentsByDate(@Param("date") java.time.OffsetDateTime date);

    /**
     * Get overlapping Appointments by comparing start and end time. <br>
     * Checks intersection by exact match of start/end and within the interval.
     *
     * @return list of overlapping Appointments
     */
    @Select("""
            SELECT * FROM appointment
            WHERE start_datetime = #{appointment.startDateTime}
               OR end_datetime = #{appointment.endDateTime}
               OR start_datetime = #{appointment.endDateTime}
               OR end_datetime = #{appointment.startDateTime}
               OR (start_datetime < #{appointment.endDateTime}
                 AND end_datetime > #{appointment.startDateTime})
            ORDER BY start_datetime ASC
            """)
    @ResultMap(APPOINTMENT)
    List<Map<String, Object>> selectOverlappedAppointments(@Param("appointment") Map<String, Object> appointment);

    @Insert("""
            INSERT INTO appointment (patient_phone_number, start_datetime, end_datetime, goal, message)
            VALUES (
                #{appointment.patientPhoneNumber, typeHandler = org.apache.ibatis.type.LongTypeHandler},
                #{appointment.startDateTime, typeHandler = nelbiasoftware.clinic_bot.core.db.OffsetDateTimeTypeHandler},
                #{appointment.endDateTime, typeHandler = nelbiasoftware.clinic_bot.core.db.OffsetDateTimeTypeHandler},
                #{appointment.goal, typeHandler = org.apache.ibatis.type.StringTypeHandler},
                #{appointment.message, typeHandler = org.apache.ibatis.type.StringTypeHandler}
            )
            """)
    void insertAppointment(@Param("appointment") Map<String, Object> appointment);

    @Delete("""
            DELETE FROM appointment
            WHERE patient_phone_number = #{phoneNumber}
            """)
    void deleteAppointment(@Param("phoneNumber") Long phoneNumber);

    @Select("""
            SELECT COUNT(*) > 0 FROM appointment
            WHERE patient_phone_number = #{phoneNumber}
            """)
    boolean existsByPhoneNumber(@Param("phoneNumber") Long phoneNumber);

}
