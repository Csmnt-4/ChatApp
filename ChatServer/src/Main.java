import java.io.IOException;

import util.ConnectionManager;

public class Main {
    public static void main(String[] args) {
        int port = 12345;
        try {
            Thread t = new ConnectionManager(port);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}