package app.security;

import app.AppConstants;
import app.AppPaths;
import app.service.FileHandler;
import app.service.UserContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;

public class AESKey {
    public static void generateSalt( String username, PublicKey publicKey) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
        // generate a random salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[AppConstants.SALT_SIZE];
        random.nextBytes(salt);

        // encrypt the salt using RSA
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", new BouncyCastleProvider());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedSalt = cipher.doFinal(salt);

        // save the encrypted salt to a file
        try (FileOutputStream fos = new FileOutputStream(AppPaths.SALT_DIR
                + File.separator + username + ".salt")){
            fos.write(Hex.encode(encryptedSalt));
        }
    }

    public static byte[] getSalt(){
        try {
            // load the encrypted salt from the file
            String saltPath = AppPaths.SALT_DIR + File.separator + UserContext.getInstance().getUsername() + ".salt";
            byte[] encryptedSalt = Hex.decode(FileHandler.readFileToString(saltPath));

            // decrypt the salt using RSA
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, UserContext.getInstance().getPrivateKey());
            byte[] salt = cipher.doFinal(encryptedSalt);
            return salt;
        }catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | BadPaddingException | IllegalBlockSizeException | IOException e){
            e.printStackTrace();
            return null;
        }
    }

    // generate AES key from salt
    public static SecretKey generateAESKey(byte[] salt, String password) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, AppConstants.ITERATIONS, AppConstants.KEY_SIZE);
        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256", "BC");
            SecretKey key = factory.generateSecret(spec);
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AES key from salt.", e);
        }
    }
}
