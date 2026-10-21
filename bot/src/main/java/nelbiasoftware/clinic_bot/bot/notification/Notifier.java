package nelbiasoftware.clinic_bot.bot.notification;

import nelbiasoftware.clinic_bot.bot.Bot;
import nelbiasoftware.clinic_bot.bot.view.AdminViews;
import nelbiasoftware.clinic_bot.bot.view.PatientViews;
import nelbiasoftware.clinic_bot.core.Entity;
import nelbiasoftware.clinic_bot.core.UserManagement;
import nelbiasoftware.clinic_bot.core.config.AppConfig;
import nelbiasoftware.clinic_bot.core.util.Collection;

import java.util.Map;
import java.util.Set;

/**
 * Cross-scenario notifications: decides <i>whom</i> and <i>how</i> to notify.
 */
public final class Notifier {

    private static Set<Long> adminsById;

    private Notifier() {
    }

    public static void notifyPatientAppointmentCreated(Map<String, Object> appointment, Map<String, Object> appointmentDataBuffer) {
        Long phoneNumber = Collection.get(appointment, Entity.Appointment.PATIENT_PHONE_NUMBER);
        long patientUserId = patientUserId(phoneNumber);
        Bot.sendMessage(patientUserId, PatientViews.appointmentCreatedNotification(appointmentDataBuffer));
    }

    @SuppressWarnings("all")
    private static long patientUserId(Long phoneNumber) {
        Map<String, Object> patient = UserManagement.patientByPhoneNumber(phoneNumber);
        return Collection.get(patient, Entity.Patient.USER_ID); // здесь не может быть null, валидация не пропустит несуществующий номер
    }

    public static void notifyAdminAboutCancelledAppointment(Long phoneNumber) {
        adminsById.forEach(userId -> Bot.sendMessage(userId, AdminViews.notifyAdminCancelledAppointment(phoneNumber)));
    }

    public static void notifyAdminAboutRegisteredPatient(Long phoneNumber) {
        adminsById.forEach(userId -> Bot.sendMessage(userId, AdminViews.notifyAdminPatientRegistered(phoneNumber)));
    }

    public static class Initialization {

        public static void init(AppConfig config) {
            Notifier.adminsById = config.adminsById();
        }

    }
}
