package nelbiasoftware.clinic_bot.bot.task;

import com.github.kagkarlsson.scheduler.task.SchedulableInstance;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static nelbiasoftware.clinic_bot.bot.task.NotifyPatientTask.DESCRIPTOR;
import static java.time.Duration.ofSeconds;

public class Scheduler {

    private static com.github.kagkarlsson.scheduler.Scheduler scheduler;

    public static void init(DataSource dataSource) {
        scheduler = com.github.kagkarlsson.scheduler.Scheduler.create(dataSource, NotifyPatientTask.TASK, DeleteAppointmentTask.TASK)
                                                              .alwaysPersistTimestampInUTC()
                                                              .registerShutdownHook()
                                                              .threads(2)
                                                              .build();
        scheduler.start();
    }

    public static SchedulableInstance<?> patientNotification(long patientPhoneNumber, Instant scheduledTime) {
        return DESCRIPTOR.instance(UUID.randomUUID().toString())
                                           .data(patientPhoneNumber)
                                           .scheduledTo(scheduledTime);
    }

    public static SchedulableInstance<?> appointmentDeletion(long patientPhoneNumber, Instant scheduledTime) {
        return DeleteAppointmentTask.DESCRIPTOR.instance(UUID.randomUUID().toString())
                                               .data(patientPhoneNumber)
                                               .scheduledTo(scheduledTime);
    }

    /**
     * Schedule batch
     *
     * @param tasks List of {@link SchedulableInstance}
     */
    @SuppressWarnings("all")
    public static void schedule(List<SchedulableInstance<?>> tasks) {
        scheduler.scheduleBatch(tasks);
    }

    public static void shutdown() {
        scheduler.stop();
    }
}
