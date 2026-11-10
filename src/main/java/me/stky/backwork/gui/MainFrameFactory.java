package me.stky.backwork.gui;

import me.stky.backwork.service.RoutineService;
import me.stky.backwork.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainFrameFactory {

    private final TaskService apiService;
    private final RoutineService routineService;
    private MainFrame frame;

    @Autowired
    public MainFrameFactory(TaskService apiService, RoutineService routineService) {
        this.apiService = apiService;
        this.routineService = routineService;
    }


    public MainFrame getMainFrame() {
        if (frame == null) {
            frame = new MainFrame(apiService, routineService);
        }
        return frame;
    }
}
