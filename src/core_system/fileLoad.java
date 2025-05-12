package core_system;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class fileLoad {

    private static final String randomDataTextFilePAth= "/home/vazek/Documents/internship document/random_data_text_file.txt";
    private static final String randomDataTextFilePath100MB="/home/vazek/Documents/internship document/random_text_100MB.txt"; //for test
    private String contenu;

    public fileLoad() {}
    public static long countLines(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            long lines = 0;
            while (reader.readLine() != null) lines++;
            return lines;
        }
    }

    public static void main(String[] args) throws IOException {
        long lines = countLines(randomDataTextFilePath100MB);
        System.out.println(lines);
    }

}
