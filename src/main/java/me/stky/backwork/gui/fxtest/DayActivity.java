package me.stky.backwork.gui.fxtest;

import java.time.LocalDate;
import java.util.Objects;

public final class DayActivity {

    private final LocalDate date;
    private final int completedHabits;
    private final int totalHabits;


    public DayActivity(
            LocalDate date,
            int completedHabits,
            int totalHabits) {

        this.date = Objects.requireNonNull(date, "date cannot be null");

        if (totalHabits < 0) {
            throw new IllegalArgumentException(
                    "Total habits cannot be negative");
        }

        if (completedHabits < 0 || completedHabits > totalHabits) {
            throw new IllegalArgumentException(
                    "Completed habits must be between 0 and total habits");
        }

        this.completedHabits = completedHabits;
        this.totalHabits = totalHabits;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getCompletedHabits() {
        return completedHabits;
    }

    public int getTotalHabits() {
        return totalHabits;
    }

    public double getCompletionPercentage() {

        if (totalHabits == 0) {
            return 0.0;
        }

        return (completedHabits * 100.0) / totalHabits;
    }

    public boolean hasActivity() {
        return completedHabits > 0;
    }
}
