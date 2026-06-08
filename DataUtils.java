import java.util.Random;

public class DataUtils {

    public static byte[] hexToBytes(String hex) {
        hex = hex.replaceAll("\\s+", "").toLowerCase();
        if (hex.length() % 2 != 0) hex = "0" + hex;
        // every byte is represented by 2 hex chars
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            // parse hex characters, so we use base 16
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        // 02x -> 0 padded format to be sure it is well represented in hex
        // the & is used here to prevent getting a bad negative overflow, when doing & ff we make sure it is unsigned
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    public static int[] bytesToBits(byte[] bytes) {
        // every byte will be represented in 8 bits (8 items in this array)
        int[] bits = new int[bytes.length * 8];
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                // each bit in the one byte is:-
                // shift by the bit index (7 - j)
                // then & 1 to determine if the bit here is 1 or 0
                bits[i * 8 + j] = (bytes[i] >> (7 - j)) & 1;
            }
        }
        return bits;
    }

    public static byte[] bitsToBytes(int[] bits) {
        int numBytes = (bits.length + 7) / 8;
        byte[] bytes = new byte[numBytes];
        for (int i = 0; i < bits.length; i++) {
            // the byte is initially 00000000
            // for every one bit we can put it in it's position by shifting by it's index
            // example: 1 in index 7 (leftmost bit)
            // shift 1 by 7 positions => 10000000
            // then | with the byte
            // 1 will be there
            bytes[i / 8] = (byte) (bytes[i / 8] | (bits[i] << (7 - (i % 8))));
        }
        return bytes;
    }

    public static String bitsToHex(int[] bits) {
        return bytesToHex(bitsToBytes(bits));
    }

    public static String bitsToBinaryString(int[] bits) {
        // just normal auto casting so the integer is in the string as a char
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bits.length; i++) {
            if (i > 0 && i % 8 == 0) sb.append(" ");
            sb.append(bits[i]);
        }
        return sb.toString();
    }

    public static int[] hexToBits(String hex) {
        return bytesToBits(hexToBytes(hex));
    }

    public static int[] stringToBits(String str) {
        return bytesToBits(str.getBytes());
    }

    public static String bitsToString(int[] bits) {
        return new String(bitsToBytes(bits));
    }

    public static int[] generateRandomKey() {
        Random rnd = new Random();
        byte[] keyBytes = new byte[8];
        rnd.nextBytes(keyBytes);
        return bytesToBits(keyBytes);
    }

    public static int[] padBytes(byte[] data) {
        int blockSize = 8;
        // the remaining bytes so it is % 8
        int padLen = blockSize - (data.length % blockSize);
        byte[] padded = new byte[data.length + padLen];
        for (int i = 0; i < data.length; i++) padded[i] = data[i];
        for (int i = data.length; i < padded.length; i++) {
            // padding bytes will be = how much pad, so i can remove them
            padded[i] = (byte) padLen;
        }
        return bytesToBits(padded);
    }

    public static byte[] unpadBytes(byte[] data) {
        if (data.length == 0) return data;
        int padLen = data[data.length - 1] & 0xff;
        // no padding
        if (padLen < 1 || padLen > 8) return data;
        for (int i = data.length - padLen; i < data.length; i++) {
            if ((data[i] & 0xff) != padLen) return data;
        }
        // remove pad
        byte[] result = new byte[data.length - padLen];
        for (int i = 0; i < result.length; i++) result[i] = data[i];
        return result;
    }

    public static int[] hexToKeyBits(String hex) {
        return hexToBits(hex);
    }

    public static int[] stringToKeyBits(String str) {
        byte[] bytes = str.getBytes();
        if (bytes.length > 8) {
            byte[] truncated = new byte[8];
            for (int i = 0; i < 8; i++) truncated[i] = bytes[i];
            return bytesToBits(truncated);
        }
        return bytesToBits(bytes);
    }

    public static String keyBitsToString(int[] bits) {
        return bitsToString(bits);
    }
}
