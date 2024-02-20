package ui;

import manager.ConnectionManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ChatSelectionWindow extends JFrame {

    private String selectedChatName;

    public void startChatSelection(ArrayList<String> chatNames, ConnectionManager connectionManagerReference) {
        JFrame frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JPanel panel = new JPanel();
        panel.setBounds(10, 11, 414, 239);
        frame.getContentPane().add(panel);
        panel.setLayout(null);

        JTextField newChatTextField = new JTextField();
        newChatTextField.setBounds(10, 11, 150, 20);
        panel.add(newChatTextField);
        newChatTextField.setColumns(10);

        JButton createButton = new JButton("Create");
        createButton.setBounds(170, 10, 89, 23);
        panel.add(createButton);
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newChatName = newChatTextField.getText();
                if (newChatName.length() > 3) {
                    selectedChatName = newChatName;
                    frame.dispose();
                    connectionManagerReference.connectToChat(selectedChatName);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 42, 394, 186);
        panel.add(scrollPane);

        JPanel chatButtonPanel = new JPanel();
        scrollPane.setViewportView(chatButtonPanel);
        chatButtonPanel.setLayout(null);

        int y = 0;
        for (String chatName : chatNames) {
            JButton button = new JButton(chatName);
            button.setBounds(10, y, 150, 23);
            chatButtonPanel.add(button);
            y += 30;

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectedChatName = chatName;
                    frame.dispose();
                    connectionManagerReference.connectToChat(selectedChatName);
                }
            });
        }
        frame.setVisible(true);
    }

}
