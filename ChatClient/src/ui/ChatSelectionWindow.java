package ui;

import manager.ConnectionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;

public class ChatSelectionWindow extends JFrame {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private final JButton createNewChatButton;
    private final JPanel chatPanel;
    private final JPanel inputPanel;
    private final JScrollPane scrollChatPanel;
    private final JPanel mainPanel;

    private String selectedChatName;
    private final JPanel chatButtonPanel;
    private final JTextField chatNameTextField;

    public ChatSelectionWindow(ConnectionManager connectionManagerReference, ArrayList<String> chatNames) {
        setTitle("Select Chat");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 225, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 200);
        setResizable(false);
        mainPanel = new JPanel();
        inputPanel = new JPanel();
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        setBounds(100, 100, 450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        chatNameTextField = new JTextField();
        chatNameTextField.setToolTipText("Enter the name for the new chat!");
        chatNameTextField.setHorizontalAlignment(SwingConstants.CENTER);
        chatNameTextField.setColumns(10);
        chatNameTextField.setAlignmentX(0.0f);
        inputPanel.add(chatNameTextField);

        createNewChatButton = new JButton("Create New Chat");
        createNewChatButton.addActionListener(e -> {
            String newChatName = chatNameTextField.getText();
            if (newChatName.length() > 3) {
                selectedChatName = newChatName;
                dispose();
                connectionManagerReference.connectToChat(selectedChatName);
            }
        });
        inputPanel.add(createNewChatButton);

        chatPanel = new JPanel();
        mainPanel.add(chatPanel, BorderLayout.CENTER);
        chatPanel.setLayout(new BorderLayout(0, 0));

        scrollChatPanel = new JScrollPane();
        chatPanel.add(scrollChatPanel);

        chatButtonPanel = new JPanel();
        scrollChatPanel.setViewportView(chatButtonPanel);
        chatButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        int y = 0;
        for (String chatName : chatNames) {
            JButton button = new JButton(chatName);
            button.setBounds(10, y, 150, 23);
            chatButtonPanel.add(button);
            y += 30;

            button.addActionListener(e -> {
                selectedChatName = chatName;
                dispose();
                connectionManagerReference.connectToChat(selectedChatName);
            });
        }

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }
}
