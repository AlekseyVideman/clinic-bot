package nelbiasoftware.clinic_bot.bot.view;

import nelbiasoftware.clinic_bot.bot.Messages;
import nelbiasoftware.clinic_bot.core.Entity;
import nelbiasoftware.clinic_bot.core.UserManagement;
import nelbiasoftware.clinic_bot.core.util.Collection;
import nelbiasoftware.clinic_bot.core.util.DateFormat;
import org.jspecify.annotations.Nullable;
import ru.max.botapi.model.Button;
import ru.max.botapi.model.MessageButton;
import ru.max.botapi.model.NewMessageBody;
import ru.max.botapi.model.TextFormat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;
import static nelbiasoftware.clinic_bot.bot.constant.Command.*;

public final class AdminViews {

    private AdminViews() {
    }

    public static NewMessageBody adminHelp() {
        return new NewMessageBody(Messages.adminHelp(CREATE_APPOINTMENT, LIST_NEXT_APPOINTMENTS), null, null, null, TextFormat.MARKDOWN);
    }

    public static String renderAppointments(List<Map<String, Object>> appointments) {
        return range(0, appointments.size())
                .mapToObj(index -> (index + 1) + ". " + appointmentEntry(appointments.get(index), patientName(appointments.get(index))))
                .collect(Collectors.joining());
    }

    public static String appointmentEntry(Map<String, Object> appointment, @Nullable String patientName) {
        OffsetDateTime startDate = Collection.get(appointment, Entity.Appointment.START_DATE_TIME);
        OffsetDateTime endDate = Collection.get(appointment, Entity.Appointment.END_DATE_TIME);
        Long phoneNumber = Collection.get(appointment, Entity.Appointment.PATIENT_PHONE_NUMBER);
        String goal = Collection.getOrNull(appointment, Entity.Appointment.GOAL);
        String message = Collection.get(appointment, Entity.Appointment.MESSAGE);

        return Messages.appointmentView(
                DateFormat.formatDayMonthHint(startDate),
                DateFormat.formatHourMinute(startDate),
                DateFormat.formatHourMinute(endDate),
                phoneNumber.toString(),
                patientName,
                AppointmentViews.goalFormatted(goal),
                message) + "\n\n";
    }

    public static NewMessageBody notifyAdminCancelledAppointment(Long phoneNumber) {
        return new NewMessageBody(Messages.notificationAdminAppointmentCancelledByPatient(phoneNumber), null, null, null, TextFormat.MARKDOWN);
    }

    public static NewMessageBody notifyAdminPatientRegistered(Long phoneNumber) {
        List<List<Button>> createAppointmentButtons = List.of(
                List.of(new MessageButton(COMMAND_PREFIX + CREATE_APPOINTMENT))
        );
        return new NewMessageBody(Messages.notificationAdminPatientRegistered(phoneNumber),
                                  CommonViews.buildInlineKeyboard(createAppointmentButtons),
                                  null,
                                  null,
                                  TextFormat.MARKDOWN);
    }

    public static @Nullable String patientName(Map<String, Object> appointment) {
        Long phoneNumber = Collection.get(appointment, Entity.Appointment.PATIENT_PHONE_NUMBER);
        Map<String, Object> patient = UserManagement.patientByPhoneNumber(phoneNumber);
        return Collection.getOrNull(patient, Entity.Patient.FIRST_NAME);
    }
}
