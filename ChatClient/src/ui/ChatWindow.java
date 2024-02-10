package ui;

import javax.swing.*;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.text.SimpleDateFormat;
import java.util.Date;

class ChatWindow extends JFrame implements ActionListener {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private JTextPane chatPane;
    private JTextArea chatArea;
    private JTextArea messageHistoryArea;
    private JTextField messageField;
    private JButton sendButton;
    private JComboBox<String> emojiComboBox;
    private JLabel selectedEmojiLabel;
    private JButton disconnectButton;

    public ChatWindow(Socket socket, String username) {
        this.socket = socket;
        this.username = username;

        setTitle("Chatroom - " + username);
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new BorderLayout());

        // Add a combobox for emoji selection
        emojiComboBox = new JComboBox<>(new String[]{"", "😊", "😂", "😍", "👍", "🎉","⚽","🎯","🚑","🥷","🧜‍♂️","🧚‍♂️","💪"}); // Sample emojis
        emojiComboBox.addActionListener(this);
        buttonPanel.add(emojiComboBox, BorderLayout.WEST);

        // Add a label for selected emoji
        selectedEmojiLabel = new JLabel("Selected Emoji: ");
        buttonPanel.add(selectedEmojiLabel, BorderLayout.CENTER);

        messageField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(this);
        buttonPanel.add(messageField, BorderLayout.CENTER);
        buttonPanel.add(sendButton, BorderLayout.EAST);

        chatPane = new JTextPane();
        chatPane.setEditable(false);
        JScrollPane scrollPane2 = new JScrollPane(chatPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(this);

        messagePanel.add(buttonPanel, BorderLayout.SOUTH); // Move buttonPanel to the NORTH

        JScrollPane scrollPane1 = new JScrollPane(chatArea);
        messagePanel.add(scrollPane1, BorderLayout.CENTER);

        messagePanel.add(disconnectButton, BorderLayout.NORTH); // Add the Disconnect button to the SOUTH

        panel.add(messagePanel, BorderLayout.CENTER);
        chatPane.setEditorKit(new StyledEditorKit());

        add(panel);

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Start a thread to continuously listen for messages from the server
            new Thread(() -> {
                try {
                    String message;

                    while ((message = reader.readLine()) != null) {
                        final String finalMessage = message; // Create a final variable
                        SwingUtilities.invokeLater(() -> {
                            // Append received message to message history
                            Date now = new Date();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

                            String timestamp = dateFormat.format(now) + " " + timeFormat.format(now);
                            //chatArea.append(finalMessage);
                            chatArea.append("[" + timestamp + "] " + finalMessage + "\n"); // Append received message to chat area
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            String message = messageField.getText();
            String selectedEmoji = (String) emojiComboBox.getSelectedItem();

            // Append the user's typed message directly to the chat area
            sendMessage(username + ": " + message + " " + selectedEmoji, true);

            // Send the message to the server
            writer.println(message + " " + selectedEmoji);
            messageField.setText("");
        } else if (e.getSource() == emojiComboBox) {
            updateSelectedEmojiLabel(); // Update selected emoji label when an emoji is selected
        } else if (e.getSource() == disconnectButton) {
            disconnect();
        }
    }

    // Update selected emoji label
    private void updateSelectedEmojiLabel() {
        String selectedEmoji = (String) emojiComboBox.getSelectedItem();
        selectedEmojiLabel.setText("Selected Emoji: " + selectedEmoji);
    }

    private void disconnect() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to disconnect?", "Disconnect", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            writer.println(username + " has left the chat.");
            dispose();
        }
    }

    private void sendMessage(String finalMessage, boolean isOutgoing) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        String timestamp = dateFormat.format(now) + " " + timeFormat.format(now);
        String formattedMessage = "[" + timestamp + "] " + finalMessage + "\n";

        if (isOutgoing) {
            // Determine the number of invisible characters based on the alignment
            int invisibleChars = 100;

            // Add invisible characters to the message
            StringBuilder paddedMessage = new StringBuilder();
            for (int i = 0; i < invisibleChars; i++) {
                paddedMessage.append(" ");
            }
            paddedMessage.append(formattedMessage);

            // Append the padded message to the chat area without adding an extra newline
            chatArea.append(paddedMessage.toString());
        } else {
            // Append incoming message directly without padding
            chatArea.append(formattedMessage);
        }
    }

    private void appendToMessageHistory(String message) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        String timestamp = dateFormat.format(now) + " " + timeFormat.format(now);
        messageHistoryArea.append("[" + timestamp + "] " + message + "\n");
    }
}