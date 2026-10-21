package nelbiasoftware.clinic_bot.bot.scenario;

import nelbiasoftware.clinic_bot.bot.Messages;
import nelbiasoftware.clinic_bot.bot.notification.Notifier;
import nelbiasoftware.clinic_bot.core.AppointmentManagement;
import nelbiasoftware.clinic_bot.core.Entity;
import nelbiasoftware.clinic_bot.core.UserManagement;
import nelbiasoftware.clinic_bot.core.util.Collection;
import org.tinylog.Logger;
import ru.max.botapi.model.NewMessageBody;
import ru.max.botapi.model.TextFormat;

import java.util.Map;

public final class CancellationFlow {

    private CancellationFlow() {
    }

    public static NewMessageBody cancelApprovedAppointment(long userId) {
        Map<String, Object> patient = UserManagement.patientByUserId(userId);
        if (patient != null) {
            cancelAppointment(patient);
        }
        return new NewMessageBody(Messages.notificationPatientAppointmentCancelled(), null, null, null, TextFormat.MARKDOWN);
    }

    private static void cancelAppointment(Map<String, Object> patient) {
        Long phoneNumber = Collection.get(patient, Entity.Patient.PHONE_NUMBER);
        AppointmentManagement.cancelAppointment(phoneNumber);
        Notifier.notifyAdminAboutCancelledAppointment(phoneNumber);

        Logger.info("Appointment for number {} cancelled", phoneNumber);
    }
}
