package me.stky.backwork.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class RecurringTaskInfo {
    private Integer interval;
    private IntervalUnit intervalUnit;
    private Instant firstStartedDate;
    private Instant lastCompletedDate;
    private Instant nextScheduledDate;
    private final List<Date> completedDates = new ArrayList<>() {
        @Override
        public String toString() {
            return this.stream()
                    .map(d -> new SimpleDateFormat("yy/MM/dd").format(d))
                    .toList().toString();
        }
    };
    private Integer completionCount;

    private static final String KEY_INTERVAL = "interval";
    private static final String KEY_INTERVAL_UNIT = "intervalUnit";
    private static final String KEY_FIRST_STARTED_DATE = "firstStartedDate";
    private static final String KEY_LAST_COMPLETED_DATE = "lastCompletedDate";
    private static final String KEY_NEXT_SCHEDULED_DATE = "nextScheduledDate";
    private static final String KEY_COMPLETION_COUNT = "completionCount";
    private static final String KEY_COMPLETED_DATES = "completedDates";

    public RecurringTaskInfo(Integer interval, IntervalUnit intervalUnit, Instant firstStartedDate) {
        this.interval = interval;
        this.intervalUnit = intervalUnit;
        this.firstStartedDate = firstStartedDate.atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        this.lastCompletedDate = null;
        this.nextScheduledDate = firstStartedDate;
        this.completionCount = 0;

    }

    public Document toDocument() {
        return new Document().append(KEY_INTERVAL, interval)
                .append(KEY_INTERVAL_UNIT, intervalUnit)
                .append(KEY_FIRST_STARTED_DATE, firstStartedDate)
                .append(KEY_LAST_COMPLETED_DATE, lastCompletedDate)
                .append(KEY_NEXT_SCHEDULED_DATE, nextScheduledDate)
                .append(KEY_COMPLETION_COUNT, completionCount)
                .append(KEY_COMPLETED_DATES, completedDates);
    }

    public static RecurringTaskInfo fromDocument(Document document) {
        var recurringTaskInfo = new RecurringTaskInfo();
        recurringTaskInfo.setInterval(document.getInteger(KEY_INTERVAL));
        recurringTaskInfo.setIntervalUnit(IntervalUnit.valueOf(document.getString(KEY_INTERVAL_UNIT)));

        recurringTaskInfo.setFirstStartedDate(document.get(KEY_FIRST_STARTED_DATE, Date.class).toInstant());
        Date lastCompletedDate = document.get(KEY_LAST_COMPLETED_DATE, Date.class);
        recurringTaskInfo.setLastCompletedDate(lastCompletedDate == null ? null : lastCompletedDate.toInstant());
        Date nextScheduledDate = document.get(KEY_NEXT_SCHEDULED_DATE, Date.class);
        recurringTaskInfo.setNextScheduledDate(nextScheduledDate == null ? null : nextScheduledDate.toInstant());
        recurringTaskInfo.setCompletionCount(document.getInteger(KEY_COMPLETION_COUNT));
        recurringTaskInfo.completedDates.addAll(document.getList(KEY_COMPLETED_DATES, Date.class));

        return recurringTaskInfo;
    }

    public void completeOnce() {
        LocalDateTime now = LocalDateTime.now();
        ChronoUnit chronoUnit = switch (intervalUnit) {
            case DAYS -> ChronoUnit.DAYS;
            case MONTHS -> ChronoUnit.MONTHS;
        };

        completionCount += 1;
        lastCompletedDate = now.atZone(ZoneId.systemDefault()).toInstant();

        completedDates.add(Date.from(lastCompletedDate));
        nextScheduledDate = computeNextDate(lastCompletedDate);

    }

    /**
     * Get the number of days missed
     * Until a full 24 hour has passed, it won't count as +1
     *
     * @return
     */
    public int getMissedDaysCount() {
        var now = Instant.now();
        Duration duration = Duration.between(firstStartedDate, now);
        return (int) duration.toDays();
    }

    public Instant computeNextDate(Instant currentDate) {
        ZoneId zone = ZoneId.systemDefault();

        LocalDate dateLocale = LocalDate.from(currentDate
                .atZone(zone)
                .toLocalDate().atStartOfDay(zone));
        LocalDate newDate = null;
        return switch (intervalUnit) {
            case DAYS -> {
                newDate = dateLocale.plusDays(interval);
                yield newDate.atStartOfDay(zone).toInstant();
            }
            case MONTHS -> {
                newDate = dateLocale.plusMonths(interval);
                yield newDate.withDayOfMonth(1)
                        .atStartOfDay(zone)
                        .toInstant();
            }
        };

    }
}
