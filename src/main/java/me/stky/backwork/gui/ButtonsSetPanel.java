package me.stky.backwork.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static me.stky.backwork.gui.GuiConfig.BUTTON_WIDTH;

public class ButtonsSetPanel extends JPanel {

    public JPanel grid;
    Dimension size = new Dimension(BUTTON_WIDTH, 30);

    public ButtonsSetPanel(int rows, int cols, int hgap, int vgap) {
        this.grid = new JPanel((new GridLayout(rows, cols, hgap, vgap)));

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

    public void addShortcut(String name, String path, Consumer<String> messageHandler) {

        var button = new JButton(name);
        button.setPreferredSize(size);
        button.addActionListener(_ -> {
            messageHandler.accept(name + " starting ...");
            Thread.startVirtualThread(() -> {
                try {
                    new ProcessBuilder(path).start();
                    SwingUtilities.invokeLater(() ->
                            messageHandler.accept(name + " has started")
                    );
                } catch (IOException e) {
                    messageHandler.accept("Error trying to start " + name + " :" + e.getMessage());
                }
            });
        });
        grid.add(button);
    }

}
