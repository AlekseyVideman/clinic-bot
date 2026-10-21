package nelbiasoftware.clinic_bot.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

public class DateFormat {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Krasnoyarsk");
    private static final DateTimeFormatter DAY_MONTH_FORMATTER = DateTimeFormatter.ofPattern("dd.MM");
    private static final DateTimeFormatter HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Parse time in format HH:mm to OffsetTime
     *
     * @param hourMinute 12:05
     * @return OffsetTime with zone offset from ZONE_ID
     */
    public static OffsetTime parseHumanReadableTime(String hourMinute) {
        TemporalAccessor parsed = parseHourMinute(hourMinute);
        return OffsetTime.of(parsed.get(ChronoField.HOUR_OF_DAY),
                             parsed.get(ChronoField.MINUTE_OF_HOUR),
                             0,
                             0,
                             ZONE_ID.getRules()
                                    .getOffset(java.time.Instant.now()));
    }


    /**
     * Parse datetime in format dd.MM
     *
     * @param dayMonth   09.10
     */
    public static OffsetDateTime parseHumanReadableDate(String dayMonth) {
        TemporalAccessor dateParsed = parseDayMonth(dayMonth);
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(ZONE_ID)
                                                           .getYear(),
                                                  dateParsed.get(ChronoField.MONTH_OF_YEAR),
                                                  dateParsed.get(ChronoField.DAY_OF_MONTH),
                                                  0,
                                                  0);
        return dateTime.atZone(ZONE_ID)
                       .toOffsetDateTime();
    }

    /**
     * Parse datetime in format dd.MM HH:mm
     *
     * @param dayMonth   09.10
     * @param hourMinute 12:05
     */
    public static OffsetDateTime parseHumanReadableDateTime(String dayMonth, String hourMinute) {
        TemporalAccessor dateParsed = parseDayMonth(dayMonth);
        TemporalAccessor timeParsed = parseHourMinute(hourMinute);
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(ZONE_ID)
                                                           .getYear(),
                                                  dateParsed.get(ChronoField.MONTH_OF_YEAR),
                                                  dateParsed.get(ChronoField.DAY_OF_MONTH),
                                                  timeParsed.get(ChronoField.HOUR_OF_DAY),
                                                  timeParsed.get(ChronoField.MINUTE_OF_HOUR));
        return dateTime.atZone(ZONE_ID)
                       .toOffsetDateTime();
    }

    /**
     * Parse date in format dd.MM
     *
     * @param dayMonth 09.10
     * @return parsed TemporalAccessor with day and month
     */
    private static TemporalAccessor parseDayMonth(String dayMonth) {
        return DAY_MONTH_FORMATTER.parse(dayMonth);
    }

    /**
     * Parse time in format HH:mm
     *
     * @param hourMinute 12:05
     * @return parsed TemporalAccessor with hour and minute
     */
    private static TemporalAccessor parseHourMinute(String hourMinute) {
        return HOUR_MINUTE_FORMATTER.parse(hourMinute);
    }

    public static String formatDayMonthHourMinute(OffsetDateTime dateTime) {
        return formatDayMonth(dateTime) +  " " + formatHourMinute(dateTime);
    }

    public static String formatHourMinute(OffsetDateTime dateTime) {
        return dateTime.atZoneSameInstant(ZONE_ID).format(DateTimeFormatter.ofPattern("HH:mm", Locale.of("ru")));
    }

    public static String formatDayMonthHint(OffsetDateTime dateTime) {
        var dateNumber = formatDayMonth(dateTime);
        var hint = capitalize(dateTime.atZoneSameInstant(ZONE_ID).getDayOfWeek()
                                      .getDisplayName(TextStyle.SHORT, Locale.of("ru")));
        return "%s (%s)".formatted(dateNumber, hint);
    }

    public static String formatDayMonth(OffsetDateTime dateTime) {
        return dateTime.atZoneSameInstant(ZONE_ID).format(DateTimeFormatter.ofPattern("dd.MM", Locale.of("ru")));
    }

    private static String capitalize(String str) {
        return str.substring(0, 1)
                  .toUpperCase() + str.substring(1);
    }
}
