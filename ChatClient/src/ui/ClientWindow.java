package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientWindow extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JTextField ipAddressField;

    private JButton startButton;
    private String username;
    private String ipAddress;

    public ClientWindow() {
        setTitle("Chatroom Client");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel ipAddressLabel = new JLabel("IP Address:");
        ipAddressField = new JTextField();
        startButton = new JButton("START");

        startButton.addActionListener(this);

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(ipAddressLabel);
        panel.add(ipAddressField);
        panel.add(startButton);

        add(panel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            username = usernameField.getText();
            ipAddress = ipAddressField.getText();

            if (isValidIP(ipAddress)) {
                startChat();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid IP address format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void startChat() {
        try {
            Socket socket = new Socket(ipAddress, 12345); // Change port if necessary
            System.out.println("Connected to server");

            // Send username to server
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(username);

            // Open chat window without initially prompting for emoji
            new ChatWindow(socket, username).setVisible(true);

            // Close current window
            setVisible(false);
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to server", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private boolean isValidIP(String ip) {
        // Regular expression for a valid IPv4 address
        String ipv4Regex =
                "^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
                        "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
                        "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
                        "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$";

        // Regular expression for a valid IPv6 address
        String ipv6Regex =
                "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^([0-9a-fA-F]{1,4}:){1,7}:|" +
                        "^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|^([0-9a-fA-F]{1,4}:){1,5}" +
                        "(:[0-9a-fA-F]{1,4}){1,2}$|^([0-9a-fA-F]{1,4}:){1,4}" +
                        "(:[0-9a-fA-F]{1,4}){1,3}$|^([0-9a-fA-F]{1,4}:){1,3}" +
                        "(:[0-9a-fA-F]{1,4}){1,4}$|^([0-9a-fA-F]{1,4}:){1,2}" +
                        "(:[0-9a-fA-F]{1,4}){1,5}$|^[0-9a-fA-F]{1,4}:" +
                        "(:[0-9a-fA-F]{1,4}){1,6}$|^:((:[0-9a-fA-F]{0,4}){0,7}|:)$";

        return ip.matches(ipv4Regex) || ip.matches(ipv6Regex);
    }
}