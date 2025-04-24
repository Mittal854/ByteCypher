package bytecypher;

import java.io.*;
import java.util.*;

public class HuffmanCompression implements Serializable {

    private static final long serialVersionUID = 2L;

    // Node class implements Serializable so the tree can be saved and restored.
    public static class Node implements Comparable<Node>, Serializable {

        private static final long serialVersionUID = 1L;
        char ch;
        int freq;
        Node left, right;

        Node(char ch, int freq, Node left, Node right) {
            this.ch = ch;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        public int compareTo(Node other) {
            return this.freq - other.freq;
        }
    }

    private transient Map<Character, String> huffmanCodes; // Not serialized because it can be rebuilt.
    private Node root;  // The Huffman tree
    private int originalLength; // Store the original length for accurate decompression

    // Compress the data and build the Huffman tree.
    public byte[] compress(byte[] data) {
        if (data.length == 0) {
            return new byte[0]; // Handle empty input
        }

        originalLength = data.length;
        String text = new String(data);
        Map<Character, Integer> frequencyMap = new HashMap<>();

        // Build frequency map.
        for (char c : text.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        // Handle single character case
        if (frequencyMap.size() == 1) {
            char singleChar = frequencyMap.keySet().iterator().next();

            // Create a special tree for single character
            root = new Node(singleChar, frequencyMap.get(singleChar), null, null);
            huffmanCodes = new HashMap<>();
            huffmanCodes.put(singleChar, "0"); // Use "0" as the code

            // For a single repeated character, just return a small representation
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            try {
                dos.writeInt(1); // Signal that this is a single character compression
                dos.writeChar(singleChar);
                dos.writeInt(originalLength); // How many times to repeat it
                dos.close();
                return baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                return data; // Return original data if compression fails
            }
        }

        // Build the priority queue.
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            pq.offer(new Node(entry.getKey(), entry.getValue(), null, null));
        }

        // Build the Huffman tree.
        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            pq.offer(new Node('\0', left.freq + right.freq, left, right));
        }

        root = pq.poll();
        huffmanCodes = new HashMap<>();
        buildHuffmanCodes(root, "");

        // Encode the text using the generated codes.
        StringBuilder encodedString = new StringBuilder();
        for (char c : text.toCharArray()) {
            encodedString.append(huffmanCodes.get(c));
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // Regular huffman compression
            dos.writeInt(0); // Signal that this is a regular compression

            // Store the necessary info for proper decompression
            dos.writeInt(originalLength); // Original data length in bytes

            byte[] bitStream = toByteArray(encodedString.toString());
            dos.writeInt(encodedString.length()); // Bit length for proper padding handling
            dos.writeInt(bitStream.length); // Byte array length
            dos.write(bitStream);
            dos.close();

            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return data; // Return original data if compression fails
        }
    }

    // Decompress the data using the stored Huffman tree.
    public byte[] decompress(byte[] data) throws IOException {
        if (data.length == 0) {
            return new byte[0]; // Handle empty input
        }

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        int compressionType = dis.readInt();

        if (compressionType == 1) {
            // This is a single character compression
            char singleChar = dis.readChar();
            int repeatCount = dis.readInt();

            StringBuilder decodedText = new StringBuilder(repeatCount);
            for (int i = 0; i < repeatCount; i++) {
                decodedText.append(singleChar);
            }
            return decodedText.toString().getBytes();
        }

        // Regular huffman compression
        originalLength = dis.readInt();
        int bitLength = dis.readInt();
        int byteArrayLength = dis.readInt();

        byte[] compressedBytes = new byte[byteArrayLength];
        dis.readFully(compressedBytes);

        String binaryString = toBinaryString(compressedBytes, bitLength);

        if (root == null) {
            throw new IOException("Huffman tree not initialized. Cannot decompress.");
        }

        StringBuilder decodedText = new StringBuilder(originalLength);
        Node current = root;

        // Special case for single node tree
        if (root.left == null && root.right == null) {
            for (int i = 0; i < originalLength; i++) {
                decodedText.append(root.ch);
            }
            return decodedText.toString().getBytes();
        }

        // Decode bit by bit.
        for (int i = 0; i < binaryString.length(); i++) {
            char bit = binaryString.charAt(i);
            current = (bit == '0') ? current.left : current.right;

            if (current.left == null && current.right == null) {
                decodedText.append(current.ch);
                current = root;

                // Break early if we've reached the original length
                if (decodedText.length() >= originalLength) {
                    break;
                }
            }
        }

        return decodedText.toString().getBytes();
    }

    // Recursively build the Huffman codes.
    private void buildHuffmanCodes(Node node, String code) {
        if (node == null) {
            return;
        }
        if (node.left == null && node.right == null) {
            huffmanCodes.put(node.ch, code.isEmpty() ? "0" : code);
        }
        buildHuffmanCodes(node.left, code + "0");
        buildHuffmanCodes(node.right, code + "1");
    }

    // Convert a binary string into a byte array.
    private byte[] toByteArray(String binaryString) {
        int byteCount = (binaryString.length() + 7) / 8;
        byte[] byteArray = new byte[byteCount];

        for (int i = 0; i < binaryString.length(); i++) {
            if (binaryString.charAt(i) == '1') {
                byteArray[i / 8] |= (128 >> (i % 8));
            }
        }

        return byteArray;
    }

    // Convert a byte array to a binary string.
    private String toBinaryString(byte[] byteArray, int bitLength) {
        StringBuilder binaryString = new StringBuilder();

        for (int i = 0; i < byteArray.length; i++) {
            byte b = byteArray[i];

            for (int j = 7; j >= 0; j--) {
                binaryString.append((b >> j) & 1);

                // Stop if we've reached the actual bit length
                if (binaryString.length() >= bitLength) {
                    break;
                }
            }

            // Stop if we've reached the actual bit length
            if (binaryString.length() >= bitLength) {
                break;
            }
        }

        return binaryString.toString();
    }

    // Getter for the Huffman tree.
    public Node getTree() {
        return root;
    }

    // Setter to restore the Huffman tree.
    public void setTree(Node tree) {
        this.root = tree;
    }

    // Get the original length of the data
    public int getOriginalLength() {
        return originalLength;
    }

    // Set the original length of the data
    public void setOriginalLength(int length) {
        this.originalLength = length;
    }
}
