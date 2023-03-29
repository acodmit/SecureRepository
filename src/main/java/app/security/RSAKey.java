package app.security;

import app.AppPaths;
import app.service.UserContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class RSAKey {
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        // generate user's key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    public static PublicKey generatePublicKey(PrivateKey privateKey) {

        // generate the public key from the private key
        try {
            return KeyFactory.getInstance("RSA").generatePublic(
                    new RSAPublicKeySpec(((RSAPrivateCrtKey) privateKey).getModulus(), ((RSAPrivateCrtKey) privateKey).getPublicExponent()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PrivateKey getUserPrivateKey(String password) {
        try (InputStream in = Files.newInputStream(Paths.get(AppPaths.KEYSTORE_DIR
                + File.separator + UserContext.getInstance().getUsername() + ".jks"))) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(in, password.toCharArray());
            // store the private key in the key field
            PrivateKey key = (PrivateKey) keyStore.getKey(UserContext.getInstance().getUsername(), password.toCharArray());
            return key;
        } catch (IOException | GeneralSecurityException e) {
            // print an error message to standard error if loading the private key failed
            System.err.println("Failed to load private key from keystore: " + e.getMessage());
            return null;
        }
    }
}
