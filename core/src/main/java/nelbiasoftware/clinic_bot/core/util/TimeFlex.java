package nelbiasoftware.clinic_bot.core.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class TimeFlex {

    public static OffsetDateTime time(int hour, int minute, OffsetDateTime date) {
        return date.withHour(hour)
                   .withMinute(minute)
                   .withSecond(0)
                   .withNano(0);
    }

    /**
     * Находит все свободные интервалы между занятыми интервалами в пределах заданного диапазона в рамках ОДНОГО ДНЯ
     *
     * @param busyIntervals     список занятых интервалов [[start1, end1], [start2, end2], ...]
     * @param startPoint        начальная точка диапазона
     * @param endPoint          конечная точка диапазона
     * @param comfortGapMinutes требуемое расстояние между интервалами
     * @return список свободных интервалов [[freeStart1, freeEnd1], ...]
     */
    public static List<List<OffsetDateTime>> findFreeIntervals(
            List<List<OffsetDateTime>> busyIntervals,
            OffsetDateTime startPoint,
            OffsetDateTime endPoint,
            int comfortGapMinutes) {

        List<List<OffsetDateTime>> result = new ArrayList<>();
        OffsetDateTime current = startPoint;

        for (List<OffsetDateTime> busyInterval : busyIntervals) {
            OffsetDateTime start = busyInterval.get(0);
            OffsetDateTime end = busyInterval.get(1);

            // Свободный интервал ПЕРЕД занятым интервалом
            Duration gap = Duration.between(current, start);
            if (isMeaningful(gap, comfortGapMinutes)) {
                result.add(List.of(current, start));
            }

            current = end;
        }

        // Свободный интервал ПОСЛЕ последнего занятого интервала
        Duration gapAfterLast = Duration.between(current, endPoint);
        if (isMeaningful(gapAfterLast, comfortGapMinutes)) {
            result.add(List.of(current, endPoint));
        }

        return result;
    }

    private static boolean isMeaningful(Duration duration, int comfortGapMinutes) {
        return duration.toMinutes() > comfortGapMinutes;
    }
}
