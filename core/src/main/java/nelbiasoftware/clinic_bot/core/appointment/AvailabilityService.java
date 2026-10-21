package nelbiasoftware.clinic_bot.core.appointment;

import nelbiasoftware.clinic_bot.core.Entity;
import nelbiasoftware.clinic_bot.core.Storage;
import nelbiasoftware.clinic_bot.core.config.AppConfig;
import nelbiasoftware.clinic_bot.core.util.Collection;
import nelbiasoftware.clinic_bot.core.util.TimeFlex;
import org.jspecify.annotations.NonNull;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.util.stream.IntStream.range;
import static nelbiasoftware.clinic_bot.core.util.TimeFlex.time;

public final class AvailabilityService {

    private static final int MIN_SLOT_DURATION_MINUTES = 20;
    public static final int DAYS_FORWARD = 14;

    private static int WORK_START_HOUR;
    private static int WORK_END_HOUR;

    private AvailabilityService() {
    }

    /**
     * Get available date slots on next 14 days.
     *
     * @return lists of free dates
     * @see #availableDateTime()
     */
    public static List<OffsetDateTime> availableDate() {
        List<List<OffsetDateTime>> availableDateTime = availableDateTime();
        Map<Integer, OffsetDateTime> firstSlotsByDay = firstSlotsByDay(availableDateTime);
        return firstSlotsByDay.values()
                              .stream()
                              .sorted()
                              .collect(Collectors.toCollection(() -> new ArrayList<>(availableDateTime.size())));
    }

    private static Map<Integer, OffsetDateTime> firstSlotsByDay(List<List<OffsetDateTime>> availableDateTime) {
        return availableDateTime.stream()
                                .map(List::getFirst)
                                .collect(Collectors.toMap(
                                        OffsetDateTime::getDayOfMonth,
                                        dt -> dt,
                                        (existing, _) -> existing
                                ));
    }

    /**
     * Get available date and time slots on next 14 days.
     *
     * @return lists of free time, first is 'from', last is 'to' times. maximum length of sublist is 2
     */
    public static List<List<OffsetDateTime>> availableDateTime() {
        return dateRange().stream()
                          .flatMap(date -> availableDateTime(date).stream())
                          .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<OffsetDateTime> dateRange() {
        var now = OffsetDateTime.now();
        return range(0, DAYS_FORWARD).mapToObj(now::plusDays)
                                     .toList();
    }

    /**
     * Get available date and time slots for a specific date.
     *
     * @param date the date to check for available slots
     * @return list of free time intervals, where each interval contains [start, end]
     */
    public static List<List<OffsetDateTime>> availableDateTime(OffsetDateTime date) {
        List<Map<String, Object>> appointmentsByDate = listAppointmentsByDate(date);
        if (appointmentsByDate.isEmpty()) {
            return allWorkDayInterval(date);
        }
        return availableTimeBetweenAppointmentsWithinOneDay(appointmentsByDate, date);
    }

    private static List<List<OffsetDateTime>> allWorkDayInterval(OffsetDateTime date) {
        if (isWeekend(date)) {
            return List.of();
        }

        var startDateTime = date.withHour(WORK_START_HOUR)
                                .withMinute(0);
        var endDateTime = date.withHour(WORK_END_HOUR)
                              .withMinute(0);
        return List.of(List.of(startDateTime, endDateTime));
    }

    private static List<Map<String, Object>> listAppointmentsByDate(OffsetDateTime date) {
        return Storage.selectAppointmentsByDate(date);
    }

    @NonNull
    private static List<List<OffsetDateTime>> availableTimeBetweenAppointmentsWithinOneDay(List<Map<String, Object>> appointments, OffsetDateTime date) {
        return TimeFlex.findFreeIntervals(busyIntervals(appointments),
                                          time(WORK_START_HOUR, 0, date),
                                          time(WORK_END_HOUR, 0, date),
                                          MIN_SLOT_DURATION_MINUTES);
    }

    private static List<List<OffsetDateTime>> busyIntervals(List<Map<String, Object>> appointments) {
        List<List<OffsetDateTime>> busyIntervals = new ArrayList<>(appointments.size());
        appointments.forEach(appointment -> {
            OffsetDateTime start = Collection.get(appointment, Entity.Appointment.START_DATE_TIME);
            OffsetDateTime end = Collection.get(appointment, Entity.Appointment.END_DATE_TIME);
            busyIntervals.add(List.of(start, end));
        });
        return busyIntervals;
    }

    public static boolean isWeekend(OffsetDateTime time) {
        return time.getDayOfWeek() == SATURDAY || time.getDayOfWeek() == SUNDAY;
    }

    public static int workStartHour() {
        return WORK_START_HOUR;
    }

    public static int workEndHour() {
        return WORK_END_HOUR;
    }

    public static class Initialization {
        public static void init(AppConfig config) {
            AvailabilityService.WORK_START_HOUR = config.workHoursStart();
            AvailabilityService.WORK_END_HOUR = config.workHoursEnd();
        }
    }
}
