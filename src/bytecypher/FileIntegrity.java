package bytecypher;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import javax.swing.*;

public class FileIntegrity {

    public static void integrity() {
        String[] options = {"Generate Checksum", "Verify Checksum"};
        int choice = JOptionPane.showOptionDialog(null,
                "Choose an integrity operation:",
                "File Integrity",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            generateChecksum();
        } else if (choice == 1) {
            verifyChecksum();
        }
    }

    private static void generateChecksum() {
        String filePath = FileSelector.selectFile();
        if (filePath == null) {
            System.out.println("No file selected. Operation cancelled.");
            return;
        }

        try {
            byte[] fileData = Files.readAllBytes(Paths.get(filePath));
            String checksumMD5 = calculateChecksum(fileData, "MD5");
            String checksumSHA256 = calculateChecksum(fileData, "SHA-256");

            String savePath = getSavePathForChecksum(filePath);
            if (savePath == null) {
                System.out.println("No save location selected. Operation cancelled.");
                return;
            }

            // Write checksums to file
            try (PrintWriter writer = new PrintWriter(new FileWriter(savePath))) {
                writer.println("File: " + new File(filePath).getName());
                writer.println("MD5: " + checksumMD5);
                writer.println("SHA-256: " + checksumSHA256);
                writer.println("Generated: " + java.time.LocalDateTime.now());
            }

            // Show checksums to user
            JTextArea textArea = new JTextArea(
                    "Checksums for: " + new File(filePath).getName() + "\n\n"
                    + "MD5: " + checksumMD5 + "\n"
                    + "SHA-256: " + checksumSHA256 + "\n\n"
                    + "Checksum file saved to: " + savePath);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(500, 300));

            JOptionPane.showMessageDialog(null, scrollPane, "Checksum Generated", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("Checksum generated and saved to: " + savePath);

        } catch (Exception e) {
            System.out.println("Error calculating checksum: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void verifyChecksum() {
        // First select the original file
        String filePath = FileSelector.selectFile();
        if (filePath == null) {
            System.out.println("No file selected. Operation cancelled.");
            return;
        }

        // Then select the checksum file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Checksum File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("No checksum file selected. Operation cancelled.");
            return;
        }

        String checksumFilePath = fileChooser.getSelectedFile().getAbsolutePath();

        try {
            // Calculate current checksums
            byte[] fileData = Files.readAllBytes(Paths.get(filePath));
            String currentMD5 = calculateChecksum(fileData, "MD5");
            String currentSHA256 = calculateChecksum(fileData, "SHA-256");

            // Read expected checksums
            String expectedMD5 = "";
            String expectedSHA256 = "";

            try (BufferedReader reader = new BufferedReader(new FileReader(checksumFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("MD5:")) {
                        expectedMD5 = line.substring(4).trim();
                    } else if (line.startsWith("SHA-256:")) {
                        expectedSHA256 = line.substring(8).trim();
                    }
                }
            }

            // Compare checksums
            boolean md5Match = expectedMD5.equalsIgnoreCase(currentMD5);
            boolean sha256Match = expectedSHA256.equalsIgnoreCase(currentSHA256);

            StringBuilder resultMessage = new StringBuilder();
            resultMessage.append("Verification Results for: ").append(new File(filePath).getName()).append("\n\n");

            resultMessage.append("MD5 Checksum:\n")
                    .append("  Expected: ").append(expectedMD5).append("\n")
                    .append("  Current:  ").append(currentMD5).append("\n")
                    .append("  Status:   ").append(md5Match ? "MATCH ✓" : "DIFFERENT ✗").append("\n\n");

            resultMessage.append("SHA-256 Checksum:\n")
                    .append("  Expected: ").append(expectedSHA256).append("\n")
                    .append("  Current:  ").append(currentSHA256).append("\n")
                    .append("  Status:   ").append(sha256Match ? "MATCH ✓" : "DIFFERENT ✗").append("\n\n");

            if (md5Match && sha256Match) {
                resultMessage.append("VERIFICATION SUCCESSFUL: File integrity confirmed.");
                JOptionPane.showMessageDialog(null, new JScrollPane(new JTextArea(resultMessage.toString())),
                        "File Verification", JOptionPane.INFORMATION_MESSAGE);
            } else {
                resultMessage.append("VERIFICATION FAILED: File may have been modified or corrupted.");
                JOptionPane.showMessageDialog(null, new JScrollPane(new JTextArea(resultMessage.toString())),
                        "File Verification", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            System.out.println("Error verifying checksum: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String calculateChecksum(byte[] data, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hash = digest.digest(data);

        // Convert to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static String getSavePathForChecksum(String sourceFilePath) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Checksum File");
        fileChooser.setSelectedFile(new File(new File(sourceFilePath).getName() + ".checksum"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        String savePath = fileChooser.getSelectedFile().getAbsolutePath();
        if (!savePath.endsWith(".checksum")) {
            savePath += ".checksum";
        }

        return savePath;
    }
}
