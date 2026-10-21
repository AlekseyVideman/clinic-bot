package nelbiasoftware.clinic_bot.bot.controller;

import nelbiasoftware.clinic_bot.bot.scenario.AppointmentCreationFlow;
import nelbiasoftware.clinic_bot.bot.view.AdminViews;
import nelbiasoftware.clinic_bot.bot.view.CommonViews;
import nelbiasoftware.clinic_bot.bot.view.PatientViews;
import nelbiasoftware.clinic_bot.core.AppointmentManagement;
import nelbiasoftware.clinic_bot.core.config.AppConfig;
import org.jspecify.annotations.Nullable;
import org.tinylog.Logger;
import ru.max.botapi.model.NewMessageBody;
import ru.max.botapi.model.TextFormat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static nelbiasoftware.clinic_bot.bot.constant.Command.*;
import static nelbiasoftware.clinic_bot.core.appointment.AvailabilityService.DAYS_FORWARD;

public class CommandController {

    private static Set<Long> adminsById;

    private static final int COMMAND_PREFIX_LENGTH = 1;

    public static NewMessageBody processCommand(String prefixedCommand, long userId, @Nullable Map<String, Object> session) {
        var command = prefixedCommand.substring(COMMAND_PREFIX_LENGTH);
        return switch (command) {
            case String s when s.equals(CREATE_APPOINTMENT) && isAdmin(userId) -> AppointmentCreationFlow.start(userId, session);
            case String s when s.equals(CANCEL_APPOINTMENT_CREATION) && isAdmin(userId) -> AppointmentCreationFlow.cancelCreation(userId);
            case String s when s.equals(LIST_NEXT_APPOINTMENTS) && isAdmin(userId) -> listNextAppointments();
            case String s when s.equals(CANCEL_APPOINTMENT) -> PatientViews.cancelAppointmentConfirmation();
            default -> CommonViews.unknownInput();
        };
    }

    private static boolean isAdmin(long userId) {
        boolean isAdmin = adminsById.contains(userId);
        Logger.debug("Checking admin status for user [userId={}, isAdmin={}]", userId, isAdmin);
        return isAdmin;
    }

    public static NewMessageBody listNextAppointments() {
        List<Map<String, Object>> appointments = AppointmentManagement.listNextAppointments(DAYS_FORWARD);
        String result = appointments.isEmpty() ? "(нет)" : AdminViews.renderAppointments(appointments);
        return new NewMessageBody(result, null, null, null, TextFormat.MARKDOWN);
    }

    public static class Initialization {

        public static void init(AppConfig config) {
            CommandController.adminsById = config.adminsById();
        }

    }
}
