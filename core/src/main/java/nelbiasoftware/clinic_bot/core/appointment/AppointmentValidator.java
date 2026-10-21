package nelbiasoftware.clinic_bot.core.appointment;

import nelbiasoftware.clinic_bot.core.Entity;
import nelbiasoftware.clinic_bot.core.Storage;
import nelbiasoftware.clinic_bot.core.Validation;
import nelbiasoftware.clinic_bot.core.util.Collection;
import nelbiasoftware.clinic_bot.core.util.DateFormat;
import nelbiasoftware.clinic_bot.core.util.Result;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AppointmentValidator {

    private AppointmentValidator() {
    }

    /**
     * Validates and constructs an appointment from the data buffer without saving it.
     *
     * @param appointmentDataBuffer user data collected during the appointment flow
     * @return constructed Appointment map on success, or failure message on validation error
     */
    public static Result<Map<String, Object>> preCreateAppointment(Map<String, Object> appointmentDataBuffer) {
        String validationFailure = Validation.validateAppointmentDataBuffer(appointmentDataBuffer);
        if (!validationFailure.isEmpty()) {
            return failure(validationFailure);
        }

        Result<Long> phoneResult = parseAndValidatePhone(appointmentDataBuffer);
        if (!phoneResult.isOk()) {
            return failure(phoneResult.failureMessage());
        }

        Result<OffsetDateTime> dateResult = parseAndValidateDate(appointmentDataBuffer);
        if (!dateResult.isOk()) {
            return failure(dateResult.failureMessage());
        }

        Result<List<OffsetDateTime>> timeResult = parseAndValidateTime(appointmentDataBuffer);
        if (!timeResult.isOk()) {
            return failure(timeResult.failureMessage());
        }

        Result<String> messageResult = validateMessage(appointmentDataBuffer);
        if (!messageResult.isOk()) {
            return failure(messageResult.failureMessage());
        }

        List<OffsetDateTime> interval = timeResult.result();
        String goal = Collection.getOrNull(appointmentDataBuffer, Entity.AppointmentDataBuffer.GOAL);
        Map<String, Object> appointment = buildAppointment(phoneResult.result(),
                                                           interval.get(0),
                                                           interval.get(1),
                                                           goal,
                                                           messageResult.result());

        String overlapFailure = firstOverlapFailure(appointment);
        if (overlapFailure != null) {
            return failure(overlapFailure);
        }

        return new Result<>(appointment, null);
    }

    private static Result<Long> parseAndValidatePhone(Map<String, Object> appointmentDataBuffer) {
        String phoneNumberRaw = Collection.get(appointmentDataBuffer, Entity.AppointmentDataBuffer.PATIENT_PHONE_NUMBER);
        phoneNumberRaw = normalizePhoneNumber(phoneNumberRaw);

        Long phoneNumber = parsePhoneNumber(phoneNumberRaw);
        if (phoneNumber == null) {
            return failure("Номер телефона введён неверно: " + phoneNumberRaw);
        }
        if (!Storage.isPatientExists(phoneNumber)) {
            return failure("Пациент с номером " + phoneNumber + " не зарегистрирован! Помогите пациенту зарегистрироваться в боте.");
        }
        if (Storage.isAppointmentExists(phoneNumber)) {
            return failure("Запись на номер " + phoneNumber + " уже существует!");
        }
        return new Result<>(phoneNumber, null);
    }

    private static String normalizePhoneNumber(String phoneNumberRaw) {
        String normalized = phoneNumberRaw.replace("-", "")
                                          .replace("+", "")
                                          .replace(" ", "")
                                          .replace("(", "")
                                          .replace(")", "");
        if (normalized.startsWith("8")) {
            normalized = "7" + normalized.substring(1);
        }
        return normalized;
    }

    private static Result<OffsetDateTime> parseAndValidateDate(Map<String, Object> appointmentDataBuffer) {
        String date = Collection.get(appointmentDataBuffer, Entity.AppointmentDataBuffer.DATE);
        OffsetDateTime dateTime = DateFormat.parseHumanReadableDate(date);
        LocalDate localDate = dateTime.toLocalDate();
        if (localDate.isBefore(LocalDate.now())) {
            return failure("Дата не может быть в прошлом: " + date);
        }
        if (AvailabilityService.isWeekend(dateTime)) {
            return failure("Дата не должна указывать на субботу или воскресенье!");
        }
        return new Result<>(dateTime, null);
    }

    private static Result<List<OffsetDateTime>> parseAndValidateTime(Map<String, Object> appointmentDataBuffer) {
        String date = Collection.get(appointmentDataBuffer, Entity.AppointmentDataBuffer.DATE);
        Map<String, Object> startEndTime = AppointmentDataBufferParser.extractStartEndTime(appointmentDataBuffer);
        String startDateTimeRaw = Collection.get(startEndTime, Entity.AppointmentDataBuffer.START_TIME);
        String endDateTimeRaw = Collection.get(startEndTime, Entity.AppointmentDataBuffer.END_TIME);
        OffsetDateTime startDateTime = DateFormat.parseHumanReadableDateTime(date, startDateTimeRaw);
        OffsetDateTime endDateTime = DateFormat.parseHumanReadableDateTime(date, endDateTimeRaw);

        if (startDateTime.isBefore(OffsetDateTime.now())) {
            return failure("Указанное время (%s) уже прошло!" .formatted(startDateTimeRaw));
        }
        if (endDateTime.isBefore(startDateTime)) {
            return failure("Время начала (%s) не может быть позже времени окончания (%s)!" .formatted(startDateTimeRaw, endDateTimeRaw));
        }
        boolean isNotWorkingHours = startDateTime.getHour() < AvailabilityService.workStartHour() || endDateTime.getHour() > AvailabilityService.workEndHour();
        if (isNotWorkingHours) {
            return failure("Запись можно добавить только на рабочие часы (%s—%s)!" .formatted(AvailabilityService.workStartHour(), AvailabilityService.workEndHour()));
        }
        return new Result<>(List.of(startDateTime, endDateTime), null);
    }

    private static Result<String> validateMessage(Map<String, Object> appointmentDataBuffer) {
        String message = Collection.get(appointmentDataBuffer, Entity.AppointmentDataBuffer.MESSAGE);
        if (message.isEmpty()) {
            return failure("Сообщение пациенту не должно быть пустым.");
        }
        return new Result<>(message, null);
    }

    private static Map<String, Object> buildAppointment(Long phoneNumber,
                                                        OffsetDateTime startDateTime,
                                                        OffsetDateTime endDateTime,
                                                        @Nullable String goal,
                                                        String message) {
        Map<String, Object> appointment = new HashMap<>();
        appointment.put(Entity.Appointment.PATIENT_PHONE_NUMBER, phoneNumber);
        appointment.put(Entity.Appointment.START_DATE_TIME, startDateTime);
        appointment.put(Entity.Appointment.END_DATE_TIME, endDateTime);
        appointment.put(Entity.Appointment.GOAL, goal);
        appointment.put(Entity.Appointment.MESSAGE, message);
        return appointment;
    }

    private static @Nullable String firstOverlapFailure(Map<String, Object> appointment) {
        List<Map<String, Object>> overlappedAppointments = Storage.selectOverlappedAppointments(appointment);
        if (overlappedAppointments.isEmpty()) {
            return null;
        }

        Map<String, Object> overlappedAppointment = overlappedAppointments.getFirst();
        OffsetDateTime startTimeNew = Collection.get(appointment, Entity.Appointment.START_DATE_TIME);
        OffsetDateTime endTimeNew = Collection.get(appointment, Entity.Appointment.END_DATE_TIME);
        OffsetDateTime startTime = Collection.get(overlappedAppointment, Entity.Appointment.START_DATE_TIME);
        OffsetDateTime endTime = Collection.get(overlappedAppointment, Entity.Appointment.END_DATE_TIME);

        return """
                Новая запись пересекается по времени с существующими!
                
                ⏰ Дата/время новой: %s—%s
                
                ⏰ Дата/время существующей: %s—%s
                """.formatted(DateFormat.formatDayMonthHourMinute(startTimeNew),
                              DateFormat.formatDayMonthHourMinute(endTimeNew),
                              DateFormat.formatDayMonthHourMinute(startTime),
                              DateFormat.formatDayMonthHourMinute(endTime));
    }

    private static @Nullable Long parsePhoneNumber(String phoneNumber) {
        try {
            return Long.parseLong(phoneNumber);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static <T> Result<T> failure(String failureMessage) {
        return new Result<>(null, failureMessage);
    }
}
