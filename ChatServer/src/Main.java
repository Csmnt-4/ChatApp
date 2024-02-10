import java.io.IOException;

import util.ConnectionManager;

public class Main {
    public static void main(String[] args) {
        int port = 6066;
        try {
            Thread t = new ConnectionManager(port);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}