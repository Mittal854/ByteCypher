package bytecypher;

import java.io.File;
import java.util.Scanner;

public class Compressor {
    public static void compress(){
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Select your file/folder : ");
            String inputPath=sc.nextLine();
            File file=new File(inputPath);
            if (file.exists()) {
                System.out.println("✅ File exists!");
            } else {
                System.out.println("❌ File does NOT exist. Check the path.");
            }
        }

    }
}
