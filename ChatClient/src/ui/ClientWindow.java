package ui;

import manager.ConnectionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;

public class ClientWindow extends JFrame implements ActionListener {
    private final JPanel mainPanel;
    private final ConnectionManager connectionManager = new ConnectionManager();
    private final JPanel centralPanel;
    private final JButton startButton;
    private final JTextField usernameField;
    private final JTextField ipAddressField;
    private final JPasswordField passwordField;
    private final JTextField portField;

    public ClientWindow() {
        setTitle("Chatroom Client");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 175, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 100);
        setResizable(false);

        mainPanel = new JPanel();

        mainPanel.setLayout(new BorderLayout(0, 15));

        mainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 10));
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        inputPanel.setLayout(new GridLayout(5, 4, 0, 5));

        JLabel usernameLabel = new JLabel("\tUsername:");
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(usernameLabel);

        usernameField = new JTextField();
        inputPanel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(passwordLabel);

        passwordField = new JPasswordField();
        inputPanel.add(passwordField);

        JLabel ipAddressLabel = new JLabel("\tServer IP:");
        ipAddressLabel.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(ipAddressLabel);

        ipAddressField = new JTextField();
        inputPanel.add(ipAddressField);

        JLabel portLabel = new JLabel("Server Port:");
        portLabel.setEnabled(false);
        portLabel.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(portLabel);

        portField = new JTextField();
        portField.setEditable(false);
        portField.setEnabled(false);
        inputPanel.add(portField);
        portField.setColumns(10);

        centralPanel = new JPanel();
        mainPanel.add(centralPanel, BorderLayout.SOUTH);
        centralPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        startButton = new JButton("Log In");
        startButton.addActionListener(this);
        centralPanel.add(startButton);
        getContentPane().add(mainPanel);
        setVisible(true);
    }

    private boolean isValidIP(String ip) {
        // Regular expression for a valid IPv4 address
        String ipv4Regex = "^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$";

        // Regular expression for a valid IPv6 address
        String ipv6Regex = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^([0-9a-fA-F]{1,4}:){1,7}:|" + "^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|^([0-9a-fA-F]{1,4}:){1,5}" + "(:[0-9a-fA-F]{1,4}){1,2}$|^([0-9a-fA-F]{1,4}:){1,4}" + "(:[0-9a-fA-F]{1,4}){1,3}$|^([0-9a-fA-F]{1,4}:){1,3}" + "(:[0-9a-fA-F]{1,4}){1,4}$|^([0-9a-fA-F]{1,4}:){1,2}" + "(:[0-9a-fA-F]{1,4}){1,5}$|^[0-9a-fA-F]{1,4}:" + "(:[0-9a-fA-F]{1,4}){1,6}$|^:((:[0-9a-fA-F]{0,4}){0,7}|:)$";

        return ip.matches(ipv4Regex) || ip.matches(ipv6Regex);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (usernameField.getText().isEmpty() || ipAddressField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and IP address cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (isValidIP(ipAddressField.getText())) {
            connectionManager.setUsername(usernameField.getText());
            connectionManager.setIpAddress(ipAddressField.getText());
            if (connectionManager.testConnection()) {
                connectionManager.requestChatNames();
                connectionManager.selectChat();
                // Close current window
                setVisible(false);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please, check the server IP address!", "Attention", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid IP address format", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}