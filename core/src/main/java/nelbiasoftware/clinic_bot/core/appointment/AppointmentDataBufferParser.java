package nelbiasoftware.clinic_bot.core.appointment;

import nelbiasoftware.clinic_bot.core.Entity;
import nelbiasoftware.clinic_bot.core.util.Collection;

import java.util.HashMap;
import java.util.Map;

public final class AppointmentDataBufferParser {

    private AppointmentDataBufferParser() {
    }

    /**
     * Extract the start and end time from the appointment data buffer.
     *
     * @param appointmentDataBuffer user data collected during the appointment flow
     * @return map with start and end time strings under {@link Entity.AppointmentDataBuffer#START_TIME} and {@link Entity.AppointmentDataBuffer#END_TIME}
     */
    public static Map<String, Object> extractStartEndTime(Map<String, Object> appointmentDataBuffer) {
        String timeRaw = Collection.get(appointmentDataBuffer, Entity.AppointmentDataBuffer.START_END_TIME);
        String[] startEnd = splitStartEnd(normalizeTimeRaw(timeRaw));

        Map<String, Object> startEndTime = new HashMap<>();
        startEndTime.put(Entity.AppointmentDataBuffer.START_TIME, startEnd[0]);
        startEndTime.put(Entity.AppointmentDataBuffer.END_TIME, startEnd[1]);
        return startEndTime;
    }

    private static String normalizeTimeRaw(String timeRaw) {
        return timeRaw.replace('-', ' '); // допустимый ввод для удобства пользователя
    }

    private static String[] splitStartEnd(String timeRaw) {
        int spaceIdx = timeRaw.indexOf(' ');
        String startDateTimeRaw = timeRaw.substring(0, spaceIdx);
        String endDateTimeRaw = timeRaw.substring(spaceIdx + 1);
        return new String[]{startDateTimeRaw, endDateTimeRaw};
    }
}
