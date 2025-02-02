
import java.util.*;

public class Main {

    public static void main(String[] args) {

        ArrayList<String> functions = new ArrayList<>();
        functions.add("Shrink a File");
        functions.add("Shrink a Folder");
        functions.add("Expand a File");
        functions.add("Expand a Folder");
        functions.add("Secure Compression");
        functions.add("Integrity Guard");
        functions.add("Exit ByteCypher");

        try (Scanner sc = new Scanner(System.in)) {
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
                                           \u001b[0m""" //
            //
            //
            //
            //
            //
            //
            //
            //
            );
            System.out.println();
            System.out.println("\n\033[36mWelcome to ByteCypher - Your Secure File Utility Tool\033[0m");
            System.out.println("-----------------------------------------------------------");
            System.out.println("This tool helps you compress and decompress files with encryption options.");
            System.out.println();
            System.out.println("\n\033[33mPlease choose an option below:\033[0m");

            for (int i = 0; i < functions.size(); i++) {
                System.out.println("\033[33m" + (i + 1) + ". " + functions.get(i) + "\033[0m");
            }
            
        }
    }

}
