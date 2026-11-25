package me.stky.backwork.gui.fxtest;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import me.stky.backwork.gui.MainFrame;
import me.stky.backwork.model.task.Task;
import me.stky.backwork.service.TaskService;

import javax.swing.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivityPanel extends JFXPanel {


    private final MainFrame mainframe;
    private final TaskService taskService;
    private java.util.List<Task> tasks = new ArrayList<>();
    private VBox mainContainer = new VBox();
    ScrollPane scrollPane = new ScrollPane();

    public ActivityPanel(MainFrame mainFrame, TaskService taskService) {
        this.taskService = taskService;
        this.mainframe = mainFrame;
        createAnalyticsPanel();
    }

    public void createAnalyticsPanel() {


        // Use a JPanel inside the JFXPanel to hold all the individual heatmaps
        JPanel heatmapContainer = new JPanel();
        heatmapContainer.setLayout(new BoxLayout(heatmapContainer, BoxLayout.Y_AXIS));
        heatmapContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        // We need to pass the container to the initialization method
        Platform.runLater(() -> {
            initializeHeatmap();

            // https://stackoverflow.com/questions/32739269/javafx-how-do-i-change-the-amount-by-which-scrollpane-scrolls
            final double SPEED = 0.005;
            var b = scrollPane.getContent().getOnScroll();
            scrollPane.getContent().setOnScroll(scrollEvent -> {
                double deltaY = scrollEvent.getDeltaY() * SPEED;
                scrollPane.setVvalue(scrollPane.getVvalue() - deltaY);
            });
            Scene scene = new Scene(scrollPane);

            scene.setFill(javafx.scene.paint.Color.web("#0d1117"));
            setScene(scene);

        });


    }

    // Modify initializeHeatmap to accept the container
    private void initializeHeatmap() {
        List<Task> tasks = taskService.getTaskSortedByDeadline();


        for (Task task : tasks) {
            if (task.getRecurringTaskInfo() == null) {
                continue;
            }
            Map<LocalDate, String> history = task.getRecurringTaskInfo().getCompletedDates().stream()
                    .collect(Collectors.toMap(date -> date.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate(), completedDate -> ""));

            LocalDate today = LocalDate.now();
            LocalDate start = today.minusMonths(7);

            HabitHeatmap heatmap = new HabitHeatmap(
                    start,
                    today,
                    task,
                    history,
                    day -> {
                        // Handle selection logic if needed
                        System.out.println("Task " + task.getName() + " day selected.");
                    }
            );
            // FIX: Set the vertical growth priority for the heatmap component
            heatmap.setMaxHeight(Double.MAX_VALUE);
            mainContainer.getChildren().add(heatmap);

        }
        scrollPane.setContent(mainContainer);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);

    }
}
