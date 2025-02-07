
// import java.util.*;
// public class Main {
//     public static void main(String[] args) {
//         ArrayList<String> functions = new ArrayList<>();
//         functions.add("Shrink a File");
//         functions.add("Shrink a Folder");
//         functions.add("Expand a File");
//         functions.add("Expand a Folder");
//         functions.add("Secure Compression");
//         functions.add("Integrity Guard");
//         functions.add("Exit ByteCypher");
//         try (Scanner sc = new Scanner(System.in)) {
//             System.out.println("""
//                                            \r
//                                            \u001b[32m  ____        _        _____            _               \r
//                                             |  _ \\      | |      / ____|          | |              \r
//                                             | |_) |_   _| |_ ___| |    _   _ _ __ | |__   ___ _ __ \r
//                                             |  _ <| | | | __/ _ \\ |   | | | | '_ \\| '_ \\ / _ \\ '__|\r
//                                             | |_) | |_| | ||  __/ |___| |_| | |_) | | | |  __/ |   \r
//                                             |____/ \\__, |\\__\\___|\\_____\\__, | .__/|_| |_|\\___|_|   \r
//                                                     __/ |               __/ | |                    \r
//                                                    |___/               |___/|_|                    \r
//                                            \u001b[0m""" //
//             //
//             //
//             //
//             //
//             //
//             //
//             //
//             //
//             );
//             System.out.println();
//             System.out.println("\n\033[36mWelcome to ByteCypher - Your Secure File Utility Tool\033[0m");
//             System.out.println("-----------------------------------------------------------");
//             System.out.println("This tool helps you compress and decompress files with encryption options.");
//             System.out.println();
//             System.out.println("\n\033[33mPlease choose an option below:\033[0m");
//             for (int i = 0; i < functions.size(); i++) {
//                 System.out.println("\033[33m" + (i + 1) + ". " + functions.get(i) + "\033[0m");
//             }
//         }
//     }
// }
//Previous Edition


//Latest Edition

package bytecypher;
import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            ArrayList<String> functions = new ArrayList<>();
            functions.add(" Shrink a File");
            functions.add(" Shrink a Folder");
            functions.add(" Expand a File");
            functions.add(" Expand a Folder");
            functions.add(" Secure Compression");
            functions.add(" Integrity Guard");
            functions.add(" Exit ByteCypher");

            while (true) {
                clearScreen(); 
                displayBanner();

                System.out.println("\n\033[36mWelcome to ByteCypher - Your Secure File Utility Tool\033[0m");
                System.out.println("-----------------------------------------------------------");
                System.out.println("This tool allows you to compress and decompress files with additional features like encryption and integrity checks.");
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

                switch(choice)
                {
                    case 1 -> {
                        bytecypher.Compressor.compress();
                        pause(2);
                    }
                    case 2 -> {
                        bytecypher.Compressor.compress();
                        pause(2);
                    }
                    case 3 -> {
                        bytecypher.Decompressor.decompress();
                        pause(2);
                    }
                    case 4 -> {
                        bytecypher.Decompressor.decompress();
                        pause(2);
                    }
                    case 5 -> {
                        bytecypher.Encryption.encrypt();
                        pause(2);
                    }
                    case 6 -> {
                        bytecypher.FileIntegrity.integrity();
                        pause(2);
                    }
                    case 7 -> {
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
    private static void pause(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
