package app.service;

import app.AppPaths;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileHandler {

    public static byte[][] divideBytesIntoParts(byte[] bytes) {
        // calculate the size of each part
        int numParts = 5;
        int partSize = bytes.length / numParts;
        int lastPartSize = bytes.length - (partSize * (numParts - 1));

        // create the array to hold the parts
        byte[][] parts = new byte[numParts][];
        int offset = 0;

        // divide the byte array into parts
        for (int i = 0; i < numParts - 1; i++) {
            parts[i] = Arrays.copyOfRange(bytes, offset, offset + partSize);
            offset += partSize;
        }
        parts[numParts - 1] = Arrays.copyOfRange(bytes, offset, offset + lastPartSize);

        return parts;
    }

    public static String readFileToString(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static byte[] fileToBytes(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        fis.close();
        return bos.toByteArray();
    }

    public static void bytesToFile(byte[] bytes, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(bytes);
        }
    }

    public static byte[] joinByteArrays(byte[][] byteArrays) throws NullPointerException{
        int totalLength = 0;
        for (byte[] array : byteArrays) {
            totalLength += array.length;
        }
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] array : byteArrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }
        return result;
    }

    public static void openByteArray(byte[] bytes) throws IOException {
        // create a temporary file
        File tempFile = File.createTempFile("temp", null);
        tempFile.deleteOnExit();

        // write the byte array to the temporary file
        try (OutputStream os = new FileOutputStream(tempFile)) {
            os.write(bytes);
        }

        // open the temporary file
        Desktop.getDesktop().open(tempFile);
    }

    public static boolean deleteFile(String filename) {
        // success flag
        boolean success = true;

        // delete 5 files from repository directories
        String filePath = "";
        for (int i = 1; i <= 5; i++) {
            filePath = AppPaths.PART_DIR + i + File.separator
                    + UserContext.getInstance().getUsername() + "_" + filename + ".part" + i;
            File file = new File(filePath);
            if (file.exists()) {
                if (file.delete()) {
                    // file deleted successfully
                } else {
                    // failed to delete the file
                    success = false;
                }
            } else {
                System.out.println("The file part " + i + " does not exist.");
                success = false;
            }
        }
        return success;
    }
}
