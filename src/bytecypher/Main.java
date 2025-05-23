package bytecypher;

import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            ArrayList<String> functions = new ArrayList<>();
            functions.add(" Shrink a File");
            functions.add(" Shrink a Folder");
            functions.add(" Expand a File/Folder");
            functions.add(" Secure Encryption");
            functions.add(" Secure Decryption");
            functions.add(" Integrity Guard");
            functions.add(" About ByteCypher");
            functions.add(" Exit ByteCypher");

            while (true) {
                clearScreen();
                displayBanner();

                System.out.println("\n\033[36mWelcome to ByteCypher - Your Secure File Utility Tool\033[0m");
                System.out.println("-----------------------------------------------------------");
                System.out.println("This tool allows you to compress, decompress, encrypt files with additional features like integrity checks.");
                System.out.println();
                System.out.println("\n\033[33mPlease choose an option below:\033[0m");

                for (int i = 0; i < functions.size(); i++) {
                    System.out.println("\033[33m" + (i + 1) + ". " + functions.get(i) + "\033[0m");
                }

                System.out.print("\n\033[32mEnter your choice: \033[0m");

                if (!scanner.hasNextInt()) {
                    System.out.println("\n\033[31mInvalid input! Please enter a number.\033[0m");
                    scanner.next();
                    pause(2);
                    continue;
                }

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> {
                        bytecypher.Compressor.compress(true);
                        pause(5);
                    }
                    case 2 -> {
                        bytecypher.Compressor.compress(false);
                        pause(5);
                    }
                    case 3 -> {
                        bytecypher.Decompressor.decompress();
                        pause(5);
                    }
                    case 4 -> {
                        bytecypher.Encryption.encrypt();
                        pause(5);
                    }
                    case 5 -> {
                        bytecypher.Encryption.decrypt();
                        pause(5);
                    }
                    case 6 -> {
                        bytecypher.FileIntegrity.integrity();
                        pause(5);
                    }
                    case 7 -> {
                        showAbout();
                        pause(8);
                    }
                    case 8 -> {
                        System.out.println("\n\033[31mExiting ByteCypher... Thank you for using our tool!\033[0m");
                        return;
                    }
                    default -> {
                        System.out.println("\n\033[31mInvalid choice! Please try again.\033[0m");
                        pause(2);
                    }
                }
            }
        }
    }

    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error clearing the screen.");
        }
    }

    private static void displayBanner() {
        System.out.println("""
                                            \r
                                            \u001b[32m  ____        _        _____            _               \r
                                             |  _ \\      | |      / ____|          | |              \r
                                             | |_) |_   _| |_ ___| |    _   _ _ __ | |__   ___ _ __ \r
                                             |  _ <| | | | __/ _ \\ |   | | | | '_ \\| '_ \\ / _ \\ '__|\r
                                             | |_) | |_| | ||  __/ |___| |_| | |_) | | | |  __/ |   \r
                                             |____/ \\__, |\\__\\___|\\_____\\__, | .__/|_| |_|\\___|_|   \r
                                                     __/ |               __/ | |                    \r
                                                    |___/               |___/|_|                    \r
                                            \u001b[0m"""
        );
    }

    private static void showAbout() {
        clearScreen();
        displayBanner();

        System.out.println("\n\033[36m  ByteCypher: Advanced File Compression & Security Suite\033[0m");
        System.out.println("\n  \033[33mVersion:\033[0m 2.0");
        System.out.println("  \033[33mRelease Date:\033[0m April 2025");
        System.out.println("\n  \033[33mKey Features:\033[0m");
        System.out.println("  • Smart hybrid compression (Huffman, LZ77)");
        System.out.println("  • Support for all file types");
        System.out.println("  • Folder structure preservation");
        System.out.println("  • Military-grade AES-256 encryption");
        System.out.println("  • File integrity verification (MD5, SHA-256)");
        System.out.println("  • User-friendly graphical interface");

        System.out.println("\n  \033[33mSystem Requirements:\033[0m");
        System.out.println("  • Java Runtime Environment 11 or higher");
        System.out.println("  • 512MB RAM minimum");
        System.out.println("  • Compatible with Windows, macOS, and Linux");

        System.out.println("\n  \033[33mDevelopment:\033[0m");
        System.out.println("  • Created with ❤️ by ByteCypher Dev Team");

        System.out.println("\n\033[36m  Press any key to return to main menu...\033[0m");
    }

    private static void pause(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
