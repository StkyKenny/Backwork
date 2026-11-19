package me.stky.backwork.gui.fxtest;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;


// IMPORTANT PUT AWT import under, avoid IntelliJ auto-optimize import
import javax.swing.*;
import java.awt.*;

/**
 * A JPanel wrapper that embeds a JavaFX scene containing a simple form layout.
 */
public class FXEmbedDraft extends JPanel {

    private final JFXPanel fxPanel;
    private final VBox rootVBox;
    private final TextField nameField;
    private final TextArea commentArea;
    private final Button submitButton;
    private final Label statusLabel;

    public FXEmbedDraft() {
        fxPanel = new JFXPanel();

        rootVBox = new VBox(10);

        nameField = new TextField();
        nameField.setText("Enter your name...");
        commentArea = new TextArea();
        commentArea.setText("Enter your comments here...");

        submitButton = new Button("Submit Data");
        statusLabel = new Label("Status: Ready to submit.");
        statusLabel.setAlignment(javafx.geometry.Pos.CENTER);

        submitButton.setOnAction(e -> {
            String name = nameField.getText();
            String comment = commentArea.getText();

            if (name.isEmpty() || comment.isEmpty()) {
                statusLabel.setText("Status: Error! Both fields must be filled.");
            } else {
                // Displaying the captured data in the status label
                statusLabel.setText(String.format(
                        "Status: Success! Name: %s, Comments: %s",
                        name,
                        comment.length() + " characters."
                ));
            }
        });

        VBox vbox = new VBox(5); // Spacing inside the button list
        Label label = new Label("this is VBox example");
        vbox.getChildren().add(label);
        for (int i = 0; i < 10; i++) {
            vbox.getChildren().add(new Button("Button " + (i + 1)));
        }

        rootVBox.getChildren().addAll(
                nameField,
                commentArea,
                submitButton,
                statusLabel,
                vbox
        );

        rootVBox.setAlignment(javafx.geometry.Pos.CENTER);

        Scene scene = new Scene(rootVBox, 300, 300);
        fxPanel.setScene(scene);

        //Wrap JFXPanel in a Swing JPanel
        this.setLayout(new BorderLayout());
        this.add(fxPanel, BorderLayout.CENTER);
    }
}