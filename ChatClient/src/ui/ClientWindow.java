package ui;

import manager.ConnectionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientWindow extends JFrame implements ActionListener {
    private final JTextField usernameField;
    private final JTextField ipAddressField;

    private final JButton startButton;
    private final ConnectionManager connectionManager = new ConnectionManager();

    public ClientWindow() {
        setTitle("Chatroom Client");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel usernameLabel = new JLabel("\tUsername:");
        usernameLabel.setHorizontalAlignment(0);
        usernameField = new JTextField();
        JLabel ipAddressLabel = new JLabel("\tIP Address:");
        ipAddressLabel.setHorizontalAlignment(0);
        ipAddressField = new JTextField();
        startButton = new JButton("START");
//        startButton.setMaximumSize(new Dimension(20, 10));
        startButton.setHorizontalAlignment(0);
        startButton.addActionListener(this);

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(ipAddressLabel);
        panel.add(ipAddressField);
        panel.add(startButton);

        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setAlignmentY(Component.CENTER_ALIGNMENT);

        add(panel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {

            if (isValidIP(ipAddressField.getText())) {
                connectionManager.setUsername(usernameField.getText());
                connectionManager.setIpAddress(ipAddressField.getText());

                connectionManager.requestChatNames();
                connectionManager.selectChat();
                // Close current window
                setVisible(false);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid IP address format", "Error", JOptionPane.ERROR_MESSAGE);
            }
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