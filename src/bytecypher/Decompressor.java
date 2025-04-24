// package bytecypher;

// import java.io.*;
// import java.nio.file.Files;
// import java.util.zip.ZipEntry;
// import java.util.zip.ZipInputStream;

// public class Decompressor {

//     public static void decompress() {
//         String inputPath = FileSelector.selectFile();
//         if (inputPath == null) {
//             System.out.println("No file selected. Operation cancelled.");
//             return;
//         }

//         if (!inputPath.endsWith(".zip")) {
//             System.out.println("Invalid file format! Please select a valid .zip file.");
//             return;
//         }

//         String savePath = FileSelector.selectSaveLocation();
//         if (savePath == null) {
//             System.out.println("No save location selected. Operation cancelled.");
//             return;
//         }

//         System.out.println("Decompressing...");
//         File saveDirectory = new File(savePath);

//         try (ZipInputStream zis = new ZipInputStream(new FileInputStream(inputPath))) {
//             ZipEntry entry;
//             int fileCount = 0;
//             File lastExtractedFile = null;

//             while ((entry = zis.getNextEntry()) != null) {
//                 fileCount++;

//                 // Read Huffman tree and compressed data
//                 ObjectInputStream ois = new ObjectInputStream(zis);
//                 HuffmanCompression huffman = new HuffmanCompression();
//                 HuffmanCompression.Node tree = (HuffmanCompression.Node) ois.readObject();
//                 huffman.setTree(tree);
//                 int dataLength = ois.readInt();
//                 byte[] compressedData = new byte[dataLength];
//                 ois.readFully(compressedData);
//                 System.out.println("Huffman Input: " + java.util.Arrays.toString(java.util.Arrays.copyOf(compressedData, Math.min(20, compressedData.length))));

//                 byte[] huffmanDecoded = huffman.decompress(compressedData);
//                 System.out.println("After Huffman: " + java.util.Arrays.toString(java.util.Arrays.copyOf(huffmanDecoded, Math.min(20, huffmanDecoded.length))));

//                 // Get the original file name and remove ".bc" extension
//                 String originalFileName = entry.getName().replace(".bc", "");

//                 // Determine save path based on number of files
//                 File decompressedFile;
//                 if (fileCount == 1 && zis.available() == 0) {
//                     // If it's the only file in ZIP, save directly
//                     decompressedFile = new File(savePath, originalFileName);
//                 } else {
//                     // If multiple files exist, save inside a folder
//                     File outputFolder = new File(savePath);
//                     if (!outputFolder.exists()) {
//                         outputFolder.mkdirs();
//                     }
//                     decompressedFile = new File(outputFolder, originalFileName);
//                 }

//                 Files.write(decompressedFile.toPath(), huffmanDecoded);
//                 lastExtractedFile = decompressedFile;
//             }

//             if (fileCount == 1) {
//                 System.out.println("Decompression successful! File saved at: " + lastExtractedFile.getAbsolutePath());
//             } else {
//                 System.out.println("Decompression successful! Files saved inside: " + saveDirectory.getAbsolutePath());
//             }

//         } catch (IOException | ClassNotFoundException e) {
//             System.out.println("Decompression failed: " + e.getMessage());
//         }
//     }
// }


// package bytecypher;

// import java.io.*;
// import java.util.zip.ZipEntry;
// import java.util.zip.ZipInputStream;

// public class Decompressor {

//     public static void decompress() {
//         String inputPath = FileSelector.selectFile();
//         if (inputPath == null) {
//             System.out.println("No file selected. Operation cancelled.");
//             return;
//         }

//         if (!inputPath.endsWith(".zip")) {
//             System.out.println("Invalid file format! Please select a valid .zip file.");
//             return;
//         }

//         String savePath = FileSelector.selectSaveLocation();
//         if (savePath == null) {
//             System.out.println("No save location selected. Operation cancelled.");
//             return;
//         }

//         System.out.println("Decompressing...");
//         File saveDirectory = new File(savePath);

//         try (ZipInputStream zis = new ZipInputStream(new FileInputStream(inputPath))) {
//             ZipEntry entry;
//             int fileCount = 0;
//             boolean isFolderZip = false;
//             String folderName = null;

//             // First pass to determine if we have a folder structure or single file
//             ZipInputStream firstPassZis = new ZipInputStream(new FileInputStream(inputPath));
//             ZipEntry firstEntry = firstPassZis.getNextEntry();
//             if (firstEntry != null) {
//                 // Check if entries contain path separators (indicating a folder)
//                 if (firstEntry.getName().contains("/")) {
//                     isFolderZip = true;
//                     folderName = firstEntry.getName().split("/")[0];
//                 }
//             }
//             firstPassZis.close();

//             // Second pass for actual decompression
//             while ((entry = zis.getNextEntry()) != null) {
//                 fileCount++;

//                 if (entry.isDirectory()) {
//                     // Create directory structure if needed
//                     File directory = new File(saveDirectory, entry.getName());
//                     if (!directory.exists()) {
//                         directory.mkdirs();
//                     }
//                     continue;
//                 }

//                 // Get entry name without the .bc extension
//                 String entryName = entry.getName().replace(".bc", "");

//                 // Determine where to save the file
//                 File outputFile;
//                 if (isFolderZip) {
//                     // Maintain folder structure
//                     outputFile = new File(saveDirectory, entryName);
//                 } else if (fileCount == 1) {
//                     // If it's a single file, save directly in the selected folder
//                     outputFile = new File(saveDirectory, new File(entryName).getName());
//                 } else {
//                     // If multiple files but no folder structure, create a folder with zip name
//                     String zipFileName = new File(inputPath).getName().replace(".zip", "");
//                     File outputFolder = new File(saveDirectory, zipFileName);
//                     if (!outputFolder.exists()) {
//                         outputFolder.mkdirs();
//                     }
//                     outputFile = new File(outputFolder, new File(entryName).getName());
//                 }

//                 // Create parent directories if needed
//                 if (!outputFile.getParentFile().exists()) {
//                     outputFile.getParentFile().mkdirs();
//                 }

//                 // Read compression method and other metadata
//                 DataInputStream dis = new DataInputStream(zis);
//                 String compressionMethod = dis.readUTF();
//                 long originalSize = dis.readLong();

//                 byte[] decompressedData;

//                 if (compressionMethod.equals("huffman")) {
//                     // Handle Huffman decompression
//                     ObjectInputStream ois = new ObjectInputStream(dis);
//                     HuffmanCompression huffman = new HuffmanCompression();
//                     HuffmanCompression.Node tree = (HuffmanCompression.Node) ois.readObject();
//                     huffman.setTree(tree);

//                     int dataLength = dis.readInt();
//                     byte[] compressedData = new byte[dataLength];
//                     dis.readFully(compressedData);

//                     decompressedData = huffman.decompress(compressedData);
//                 } else if (compressionMethod.equals("lz77")) {
//                     // Handle LZ77 decompression
//                     int dataLength = dis.readInt();
//                     byte[] compressedData = new byte[dataLength];
//                     dis.readFully(compressedData);

//                     decompressedData = LZ77.decompress(compressedData);
//                 } else {
//                     // Handle stored files (no compression)
//                     int dataLength = dis.readInt();
//                     decompressedData = new byte[dataLength];
//                     dis.readFully(decompressedData);
//                 }

//                 // Write the decompressed data to the output file
//                 try (FileOutputStream fos = new FileOutputStream(outputFile)) {
//                     fos.write(decompressedData);
//                 }

//                 System.out.printf("Decompressed: %s (%d bytes)%n", outputFile.getName(), decompressedData.length);
//             }

//             if (fileCount == 0) {
//                 System.out.println("No files found in the archive.");
//             } else if (isFolderZip) {
//                 System.out.println("Decompression successful! Files saved with folder structure at: " + saveDirectory.getAbsolutePath());
//             } else if (fileCount == 1) {
//                 System.out.println("Decompression successful! File saved at: " + saveDirectory.getAbsolutePath());
//             } else {
//                 System.out.println("Decompression successful! Files saved inside: " + saveDirectory.getAbsolutePath());
//             }

//         } catch (IOException | ClassNotFoundException e) {
//             System.out.println("Decompression failed: " + e.getMessage());
//             e.printStackTrace();
//         }
//     }
// }


package bytecypher;

import java.awt.BorderLayout;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class Decompressor {

    private static JProgressBar progressBar;
    private static JDialog progressDialog;
    private static int processedFiles = 0;
    private static int totalFiles = 0;

    public static void decompress() {
        String inputPath = FileSelector.selectFile();
        if (inputPath == null) {
            System.out.println("No file selected. Operation cancelled.");
            return;
        }

        // Accept both .bc and .zip files
        if (!inputPath.endsWith(".bc") && !inputPath.endsWith(".zip")) {
            System.out.println("Invalid file format! Please select a valid .bc or .zip file.");
            JOptionPane.showMessageDialog(null,
                    "Invalid file format! Please select a valid .bc or .zip file.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String savePath = FileSelector.selectDecompressionSaveLocation();
        if (savePath == null) {
            System.out.println("No save location selected. Operation cancelled.");
            return;
        }

        System.out.println("Decompressing...");
        File saveDirectory = new File(savePath);

        // Count files to decompress for progress tracking
        try {
            totalFiles = countFilesInZip(inputPath);
        } catch (Exception e) {
            System.out.println("Error counting files in archive: " + e.getMessage());
            totalFiles = 10; // Default value if counting fails
        }
        processedFiles = 0;

        // Initialize progress dialog
        SwingUtilities.invokeLater(() -> {
            progressDialog = new JDialog((java.awt.Frame) null, "Decompressing Files", false);
            progressBar = new JProgressBar(0, totalFiles);
            progressBar.setStringPainted(true);
            progressBar.setString("Extracting 0/" + totalFiles + " files...");

            progressDialog.add(progressBar, BorderLayout.CENTER);
            progressDialog.pack();
            progressDialog.setSize(400, 100);
            progressDialog.setLocationRelativeTo(null);
            progressDialog.setVisible(true);
        });

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(inputPath))) {
            ZipEntry entry;
            boolean isFolderZip = false;
            String folderName = null;

            // First pass to determine if we have a folder structure or single file
            try (ZipInputStream firstPassZis = new ZipInputStream(new FileInputStream(inputPath))) {
                ZipEntry firstEntry = firstPassZis.getNextEntry();
                if (firstEntry != null) {
                    // Check if entries contain path separators (indicating a folder)
                    if (firstEntry.getName().contains("/")) {
                        isFolderZip = true;
                        folderName = firstEntry.getName().split("/")[0];
                    }
                }
            }

            // Second pass for actual decompression
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    // Create directory structure if needed
                    File directory = new File(saveDirectory, entry.getName());
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    continue;
                }

                // Update progress
                updateProgress();

                // Get entry name without the .bc extension if present
                String entryName = entry.getName();
                if (entryName.endsWith(".bc")) {
                    entryName = entryName.substring(0, entryName.length() - 3);
                }

                // Determine where to save the file
                File outputFile;
                if (isFolderZip) {
                    // Maintain folder structure
                    outputFile = new File(saveDirectory, entryName);
                } else {
                    // If it's a single file, save directly in the selected folder
                    outputFile = new File(saveDirectory, new File(entryName).getName());
                }

                // Create parent directories if needed
                if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }

                try {
                    // Read compression method and other metadata
                    DataInputStream dis = new DataInputStream(zis);

                    // First try to read as a ByteCypher compressed file
                    try {
                        String compressionMethod = dis.readUTF();
                        long originalSize = dis.readLong();

                        byte[] decompressedData;

                        if (compressionMethod.equals("huffman")) {
                            // Handle Huffman decompression
                            ObjectInputStream ois = new ObjectInputStream(dis);
                            HuffmanCompression huffman = new HuffmanCompression();
                            HuffmanCompression.Node tree = (HuffmanCompression.Node) ois.readObject();
                            huffman.setTree(tree);

                            int dataLength = dis.readInt();
                            byte[] compressedData = new byte[dataLength];
                            dis.readFully(compressedData);

                            decompressedData = huffman.decompress(compressedData);
                        } else if (compressionMethod.equals("lz77")) {
                            // Handle LZ77 decompression
                            int dataLength = dis.readInt();
                            byte[] compressedData = new byte[dataLength];
                            dis.readFully(compressedData);

                            decompressedData = LZ77.decompress(compressedData);
                        } else if (compressionMethod.equals("rle")) {
                            // Handle RLE decompression
                            int dataLength = dis.readInt();
                            byte[] compressedData = new byte[dataLength];
                            dis.readFully(compressedData);

                            decompressedData = RLE.decompress(compressedData);
                        } else {
                            // Handle stored files (no compression)
                            int dataLength = dis.readInt();
                            decompressedData = new byte[dataLength];
                            dis.readFully(decompressedData);
                        }

                        // Write the decompressed data to the output file
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            fos.write(decompressedData);
                        }

                    } catch (Exception e) {
                        // If reading as ByteCypher format fails, try standard ZIP format
                        // Read all bytes from the current entry
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }

                        // Write to output file
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            fos.write(baos.toByteArray());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error decompressing entry " + entry.getName() + ": " + e.getMessage());
                }

                System.out.printf("Decompressed: %s%n", outputFile.getName());
            }

            // Close progress dialog
            SwingUtilities.invokeLater(() -> {
                if (progressDialog != null) {
                    progressDialog.dispose();
                }
            });

            if (processedFiles == 0) {
                System.out.println("No files found in the archive.");
                JOptionPane.showMessageDialog(null,
                        "No files found in the archive.",
                        "Decompression Result", JOptionPane.WARNING_MESSAGE);
            } else {
                System.out.println("Decompression successful! Files saved at: " + saveDirectory.getAbsolutePath());
                JOptionPane.showMessageDialog(null,
                        "Decompression successful!\nFiles saved at: " + saveDirectory.getAbsolutePath(),
                        "Decompression Complete", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (IOException e) {
            // Close progress dialog
            SwingUtilities.invokeLater(() -> {
                if (progressDialog != null) {
                    progressDialog.dispose();
                }
            });

            System.out.println("Decompression failed: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Decompression failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updateProgress() {
        processedFiles++;
        SwingUtilities.invokeLater(() -> {
            if (progressBar != null) {
                progressBar.setValue(processedFiles);
                progressBar.setString("Extracting " + processedFiles + "/" + totalFiles + " files...");
            }
        });
    }

    private static int countFilesInZip(String zipFilePath) throws IOException {
        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    count++;
                }
            }
        }
        return count > 0 ? count : 1; // At least 1 to avoid division by zero
    }
}
