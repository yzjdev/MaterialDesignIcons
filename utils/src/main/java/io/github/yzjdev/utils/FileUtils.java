package io.github.yzjdev.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class FileUtils {

    public static String readString(String path) throws IOException {
        return readString(new File(path));
    }

    public static String readString(File file) throws IOException {
        return readString(new FileInputStream(file));
    }

    public static String readString(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            var len = 0;
            char[] cbuf = new char[1024 * 8];
            while ((len = reader.read(cbuf)) != -1) {
                sb.append(cbuf, 0, len);
            }
        }
        return sb.toString();
    }

    public static void writeString(String path, String content) throws IOException {
        writeString(new File(path), content);
    }

    public static void writeString(File path, String content) throws IOException {
        writeString(new FileOutputStream(path), content);
    }

    public static void writeString(OutputStream out, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
            writer.write(content);
        }
    }
}
