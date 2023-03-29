package app.service;

import app.security.AESKey;
import app.security.RSAKey;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class UserContext {
    private static UserContext instance = null;
    private String username;
    private X509Certificate certificate;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private SecretKey aesKey;
    private UserContext() {
        // private constructor to prevent instantiation from outside the class
    }

    public static UserContext getInstance() {
        if (instance == null) {
            instance = new UserContext();
        }
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey( String password) {
        this.privateKey = RSAKey.getUserPrivateKey(password);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey() {
        this.publicKey = RSAKey.generatePublicKey(UserContext.getInstance().getPrivateKey());
    }

    public SecretKey getAesKey() {
        return aesKey;
    }

    public void setAesKey(String password) {
        this.aesKey = AESKey.generateAESKey(AESKey.getSalt(), password);
    }

    public void clear() {
        username = null;
        certificate = null;
        privateKey = null;
        publicKey = null;
    }
}
