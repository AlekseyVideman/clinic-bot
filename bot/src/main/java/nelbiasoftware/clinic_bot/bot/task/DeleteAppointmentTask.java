package nelbiasoftware.clinic_bot.bot.task;

import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import nelbiasoftware.clinic_bot.core.Entity;
import nelbiasoftware.clinic_bot.core.Storage;

public class DeleteAppointmentTask {

    /**
     * data = {@link Entity.Patient#PHONE_NUMBER}
     */
    public static final TaskDescriptor<Long> DESCRIPTOR = TaskDescriptor.of("deleteAppointmentTask", Long.class);

    static final OneTimeTask<Long> TASK = Tasks.oneTime(DESCRIPTOR).execute((instance, _) -> {
        Long patientPhoneNumber = instance.getData();
        Storage.deleteAppointment(patientPhoneNumber);
    });

}
