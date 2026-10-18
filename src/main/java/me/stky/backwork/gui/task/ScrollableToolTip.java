package me.stky.backwork.gui.task;

import javax.swing.*;
import javax.swing.plaf.ToolTipUI;
import java.awt.*;


/*
    AI-generated, just for tooltip that have long text
 */
public class ScrollableToolTip extends JToolTip {

    private static final int MAX_WIDTH = 500;
    private static final int MAX_HEIGHT = 700;

    private final JEditorPane htmlPane;
    private final JScrollPane scrollPane;


    public ScrollableToolTip() {
        setUI(new ToolTipUI() {
        });
        setLayout(new BorderLayout());

        htmlPane = new JEditorPane();
        htmlPane.setContentType("text/html");
        htmlPane.setEditable(false);
        htmlPane.setOpaque(true);

        // Force HTML content to wrap to the available width
        htmlPane.putClientProperty(
                javax.swing.JEditorPane.HONOR_DISPLAY_PROPERTIES,
                Boolean.TRUE
        );

        scrollPane = new JScrollPane(htmlPane);
        scrollPane.setPreferredSize(new Dimension(MAX_WIDTH, 200));

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void setTipText(String text) {
        super.setTipText(text);
        htmlPane.setText(text == null ? "" : text);
        htmlPane.setCaretPosition(0);
    }

    @Override
    public Dimension getPreferredSize() {
        int width = MAX_WIDTH;

        htmlPane.setSize(width, Short.MAX_VALUE);

        Dimension contentSize = htmlPane.getPreferredSize();

        int height = Math.min(contentSize.height + 20, MAX_HEIGHT);

        return new Dimension(width, height);
    }
}
