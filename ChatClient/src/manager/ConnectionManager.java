package manager;

import org.json.JSONObject;
import ui.ChatSelectionWindow;
import ui.ChatWindow;

import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.*;

public class ConnectionManager extends Thread {

    private final Random random = new Random();
    public boolean needsToUpdateChatWindow;
    private final long lastUpdateTimeSeconds = Instant.now().getEpochSecond();
    private String username;
    private String ipAddress;
    private int port = 12345;
    private final ArrayList<String> chatNames = new ArrayList<>();
    private String selectedChatName;
    private ChatWindow chatWindow;
    private int tMax = 1;
    private int tRandomUpdate = 0;

    public void connectToChat(String chatName) {
        setSelectedChatName(chatName);
        // Open chat window without initially prompting for emoji
        setChatWindow(new ChatWindow(getUsername(), getSelectedChatName(), this));
        if (chatNames.contains(chatName)) {
            requestMessages(0, 30);
            chatWindow.sendMessage(getUsername() + " has joined the chat~");
        } else {
            createChat(chatName);
        }
        needsToUpdateChatWindow = true;
        chatWindow.setVisible(true);
    }

    public void createChat(String chatName) {
        JSONObject message = new JSONObject();
        message.put("messageType", "CREATE_CHAT");
        message.put("chatName", chatName);
        establishConnectionToServer(message);
    }

    public void update() {
        typingUpdate(chatWindow.hasMessageInProcess());
        long currentTimeSeconds = Instant.now().getEpochSecond();
        if (lastUpdateTimeSeconds + tRandomUpdate < currentTimeSeconds) {
            requestMessages(chatWindow.getLastMessageTimestamp(), 10);
        }
        tRandomUpdate = getRandomTime();
        try {
            Thread.sleep(tRandomUpdate * 100L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int getRandomTime() {
        tMax++;
        return random.nextInt(tMax);
    }

    public void sendMessage(HashMap<String, String> textMessage) {
        JSONObject message = new JSONObject();
        message.put("messageType", "CREATE_MESSAGE");
        message.put("message", new JSONObject(textMessage));

        establishConnectionToServer(message);
    }

    public void requestMessages(long lastMessageTimestamp, int optional_NLastMessages) {
        JSONObject message = new JSONObject();
        message.put("messageType", "REQUEST_MESSAGE");

        message.put("chatName", getSelectedChatName());
        message.put("timestamp", lastMessageTimestamp);
        message.put("numberOfMessages", optional_NLastMessages);

        establishConnectionToServer(message);
    }

    public void requestChatNames() {
        JSONObject message = new JSONObject();
        message.put("messageType", "REQUEST_CHAT_NAMES");

        establishConnectionToServer(message);
    }

    public void requestToCompareNumberOfMessagesSince(long timestamp, int numberOfMessages) {
        JSONObject message = new JSONObject();
        message.put("messageType", "REQUEST_COMPARE");

        message.put("chatName", getSelectedChatName());
        message.put("timestamp", timestamp);
        message.put("numberOfMessages", numberOfMessages);
    }

    public void typingUpdate(boolean isTyping) {
        JSONObject message = new JSONObject();
        message.put("messageType", "TYPING");

        message.put("chatName", getSelectedChatName());
        message.put("username", getUsername());
        message.put("isTyping", isTyping);

        establishConnectionToServer(message);
    }

    public boolean establishConnectionToServer(JSONObject jsonToSend) {
        try {
            Socket client = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(ipAddress, port);
            client.connect(socketAddress, 4000);
            OutputStream outToServer = client.getOutputStream();

            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF(jsonToSend.toString());
            out.flush();

            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            processReceivedDataFromServer(new JSONObject(in.readUTF()));

            client.close();
            return true;
        } catch (SocketTimeoutException socketTimeoutException) {
            JOptionPane.showMessageDialog(JOptionPane.getDesktopPaneForComponent(null), "Socket timed out", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(JOptionPane.getDesktopPaneForComponent(null), "Couldn't connect to server", "Error", JOptionPane.ERROR_MESSAGE);
            exception.printStackTrace();
            return false;
        }
    }

    public void processReceivedDataFromServer(JSONObject jsonObject) {
        switch (jsonObject.getString("messageType")) {

            case "TYPING" -> {
                //Update chat to reflect new people who are typing
                StringBuilder typingUsernames = new StringBuilder("Typing: ");
                for (Object jsonArtifact : jsonObject.getJSONArray("typing")) {
                    typingUsernames.append(jsonArtifact.toString()).append(", ");
                }
                if (typingUsernames.length() > 9) {
                    typingUsernames = new StringBuilder(typingUsernames.substring(0, Math.min(50, typingUsernames.length() - 2)));
                    if (typingUsernames.length() == 50) {
                        typingUsernames.append("...");
                    }
                    chatWindow.setTypingPeople(typingUsernames.toString());
                } else {
                    chatWindow.setTypingPeople("");
                }

            }
            case "NEW_MESSAGES" -> chatWindow.addMessages(jsonObject.getJSONArray("messages"));
            case "CHAT_NAMES" -> {
                chatNames.clear();
                for (Object chatName : jsonObject.getJSONArray("chatNames")) {
                    chatNames.add(chatName.toString());
                }

            }
            case "OK" -> tMax++;

            default -> System.out.println(jsonObject.getString("responseMessage"));
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public void setChatWindow(ChatWindow chatWindow) {
        this.chatWindow = chatWindow;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public ArrayList<String> getChatNames() {
        return chatNames;
    }

    public void selectChat() {
        new ChatSelectionWindow(this, getChatNames());
    }

    public String getSelectedChatName() {
        return selectedChatName;
    }

    public void setSelectedChatName(String selectedChatName) {
        this.selectedChatName = selectedChatName;
    }

    public boolean testConnection() {
        JSONObject test = new JSONObject();
        test.put("messageType", "TEST");
        return establishConnectionToServer(test);
    }

    public void setTMax(int i) {
        tMax = i;
    }

/*  "REQUEST_COMPARE"
    "REQUEST_MESSAGE"
    "REQUEST_LOGIN"

	"REQUEST_CHANGE_MESSAGE" 
    "REQUEST_CHANGE_USERNAME" 

    "CREATE_MESSAGE" 
    "CREATE_CHAT"
    "CREATE_USER"

    "TYPING"
    */

}
