package bytecypher;

import java.io.ByteArrayOutputStream;

public class LZ77 {

    private static final int WINDOW_SIZE = 4096; // Increased window size for better compression
    private static final int MIN_MATCH_LENGTH = 3;
    private static final int MAX_MATCH_LENGTH = 258; // Standard maximum match length

    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
        int dataLength = data.length;
        int position = 0;

        // Format: 2 bytes for offset, 2 bytes for length, followed by next byte
        // If offset is 0, this means literal byte
        while (position < dataLength) {
            // Find the longest match in the sliding window
            Match match = findLongestMatch(data, position, dataLength);

            if (match.length >= MIN_MATCH_LENGTH) {
                // Write offset and length (2 bytes each for larger values)
                writeShort(compressedData, match.offset);
                writeShort(compressedData, match.length);

                // Move forward by match length
                position += match.length;

                // Write next byte if not at the end
                if (position < dataLength) {
                    compressedData.write(data[position++]);
                } else {
                    compressedData.write(0); // End marker
                }
            } else {
                // Write literal byte
                writeShort(compressedData, 0); // 0 offset means literal
                writeShort(compressedData, 1); // Length 1
                compressedData.write(data[position++]);
            }
        }

        return compressedData.toByteArray();
    }

    public static byte[] decompress(byte[] data) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int dataLength = data.length;
        int position = 0;

        while (position < dataLength - 4) { // Need at least 4 bytes for offset+length
            int offset = readShort(data, position);
            position += 2;

            int length = readShort(data, position);
            position += 2;

            // Make sure we don't read past the end
            if (position >= dataLength) {
                break;
            }

            byte nextByte = data[position++];

            if (offset == 0) {
                // This is a literal byte
                output.write(nextByte);
            } else {
                // This is a back-reference
                int start = output.size() - offset;

                if (start < 0) {
                    // Invalid reference, skip
                    continue;
                }

                byte[] buffer = output.toByteArray();
                for (int i = 0; i < length; i++) {
                    // Handle overlapping matches by reading from output as we go
                    if (start + i < buffer.length) {
                        output.write(buffer[start + i]);
                    }
                }

                // Write the next byte
                output.write(nextByte);
            }
        }

        return output.toByteArray();
    }

    private static void writeShort(ByteArrayOutputStream stream, int value) {
        stream.write((value >> 8) & 0xFF); // High byte
        stream.write(value & 0xFF);        // Low byte
    }

    private static int readShort(byte[] data, int position) {
        return ((data[position] & 0xFF) << 8) | (data[position + 1] & 0xFF);
    }

    private static Match findLongestMatch(byte[] data, int currentPosition, int dataLength) {
        int maxMatchDistance = Math.min(currentPosition, WINDOW_SIZE);
        int matchPosition = 0;
        int maxMatchLength = 0;

        // Don't try to match more bytes than we have
        int maxBytesToMatch = Math.min(MAX_MATCH_LENGTH, dataLength - currentPosition);

        if (maxBytesToMatch < MIN_MATCH_LENGTH) {
            // Not enough bytes left to make a worthwhile match
            return new Match(0, 0);
        }

        // Search backward through the window
        for (int i = currentPosition - 1; i >= currentPosition - maxMatchDistance; i--) {
            // Quick check if first byte matches
            if (data[i] != data[currentPosition]) {
                continue;
            }

            // Count matching bytes
            int matchLength = 1;
            while (matchLength < maxBytesToMatch
                    && data[i + matchLength] == data[currentPosition + matchLength]) {
                matchLength++;
            }

            if (matchLength > maxMatchLength) {
                maxMatchLength = matchLength;
                matchPosition = i;

                // If we found a match of maximum length, we can stop early
                if (maxMatchLength == maxBytesToMatch) {
                    break;
                }
            }
        }

        if (maxMatchLength >= MIN_MATCH_LENGTH) {
            return new Match(currentPosition - matchPosition, maxMatchLength);
        }

        return new Match(0, 0);
    }

    private static class Match {

        final int offset;
        final int length;

        Match(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }
    }
}
