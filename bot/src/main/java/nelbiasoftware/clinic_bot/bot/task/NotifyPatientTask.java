package nelbiasoftware.clinic_bot.bot.task;

import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import nelbiasoftware.clinic_bot.bot.Bot;
import nelbiasoftware.clinic_bot.bot.Messages;
import nelbiasoftware.clinic_bot.bot.constant.Callback;
import nelbiasoftware.clinic_bot.bot.view.CommonViews;
import nelbiasoftware.clinic_bot.core.AppointmentManagement;
import nelbiasoftware.clinic_bot.core.Entity;
import nelbiasoftware.clinic_bot.core.UserManagement;
import nelbiasoftware.clinic_bot.core.util.Collection;
import ru.max.botapi.model.Button;
import ru.max.botapi.model.CallbackButton;
import ru.max.botapi.model.NewMessageBody;
import ru.max.botapi.model.TextFormat;

import java.util.List;
import java.util.Map;

public class NotifyPatientTask {

    /**
     * data = {@link Entity.Patient#PHONE_NUMBER}
     */
    public static final TaskDescriptor<Long> DESCRIPTOR = TaskDescriptor.of("notifyPatientTask", Long.class);

    @SuppressWarnings("all") // в проде patient и appointment всегда != null
    static final OneTimeTask<Long> TASK = Tasks.oneTime(DESCRIPTOR).execute((instance, _) -> {
        Long patientPhoneNumber = instance.getData();
        Map<String, Object> appointment = AppointmentManagement.appointmentByPhoneNumber(patientPhoneNumber);
        if (appointment == null) {
            return; // appointment is canceled, ignore
        }

        Map<String, Object> patient = UserManagement.patientByPhoneNumber(patientPhoneNumber);
        long userId = Collection.get(patient, Entity.Patient.USER_ID);

        List<List<Button>> cancelButtons = List.of(
                List.of(new CallbackButton(Messages.buttonPatientCancelAppointment(), Callback.CANCEL, null))
        );
        String message = Collection.getOrNull(appointment, Entity.Appointment.MESSAGE);
        Bot.sendMessage(userId, new NewMessageBody(message, CommonViews.buildInlineKeyboard(cancelButtons), null, true, TextFormat.MARKDOWN));
    });

}
