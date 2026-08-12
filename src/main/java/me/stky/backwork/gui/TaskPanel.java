package me.stky.backwork.gui;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class TaskPanel extends JPanel {

    public JPanel taskPanel;
    private MainFrame mainframe;

    public TaskPanel(MainFrame mainframe) {
        taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));


        String[] tasks = {
                "something1",
                "something2",
                "something3",
                "something4",
                "something5"
        };

        for (String task : tasks) {
            JCheckBox checkBox = new JCheckBox(task);
            checkBox.setFont(checkBox.getFont().deriveFont(18f));
            checkBox.addActionListener(e -> {

                String soundName = "src/main/resources/sfx/bloopsfx_cut.wav";
                AudioInputStream audioInputStream = null;
                try {
                    audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile());
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    clip.start();

                    // TODO with correct exception management / refactor it out ?
                } catch (UnsupportedAudioFileException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (Exception ex) {
                }

                taskPanel.remove(checkBox);
                mainframe.appendMessage("Completed : " + checkBox.getText());
                // Additionnal logic
                taskPanel.revalidate();
                taskPanel.repaint();
            });

            taskPanel.add(checkBox);
        }


        // THIS IS IMPORTANT
        setLayout(new BorderLayout());
        add(taskPanel, BorderLayout.NORTH);
    }

}