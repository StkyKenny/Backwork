package me.stky.backwork.gui;

import lombok.Data;
import me.stky.backwork.model.Task;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

@Data
public class TaskComponent extends JPanel {

    private Task task;
    private MainFrame mainframe;
    private JCheckBox checkBox;
    private int height = 40;


    public TaskComponent(MainFrame mainframe, Task task, Consumer<TaskComponent> consumer) {
        this.mainframe = mainframe;
        this.task = task;
        checkBox = new JCheckBox(task.getName());

        init(consumer);
        setMinimumSize(
                new Dimension(Integer.MIN_VALUE, height)
        );
        setMaximumSize(
                new Dimension(Integer.MAX_VALUE, height)
        );

    }

    private void init(Consumer<TaskComponent> consumer) {
        checkBox.setFont(checkBox.getFont().deriveFont(18f));
        checkBox.addActionListener(e -> {

            String soundName = "src/main/resources/sfx/bloopsfx_cut.wav";

            try {
                AudioInputStream audioInputStream =
                        AudioSystem.getAudioInputStream(new File(soundName));

                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            consumer.accept(this);
        });

        add(checkBox);
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

}
