// package bytecypher;

// import java.io.*;
// import java.nio.file.*;
// import java.security.*;
// import java.security.spec.KeySpec;
// import java.util.*;
// import java.util.zip.*;
// import javax.crypto.*;
// import javax.crypto.spec.*;
// import javax.swing.*;

// public class Encryption {

//     private static final String ALGORITHM = "AES";
//     private static final String CIPHER_INSTANCE = "AES/CBC/PKCS5Padding";
//     private static final int KEY_SIZE = 256;

//     public static void encrypt() {
//         String inputPath = FileSelector.selectFile();
//         if (inputPath == null) {
//             System.out.println("No file selected. Operation cancelled.");
//             return;
//         }

//         File inputFile = new File(inputPath);
//         if (!inputFile.exists()) {
//             System.out.println("Selected file does not exist. Check the path.");
//             return;
//         }

//         System.out.println("File selected: " + inputPath);
//         String savePath = FileSelector.selectSaveLocation();
//         if (savePath == null) {
//             System.out.println("No save location selected. Operation cancelled.");
//             return;
//         }

//         if (!savePath.endsWith(".seczip")) {
//             savePath += ".seczip";
//         }

//         // Ask for password
//         JPasswordField pf = new JPasswordField();
//         int option = JOptionPane.showConfirmDialog(null, pf, "Enter a strong password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

//         if (option != JOptionPane.OK_OPTION) {
//             System.out.println("Password entry cancelled. Encryption aborted.");
//             return;
//         }

//         char[] password = pf.getPassword();
//         if (password.length < 8) {
//             System.out.println("Password too short. Please use at least 8 characters.");
//             return;
//         }

//         System.out.println("Encrypting...");
//         try {
//             // Generate a secure key from the password
//             SecureRandom random = new SecureRandom();
//             byte[] salt = new byte[16];
//             random.nextBytes(salt);

//             // Generate IV
//             byte[] iv = new byte[16];
//             random.nextBytes(iv);
//             IvParameterSpec ivSpec = new IvParameterSpec(iv);

//             // Generate key
//             SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//             KeySpec spec = new PBEKeySpec(password, salt, 65536, KEY_SIZE);
//             SecretKey tmp = factory.generateSecret(spec);
//             SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), ALGORITHM);

//             // Initialize Cipher
//             Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE);
//             cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

//             // Create output streams
//             try (FileOutputStream fos = new FileOutputStream(savePath); BufferedOutputStream bos = new BufferedOutputStream(fos)) {

//                 // Write salt and IV first
//                 bos.write(salt);
//                 bos.write(iv);

//                 // Create temporary zip file
//                 Path tempZipPath = Files.createTempFile("temp_encrypt", ".zip");

//                 // Compress the file first
//                 try (FileOutputStream tempFos = new FileOutputStream(tempZipPath.toFile()); ZipOutputStream zos = new ZipOutputStream(tempFos)) {

//                     // Add file to zip
//                     ZipEntry entry = new ZipEntry(inputFile.getName());
//                     zos.putNextEntry(entry);

//                     byte[] fileBytes = Files.readAllBytes(inputFile.toPath());
//                     zos.write(fileBytes);
//                     zos.closeEntry();
//                 }

//                 // Read the temp zip file and encrypt it
//                 byte[] zipData = Files.readAllBytes(tempZipPath);
//                 byte[] encryptedData = cipher.doFinal(zipData);

//                 // Write encrypted data
//                 bos.write(encryptedData);

//                 // Delete temp file
//                 Files.delete(tempZipPath);

//                 System.out.println("Encryption successful! Encrypted file saved at: " + savePath);
//             }

//         } catch (Exception e) {
//             System.out.println("Encryption failed: " + e.getMessage());
//             e.printStackTrace();
//         }

//         // Clear password from memory
//         Arrays.fill(password, '\0');
//     }

//     public static void decrypt() {
//         String inputPath = FileSelector.selectFile();
//         if (inputPath == null) {
//             System.out.println("No file selected. Operation cancelled.");
//             return;
//         }

//         if (!inputPath.endsWith(".seczip")) {
//             System.out.println("Invalid file format! Please select a valid .seczip file.");
//             return;
//         }

//         String savePath = FileSelector.selectSaveLocation();
//         if (savePath == null) {
//             System.out.println("No save location selected. Operation cancelled.");
//             return;
//         }

//         // Ask for password
//         JPasswordField pf = new JPasswordField();
//         int option = JOptionPane.showConfirmDialog(null, pf, "Enter your password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

//         if (option != JOptionPane.OK_OPTION) {
//             System.out.println("Password entry cancelled. Decryption aborted.");
//             return;
//         }

//         char[] password = pf.getPassword();

//         System.out.println("Decrypting...");
//         try {
//             // Read encrypted file
//             byte[] fileData = Files.readAllBytes(Paths.get(inputPath));

//             // Extract salt and IV
//             byte[] salt = Arrays.copyOfRange(fileData, 0, 16);
//             byte[] iv = Arrays.copyOfRange(fileData, 16, 32);
//             byte[] encryptedData = Arrays.copyOfRange(fileData, 32, fileData.length);

//             // Generate key from password and salt
//             SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//             KeySpec spec = new PBEKeySpec(password, salt, 65536, KEY_SIZE);
//             SecretKey tmp = factory.generateSecret(spec);
//             SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), ALGORITHM);

//             // Initialize cipher for decryption
//             Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE);
//             cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

//             try {
//                 // Decrypt the data
//                 byte[] decryptedZipData = cipher.doFinal(encryptedData);

//                 // Create temporary zip file
//                 Path tempZipPath = Files.createTempFile("temp_decrypt", ".zip");
//                 Files.write(tempZipPath, decryptedZipData);

//                 // Extract from zip file
//                 try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempZipPath.toFile()))) {
//                     ZipEntry entry;

//                     while ((entry = zis.getNextEntry()) != null) {
//                         File outputFile = new File(savePath, entry.getName());

//                         // Create parent directories if needed
//                         if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
//                             outputFile.getParentFile().mkdirs();
//                         }

//                         // Extract file
//                         try (FileOutputStream fos = new FileOutputStream(outputFile)) {
//                             byte[] buffer = new byte[8192];
//                             int len;
//                             while ((len = zis.read(buffer)) > 0) {
//                                 fos.write(buffer, 0, len);
//                             }
//                         }

//                         System.out.println("Extracted: " + outputFile.getAbsolutePath());
//                     }
//                 }

//                 // Delete temp file
//                 Files.delete(tempZipPath);

//                 System.out.println("Decryption successful! Files saved at: " + savePath);

//             } catch (BadPaddingException e) {
//                 System.out.println("Decryption failed: Incorrect password or corrupted file.");
//             }

//         } catch (Exception e) {
//             System.out.println("Decryption failed: " + e.getMessage());
//             e.printStackTrace();
//         }

//         // Clear password from memory
//         Arrays.fill(password, '\0');
//     }
// }


package bytecypher;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.swing.*;

public class Encryption {

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_INSTANCE = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;

    public static void encrypt() {
        String inputPath = FileSelector.selectFile();
        if (inputPath == null) {
            System.out.println("No file selected. Operation cancelled.");
            return;
        }

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.out.println("Selected file does not exist. Check the path.");
            return;
        }

        System.out.println("File selected: " + inputPath);
        String savePath = FileSelector.selectSaveLocation("Encryption",".secfile");
        if (savePath == null) {
            System.out.println("No save location selected. Operation cancelled.");
            return;
        }

        // if (!savePath.endsWith(".secfile")) {
        //     savePath += ".secfile";
        // }

        // Ask for password
        JPasswordField pf = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(null, pf, "Enter a strong password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) {
            System.out.println("Password entry cancelled. Encryption aborted.");
            return;
        }

        char[] password = pf.getPassword();
        if (password.length < 8) {
            System.out.println("Password too short. Please use at least 8 characters.");
            return;
        }

        System.out.println("Encrypting...");
        try {
            // Generate a secure key from the password
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // Generate IV
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Generate key
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password, salt, 65536, KEY_SIZE);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), ALGORITHM);

            // Initialize Cipher
            Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // Create output streams
            try (FileOutputStream fos = new FileOutputStream(savePath); BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                // Write salt and IV first
                bos.write(salt);
                bos.write(iv);

                // Store original filename in the encrypted file (optional)
                byte[] fileNameBytes = inputFile.getName().getBytes("UTF-8");
                bos.write(fileNameBytes.length); // Storing filename length (1 byte - limits filenames to 255 chars)
                bos.write(fileNameBytes);        // Storing actual filename

                // Read original file and encrypt it directly
                byte[] fileData = Files.readAllBytes(inputFile.toPath());
                byte[] encryptedData = cipher.doFinal(fileData);

                // Write encrypted data
                bos.write(encryptedData);

                System.out.println("Encryption successful! Encrypted file saved at: " + savePath);
            }

        } catch (Exception e) {
            System.out.println("Encryption failed: " + e.getMessage());
            e.printStackTrace();
        }

        // Clear password from memory
        Arrays.fill(password, '\0');
    }

    public static void decrypt() {
        String inputPath = FileSelector.selectFile();
        if (inputPath == null) {
            System.out.println("No file selected. Operation cancelled.");
            return;
        }

        if (!inputPath.endsWith(".secfile")) {
            System.out.println("Invalid file format! Please select a valid .secfile file.");
            return;
        }

        String savePath = FileSelector.selectSaveLocation("DeEncryption","");
        if (savePath == null) {
            System.out.println("No save location selected. Operation cancelled.");
            return;
        }

        // Ask for password
        JPasswordField pf = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(null, pf, "Enter your password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) {
            System.out.println("Password entry cancelled. Decryption aborted.");
            return;
        }

        char[] password = pf.getPassword();

        System.out.println("Decrypting...");
        try {
            // Read encrypted file
            byte[] fileData = Files.readAllBytes(Paths.get(inputPath));

            if (fileData.length < 33) { // 16 (salt) + 16 (IV) + 1 (filename length)
                throw new IllegalArgumentException("Invalid encrypted file format");
            }

            // Extract salt and IV
            byte[] salt = Arrays.copyOfRange(fileData, 0, 16);
            byte[] iv = Arrays.copyOfRange(fileData, 16, 32);

            // Extract filename
            int fileNameLength = fileData[32] & 0xff; // Convert to unsigned

            if (fileData.length < 33 + fileNameLength) {
                throw new IllegalArgumentException("Invalid encrypted file format");
            }

            byte[] fileNameBytes = Arrays.copyOfRange(fileData, 33, 33 + fileNameLength);
            String originalFileName = new String(fileNameBytes, "UTF-8");

            // Get encrypted data
            byte[] encryptedData = Arrays.copyOfRange(fileData, 33 + fileNameLength, fileData.length);

            // Generate key from password and salt
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password, salt, 65536, KEY_SIZE);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), ALGORITHM);

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            try {
                // Decrypt the data
                byte[] decryptedData = cipher.doFinal(encryptedData);

                // Create output file path with original filename
                File outputDir = new File(savePath);
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }

                File outputFile = new File(outputDir, originalFileName);

                // If file exists, ask for confirmation
                if (outputFile.exists()) {
                    int overwrite = JOptionPane.showConfirmDialog(null,
                            "File '" + originalFileName + "' already exists. Overwrite?",
                            "File Exists", JOptionPane.YES_NO_OPTION);
                    if (overwrite != JOptionPane.YES_OPTION) {
                        // Ask for a new filename
                        String newName = JOptionPane.showInputDialog(
                                "Enter a new filename:", originalFileName);
                        if (newName == null || newName.trim().isEmpty()) {
                            System.out.println("Decryption cancelled by user.");
                            return;
                        }
                        outputFile = new File(outputDir, newName);
                    }
                }

                // Write decrypted data to file
                Files.write(outputFile.toPath(), decryptedData);

                System.out.println("Decryption successful! File saved as: " + outputFile.getAbsolutePath());

            } catch (BadPaddingException e) {
                System.out.println("Decryption failed: Incorrect password or corrupted file.");
            }

        } catch (Exception e) {
            System.out.println("Decryption failed: " + e.getMessage());
            e.printStackTrace();
        }

        // Clear password from memory
        Arrays.fill(password, '\0');
    }
}
