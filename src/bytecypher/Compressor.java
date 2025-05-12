package bytecypher;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.concurrent.atomic.AtomicInteger;

public class Compressor {

    private static JProgressBar progressBar;
    private static JDialog progressDialog;
    private static AtomicInteger processedFiles = new AtomicInteger(0);
    private static int totalFiles = 0;

    public static void compress(boolean isFile) {
        String inputPath = isFile ? FileSelector.selectFile() : FileSelector.selectFolder();
        if (inputPath == null) {
            System.out.println("No file or folder selected. Operation cancelled.");
            return;
        }

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.out.println("Selected file or folder does not exist. Check the path.");
            return;
        }

        System.out.println("File/Folder selected: " + inputPath);
        String savePath = FileSelector.selectSaveLocation("Compressed", ".bc");
        if (savePath == null) {
            System.out.println("No save location selected. Operation cancelled.");
            return;
        }

        // Count files to compress for progress tracking
        totalFiles = 0;
        processedFiles.set(0);
        try {
            if (isFile) {
                totalFiles = 1;
            } else {
                totalFiles = countFiles(inputFile);
            }
        } catch (Exception e) {
            System.out.println("Error counting files: " + e.getMessage());
        }

        // Initialize progress dialog
        SwingUtilities.invokeLater(() -> {
            progressDialog = new JDialog((java.awt.Frame) null, "Compressing Files", false);
            progressBar = new JProgressBar(0, totalFiles);
            progressBar.setStringPainted(true);
            progressBar.setString("Processing 0/" + totalFiles + " files...");

            progressDialog.add(progressBar, BorderLayout.CENTER);
            progressDialog.pack();
            progressDialog.setSize(400, 100);
            progressDialog.setLocationRelativeTo(null);
            progressDialog.setVisible(true);
        });

        System.out.println("Compressing...");
        try (FileOutputStream fos = new FileOutputStream(savePath); ZipOutputStream zos = new ZipOutputStream(fos)) {

            Path rootPath = inputFile.toPath();
            String rootName = rootPath.getFileName().toString();

            if (isFile) {
                compressFile(inputFile, inputFile.getName(), zos);
                updateProgress();
            } else {
                // Use Files.walkFileTree for proper directory traversal
                Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        // Get the relative path from the root folder
                        String relativePath = rootName + "/" + rootPath.relativize(file).toString();
                        compressFile(file.toFile(), relativePath, zos);
                        updateProgress();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (!dir.equals(rootPath)) {
                            String relativePath = rootName + "/" + rootPath.relativize(dir).toString() + "/";
                            zos.putNextEntry(new ZipEntry(relativePath));
                            zos.closeEntry();
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            // Close progress dialog
            SwingUtilities.invokeLater(() -> {
                if (progressDialog != null) {
                    progressDialog.dispose();
                }
            });

            System.out.println("Compression successful! Compressed file saved at: " + savePath);
            JOptionPane.showMessageDialog(null,
                    "Compression successful!\nCompressed file saved at: " + savePath,
                    "Compression Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                if (progressDialog != null) {
                    progressDialog.dispose();
                }
            });
            System.out.println("Compression failed: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Compression failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updateProgress() {
        int current = processedFiles.incrementAndGet();
        SwingUtilities.invokeLater(() -> {
            if (progressBar != null) {
                progressBar.setValue(current);
                progressBar.setString("Processing " + current + "/" + totalFiles + " files...");
            }
        });
    }

    private static int countFiles(File directory) throws IOException {
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    count++;
                } else if (file.isDirectory()) {
                    count += countFiles(file);
                }
            }
        }
        return count;
    }

    private static void compressFile(File file, String entryName, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {
            return; // Skip directories as they are handled separately
        }

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        // Determine the best compression method based on file type
        String fileExtension = getFileExtension(file.getName()).toLowerCase();
        byte[] compressedData;
        String compressionMethod;

        if (isAlreadyCompressedFile(fileExtension)) {
            // For already compressed files, just store as is
            compressedData = fileBytes;
            compressionMethod = "store";
            // System.out.println("Storing already compressed file: " + file.getName());
        } else if (isTextFile(fileExtension)) {
            // Use Huffman for text files
            HuffmanCompression huffman = new HuffmanCompression();
            compressedData = huffman.compress(fileBytes);

            // If Huffman compression isn't effective, try LZ77
            if (compressedData.length > fileBytes.length * 0.9) {
                compressedData = LZ77.compress(fileBytes);
                compressionMethod = "lz77";
                // System.out.println("Using LZ77 compression for: " + file.getName());
            } else {
                compressionMethod = "huffman";
                // System.out.println("Using Huffman compression for: " + file.getName());
            }
        } else if (isBinaryFile(fileExtension)) {
            // Use LZ77 for binary files
            compressedData = LZ77.compress(fileBytes);

            // If compression isn't effective, just store
            if (compressedData.length > fileBytes.length * 0.95) {
                compressedData = fileBytes;
                compressionMethod = "store";
                // System.out.println("Storing incompressible file: " + file.getName());
            } else {
                compressionMethod = "lz77";
                // System.out.println("Using LZ77 compression for: " + file.getName());
            }
        } else {
            // For unknown files, try RLE which is better for binary data with repetition
            compressedData = RLE.compress(fileBytes);

            // Check if RLE is effective
            if (compressedData.length > fileBytes.length * 0.95) {
                // If not effective, try LZ77
                byte[] lz77Data = LZ77.compress(fileBytes);

                if (lz77Data.length < compressedData.length && lz77Data.length < fileBytes.length * 0.95) {
                    compressedData = lz77Data;
                    compressionMethod = "lz77";
                    // System.out.println("Using LZ77 compression for: " + file.getName());
                } else {
                    // If nothing works well, just store
                    compressedData = fileBytes;
                    compressionMethod = "store";
                    // System.out.println("Storing incompressible file: " + file.getName());
                }
            } else {
                compressionMethod = "rle";
                // System.out.println("Using RLE compression for: " + file.getName());
            }
        }

        // Create zip entry for the compressed file
        ZipEntry zipEntry = new ZipEntry(entryName);
        zos.putNextEntry(zipEntry);

        // Use a new DataOutputStream that doesn't close the underlying stream
        DataOutputStream dos = new DataOutputStream(new NonClosingOutputStream(zos));

        try {
            dos.writeUTF(compressionMethod);              // Write compression method used
            dos.writeLong(fileBytes.length);              // Write original file size

            if (compressionMethod.equals("huffman")) {
                // For Huffman, we need to save the tree
                HuffmanCompression huffman = new HuffmanCompression();
                huffman.compress(fileBytes); // Recreate the tree

                // Use ObjectOutputStream that doesn't close the underlying stream
                ObjectOutputStream oos = new ObjectOutputStream(new NonClosingOutputStream(dos));
                oos.writeObject(huffman.getTree());       // Save the Huffman tree
                oos.flush();
            }

            dos.writeInt(compressedData.length);          // Write compressed data length
            dos.write(compressedData);                    // Write compressed data
            dos.flush();
        } finally {
            // Don't close dos here, just flush it
            dos.flush();
        }

        zos.closeEntry();

        // Print compression ratio
        double ratio = 100.0 - ((double) compressedData.length * 100.0 / fileBytes.length);
        System.out.printf("Compressed %s - Original: %d bytes, Compressed: %d bytes (%.2f%% reduction)%n",
                file.getName(), fileBytes.length, compressedData.length, ratio);
    }

    // Add this helper class to prevent stream closing
    private static class NonClosingOutputStream extends java.io.FilterOutputStream {

        public NonClosingOutputStream(java.io.OutputStream out) {
            super(out);
        }

        @Override
        public void close() throws IOException {
            // Don't close the underlying stream
            flush();
        }
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    private static boolean isTextFile(String extension) {
        return extension.matches("txt|html|css|js|java|py|c|cpp|h|xml|json|md|csv|log|ini|conf|properties|sql|sh|bat");
    }

    private static boolean isAlreadyCompressedFile(String extension) {
        return extension.matches("jpg|jpeg|png|gif|mp3|mp4|avi|mov|mkv|flac|wav|ogg|webm|pdf|zip|rar|7z|gz|bz2|xz|tar|webp");
    }

    private static boolean isBinaryFile(String extension) {
        return extension.matches("exe|dll|so|class|obj|bin|dat|db|sqlite|mdb|accdb|pdb|o");
    }
}
