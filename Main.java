import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DES des = new DES();
    private static final FileProcessor fileProcessor = new FileProcessor();

    public static void main(String[] args) {
        while (true) {
            showMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": keyExpansion(); break;
                case "2": encryptText(); break;
                case "3": decryptText(); break;
                case "4": encryptFile(); break;
                case "5": decryptFile(); break;
                case "6": sendEmail(); break;
                case "7": sendSMS(); break;
                case "8": generateRandomKey(); break;
                case "9":
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n===== Enhanced DES Secure Communication System =====");
        System.out.println("1. Key Expansion");
        System.out.println("2. Encrypt Text");
        System.out.println("3. Decrypt Text");
        System.out.println("4. Encrypt File");
        System.out.println("5. Decrypt File");
        System.out.println("6. Send Ciphertext via Email");
        System.out.println("7. Send DES Key via SMS");
        System.out.println("8. Generate Random DES Key");
        System.out.println("9. Exit");
        System.out.print("Enter your choice: ");
    }

    private static int[] getKeyFromUser() {
        System.out.println("--- Key Input ---");
        System.out.println("1. Enter key as hex (16 hex chars)");
        System.out.println("2. Enter key as text (up to 8 chars)");
        System.out.println("3. Read key from file (hex)");
        System.out.println("4. Read key from file (text)");
        System.out.print("Choice: ");
        String opt = scanner.nextLine().trim();
        while (true) {
            try {
                switch (opt) {
                    case "1":
                        System.out.print("Enter key (hex, 16 hex chars): ");
                        String hex = scanner.nextLine().trim();
                        return validateHexKey(hex);
                    case "2":
                        System.out.print("Enter key (text, up to 8 chars): ");
                        String text = scanner.nextLine();
                        return DataUtils.stringToKeyBits(text);
                    case "3":
                        System.out.print("Enter file path: ");
                        String path = scanner.nextLine().trim();
                        String content = FileProcessor.readString(path);
                        return validateHexKey(content);
                    case "4":
                        System.out.print("Enter file path: ");
                        String fpath = scanner.nextLine().trim();
                        String fcontent = FileProcessor.readString(fpath);
                        return DataUtils.stringToKeyBits(fcontent);
                    default:
                        System.out.print("Invalid. Try again: ");
                        opt = scanner.nextLine().trim();
                }
            } catch (Exception e) {
                System.out.print("Error: " + e.getMessage() + ". Try again: ");
                opt = scanner.nextLine().trim();
            }
        }
    }

    private static int[] validateHexKey(String hex) {
        hex = hex.replaceAll("\\s+", "");
        if (hex.length() != 16) {
            throw new IllegalArgumentException("Hex key must be exactly 16 hex characters (64 bits)");
        }
        int[] key = DataUtils.hexToKeyBits(hex);
        if (key.length != 64) {
            throw new IllegalArgumentException("Invalid key length");
        }
        return key;
    }

    private static int[] getDataFromUser(boolean expectHex) {
        System.out.println("--- Data Input ---");
        System.out.println("1. Enter from console");
        System.out.println("2. Read from file");
        System.out.print("Choice: ");
        String opt = scanner.nextLine().trim();
        try {
            switch (opt) {
                case "1":
                    if (expectHex) {
                        System.out.print("Enter hex data: ");
                        String hex = scanner.nextLine().trim();
                        return DataUtils.hexToBits(hex);
                    } else {
                        System.out.print("Enter text: ");
                        String text = scanner.nextLine();
                        return DataUtils.stringToBits(text);
                    }
                case "2":
                    System.out.print("Enter file path: ");
                    String path = scanner.nextLine().trim();
                    byte[] fileData = FileProcessor.readAllBytes(path);
                    if (fileData.length % 8 != 0)
                        return DataUtils.bytesToBits(fileData);
                default:
                    System.out.println("Invalid option.");
                    return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    private static void keyExpansion() {
        System.out.println("\n--- Key Expansion ---");
        int[] key = getKeyFromUser();
        if (key == null) return;

        System.out.println("\nOriginal Key (64 bits):");
        System.out.println("  Binary: " + DataUtils.bitsToBinaryString(key));
        System.out.println("  Hex: " + DataUtils.bitsToHex(key));

        int[] pc1Result = des.getPC1Result(key);
        System.out.println("\nAfter PC-1 (56 bits):");
        System.out.println("  Binary: " + DataUtils.bitsToBinaryString(pc1Result));
        System.out.println("  Hex: " + DataUtils.bitsToHex(pc1Result));

        int[] c = des.splitLeft(pc1Result, 28);
        int[] d = des.splitRight(pc1Result, 28);
        System.out.println("\nC0 (28 bits): " + DataUtils.bitsToBinaryString(c));
        System.out.println("D0 (28 bits): " + DataUtils.bitsToBinaryString(d));

        int[][] roundKeys = des.generateRoundKeys(key);
        System.out.println("\n--- 16 Round Subkeys ---");
        for (int r = 0; r < 16; r++) {
            c = des.circularShift(c, DES.getSHIFTS()[r]);
            d = des.circularShift(d, DES.getSHIFTS()[r]);
            System.out.println("\nRound " + (r + 1) + ":");
            System.out.println("  C" + (r + 1) + " (28 bits): " + DataUtils.bitsToBinaryString(c));
            System.out.println("  D" + (r + 1) + " (28 bits): " + DataUtils.bitsToBinaryString(d));
            System.out.println("  K" + (r + 1) + " (48 bits): " + DataUtils.bitsToBinaryString(roundKeys[r]));
            System.out.println("  K" + (r + 1) + " (hex): " + DataUtils.bitsToHex(roundKeys[r]));
        }
    }

    private static void encryptText() {
        System.out.println("\n--- Encrypt Text ---");
        int[] key = getKeyFromUser();
        if (key == null) return;

        System.out.println("Input format:");
        System.out.println("1. Plain text");
        System.out.println("2. Hexadecimal");
        System.out.print("Choice: ");
        String fmt = scanner.nextLine().trim();

        int[] dataBits = getDataFromUser("2".equals(fmt));
        if (dataBits == null || dataBits.length == 0) return;

        int[] padded = DataUtils.padBytes(DataUtils.bitsToBytes(dataBits));
        int numBlocks = padded.length / 64;
        byte[] cipherBytes = new byte[numBlocks * 8];

        for (int i = 0; i < numBlocks; i++) {
            int[] block = new int[64];
            DES.copy(padded, i * 64, block, 0, 64);
            int[] encBlock = des.encrypt(block, key);
            byte[] encBytes = DataUtils.bitsToBytes(encBlock);
            for (int j = 0; j < 8; j++) cipherBytes[i * 8 + j] = encBytes[j];
        }

        System.out.println("\nCiphertext (hex): " + DataUtils.bytesToHex(cipherBytes));
    }

    private static void decryptText() {
        System.out.println("\n--- Decrypt Text ---");
        int[] key = getKeyFromUser();
        if (key == null) return;

        System.out.println("Input format:");
        System.out.println("1. Hexadecimal");
        System.out.println("2. From file");
        System.out.print("Choice: ");
        String fmt = scanner.nextLine().trim();

        if ("2".equals(fmt)) {
            System.out.print("Enter file path: ");
            try {
                String path = scanner.nextLine().trim();
                byte[] fileData = FileProcessor.readAllBytes(path);
                if (fileData.length % 8 != 0) {
                    System.out.println("Error: Invalid ciphertext length.");
                    return;
                }
                int numBlocks = fileData.length / 8;
                byte[] decrypted = new byte[fileData.length];
                for (int i = 0; i < numBlocks; i++) {
                    byte[] block = new byte[8];
                    for (int j = 0; j < 8; j++) block[j] = fileData[i * 8 + j];
                    int[] decBlock = des.decrypt(DataUtils.bytesToBits(block), key);
                    byte[] decBytes = DataUtils.bitsToBytes(decBlock);
                    for (int j = 0; j < 8; j++) decrypted[i * 8 + j] = decBytes[j];
                }
                byte[] unpadded = DataUtils.unpadBytes(decrypted);
                System.out.println("\nDecrypted text: " + new String(unpadded));
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
            return;
        }

        System.out.print("Enter ciphertext (hex): ");
        String hexCipher = scanner.nextLine().trim().replaceAll("\\s+", "");
        try {
            byte[] cipherBytes = DataUtils.hexToBytes(hexCipher);
            if (cipherBytes.length % 8 != 0) {
                System.out.println("Error: Invalid ciphertext length.");
                return;
            }
            int numBlocks = cipherBytes.length / 8;
            byte[] decrypted = new byte[cipherBytes.length];
            for (int i = 0; i < numBlocks; i++) {
                byte[] block = new byte[8];
                for (int j = 0; j < 8; j++) block[j] = cipherBytes[i * 8 + j];
                int[] decBlock = des.decrypt(DataUtils.bytesToBits(block), key);
                byte[] decBytes = DataUtils.bitsToBytes(decBlock);
                for (int j = 0; j < 8; j++) decrypted[i * 8 + j] = decBytes[j];
            }
            byte[] unpadded = DataUtils.unpadBytes(decrypted);
            System.out.println("\nDecrypted text: " + new String(unpadded));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void encryptFile() {
        System.out.println("\n--- Encrypt File ---");
        int[] key = getKeyFromUser();
        if (key == null) return;

        System.out.print("Enter input file path: ");
        String inputPath = scanner.nextLine().trim();
        String outputPath = fileProcessor.getEncryptedOutputPath(inputPath);

        try {
            fileProcessor.encryptFile(inputPath, outputPath, key);
            System.out.println("File encrypted successfully.");
            System.out.println("Output: " + outputPath);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void decryptFile() {
        System.out.println("\n--- Decrypt File ---");
        int[] key = getKeyFromUser();
        if (key == null) return;

        System.out.print("Enter encrypted file path: ");
        String inputPath = scanner.nextLine().trim();
        String outputPath = fileProcessor.getDecryptedOutputPath(inputPath);

        try {
            fileProcessor.decryptFile(inputPath, outputPath, key);
            System.out.println("File decrypted successfully.");
            System.out.println("Output: " + outputPath);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void sendEmail() {
        System.out.println("\n--- Send Ciphertext via Email ---");
        System.out.print("Recipient email: ");
        String recipient = scanner.nextLine().trim();

        System.out.println("Send:");
        System.out.println("1. Ciphertext from console");
        System.out.println("2. Encrypted file");
        System.out.print("Choice: ");
        String opt = scanner.nextLine().trim();

        try {
            String body;
            String subject = "Encrypted DES Ciphertext";
            if ("2".equals(opt)) {
                System.out.print("Enter file path: ");
                String path = scanner.nextLine().trim();
                byte[] data = FileProcessor.readAllBytes(path);
                body = "Encrypted file contents (hex):\n" + DataUtils.bytesToHex(data);
            } else {
                System.out.print("Enter ciphertext (hex): ");
                body = "Ciphertext:\n" + scanner.nextLine().trim();
            }
            EmailService.sendEmail(recipient, subject, body);
            System.out.println("Email sent successfully.");
        } catch (Exception e) {
            System.out.println("Email sending failed: " + e.getMessage());
        }
    }

    private static void sendSMS() {
        System.out.println("\n--- Send DES Key via SMS ---");
        int[] key;
        System.out.println("Key source:");
        System.out.println("1. Use existing key");
        System.out.println("2. Generate new key");
        System.out.print("Choice: ");
        String opt = scanner.nextLine().trim();
        if ("2".equals(opt)) {
            key = DataUtils.generateRandomKey();
            System.out.println("Generated key (hex): " + DataUtils.bitsToHex(key));
        } else {
            key = getKeyFromUser();
            if (key == null) return;
        }

        System.out.print("Phone number (with country code, e.g. +1234567890): ");
        String phone = scanner.nextLine().trim();

        try {
            String keyHex = DataUtils.bitsToHex(key);
            String keyBin = DataUtils.bitsToBinaryString(key);
            String content = "(h): " + keyHex + " | (b): " + keyBin;
            FileProcessor.writeString("des_key.txt", content);
            System.out.println("Key saved to des_key.txt");

            String sid = SMSService.sendSMS(phone, "Your DES Secret Key: " + keyHex);
            System.out.println("SMS sent successfully! SID: " + sid);
        } catch (Exception e) {
            System.out.println("SMS sending failed: " + e.getMessage());
        }
    }

    private static void generateRandomKey() {
        System.out.println("\n--- Generate Random DES Key ---");
        int[] key = DataUtils.generateRandomKey();
        System.out.println("Generated DES Key:");
        System.out.println("  Hex: " + DataUtils.bitsToHex(key));
        System.out.println("  Binary: " + DataUtils.bitsToBinaryString(key));

        System.out.print("Use this key for encryption? (y/n): ");
        String ans = scanner.nextLine().trim().toLowerCase();
        if (ans.equals("y")) {
            System.out.println("--- Encrypt Text with Generated Key ---");
            System.out.println("Input format:");
            System.out.println("1. Plain text");
            System.out.println("2. Hexadecimal");
            System.out.print("Choice: ");
            String fmt = scanner.nextLine().trim();

            int[] dataBits = getDataFromUser("2".equals(fmt));
            if (dataBits == null || dataBits.length == 0) return;

            int[] padded = DataUtils.padBytes(DataUtils.bitsToBytes(dataBits));
            int numBlocks = padded.length / 64;
            byte[] cipherBytes = new byte[numBlocks * 8];

            for (int i = 0; i < numBlocks; i++) {
                int[] block = new int[64];
                DES.copy(padded, i * 64, block, 0, 64);
                int[] encBlock = des.encrypt(block, key);
                byte[] encBytes = DataUtils.bitsToBytes(encBlock);
                for (int j = 0; j < 8; j++) cipherBytes[i * 8 + j] = encBytes[j];
            }

            System.out.println("\nCiphertext (hex): " + DataUtils.bytesToHex(cipherBytes));
        }
    }
}
