package nelbiasoftware.clinic_bot.bot.view;

import nelbiasoftware.clinic_bot.bot.Messages;
import nelbiasoftware.clinic_bot.bot.constant.Callback;
import nelbiasoftware.clinic_bot.core.Entity;
import nelbiasoftware.clinic_bot.core.appointment.AppointmentDataBufferParser;
import nelbiasoftware.clinic_bot.core.appointment.AvailabilityService;
import nelbiasoftware.clinic_bot.core.util.Collection;
import nelbiasoftware.clinic_bot.core.util.DateFormat;
import org.jspecify.annotations.Nullable;
import ru.max.botapi.model.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nelbiasoftware.clinic_bot.bot.constant.Button.*;

public final class AppointmentViews {

    private AppointmentViews() {
    }

    public static NewMessageBody responsePhoneNumber() {
        return new NewMessageBody(Messages.appointmentCreationPromptPhoneNumber(), null, null, null, TextFormat.MARKDOWN);
    }

    public static NewMessageBody responseDate() {
        return new NewMessageBody(Messages.appointmentCreationPromptDate(), CommonViews.buildInlineKeyboard(dateButtons()), null, null, TextFormat.MARKDOWN);
    }

    private static List<List<Button>> dateButtons() {
        return AvailabilityService.availableDate()
                                  .stream()
                                  .map(date -> (Button) new CallbackButton(DateFormat.formatDayMonthHint(date),
                                                                           Callback.DATE_PREFIX + DateFormat.formatDayMonth(date),
                                                                           null))
                                  .map(List::of)
                                  .toList();
    }

    public static NewMessageBody responseStartTime(Map<String, Object> session) {
        OffsetDateTime date = DateFormat.parseHumanReadableDate(Collection.get(session, Entity.Session.APPOINTMENT_DATA_BUFFER, Entity.AppointmentDataBuffer.DATE));
        return new NewMessageBody(Messages.appointmentCreationPromptStartEndTime(availableTimeByDate(date)), null, null, null, TextFormat.MARKDOWN);
    }

    private static String availableTimeByDate(OffsetDateTime date) {
        return AvailabilityService.availableDateTime(date)
                                  .stream()
                                  .map(it -> {
                                      var start = DateFormat.formatHourMinute(it.getFirst());
                                      var end = DateFormat.formatHourMinute(it.getLast());
                                      return start + "—" + end;
                                  })
                                  .collect(Collectors.joining("\n"));
    }

    public static NewMessageBody responseGoal() {
        List<List<Button>> skipButtons = List.of(
                List.of(new CallbackButton(Messages.buttonSkipGoalInput(), Callback.SKIP_GOAL, null)),
                List.of(new MessageButton(GOAL_TREATMENT)),
                List.of(new MessageButton(GOAL_CONSULTATION)),
                List.of(new MessageButton(GOAL_DELETION))
        );

        return new NewMessageBody(Messages.appointmentCreationPromptGoal(), CommonViews.buildInlineKeyboard(skipButtons), null, null, TextFormat.MARKDOWN);
    }

    public static NewMessageBody responseMessageChoose() {
        List<List<Button>> chooseButtons = List.of(
                List.of(new CallbackButton(Messages.buttonAdminAppointmentCreationMessageTemplate(), Callback.MESSAGE_TEMPLATE, null))
        );

        return new NewMessageBody(Messages.appointmentCreationPromptMessageChoose(), CommonViews.buildInlineKeyboard(chooseButtons), null, null, TextFormat.MARKDOWN);
    }

    public static List<List<Button>> approvementButtons() {
        return List.of(
                List.of(new CallbackButton(Messages.buttonAdminAppointmentCreationSave(), Callback.SAVE_APPOINTMENT, null),
                        new CallbackButton(Messages.buttonAdminAppointmentCreationCancel(), Callback.DO_NOT_SAVE_APPOINTMENT, null))
        );
    }

    public static NewMessageBody appointmentCreated() {
        return new NewMessageBody(Messages.notificationAdminAppointmentCreated(), null, null, null, TextFormat.MARKDOWN);
    }

    @SuppressWarnings("all")
    public static String formatAppointmentDataBuffer(Map<String, Object> appointmentDataBuffer, String patientName) {
        String phone = Collection.get(appointmentDataBuffer, Entity.AppointmentDataBuffer.PATIENT_PHONE_NUMBER);
        String date = Collection.get(appointmentDataBuffer, Entity.AppointmentDataBuffer.DATE);

        Map<String, Object> startEndTime = AppointmentDataBufferParser.extractStartEndTime(appointmentDataBuffer);
        String startDateTimeRaw = Collection.get(startEndTime, Entity.AppointmentDataBuffer.START_TIME);
        String endDateTimeRaw = Collection.get(startEndTime, Entity.AppointmentDataBuffer.END_TIME);

        String goal = Collection.getOrNull(appointmentDataBuffer, Entity.AppointmentDataBuffer.GOAL);
        String message = Collection.get(appointmentDataBuffer, Entity.AppointmentDataBuffer.MESSAGE);

        return Messages.appointmentView(date, startDateTimeRaw, endDateTimeRaw, phone, patientName, goalFormatted(goal), message.formatted(patientName));
    }

    public static String goalFormatted(@Nullable String goal) {
        if (goal == null || goal.isEmpty()) {
            return Messages.appointmentGoalNotSpecified();
        }
        return goal;
    }

    public static String notificationFooter(Map<String, Object> appointmentDataBuffer) {
        Map<String, Object> startEndTime = AppointmentDataBufferParser.extractStartEndTime(appointmentDataBuffer);
        String startDateTimeRaw = Collection.get(startEndTime, Entity.AppointmentDataBuffer.START_TIME);
        String endDateTimeRaw = Collection.get(startEndTime, Entity.AppointmentDataBuffer.END_TIME);

        String date = Collection.get(appointmentDataBuffer, Entity.AppointmentDataBuffer.DATE);
        String goal = Collection.getOrNull(appointmentDataBuffer, Entity.AppointmentDataBuffer.GOAL);

        return Messages.notificationFooter(goalFormatted(goal),
                                           DateFormat.formatDayMonthHint(DateFormat.parseHumanReadableDate(date)),
                                           startDateTimeRaw,
                                           endDateTimeRaw);
    }

    public static NewMessageBody sessionAlreadyExist() {
        return new NewMessageBody(Messages.errorAdminAppointmentCreationSessionAlreadyExist(), null, null, null, TextFormat.MARKDOWN);
    }
}
