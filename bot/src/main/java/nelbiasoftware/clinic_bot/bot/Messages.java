package nelbiasoftware.clinic_bot.bot;

import nelbiasoftware.clinic_bot.core.Configuration;

import java.util.Locale;
import java.util.Map;

public final class Messages {

    private static final Map<String, String> MESSAGES = Configuration.loadMessages();

    private Messages() {
    }

    public static String get(String key, Object... args) {
        String pattern = MESSAGES.get(key);
        if (args.length == 0) {
            return pattern;
        }
        return String.format(Locale.getDefault(), pattern, args);
    }

    public static String errorAdminAppointmentCreationPhone() {
        return get("error.admin.appointment_creation.phone");
    }

    public static String errorAdminAppointmentCreationDate() {
        return get("error.admin.appointment_creation.date");
    }

    public static String errorAdminAppointmentCreationStartEndTime() {
        return get("error.admin.appointment_creation.start_end_time");
    }

    public static String appointmentCreationPromptPhoneNumber() {
        return get("appointment_creation.prompt.phone_number");
    }

    public static String appointmentCreationPromptDate() {
        return get("appointment_creation.prompt.date");
    }

    public static String appointmentCreationPromptStartEndTime(String availableTimes) {
        return get("appointment_creation.prompt.start_end_time", availableTimes);
    }

    public static String appointmentCreationPromptGoal() {
        return get("appointment_creation.prompt.goal");
    }

    public static String appointmentCreationPromptMessageChoose() {
        return get("appointment_creation.prompt.message_choose");
    }

    public static String buttonAdminAppointmentCreationMessageTemplate() {
        return get("button.admin.appointment_creation.message_template");
    }

    public static String appointmentView(String date, String startTime, String endTime, String phone, String patientName, String goal, String message) {
        return get("appointment.view", date, startTime, endTime, phone, patientName, goal, message);
    }

    public static String confirmationAdminAppointmentCreation(String formattedData) {
        return get("confirmation.admin.appointment_creation", formattedData);
    }

    public static String errorAdminAppointmentCreationInvalidAppointment(String errorMessage) {
        return get("error.admin.appointment_creation.invalid_appointment", errorMessage);
    }

    public static String notificationPatientAppointmentCreated(String formattedData) {
        return get("notification.patient.appointment_created", formattedData);
    }

    public static String notificationAdminAppointmentCreated() {
        return get("notification.admin.appointment_created");
    }

    public static String appointmentGoalNotSpecified() {
        return get("appointment.goal_not_specified");
    }

    public static String buttonAdminAppointmentCreationSave() {
        return get("button.admin.appointment_creation.save");
    }

    public static String buttonAdminAppointmentCreationCancel() {
        return get("button.admin.appointment_creation.cancel");
    }

    public static String buttonSkipGoalInput() {
        return get("button.skip_goal_input");
    }

    public static String commandCreateAppointmentDesc() {
        return get("command.create_appointment.desc");
    }

    public static String commandCancelAppointmentCreationDesc() {
        return get("command.cancel_appointment_creation.desc");
    }

    public static String commandCancelAppointmentDesc() {
        return get("command.cancel_appointment.desc");
    }

    public static String commandListNextAppointmentsDesc() {
        return get("command.list_next_appointments.desc");
    }

    public static String errorTechnical(String rayId) {
        return get("error.technical", rayId);
    }

    public static String errorAdminAppointmentCreationSessionAlreadyExist() {
        return get("error.admin.appointment_creation.session_already_exist");
    }

    public static String notificationAdminAppointmentCancelledByPatient(Long phoneNumber) {
        return get("notification.admin.appointment_cancelled_by_patient", phoneNumber);
    }

    public static String notificationPatientAppointmentCancelled() {
        return get("notification.patient.appointment_cancelled");
    }

    public static String notificationAdminCreationCancelled() {
        return get("notification.admin.creation_cancelled");
    }

    public static String confirmationCancelAppointment() {
        return get("confirmation.cancel_appointment");
    }

    public static String patientRegistrationCompleted() {
        return get("patient.registration_completed");
    }

    public static String errorPatientAlreadyRegistered() {
        return get("error.patient.already_registered");
    }

    public static String notificationAdminPatientRegistered(Long phoneNumber) {
        return get("notification.admin.patient_registered", phoneNumber);
    }

    public static String patientGreeting() {
        return get("patient.greeting");
    }

    public static String buttonRegister() {
        return get("button.register");
    }

    public static String authorInfo() {
        return get("author.info");
    }

    public static String errorUnknownInput() {
        return get("error.unknown_input");
    }

    public static String buttonPatientHelp() {
        return get("button.help.patient");
    }

    public static String buttonAdminHelp() {
        return get("button.help.admin");
    }

    public static String notificationTemplate(String firstName) {
        return get("notification.template", firstName);
    }

    public static String notificationFooter(String goalFormatted, String formattedDate, String startDateTimeRaw, String endDateTimeRaw) {
        return get("notification.footer", goalFormatted, formattedDate, startDateTimeRaw, endDateTimeRaw);
    }

    public static String buttonPatientCancelAppointment() {
        return get("button.patient.cancel_appointment");
    }

    public static String adminHelp(String createCommand, String listCommand) {
        return get("help.admin", createCommand, listCommand);
    }

    public static String patientHelp(String cancelCommand) {
        return get("help.patient", cancelCommand);
    }
}
