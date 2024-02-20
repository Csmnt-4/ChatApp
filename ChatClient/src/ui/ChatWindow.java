package ui;

import manager.ConnectionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatWindow extends JFrame implements ActionListener {

    private final ConnectionManager connectionManagerReference;
    private final JTextPane chatPane;
    private final JTextArea chatArea;
    private final JTextField messageField;
    private final JButton sendButton;
    private final JButton loadPreviousMessagesButton;
    private final JComboBox<String> emojiComboBox;
    private final JLabel selectedEmojiLabel;
    private final JPanel topPane;
    private final JPanel pane;
    private final JButton disconnectButton;
    public boolean hasNewMessages = false;
    private ArrayList<HashMap<String, String>> messageList = new ArrayList<>();
    private int numberOfMessages = 0;

    public ChatWindow(String username, String chatName, ConnectionManager connectionManager) {
        connectionManagerReference = connectionManager;

        setTitle("Chatroom - " + chatName + " (" + username + ")");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        pane.add(scrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel();
        JPanel buttonPanel = new JPanel(new BorderLayout());

        // Add a combobox for emoji selection
        emojiComboBox = new JComboBox<>(new String[]{"", "😊", "😂", "😍", "👍", "🎉", "⚽", "🎯", "🚑", "🥷", "🧜‍♂️", "🧚‍♂️", "💪"}); // Sample emojis
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
        pane.add(scrollPane, BorderLayout.CENTER);
        messagePanel.setLayout(new BorderLayout(0, 0));

        messagePanel.add(buttonPanel, BorderLayout.SOUTH); // Move buttonPanel to the NORTH

        JScrollPane scrollPane1 = new JScrollPane(chatArea);
        messagePanel.add(scrollPane1);

        pane.add(messagePanel);

        topPane = new JPanel();
        messagePanel.add(topPane, BorderLayout.NORTH);
        topPane.setLayout(new BorderLayout(0, 0));

        disconnectButton = new JButton("Disconnect");
        disconnectButton.setHorizontalAlignment(SwingConstants.LEFT);
        disconnectButton.setAlignmentY(Component.TOP_ALIGNMENT);
        topPane.add(disconnectButton, BorderLayout.WEST);

        loadPreviousMessagesButton = new JButton("Load previous");
        loadPreviousMessagesButton.setHorizontalAlignment(SwingConstants.RIGHT);
        topPane.add(loadPreviousMessagesButton, BorderLayout.EAST);

        JLabel currentlyTypingLabel = new JLabel("");
        topPane.add(currentlyTypingLabel, BorderLayout.CENTER);
        chatPane.setEditorKit(new StyledEditorKit());

        getContentPane().add(pane);

        connectionManagerReference.needsToUpdateChatWindow = true;
        new Thread(this::updateMessages).start();

    }

    private void updateMessages() {
        while (connectionManagerReference.needsToUpdateChatWindow) {
            connectionManagerReference.update();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            String message = messageField.getText();
            String selectedEmoji = (String) emojiComboBox.getSelectedItem();

            // Append the user's typed message directly to the chat area
            sendMessage(connectionManagerReference.getUsername() + ": " + message + " " + selectedEmoji);
            messageField.setText("");
        } else if (e.getSource() == emojiComboBox) {
            updateSelectedEmojiLabel(); // Update selected emoji label when an emoji is selected
        } else if (e.getSource() == disconnectButton) {
            disconnect();
        } else if (e.getSource() == loadPreviousMessagesButton) {
            numberOfMessages = messageList.size() + 30;
            connectionManagerReference.requestMessages(0, numberOfMessages);
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
            sendMessage("User " + connectionManagerReference.getUsername() + " has left the chat.");
            this.setVisible(false);
            dispose();
            connectionManagerReference.needsToUpdateChatWindow = false;
            connectionManagerReference.interrupt();
            connectionManagerReference.selectChat();
        }
    }

    private void sendMessage(String finalMessage) {
        long timestamp = System.currentTimeMillis();
        HashMap<String, String> message = new HashMap<>();

        message.put("chatName", connectionManagerReference.getSelectedChatName());
        message.put("text", finalMessage);
        message.put("username", connectionManagerReference.getUsername());
        message.put("timestamp", Long.toString(timestamp));

        messageList.add(message);
        hasNewMessages = true;
        connectionManagerReference.sendMessage(message);
    }

    private void appendMessageToChat(String username, String message, long timestamp) {
        Date now = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        String formattedTimestamp = dateFormat.format(now) + " " + timeFormat.format(now);

        if (Objects.equals(username, connectionManagerReference.getUsername())) {
            // Determine the number of invisible characters based on the alignment
            int invisibleChars = 10;

            // Add invisible characters to the message
            String paddedMessage = " ".repeat(invisibleChars) + "[" + formattedTimestamp + "] " + message + "\n";

            // Append the padded message to the chat area without adding an extra newline
            chatArea.append(paddedMessage);
        } else {
            chatArea.append("[" + formattedTimestamp + "] " + message + "\n");
        }
    }

    public long getLastMessageTimestamp() {
        messageList.sort(Comparator.comparingLong(map -> Long.parseLong(map.get("timestamp"))));
        if (messageList.size() > 0) return Long.parseLong(messageList.get(0).get("timestamp"));
        else return 0;
    }

    public void addMessages(JSONArray messages) {
        Set<String> uniqueTimestamps = new HashSet<>();

        // Add timestamps from messageList
        for (HashMap<String, String> map : messageList) {
            String timestamp = map.get("timestamp");
            if (timestamp != null) {
                uniqueTimestamps.add(timestamp);
            }
        }

        // Add messages with unique timestamps from jsonArray to messageList
        for (int index = 0; index < messages.length(); index++) {
            JSONObject jsonMessage = messages.getJSONObject(index);
            if (!uniqueTimestamps.contains(String.valueOf(jsonMessage.get("timestamp")))) {
                // Convert JSONObject to HashMap
                HashMap<String, String> hashMapMessage = new HashMap<>();
                for (String key : jsonMessage.keySet()) {
                    hashMapMessage.put(key, jsonMessage.getString(key));
                }
                uniqueTimestamps.add(String.valueOf(jsonMessage.get("timestamp")));
                messageList.add(hashMapMessage);
                hasNewMessages = true;
            }
        }

        if (hasNewMessages) updateMessagesDisplay();
    }

    private void updateMessagesDisplay() {
        messageList.sort(Comparator.comparingLong(map -> Long.parseLong(map.get("timestamp"))));

        for (HashMap<String, String> message : messageList) {
            if (!Boolean.parseBoolean(message.get("displayed"))) {
                appendMessageToChat(message.get("username"), message.get("text"), Long.parseLong(message.get("timestamp")));
                message.put("displayed", String.valueOf(true));
            }
        }
        hasNewMessages = false;
    }

    public void setMessageList(ArrayList<HashMap<String, String>> messageList) {
        this.messageList = messageList;
    }
}