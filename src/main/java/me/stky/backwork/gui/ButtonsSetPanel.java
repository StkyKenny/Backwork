package me.stky.backwork.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import static me.stky.backwork.gui.GuiConfig.BUTTON_WIDTH;

public class ButtonsSetPanel extends JPanel {

    public JPanel grid;
    private MainFrame mainframe;
    Dimension size = new Dimension(BUTTON_WIDTH, 30);

    public ButtonsSetPanel(MainFrame mainframe, int rows, int cols, int hgap, int vgap) {
        this.grid = new JPanel((new GridLayout(rows, cols, hgap, vgap)));
        this.mainframe = mainframe;
        // THIS IS IMPORTANT
        setLayout(new BorderLayout());
        add(grid, BorderLayout.NORTH);
    }

    public ButtonsSetPanel(int rows, int cols, int hgap, int vgap, List<JButton> buttons) {
        this.grid = new JPanel((new GridLayout(rows, cols, hgap, vgap)));
        for (int i = 0; i < buttons.size(); i++) {
            grid.add(buttons.get(i));
        }
    }

    public void addButton(String name, ActionListener action) {
        var button = new JButton(name);
        button.setPreferredSize(size);
        button.addActionListener(action);
        grid.add(button);
    }

    public void addShortcut(String name, String path) {

        var button = new JButton(name);
        button.setPreferredSize(size);
        button.addActionListener(_ -> {
            mainframe.appendMessage(name + " starting ...");
            Thread.startVirtualThread(() -> {
                try {
                    new ProcessBuilder(path).start();
                    SwingUtilities.invokeLater(() ->
                            mainframe.appendMessage(name + " has started")
                    );
                } catch (IOException e) {
                    mainframe.appendMessage("Error trying to start " + name + " :" + e.getMessage());
                }
            });
        });
        grid.add(button);
    }

}
