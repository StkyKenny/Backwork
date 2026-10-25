package me.stky.backwork.gui;

import me.stky.backwork.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainFrameFactory {

    private final TaskService apiService;
    private MainFrame frame;

    @Autowired
    public MainFrameFactory(TaskService apiService) {
        this.apiService = apiService;
    }


    public MainFrame getMainFrame() {
        if (frame == null) {
            frame = new MainFrame(apiService);
        }
        return frame;
    }
}
