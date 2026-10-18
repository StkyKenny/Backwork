package me.stky.backwork.gui;

import javax.swing.*;
import java.awt.*;

public class ChatBox extends JPanel {

    private JTextArea chatArea;
    private JTextArea messageArea;
    private JButton sendButton;
    private JButton fakeBotButton;

    public ChatBox() {

        // Chat display
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 15));

        JScrollPane chatScroll = new JScrollPane(chatArea);

        // Multiline message input
        messageArea = new JTextArea(3, 20);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane inputScroll = new JScrollPane(messageArea);

        // Buttons
        sendButton = new JButton("Send");
        fakeBotButton = new JButton("Fake Bot Reply");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(fakeBotButton);
        buttonPanel.add(sendButton);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );
        inputPanel.add(inputScroll, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout(5, 5));
        add(chatScroll, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // Send message
        sendButton.addActionListener(e -> sendMessage());

        // Fake chatbot reply
        fakeBotButton.addActionListener(e -> fakeBotReply());

        setVisible(true);
    }

    private void sendMessage() {
        String message = messageArea.getText().trim();

        if (!message.isEmpty()) {
            chatArea.append("You: " + message + "\n\n");
            messageArea.setText("");
            scrollToBottom();
        }

        messageArea.requestFocusInWindow();
    }

    private void fakeBotReply() {
        String[] replies = {
                "Hello! How can I help you?",
                "That's interesting!",
                "I'm just a fake chatbot!",
                "Can you tell me more?",
                "Thanks for your message!"
        };

        int index = (int) (Math.random() * replies.length);

        chatArea.append("ChatBot: " + replies[index] + "\n\n");
        scrollToBottom();
        messageArea.requestFocusInWindow();
    }

    private void scrollToBottom() {
        chatArea.setCaretPosition(
                chatArea.getDocument().getLength()
        );
    }

}
