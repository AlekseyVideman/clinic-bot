package nelbiasoftware.clinic_bot.bot.view;

import nelbiasoftware.clinic_bot.bot.Messages;
import nelbiasoftware.clinic_bot.bot.constant.Callback;
import ru.max.botapi.model.Button;
import ru.max.botapi.model.CallbackButton;
import ru.max.botapi.model.NewMessageBody;
import ru.max.botapi.model.RequestContactButton;
import ru.max.botapi.model.TextFormat;

import java.util.List;
import java.util.Map;

import static nelbiasoftware.clinic_bot.bot.constant.Command.CANCEL_APPOINTMENT;

public final class PatientViews {

    private PatientViews() {
    }

    public static NewMessageBody cancelAppointmentConfirmation() {
        List<List<Button>> cancelAppointmentButtons = List.of(
                List.of(new CallbackButton(Messages.buttonPatientCancelAppointment(), Callback.CANCEL_APPROVED, null))
        );
        return new NewMessageBody(Messages.confirmationCancelAppointment(), CommonViews.buildInlineKeyboard(cancelAppointmentButtons), null, null, TextFormat.MARKDOWN);
    }

    public static NewMessageBody patientHelp() {
        return new NewMessageBody(Messages.patientHelp(CANCEL_APPOINTMENT), null, null, null, TextFormat.MARKDOWN);
    }

    public static NewMessageBody appointmentCreatedNotification(Map<String, Object> appointmentDataBuffer) {
        List<List<Button>> buttons = List.of(
                List.of(new CallbackButton(Messages.buttonPatientCancelAppointment(), Callback.CANCEL, null))
        );
        return new NewMessageBody(Messages.notificationPatientAppointmentCreated(AppointmentViews.notificationFooter(appointmentDataBuffer)),
                                  CommonViews.buildInlineKeyboard(buttons),
                                  null,
                                  null,
                                  TextFormat.MARKDOWN);
    }

    public static NewMessageBody greetingMessage() {
        List<List<Button>> startButtons = List.of(
                List.of(new RequestContactButton(Messages.buttonRegister()))
        );

        return new NewMessageBody(Messages.patientGreeting(), CommonViews.buildInlineKeyboard(startButtons), null, null, TextFormat.MARKDOWN);
    }
}
