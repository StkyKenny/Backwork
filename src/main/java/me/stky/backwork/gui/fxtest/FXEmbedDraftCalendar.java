package me.stky.backwork.gui.fxtest;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DayViewBase;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.stky.backwork.gui.MainFrame;
import me.stky.backwork.service.TaskService;

import javax.swing.*;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * A JPanel wrapper that embeds a JavaFX scene containing a simple form layout.
 */
public class FXEmbedDraftCalendar extends JPanel {

    private final JFXPanel fxPanel;
    private final VBox rootVBox;
    private final TaskService taskService;

    public FXEmbedDraftCalendar(MainFrame mainFrame, TaskService taskService) {
        this.taskService = taskService;
        var task = taskService.getTaskSortedByDeadline().get(0);
        fxPanel = new JFXPanel();

        rootVBox = new VBox(10);

        CalendarView calendarView = new CalendarView(); // (1)
        calendarView.showWeekPage();
        var weekPage = calendarView.getWeekPage();
        var weekView = calendarView.getWeekPage()
                .getDetailedWeekView()
                .getWeekView();
        weekView.setHoursLayoutStrategy(
                DayViewBase.HoursLayoutStrategy.FIXED_HOUR_HEIGHT
        );

        weekView.setHourHeight(25);
        weekView.setVisibleHours(24);
        //weekPage.setMaxHeight(mainFrame.getHeight() * 0.9);

        Calendar birthdays = new Calendar("Birthdays"); // (2)
        Calendar holidays = new Calendar("Holidays");
        Calendar taskCalendar = new Calendar("Tasks");
        //taskCalendar.addEntry();
        var e = new Entry<>();
        System.out.println(task);
        e.setTitle(task.getName());
        e.setCalendar(taskCalendar);
        e.setLocation(task.getDescription());
        e.setInterval(LocalDate.ofInstant(task.getCreatedDate(), ZoneId.systemDefault()), LocalDate.ofInstant(task.getDeadline(), ZoneId.systemDefault()));

        birthdays.setStyle(Calendar.Style.STYLE1); // (3)
        holidays.setStyle(Calendar.Style.STYLE2);

        CalendarSource myCalendarSource = new CalendarSource("My Calendars"); // (4)
        myCalendarSource.getCalendars().addAll(birthdays, holidays, taskCalendar);

        calendarView.getCalendarSources().addAll(myCalendarSource); // (5)

        calendarView.setRequestedTime(LocalTime.now());

        /*var updateTimeThread = Thread.ofVirtual().name("Calendar: Update Time Thread").start(() -> {
            while (true) {
                Platform.runLater(() -> {
                    calendarView.setToday(LocalDate.now());
                    calendarView.setTime(LocalTime.now());
                });
                try {
                    // update every 10 seconds
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        updateTimeThread.setPriority(Thread.MIN_PRIORITY);
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();*/

        Timeline clockUpdater = new Timeline(
                new KeyFrame(Duration.ZERO, event -> {
                    calendarView.setToday(LocalDate.now());
                    calendarView.setTime(LocalTime.now());
                }),
                new KeyFrame(Duration.seconds(10))
        );

        clockUpdater.setCycleCount(Animation.INDEFINITE);
        clockUpdater.play();

        Scene scene = new Scene(calendarView);
        //Scene scene = new Scene(rootVBox, 300, 300);
        fxPanel.setScene(scene);

        //Wrap JFXPanel in a Swing JPanel
        this.setLayout(new BorderLayout());
        this.add(fxPanel, BorderLayout.CENTER);

        mainFrame.setSize(900, mainFrame.getHeight());
    }
}