package me.stky.backwork.gui;

import lombok.Getter;
import me.stky.backwork.model.Task;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

public class TaskComponent extends JPanel {

    @Getter
    private Task task;
    private final MainFrame mainframe;
    private JCheckBox checkBox;
    private final int height = 40;

    /**
     * Constructor for TaskComponent
     *
     * @param mainframe
     * @param task
     * @param sideEffect Consume self, for additional instructions that aren't linked to this component
     */
    public TaskComponent(MainFrame mainframe, Task task, Consumer<TaskComponent> sideEffect) {
        this.mainframe = mainframe;
        this.task = task;
        init(sideEffect);
        setMinimumSize(
                new Dimension(Integer.MIN_VALUE, height)
        );
        setMaximumSize(
                new Dimension(Integer.MAX_VALUE, height)
        );

    }

    /**
     * @param sideEffect Consume self, for additional instructions that aren't linked to this component
     */
    private void init(Consumer<TaskComponent> sideEffect) {
        checkBox = new JCheckBox(task.getName());
        checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD, 18f));
        Font newFont = checkBox.getFont().deriveFont(
                Map.of(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON)
        );
        switch (task.getStatus()) {
            case TODO -> {
                checkBox.setForeground(new Color(234, 88, 12));
            }
            case IN_PROGRESS -> {
                checkBox.setForeground(new Color(37, 99, 235));
            }
            case COMPLETED -> {
                checkBox.setForeground(new Color(100, 116, 139));
                checkBox.setFont(newFont);
                checkBox.setEnabled(false);
            }
            case ARCHIVED -> {
                checkBox.setForeground(new Color(124, 58, 237));
            }
        }

        checkBox.addActionListener(_ -> {
            String soundName = "src/main/resources/sfx/bloopsfx_cut.wav";
            try {
                AudioInputStream audioInputStream =
                        AudioSystem.getAudioInputStream(new File(soundName));
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception _) {
                this.mainframe.appendMessageDEBUG("Error trying to play SFX :" + soundName);
            }
            sideEffect.accept(this);
        });

        add(checkBox);
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

}
