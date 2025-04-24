package bytecypher;

import java.awt.Dimension;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class FileSelector {

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {
        }
    }

    private static JFileChooser createFileChooser(String title, int mode, String tooltip) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);
        fileChooser.setFileSelectionMode(mode);
        fileChooser.setApproveButtonText("Select");
        fileChooser.setPreferredSize(new Dimension(650, 450));
        fileChooser.setToolTipText(tooltip);
        fileChooser.setMultiSelectionEnabled(false);
        return fileChooser;
    }

    public static String selectFile() {
        JFileChooser fileChooser = createFileChooser("Select a File", JFileChooser.FILES_ONLY, "Choose a file");
        int userSelection = fileChooser.showOpenDialog(null);
        return (userSelection == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile().getAbsolutePath() : null;
    }

    public static String selectFolder() {
        JFileChooser fileChooser = createFileChooser("Select a Folder", JFileChooser.DIRECTORIES_ONLY, "Choose a folder");
        int userSelection = fileChooser.showOpenDialog(null);
        return (userSelection == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile().getAbsolutePath() : null;
    }

    public static String selectSaveLocation(String operation, String defaultExtension) {
        // First select folder
        JFileChooser folderChooser = createFileChooser("Select a Save Location", JFileChooser.DIRECTORIES_ONLY,
                "Choose where to save the " + operation + " file");
        int folderSelection = folderChooser.showDialog(null, "Select Folder");

        if (folderSelection == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = folderChooser.getSelectedFile();

            // Then ask for filename
            while (true) {
                JTextField textField = new JTextField();
                Object[] message = {
                    new JLabel("📂 Enter output file name (without extension):"), textField
                };
                int option = JOptionPane.showConfirmDialog(null, message,
                        "Save " + operation + " File", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                    return null;
                }

                String fileName = textField.getText().trim();
                if (!fileName.isEmpty() && fileName.matches("^[a-zA-Z0-9_.-]+$")) {
                    return selectedDirectory.getAbsolutePath() + File.separator + fileName + defaultExtension;
                }

                JOptionPane.showMessageDialog(null,
                        "Invalid file name! Use letters, numbers, '-', '.' or '_'.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    public static String selectDecompressionSaveLocation() {
        JFileChooser fileChooser = createFileChooser("Select Extraction Location", JFileChooser.DIRECTORIES_ONLY,
                "Choose where to extract files");
        int userSelection = fileChooser.showDialog(null, "Select Folder");
        return (userSelection == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile().getAbsolutePath() : null;
    }
}
