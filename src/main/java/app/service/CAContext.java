package app.service;

import app.AppPaths;
import app.security.RSAKey;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.*;

public class CAContext {

    private static CAContext instance = null;
    private X509Certificate cert;
    private PrivateKey key;
    private X509CRL crl;
    public static final String CA_KEYSTORE_PASSWORD = "sigurnost";
    public static final String CA_PRIVATE_KEY_PASSWORD = "sigurnost";
    public static final String CA_ALIAS = "ca";

    private CAContext() {
        // private constructor to enforce singleton pattern
        loadCACertificate();
        loadCAPrivateKey();
    }


    public static CAContext getInstance() {
        if (instance == null) {
            instance = new CAContext();
        }
        return instance;
    }

    public X509Certificate getCertificate() {
        return cert;
    }
    public PrivateKey getPrivateKey() {
        return key;
    }
    public PublicKey getPublicKey() {
        return RSAKey.generatePublicKey(getPrivateKey());
    }

    public void setCRL( X509CRL crl){
        this.crl = crl;
    }
    public X509CRL getCRL(){ return crl;}

    private void loadCACertificate() {
        try (InputStream in = Files.newInputStream(Paths.get(AppPaths.CA_KEYSTORE))) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(in, CA_KEYSTORE_PASSWORD.toCharArray());
            // store the certificate in the cert field
            cert = (X509Certificate) keyStore.getCertificate(CA_ALIAS);
        } catch (IOException | GeneralSecurityException e) {
            // print an error message to standard error if loading the certificate failed
            System.err.println("Failed to load certificate from keystore: " + e.getMessage());
        }
    }

    private void loadCAPrivateKey() {
        try (InputStream in = Files.newInputStream(Paths.get(AppPaths.CA_KEYSTORE))) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(in, CA_KEYSTORE_PASSWORD.toCharArray());
            // store the private key in the key field
            key = (PrivateKey) keyStore.getKey(CA_ALIAS, CA_PRIVATE_KEY_PASSWORD.toCharArray());
        } catch (IOException | GeneralSecurityException e) {
            // print an error message to standard error if loading the private key failed
            System.err.println("Failed to load private key from keystore: " + e.getMessage());
        }
    }
    public void loadCRL() {
        try {
            // load the existing CRL file
            InputStream crlInputStream = new FileInputStream(AppPaths.CRL);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            crl = (X509CRL) cf.generateCRL(crlInputStream);
            crlInputStream.close();

            // verify the CRL's signature using the CA's public key
            //PublicKey caPublicKey = getPublicKey(); // Replace with your method to get the CA's public key
            //crl.verify(caPublicKey);

            // verify that the CRL is not expired
            //crl.checkValidity();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

