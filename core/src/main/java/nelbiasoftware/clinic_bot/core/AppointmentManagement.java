package nelbiasoftware.clinic_bot.core;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class AppointmentManagement {

    public static @Nullable Map<String, Object> appointmentByPhoneNumber(Long phoneNumber) {
        return Storage.appointmentByPhoneNumber(phoneNumber);
    }

    public static List<Map<String, Object>> listNextAppointments(int days) {
        return Storage.selectNextAppointments(days);
    }

    public static void createAppointment(Map<String, Object> appointment) {
        Storage.insertAppointment(appointment);
    }

    /**
     * Cancel appointment of the Patient with <code>patientPhoneNumber</code>. Notifications will be ignored.
     */
    public static void cancelAppointment(long patientPhoneNumber) {
        Storage.deleteAppointment(patientPhoneNumber);
    }
}
