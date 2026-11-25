package me.stky.backwork.gui.fxtest;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import me.stky.backwork.model.task.Task;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class HabitHeatmap extends VBox {

    // Grid appearance
    private static final double CELL_SIZE = 13;
    private static final double CELL_GAP = 4;
    private static final double LABEL_WIDTH = 85;
    private static final double MONTH_HEADER_HEIGHT = 25;

    private final HBox heatmap = new HBox();

    private static final DateTimeFormatter TOOLTIP_FORMAT =
            DateTimeFormatter.ofPattern(
                    "EEEE, MMMM d, yyyy", Locale.ENGLISH);

    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

    // Activity colors
    private static final Color[] COLORS = {
            Color.WHITE,
            Color.web("#e8f5e9"),
            Color.web("#b7e1c0"),
            Color.web("#66bb6a"),
            Color.web("#2e8b45"),
            Color.web("#08752d")
    };

    private LocalDate startDate;
    private LocalDate endDate;

    private Map<LocalDate, String> activities =
            new HashMap<>();

    private Consumer<DayActivity> onDaySelected;

    private final GridPane heatmapGrid = new GridPane();
    private final GridPane monthGrid = new GridPane();
    private final Task task;

    public HabitHeatmap(
            LocalDate startDate,
            LocalDate endDate,
            Task task,
            Map<LocalDate, String> activities,
            Consumer<DayActivity> onDaySelected) {

        this.task = task;

        validateDateRange(startDate, endDate);
        checkFxThread();

        this.startDate = startDate;
        this.endDate = endDate;
        this.onDaySelected = onDaySelected;

        setSpacing(10);
        setPadding(new Insets(15));

        //setActivityData(activities);
        buildLayout();
    }

    private void buildLayout() {

        ScrollPane scrollPane = renderMonths();

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Label taskName = new Label(task.getName());
        heatmap.getChildren().add(scrollPane);
        getChildren().setAll(taskName, heatmap);
        scrollPane.setMinHeight(160); // todo HARDCODED need to fix
    }

    private void renderHeatmap() {

        checkFxThread();

        heatmapGrid.getChildren().clear();
        monthGrid.getChildren().clear();

        LocalDate firstWeek = startDate.with(
                TemporalAdjusters.previousOrSame(
                        DayOfWeek.MONDAY));

        LocalDate lastWeek = endDate.with(
                TemporalAdjusters.previousOrSame(
                        DayOfWeek.MONDAY));

        int weekCount = (int) ChronoUnit.WEEKS.between(
                firstWeek,
                lastWeek
        ) + 1;

        //renderMonthLabels(firstWeek, weekCount);
        //renderDayCells(firstWeek, weekCount);
    }

    private void renderMonthLabels(
            LocalDate firstWeek,
            int weekCount) {

        java.time.YearMonth previousMonth = null;

        for (int week = 0; week < weekCount; week++) {

            LocalDate monday = firstWeek.plusWeeks(week);

            java.time.YearMonth monthToDisplay = null;

            // Prefer the month whose first day falls in this week.
            for (int day = 0; day < 7; day++) {

                LocalDate date = monday.plusDays(day);

                if (isInsideRange(date)
                        && date.getDayOfMonth() == 1) {

                    monthToDisplay =
                            java.time.YearMonth.from(date);

                    break;
                }
            }

            // Handle a range starting in the middle of a month.
            if (monthToDisplay == null) {

                for (int day = 0; day < 7; day++) {

                    LocalDate date = monday.plusDays(day);

                    if (isInsideRange(date)) {
                        monthToDisplay =
                                java.time.YearMonth.from(date);
                        break;
                    }
                }
            }

            if (monthToDisplay != null
                    && !monthToDisplay.equals(previousMonth)) {

                Label monthLabel = new Label(
                        monthToDisplay.format(MONTH_FORMAT));

                monthLabel.setStyle(
                        "-fx-font-size: 11px;"
                                + "-fx-font-weight: bold;"
                                + "-fx-text-fill: #666666;"
                );

                monthGrid.add(monthLabel, week, 0);
                monthGrid.setMaxHeight(Double.MAX_VALUE);
                previousMonth = monthToDisplay;
            }
        }
    }

    private void renderDayCells(
            LocalDate firstWeek,
            int weekCount) {


        Map<LocalDate, DayActivity> completedDatesActivity = task.getRecurringTaskInfo().getCompletedDates().stream()
                .map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toMap(date -> date,
                        date -> new DayActivity(date, 1, 1)));


        for (int week = 0; week < weekCount; week++) {

            LocalDate monday = firstWeek.plusWeeks(week);

            for (int day = 0; day < 7; day++) {

                LocalDate date = monday.plusDays(day);

                // Keep calendar alignment but don't show
                // interactive cells outside the requested range.
                if (!isInsideRange(date)) {
                    Region emptyCell = new Region();

                    setCellSize(emptyCell);

                    heatmapGrid.add(emptyCell, week, day);

                    continue;
                }

                DayActivity activity = completedDatesActivity.get(date);

                if (activity == null) {
                    activity = new DayActivity(date, 0, 0);
                }
                Region cell = createDayCell(date, activity);

                heatmapGrid.add(cell, week, day);
            }
        }
        heatmapGrid.setMinHeight(50);
    }

    private Region createDayCell(LocalDate date, DayActivity dayActivity) {

        Region cell = new Region();

        cell.setMinSize(CELL_SIZE, CELL_SIZE);
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setMaxSize(CELL_SIZE, CELL_SIZE);

        cell.setBackground(new Background(
                new BackgroundFill(
                        date.isAfter(LocalDate.now()) ? Color.DARKGRAY : getActivityColor(dayActivity),
                        new CornerRadii(2),
                        Insets.EMPTY
                )
        ));

        cell.setBorder(new Border(
                new BorderStroke(
                        Color.LIGHTGRAY,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(2),
                        new BorderWidths(0.5)
                )
        ));

        cell.setCursor(Cursor.HAND);

        Tooltip.install(
                cell,
                new Tooltip(
                        date + "\n" +
                                dayActivity.getCompletedHabits() + " / " +
                                dayActivity.getTotalHabits() + " habits completed"
                )
        );

        /*cell.setOnMouseClicked(event -> {
            if (onDaySelected != null) {
                onDaySelected.accept(new DayActivity());
            }
        });*/

        return cell;
    }

    private GridPane createMonthGrid(YearMonth month, Map<LocalDate, DayActivity> completedDatesActivity) {
        GridPane grid = new GridPane();

        grid.setHgap(CELL_GAP);
        grid.setVgap(CELL_GAP);

        LocalDate firstDay = month.atDay(1);
        LocalDate lastDay = month.atEndOfMonth();

        int startColumn = firstDay.getDayOfWeek().getValue() - 1;

        for (LocalDate date = firstDay;
             !date.isAfter(lastDay);
             date = date.plusDays(1)) {

            int dayIndex = date.getDayOfMonth() - 1;
            int column = (startColumn + dayIndex) % 7;
            int row = (startColumn + dayIndex) / 7;

            DayActivity activity = completedDatesActivity.get(date);

            if (activity == null) {
                activity = new DayActivity(date, 0, 0);
            }
            Region cell = createDayCell(date, activity);

            grid.add(cell, column, row);
        }

        return grid;
    }

    private ScrollPane renderMonths() {

        HBox monthsContainer = new HBox(24);
        monthsContainer.setAlignment(Pos.TOP_LEFT);
        monthsContainer.setSpacing(10);
        monthsContainer.setPadding(new Insets(8));

        YearMonth current = YearMonth.from(startDate);
        YearMonth last = YearMonth.from(endDate);

        Map<LocalDate, DayActivity> completedDatesActivity = task.getRecurringTaskInfo().getCompletedDates().stream()
                .map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toMap(date -> date,
                        date -> new DayActivity(date, 1, 1)));


        while (!current.isAfter(last)) {

            VBox monthBox = new VBox(8);

            Label monthLabel = new Label(
                    current.getMonth().getDisplayName(
                            TextStyle.FULL,
                            Locale.getDefault()
                    )
            );

            GridPane monthGrid = createMonthGrid(current, completedDatesActivity);

            monthBox.getChildren().addAll(
                    monthLabel,
                    monthGrid
            );

            // Keep each month's natural width.
            monthBox.setMinHeight(Region.USE_PREF_SIZE);

            monthsContainer.getChildren().add(monthBox);

            current = current.plusMonths(1);
        }

        ScrollPane scrollPane = new ScrollPane(monthsContainer);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(false);

        scrollPane.setMaxHeight(Double.MAX_VALUE);
        scrollPane.setMaxWidth(Double.MAX_VALUE);


        scrollPane.setHvalue(1.0);

        return scrollPane;
    }


    private String buildTooltipText(DayActivity activity) {

        return activity.getDate().format(TOOLTIP_FORMAT)
                + "\n"
                + "Completed habits: "
                + activity.getCompletedHabits()
                + "\n"
                + "Total habits: "
                + activity.getTotalHabits()
                + "\n"
                + "Completion: "
                + String.format(
                Locale.ROOT,
                "%.1f%%",
                activity.getCompletionPercentage()
        );
    }

    private Color getActivityColor(DayActivity dayActivity) {

        var percentage = dayActivity.getCompletionPercentage();
        if (percentage == 0) {
            return COLORS[0];
        }

        if (percentage <= 25) {
            return COLORS[1];
        }

        if (percentage <= 50) {
            return COLORS[2];
        }

        if (percentage <= 75) {
            return COLORS[3];
        }

        if (percentage < 100) {
            return COLORS[4];
        }

        return COLORS[5];
    }

    private void setCellSize(Region cell) {

        cell.setMinSize(CELL_SIZE, CELL_SIZE);
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setMaxSize(CELL_SIZE, CELL_SIZE);
    }

    private boolean isInsideRange(LocalDate date) {

        return !date.isBefore(startDate)
                && !date.isAfter(endDate);
    }

    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(endDate, "endDate");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date must not be before start date");
        }
    }

    private void checkFxThread() {

        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "HabitHeatmap must be accessed "
                            + "on the JavaFX Application Thread");
        }
    }

    // Public API for updating activity data

    /*public void setActivityData(Map<LocalDate, String> activities) {

        checkFxThread();
        Objects.requireNonNull(activities, "activities");

        Map<LocalDate, String> copy = new HashMap<>(activities);

        for (Map.Entry<LocalDate, String> entry : copy.entrySet()) {

            if (entry.getKey() == null || entry.getValue() == null) {

                throw new IllegalArgumentException(
                        "Activity dates and values cannot be null");
            }
        }

        this.activities = copy;

        // Re-render only when the grid already exists.
        if (heatmapGrid != null
                && getChildren().size() > 0) {

            renderHeatmap();
        }
    }*/

    /*public void setDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        checkFxThread();

        validateDateRange(startDate, endDate);

        this.startDate = startDate;
        this.endDate = endDate;

        renderHeatmap();
    }*/
}