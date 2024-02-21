package util;

import javax.swing.*;
import java.awt.*;

// Credit: MadProgrammer, May 19/13
// Link: https://stackoverflow.com/questions/16632987/how-to-close-joptionpane-automatically

public class BackgroundWorker extends SwingWorker<Void, Void> {

    private JProgressBar pb;
    private JDialog dialog;

    public BackgroundWorker() {
        addPropertyChangeListener(evt -> {
            if ("progress".equalsIgnoreCase(evt.getPropertyName())) {
                if (dialog == null) {
                    dialog = new JDialog();
                    dialog.setTitle("Loading");
                    dialog.setLayout(new GridBagLayout());
                    dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(2, 2, 2, 2);
                    gbc.weightx = 1;
                    gbc.gridy = 0;
                    dialog.add(new JLabel("Fetching messages..."), gbc);
                    pb = new JProgressBar();
                    gbc.gridy = 1;
                    dialog.add(pb, gbc);
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                }
                pb.setValue(getProgress());
            }
        });
    }

    @Override
    protected void done() {
        if (dialog != null) {
            dialog.dispose();
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        for (int index = 0; index < 100; index++) {
            setProgress(index);
            Thread.sleep(13);
        }
        return null;
    }
}