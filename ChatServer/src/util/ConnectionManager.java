package util;

import manager.ClientManager;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

public class ConnectionManager extends Thread {

    private final ServerSocket serverSocket;
    private final ClientManager clientManager = new ClientManager();
    public ConnectionManager(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(0);
    }

    public void run() {
        while (true) {
            try {
                System.out.println("Waiting on port " + serverSocket.getLocalPort());
                Socket server = serverSocket.accept();

                System.out.println("Connected to " + server.getRemoteSocketAddress());

                // Read a command (input) and prepare the output stream
                DataInputStream in = new DataInputStream(server.getInputStream());
                DataOutputStream out = new DataOutputStream(server.getOutputStream());

                // Process the command
                out.writeUTF(processReceivedData(new JSONObject(in.readUTF())).toString());
                server.close();

            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public JSONObject processReceivedData(JSONObject jsonMessage) {
        JSONObject jsonResponse = null;
        switch ((String) jsonMessage.get("messageType")) {
            case "REQUEST_COMPARE" ->
                    jsonResponse = clientManager.compareTheNumberOfMessagesSince(jsonMessage.getString("chatName"),
                            jsonMessage.getLong("timestamp"),
                            jsonMessage.getInt("numberOfMessages"));
            case "REQUEST_MESSAGE" -> jsonResponse = clientManager.getMessagesSince(jsonMessage.getString("chatName"),
                    jsonMessage.getLong("timestamp"), jsonMessage.getInt("numberOfMessages"));
            case "REQUEST_CHAT_NAMES" -> jsonResponse = clientManager.getChatNames();
            case "REQUEST_LOGIN" -> {
            }

            case "REQUEST_CHANGE_MESSAGE" -> {
            }
            case "REQUEST_CHANGE_USERNAME" -> {
            }

            case "CREATE_MESSAGE" ->
                    jsonResponse = clientManager.addNewMessageToChat(jsonMessage.getJSONObject("message"));
            case "CREATE_CHAT" -> jsonResponse = clientManager.addNewChat(jsonMessage.getString("chatName"));
            case "CREATE_USER" -> {
            }

            case "TYPING" -> clientManager.updateTypingList(jsonMessage);
            default -> {
                jsonResponse = new JSONObject();
                jsonResponse.put("responseType", "ERROR");
                jsonResponse.put("responseMessage", "Incorrect or invalid messageType");
            }
        }
        return jsonResponse;
    }
}
