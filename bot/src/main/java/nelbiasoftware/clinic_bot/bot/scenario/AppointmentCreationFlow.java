package nelbiasoftware.clinic_bot.bot.scenario;

import nelbiasoftware.clinic_bot.bot.Messages;
import nelbiasoftware.clinic_bot.bot.constant.Callback;
import nelbiasoftware.clinic_bot.bot.notification.Notifier;
import nelbiasoftware.clinic_bot.bot.task.AppointmentReminders;
import nelbiasoftware.clinic_bot.bot.view.AppointmentViews;
import nelbiasoftware.clinic_bot.bot.view.CommonViews;
import nelbiasoftware.clinic_bot.core.AppointmentManagement;
import nelbiasoftware.clinic_bot.core.Entity;
import nelbiasoftware.clinic_bot.core.Step;
import nelbiasoftware.clinic_bot.core.UserManagement;
import nelbiasoftware.clinic_bot.core.appointment.AppointmentValidator;
import nelbiasoftware.clinic_bot.core.session.SessionStore;
import nelbiasoftware.clinic_bot.core.util.Collection;
import nelbiasoftware.clinic_bot.core.util.Result;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ru.max.botapi.model.NewMessageBody;
import ru.max.botapi.model.TextFormat;

import java.util.Map;

import static nelbiasoftware.clinic_bot.core.Step.*;

public final class AppointmentCreationFlow {

    private static final Map<Step, String> FIELD_BY_STEP = Map.of(
            PHONE_NUMBER, Entity.AppointmentDataBuffer.PATIENT_PHONE_NUMBER,
            DATE, Entity.AppointmentDataBuffer.DATE,
            TIME, Entity.AppointmentDataBuffer.START_END_TIME,
            GOAL, Entity.AppointmentDataBuffer.GOAL,
            MESSAGE_CHOOSE, Entity.AppointmentDataBuffer.MESSAGE
    );

    private AppointmentCreationFlow() {
    }

    public static NewMessageBody start(long userId, @Nullable Map<String, Object> session) {
        if (session != null) {
            return AppointmentViews.sessionAlreadyExist();
        }
        var createdSession = SessionStore.startSession(userId);
        return appendSessionParameter(createdSession, "", userId);
    }

    public static NewMessageBody cancelCreation(long userId) {
        SessionStore.endSession(userId);
        return new NewMessageBody(Messages.notificationAdminCreationCancelled(), null, null, null, TextFormat.MARKDOWN);
    }

    public static NewMessageBody skipGoal(Map<String, Object> session, long userId) {
        return appendSessionParameter(session, "", userId);
    }

    public static NewMessageBody date(Map<String, Object> session, long userId, String payload) {
        var date = payload.replace(Callback.DATE_PREFIX, "");
        return appendSessionParameter(session, date, userId);
    }

    public static NewMessageBody doNotSaveAppointment(long userId) {
        SessionStore.endSession(userId);
        return new NewMessageBody(Messages.notificationAdminCreationCancelled(), null, null, null, TextFormat.MARKDOWN);
    }

    public static NewMessageBody messageTemplate(Map<String, Object> session, long userId) {
        Map<String, Object> appointmentDataBuffer = Collection.get(session, Entity.Session.APPOINTMENT_DATA_BUFFER);
        String patientFirstName = patientFirstName(appointmentDataBuffer);

        var template = Messages.notificationTemplate(patientFirstName);
        return appendSessionParameter(session, template, userId);
    }

    private static @Nullable String patientFirstName(Map<String, Object> appointmentDataBuffer) {
        Long phoneNumber = Long.parseLong(Collection.get(appointmentDataBuffer, Entity.AppointmentDataBuffer.PATIENT_PHONE_NUMBER));
        Map<String, Object> patient = UserManagement.patientByPhoneNumber(phoneNumber);
        return Collection.getOrNull(patient, Entity.Patient.FIRST_NAME); // здесь номер ещё не валидирован, поэтому пациента с таким номером может и не быть
    }

    public static @NonNull NewMessageBody appendSessionParameter(Map<String, Object> session, String input, long userId) {
        Step step = Collection.get(session, Entity.Session.STEP);
        input = withMessageFooterIfNeeded(session, step, input);

        var updatedSession = accumulate(session, step, input);
        Step nextStep = nextStep(step);

        if (nextStep == FILLED) {
            return confirmPreCreatedAppointment(updatedSession, userId);
        }
        if (nextStep == WAITING_APPROVEMENT) {
            return appointmentDataBufferDone(updatedSession, userId);
        }
        return continueAppointmentCreation(updatedSession, nextStep, session, userId);
    }

    private static String withMessageFooterIfNeeded(Map<String, Object> session, Step step, String input) {
        if (step != MESSAGE_CHOOSE) {
            return input;
        }
        Map<String, Object> appointmentDataBuffer = Collection.get(session, Entity.Session.APPOINTMENT_DATA_BUFFER);
        return input + "\n" + AppointmentViews.notificationFooter(appointmentDataBuffer);
    }

    private static NewMessageBody confirmPreCreatedAppointment(Map<String, Object> updatedSession, long userId) {
        Map<String, Object> appointmentDataBuffer = Collection.get(updatedSession, Entity.Session.APPOINTMENT_DATA_BUFFER);
        Result<Map<String, Object>> preResult = AppointmentValidator.preCreateAppointment(appointmentDataBuffer);
        if (!preResult.isOk()) {
            SessionStore.endSession(userId);
            return new NewMessageBody(Messages.errorAdminAppointmentCreationInvalidAppointment(localizedValidationFailure(preResult.failureMessage())), null, null, null, TextFormat.MARKDOWN);
        }

        updatedSession.put(Entity.Session.PRECREATED_APPOINTMENT, preResult.result());
        updatedSession.put(Entity.Session.STEP, FILLED);
        SessionStore.updateSession(updatedSession, userId);

        return new NewMessageBody(Messages.confirmationAdminAppointmentCreation(formatAppointmentDataBuffer(appointmentDataBuffer)),
                                  CommonViews.buildInlineKeyboard(AppointmentViews.approvementButtons()),
                                  null,
                                  null,
                                  TextFormat.MARKDOWN);
    }

    private static NewMessageBody continueAppointmentCreation(Map<String, Object> updatedSession, Step nextStep, Map<String, Object> session, long userId) {
        updatedSession.put(Entity.Session.STEP, nextStep);
        SessionStore.updateSession(updatedSession, userId);

        return response(nextStep, session);
    }

    private static NewMessageBody appointmentDataBufferDone(Map<String, Object> session, long userId) {
        Map<String, Object> appointment = Collection.get(session, Entity.Session.PRECREATED_APPOINTMENT);
        saveAppointment(appointment);
        AppointmentReminders.schedule(appointment);
        Notifier.notifyPatientAppointmentCreated(appointment, Collection.get(session, Entity.Session.APPOINTMENT_DATA_BUFFER));
        SessionStore.endSession(userId);
        return AppointmentViews.appointmentCreated();
    }

    private static void saveAppointment(Map<String, Object> appointment) {
        AppointmentManagement.createAppointment(appointment);
    }

    private static String localizedValidationFailure(String validationFailure) {
        final String startSymbol = "/";
        return switch (validationFailure) {
            case String _ when validationFailure.startsWith(startSymbol + Entity.AppointmentDataBuffer.PATIENT_PHONE_NUMBER) -> Messages.errorAdminAppointmentCreationPhone();
            case String _ when validationFailure.startsWith(startSymbol + Entity.AppointmentDataBuffer.DATE) -> Messages.errorAdminAppointmentCreationDate();
            case String _ when validationFailure.startsWith(startSymbol + Entity.AppointmentDataBuffer.START_END_TIME) -> Messages.errorAdminAppointmentCreationStartEndTime();
            default -> validationFailure;
        };
    }

    private static String formatAppointmentDataBuffer(Map<String, Object> appointmentDataBuffer) {
        String phone = Collection.get(appointmentDataBuffer, Entity.AppointmentDataBuffer.PATIENT_PHONE_NUMBER);
        String patientName = patientNameByPhone(phone);

        return AppointmentViews.formatAppointmentDataBuffer(appointmentDataBuffer, patientName);
    }

    @SuppressWarnings("all")
    private static String patientNameByPhone(String phone) {
        Map<String, Object> patient = UserManagement.patientByPhoneNumber(Long.parseLong(phone));
        return Collection.get(patient, Entity.Patient.FIRST_NAME);
    }

    /**
     * Store (accumulate) data for the current step.
     *
     * @return updated Session
     */
    private static Map<String, Object> accumulate(Map<String, Object> session, Step step, String data) {
        if (step == INITIAL) {
            return session; // no modifications
        }

        Map<String, Object> appointmentDataBuffer = Collection.get(session, Entity.Session.APPOINTMENT_DATA_BUFFER);
        var key = FIELD_BY_STEP.get(step);
        appointmentDataBuffer.put(key, data);

        return session;
    }

    private static Step nextStep(Step step) {
        return step == INITIAL
                ? PHONE_NUMBER
                : Step.next(step);
    }

    private static NewMessageBody response(Step step, Map<String, Object> session) {
        return switch (step) {
            case PHONE_NUMBER -> AppointmentViews.responsePhoneNumber();
            case DATE -> AppointmentViews.responseDate();
            case TIME -> AppointmentViews.responseStartTime(session);
            case GOAL -> AppointmentViews.responseGoal();
            case MESSAGE_CHOOSE -> AppointmentViews.responseMessageChoose();
            case INITIAL, FILLED, WAITING_APPROVEMENT -> new NewMessageBody("_", null, null, null, TextFormat.MARKDOWN); // пользователь этого никогда не увидит
        };
    }
}
