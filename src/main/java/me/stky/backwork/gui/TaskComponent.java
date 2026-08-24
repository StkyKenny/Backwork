package me.stky.backwork.gui;

import lombok.Getter;
import me.stky.backwork.model.Task;
import me.stky.backwork.service.Helper;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

public class TaskComponent extends JPanel {

    @Getter
    private Task task;
    private final MainFrame mainframe;
    private JCheckBox checkBox;
    private final int height = 40;
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd | HH:mm:ss");
    private Font defaultFont;

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
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                setToolTipText("<html><p>" + Helper.prettyPrintUsingGson(task)
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\n", "<br>")
                        .replace(" ", "&nbsp;")
                        + "</p></html>");
            }
        });
    }

    /**
     * @param sideEffect Consume self, for additional instructions that aren't linked to this component
     */
    private void init(Consumer<TaskComponent> sideEffect) {
        var taskName = task.getName();
        var lowerInfo = timeFormat.format(Date.from(task.getCreatedDate()));
        var recurTaskInfo = task.getRecurringTaskInfo();

        if (recurTaskInfo != null) {
            taskName = "🔁 " + task.getName();
            lowerInfo += " | Each " + recurTaskInfo.getInterval() + " " + recurTaskInfo.getIntervalUnit();
        }
        checkBox = new JCheckBox(taskName);
        checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD, 18f));
        defaultFont = checkBox.getFont();
        updateGUIGivenStatus();

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
            updateGUIGivenStatus();
            sideEffect.accept(this);
        });


        add(checkBox);
        var dateLabel = new JLabel(lowerInfo);
        add(dateLabel);
    }

    public void updateTask(Task task) {
        this.task = task;
        updateGUIGivenStatus();
    }

    public void updateGUIGivenStatus() {
        switch (task.getStatus()) {
            case TODO -> {
                checkBox.setFont(defaultFont);
                checkBox.setEnabled(true);
                checkBox.setSelected(false);
                if (task.getRecurringTaskInfo() != null) {
                    checkBox.setForeground(new Color(234, 88, 12));
                } else {
                    checkBox.setForeground(new Color(234, 234, 12));
                }
            }
            case IN_PROGRESS -> {
                checkBox.setForeground(new Color(37, 99, 235));
            }
            case COMPLETED -> {
                Font newFont = checkBox.getFont().deriveFont(
                        Map.of(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON)
                );
                checkBox.setForeground(new Color(100, 116, 139));
                checkBox.setFont(newFont);
                checkBox.setEnabled(false);
            }
            case ARCHIVED -> {
                checkBox.setForeground(new Color(124, 58, 237));
            }
        }
    }

}
