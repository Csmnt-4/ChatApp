package manager;

import org.json.JSONObject;
import ui.ChatSelectionWindow;
import ui.ChatWindow;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ConnectionManager extends Thread {

    private final Random random = new Random();
    private final ChatSelectionWindow chatSelectionWindow = new ChatSelectionWindow();
    private final long lastUpdateTimeSeconds = Instant.now().getEpochSecond();
    public boolean needsToUpdateChatWindow;
    private String username;
    private String ipAddress;
    private int port = 12345;
    private final ArrayList<String> chatNames = new ArrayList<String>();
    private String selectedChatName;
    private ChatWindow chatWindow;
    private long currentTimeSeconds;
    private int tMax = 1;
    private int tRandomUpdate = 0;

    public void update() {
        currentTimeSeconds = Instant.now().getEpochSecond();
        if (lastUpdateTimeSeconds + tRandomUpdate < currentTimeSeconds) {
            requestMessages(chatWindow.getLastMessageTimestamp(), 10);
        }
        if (chatWindow.hasNewMessages) {
            tMax = 1;
        }
        tRandomUpdate = getRandomTime();
        try {
            Thread.sleep(tRandomUpdate * 1000L);
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

    public void typingUpdate(String username, boolean isTyping) {
        JSONObject message = new JSONObject();
        message.put("messageType", "TYPING");

        message.put("chatName", getSelectedChatName());
        message.put("username", username);
        message.put("isTyping", isTyping);

        establishConnectionToServer(message);
    }

    public boolean establishConnectionToServer(JSONObject jsonToSend) {
        try {
            Socket client = new Socket(ipAddress, port);
            OutputStream outToServer = client.getOutputStream();

            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF(jsonToSend.toString());
            out.flush();

            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            processReceivedDataFromServer(new JSONObject(in.readUTF()));

            client.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void processReceivedDataFromServer(JSONObject jsonObject) {
        switch ((String) jsonObject.get("messageType")) {

            case "TYPING" -> {
                //Update chat to reflect new people who are typing
            }
            case "NEW_MESSAGES" -> {
                if (jsonObject.getJSONArray("messages").length() == 0) {
                    tMax++;
                } else {
                    tMax = 1;
                    chatWindow.addMessages(jsonObject.getJSONArray("messages"));
                }
            }
            case "CHAT_NAMES" -> {
                chatNames.clear();
                for (Object chatName : jsonObject.getJSONArray("chatNames")) {
                    chatNames.add(chatName.toString());
                }

            }
            case "OK" -> {
                tMax++;
            }
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
        chatSelectionWindow.startChatSelection(getChatNames(), this);
    }

    public void connectToChat(String chatName) {
        setSelectedChatName(chatName);
        // Open chat window without initially prompting for emoji
        setChatWindow(new ChatWindow(getUsername(), getSelectedChatName(), this));
        if (chatNames.contains(chatName)) {
            requestMessages(0, 30);
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

    public String getSelectedChatName() {
        return selectedChatName;
    }

    public void setSelectedChatName(String selectedChatName) {
        this.selectedChatName = selectedChatName;
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
