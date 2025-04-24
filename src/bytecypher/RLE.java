
package bytecypher;

import java.io.ByteArrayOutputStream;

public class RLE {

    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
        int count = 1;

        for (int i = 1; i < data.length; i++) {
            if (data[i] == data[i - 1] && count < 255) {
                count++;
            } else {
                compressedData.write((byte) count);
                compressedData.write(data[i - 1]);
                count = 1;
            }
        }

        compressedData.write((byte) count);
        compressedData.write(data[data.length - 1]);

        return compressedData.toByteArray();
    }

    public static byte[] decompress(byte[] data) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (int i = 0; i < data.length; i += 2) {
            if (i + 1 >= data.length) {
                // Handle cases where there's an odd number of bytes
                break;
            }

            int count = Byte.toUnsignedInt(data[i]);
            byte value = data[i + 1];

            for (int j = 0; j < count; j++) {
                output.write(value);
            }
        }

        return output.toByteArray();
    }
}
