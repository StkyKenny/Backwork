package me.stky.backwork.gui;

import me.stky.backwork.service.CustomExecutionsService;
import me.stky.backwork.service.RoutineService;
import me.stky.backwork.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainFrameFactory {

    private final TaskService apiService;
    private final RoutineService routineService;
    CustomExecutionsService customExecutionsService;
    private MainFrame frame;

    @Autowired
    public MainFrameFactory(TaskService apiService, RoutineService routineService,
                            CustomExecutionsService customExecutionsService) {
        this.apiService = apiService;
        this.routineService = routineService;
        this.customExecutionsService = customExecutionsService;
    }


    public MainFrame getMainFrame() {
        if (frame == null) {
            frame = new MainFrame(apiService, routineService, customExecutionsService);
        }
        return frame;
    }
}
