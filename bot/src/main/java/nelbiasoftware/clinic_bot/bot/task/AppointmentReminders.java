package nelbiasoftware.clinic_bot.bot.task;

import com.github.kagkarlsson.scheduler.task.SchedulableInstance;
import nelbiasoftware.clinic_bot.core.Entity;
import nelbiasoftware.clinic_bot.core.util.Collection;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static nelbiasoftware.clinic_bot.bot.task.Scheduler.appointmentDeletion;
import static nelbiasoftware.clinic_bot.bot.task.Scheduler.patientNotification;

/**
 * Reminder/notifications policy for an appointment.
 */
public final class AppointmentReminders {

    private AppointmentReminders() {
    }

    public static void schedule(Map<String, Object> appointment) {
        long patientPhoneNumber = Collection.get(appointment, Entity.Appointment.PATIENT_PHONE_NUMBER);
        OffsetDateTime startDateTime = Collection.get(appointment, Entity.Appointment.START_DATE_TIME);
        Scheduler.schedule(tasks(patientPhoneNumber, startDateTime));
    }

    private static List<SchedulableInstance<?>> tasks(long patientPhoneNumber, OffsetDateTime startDateTime) {
        return List.of(
                patientNotification(patientPhoneNumber, startDateTime.minusDays(1)
                                                                    .toInstant()),
                patientNotification(patientPhoneNumber, startDateTime.minusHours(1)
                                                                    .toInstant()),
                appointmentDeletion(patientPhoneNumber, startDateTime.plusDays(1)
                                                                    .toInstant())
        );
    }
}
