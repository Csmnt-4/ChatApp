package manager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class ClientManager {

    private final HashMap<String, List<String>> currentlyTyping = new HashMap<>();
    private HashMap<String, String> usernames = new HashMap<>();
    private HashMap<String, ArrayList<Map<String, String>>> chats = new HashMap<>();

    public ClientManager() {
        try {
            deserializeUserNames();
            deserializeChats();
        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
    }

    public boolean checkSerialNumber(String serialNumber, String userName) {
        return false;
    }

    public void deserializeUserNames() throws IOException, ClassNotFoundException {
        try (FileInputStream fileInput = new FileInputStream("userNames.txt")) {

            ObjectInputStream objectInputUsernames = new ObjectInputStream(fileInput);

            usernames = (HashMap<String, String>) objectInputUsernames.readObject();

            objectInputUsernames.close();
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("No serialized files were found.");
        }
    }

    public void deserializeChats() throws IOException, ClassNotFoundException {
        try (FileInputStream fileInput1 = new FileInputStream("chats.txt")) {

            ObjectInputStream objectInput1 = new ObjectInputStream(fileInput1);

            chats = (HashMap<String, ArrayList<Map<String, String>>>) objectInput1.readObject();

            objectInput1.close();
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("No serialized files were found.");
        }
    }

    public void serializeUserNames() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("userNames.txt");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(usernames);
        objectOutputStream.flush();
        objectOutputStream.close();
    }

    public void serializeChats() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("chats.txt");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(chats);
        objectOutputStream.flush();
        objectOutputStream.close();
    }

    public JSONObject getChatNames() {
        JSONObject response = new JSONObject();

        ArrayList<String> chatNames = new ArrayList<>();
        chats.forEach((key, value) -> chatNames.add(key));
        response.put("messageType", "CHAT_NAMES");
        response.put("chatNames", chatNames);
        return response;
    }

    public JSONObject addNewChat(String chatName) {
        JSONObject response = new JSONObject();

        chats.put(chatName, new ArrayList<>());

        response.put("messageType", "OK");
        if (currentlyTyping.containsKey(chatName)) response.put("typing", currentlyTyping.get(chatName));
        return response;
    }

    public JSONObject addNewMessageToChat(JSONObject jsonObject) {
        JSONObject response = new JSONObject();

        HashMap<String, String> message = new HashMap<>();
        message.put("text", (String) jsonObject.get("text"));
        message.put("username", (String) jsonObject.get("username"));
        message.put("timestamp", (String) jsonObject.get("timestamp"));
        chats.get(jsonObject.getString("chatName")).add(message);
        response.put("messageType", "OK");

        try {
            serializeChats();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (currentlyTyping.containsKey(jsonObject.getString("chatName")))
            response.put("typing", currentlyTyping.get(jsonObject.getString("chatName")));
        return response;
    }

    public JSONObject getMessagesSince(String chatName, Long timestamp, int numberOfMessages) {
        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            if (timestamp > 0) {
                for (Map<String, String> map : chats.get(chatName)) {
                    if (map.containsKey("timestamp") && Long.parseLong(map.get("timestamp")) > timestamp) {
                        jsonArray.put(new JSONObject(map));
                    }
                }

                response.put("messageType", "NEW_MESSAGES");
                response.put("messages", jsonArray);
            } else if (numberOfMessages > 0) {
                chats.get(chatName).sort(Comparator.comparingLong(map -> Long.parseLong(map.get("timestamp"))));

                // Add the last n HashMaps to a new list
                int startIndex = Math.max(0, chats.get(chatName).size() - numberOfMessages);
                for (int i = startIndex; i < chats.get(chatName).size(); i++) {
                    jsonArray.put(new JSONObject(chats.get(chatName).get(i)));
                }

                response.put("messageType", "NEW_MESSAGES");
                response.put("messages", jsonArray);
            } else {
                response.put("messageType", "OK");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Unable to parse the number.");

            response.put("messageType", "ERROR");
            response.put("message", "Unable to parse timestamp or number of messages: Incorrect number format");
        }
        if (currentlyTyping.containsKey(chatName)) response.put("typing", currentlyTyping.get(chatName));
        return response;
    }

    public JSONObject compareTheNumberOfMessagesSince(String chatName, Long timestamp, int numberOfMessages) {
        JSONObject response = new JSONObject();
        int localNumberOfMessages = 0;
        try {
            for (Map<String, String> map : chats.get(chatName)) {

                if (map.containsKey("timestamp") && Long.parseLong(map.get("timestamp")) > timestamp) {
                    localNumberOfMessages++;
                }
            }

            if (localNumberOfMessages > numberOfMessages) {
                return getMessagesSince(chatName, timestamp, 0);
            }

        } catch (NumberFormatException exception) {
            System.out.println("Unable to parse a number.");
            response.put("messageType", "ERROR");
            response.put("message", "Unable to parse timestamp or number of messages: Incorrect number format");
            if (currentlyTyping.containsKey(chatName)) response.put("typing", currentlyTyping.get(chatName));
        }

        return response;
    }

    public JSONObject updateTypingList(JSONObject jsonObject) {
        JSONObject response = new JSONObject();

        String username = jsonObject.getString("username");
        boolean isTyping = jsonObject.getBoolean("isTyping");
        if (isTyping) {
            if (currentlyTyping.containsKey(jsonObject.getString("chatName"))) {
                if (!currentlyTyping.get(jsonObject.getString("chatName")).contains(username))
                    currentlyTyping.get(jsonObject.getString("chatName")).add(username);
            } else {
                List<String> usernames = List.of(new String[]{username});
                currentlyTyping.put(jsonObject.getString("chatName"), usernames);
            }
        } else {
            if (currentlyTyping.containsKey(jsonObject.getString("chatName"))) {
                currentlyTyping.get(jsonObject.getString("chatName")).removeIf(s -> Objects.equals(s, username));
            } else {
                currentlyTyping.put(jsonObject.getString("chatName"), new ArrayList<>());
            }
        }
        response.put("messageType", "TYPING");
        response.put("typing", currentlyTyping.get(jsonObject.getString("chatName")));
        return response;
    }
}
