package manager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

public class ClientManager {

    HashMap<String, String> userNames = new HashMap<>();
    HashMap<String, ArrayList<Map<String, String>>> chats = new HashMap<>();

    public ClientManager() {
        try {
            FileInputStream fileInput = new FileInputStream(
                    "userNames.txt");

            ObjectInputStream objectInput
                    = new ObjectInputStream(fileInput);

			userNames = (HashMap<String, String>) objectInput.readObject();

            objectInput.close();
            fileInput.close();

			FileInputStream fileInput1 = new FileInputStream(
					"chats.txt");

			ObjectInputStream objectInput1
					= new ObjectInputStream(fileInput1);

			userNames = (HashMap<String, String>) objectInput1.readObject();

			objectInput1.close();
			fileInput1.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException classNotFoundException) {
            System.out.println("A class in the map is not found");
            classNotFoundException.printStackTrace();
        }
        
        
    }

    public boolean checkSerialNumber(String serialNumber, String userName) {
        return false;
    }
    
    public void deserializeUserNames() {
    	
    }
    
    public void deserializeUserNames() {
    	
    }
    
    public void serializeUserNames() {
    	
    }
    
    public void serializeChats() {
    	
    }

    public ArrayList getChatNames() {
        ArrayList<String> chatNames = new ArrayList<String>();
        chats.forEach((key, value) -> {
            chatNames.add(key);
        });
        return chatNames;
    }
}
