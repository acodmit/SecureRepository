package app.security;

import app.service.UserContext;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;

import java.io.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class Encryption {

    public static byte[][] encryptParts(byte[][] inputBytes) {
        try {
            // get AES key from UserContext
            SecretKey aesKey = UserContext.getInstance().getAesKey();

            // create array to hold encrypted files
            byte[][] encryptedBytes = new byte[inputBytes.length][];

            // encrypt each file
            for (int i = 0; i < inputBytes.length; i++) {
                byte[] inputByteArray = inputBytes[i];
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                byte[] outputBytes = cipher.doFinal(inputByteArray);
                encryptedBytes[i] = outputBytes;
            }

            return encryptedBytes;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            // handle exception here
            e.printStackTrace();
            return null;
        }
    }

    public static byte[][] decryptParts(byte[][] encryptedParts) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException, BadPaddingException{
            // get AES key from UserContext
            SecretKey aesKey = UserContext.getInstance().getAesKey();

            // create array to hold decrypted parts
            byte[][] decryptedParts = new byte[encryptedParts.length][];

            // decrypt each part
            for (int i = 0; i < encryptedParts.length; i++) {
                byte[] encryptedPart = encryptedParts[i];
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, aesKey);
                byte[] decryptedPart = cipher.doFinal(encryptedPart);
                decryptedParts[i] = decryptedPart;
            }

            return decryptedParts;
    }

}
