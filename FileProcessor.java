import java.io.*;

public class FileProcessor {
    private DES des;

    public FileProcessor() {
        this.des = new DES();
    }

    public void encryptFile(String inputPath, String outputPath, int[] key) throws IOException {
        // read file as bytes (this allows us encrypt images and other files
        byte[] fileData = readAllBytes(inputPath);

        // pad file bytes so we can use it in our DES
        // give us a bit array of our bytes
        int[] paddedBits = DataUtils.padBytes(fileData);
        // count DES blocks (64 bits each)
        int numBlocks = paddedBits.length / 64;
        // each block is 8 bytes
        byte[] encrypted = new byte[numBlocks * 8];

        for (int i = 0; i < numBlocks; i++) {
            int[] block = new int[64];
            DES.copy(paddedBits, i * 64, block, 0, 64);
            int[] encBlock = des.encrypt(block, key);
            byte[] encBytes = DataUtils.bitsToBytes(encBlock);
            for (int j = 0; j < 8; j++) encrypted[i * 8 + j] = encBytes[j];
        }

        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            // write to encryption file
            fos.write(encrypted);
        }
    }

    public void decryptFile(String inputPath, String outputPath, int[] key) throws IOException {
        // file as bytes
        byte[] fileData = readAllBytes(inputPath);

        // not encrypted by us
        if (fileData.length % 8 != 0) {
            throw new IOException("Invalid encrypted file: size not multiple of 8 bytes");
        }

        int numBlocks = fileData.length / 8;
        byte[] decrypted = new byte[fileData.length];
        for (int i = 0; i < numBlocks; i++) {
            byte[] blockBytes = new byte[8];
            // bytes (we have no copy method for bytes
            for (int j = 0; j < 8; j++) blockBytes[j] = fileData[i * 8 + j];

            // no padding so we need separately to convert to bits
            int[] block = DataUtils.bytesToBits(blockBytes);
            int[] decBlock = des.decrypt(block, key);
            byte[] decBytes = DataUtils.bitsToBytes(decBlock);
            for (int j = 0; j < 8; j++) decrypted[i * 8 + j] = decBytes[j];
        }

        // remove padding if there is
        byte[] unpadded = DataUtils.unpadBytes(decrypted);
        // write to decryption file
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(unpadded);
        }
    }

    // read as bytes
    public static byte[] readAllBytes(String path) throws IOException {
        File file = new File(path);
        byte[] data = new byte[(int) file.length()];
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            int offset = 0;
            while (offset < data.length) {
                int bytesRead = bis.read(data, offset, data.length - offset);
                if (bytesRead == -1) break;

                // move offset forward so we don't return to the first bit
                offset += bytesRead;
            }
        }
        return data;
    }

    public static String readString(String path) throws IOException {
        return new String(readAllBytes(path)).trim();
    }

    public static void writeString(String path, String content) throws IOException {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write(content);
        }
    }

    public String getEncryptedOutputPath(String inputPath) {
        return inputPath + ".des";
    }

    public String getDecryptedOutputPath(String encryptedPath) {
        if (encryptedPath.endsWith(".des")) {
            return encryptedPath.substring(0, encryptedPath.length() - 4) + ".dec";
        }
        return encryptedPath + ".dec";
    }
}
