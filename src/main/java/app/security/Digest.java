package app.security;

import app.AppConstants;
import app.AppPaths;
import app.service.FileHandler;

import java.io.*;
import java.security.*;

import static app.util.Util.bytesToHex;

public class Digest {

    // generate hash of a string
    public static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(AppConstants.DGST_ALGORITHM);
            md.update(input.getBytes());
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // handle exception
            e.printStackTrace();
        }
        return null;
    }

    // digest a file and return digested bytes
    public static byte[] digest(File inputFile) {
        try {
            MessageDigest md = MessageDigest.getInstance(AppConstants.DGST_ALGORITHM);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            bis.close();
            return md.digest();
        } catch (IOException | NoSuchAlgorithmException e) {
            // handle exceptions
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] digest(byte[] inputBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance(AppConstants.DGST_ALGORITHM);
            md.update(inputBytes);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            // handle exceptions
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isFileCorrupted( byte[] bytes, String filename) throws IOException{
        // find the corresponding digest file for the input file
        String digestFileName = AppPaths.DGST_DIR + File.separator + filename + "." + AppConstants.DGST_ALGORITHM.toLowerCase();
        File digestedFile = new File(digestFileName);

        // load the existing digest file
        byte[] existingDigest = FileHandler.fileToBytes(digestedFile);

        // generate a new digest for the input file
        byte[] generatedDigest = digest(bytes);

        // compare the two digest files
        return !MessageDigest.isEqual(existingDigest, generatedDigest);
    }

    public static void saveUser(String username, String password) {
        try {
            // hash the password using SHA-256 algorithm
            String hashedPassword = hash(password);

            // append the username and hashed password to the users file
            BufferedWriter writer = new BufferedWriter(new FileWriter(AppPaths.USERS, true));
            writer.write(username + "," + hashedPassword + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
