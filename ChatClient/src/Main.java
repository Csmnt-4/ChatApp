import ui.ClientWindow;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientWindow::new);
    }
}