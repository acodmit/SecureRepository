package app.security;

import app.AppConstants;
import app.AppPaths;
import app.service.CAContext;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CertManager {
    public static void generateSelfSignedCACert() throws Exception {
        // generate key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // generate certificate
        X500Name issuer = new X500Name("CN=My CA");
        X500Name subject = issuer;
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L);
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issuer, serial, notBefore, notAfter, subject, publicKeyInfo);
        builder.addExtension(org.bouncycastle.asn1.x509.Extension.basicConstraints, true, new BasicConstraints(true));
        builder.addExtension(org.bouncycastle.asn1.x509.Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign));
        builder.addExtension(org.bouncycastle.asn1.x509.Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
        builder.addExtension(org.bouncycastle.asn1.x509.Extension.subjectKeyIdentifier, false, new JcaX509ExtensionUtils().createSubjectKeyIdentifier(keyPair.getPublic()));
        builder.addExtension(Extension.authorityKeyIdentifier, false, new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(keyPair.getPublic()));
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(keyPair.getPrivate());
        X509CertificateHolder certificateHolder = builder.build(contentSigner);
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certificateHolder);

        // save private key and certificate to keystore file with password 'sigurnost'
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setKeyEntry("ca", keyPair.getPrivate(), "sigurnost".toCharArray(), new Certificate[]{certificate});
        try (OutputStream os = new FileOutputStream(AppPaths.CA_KEYSTORE)) {
            keyStore.store(os, "sigurnost".toCharArray());
        }
    }

    public static File generateUserCert(String username, String password) throws IOException, GeneralSecurityException, OperatorCreationException {
        X509Certificate caCert = CAContext.getInstance().getCertificate();
        PrivateKey caPrivateKey = CAContext.getInstance().getPrivateKey();

        // generate a RSA key pair for the user
        KeyPair keyPair = RSAKey.generateKeyPair();

        // create a certificate signing request (CSR) for the user
        X500Name subjectName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, username)
                .build();
        PKCS10CertificationRequest csr = new JcaPKCS10CertificationRequestBuilder(subjectName, keyPair.getPublic())
                .build(new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate()));

        // sign the CSR with the CA's private key to issue a user certificate
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                new X500Name(caCert.getIssuerDN().getName()),
                BigInteger.valueOf(System.currentTimeMillis()),
                new Time(new Date(System.currentTimeMillis())),
                new Time(new Date(System.currentTimeMillis() + AppConstants.USER_CERT_VALIDITY_PERIOD)),
                new X500Name(csr.getSubject().toString()),
                keyPair.getPublic()
        );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(caPrivateKey);
        X509CertificateHolder certHolder = certBuilder.build(signer);
        X509Certificate userCert = new JcaX509CertificateConverter().getCertificate(certHolder);

        // create a keystore object
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null); // initialize the keystore

        // set the key pair entry in the keystore
        KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(password.toCharArray());
        KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), new Certificate[]{userCert});
        keyStore.setEntry(username, privateKeyEntry, keyPassword);

        // save the keystore file to disk
        FileOutputStream fos = new FileOutputStream(AppPaths.KEYSTORE_DIR + File.separator + username + ".jks");
        keyStore.store(fos, password.toCharArray());
        fos.close();

        // generate encrypted salt for user
        AESKey.generateSalt( username, keyPair.getPublic());

        // export the user certificate to a temporary file
        File certificateFile = new File(username + ".pem");
        try (FileOutputStream out = new FileOutputStream(certificateFile)) {
            out.write(userCert.getEncoded());
        }
        return certificateFile;
    }

    public static X509Certificate getUserCertificate(File file) throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        FileInputStream inputStream = new FileInputStream(file);
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(inputStream);
        inputStream.close();
        return certificate;
    }

    public static boolean verifyCertificate(File file) {
        try {
            // load the selected file as a certificate
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            FileInputStream fis = new FileInputStream(file);
            X509Certificate userCert = (X509Certificate) cf.generateCertificate(fis);

            // load the CA certificate from the JKS file
            X509Certificate caCert = CAContext.getInstance().getCertificate();

            // verify the user certificate using the CA certificate
            userCert.verify(caCert.getPublicKey());
            // certificate is valid
            return true;
        } catch (IOException | GeneralSecurityException e) {
            // certificate is not valid
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkIfCertificateBelongsToUser(X509Certificate cert, String username) {
        // get the Subject DN from the certificate
        String certSubjectDN = cert.getSubjectDN().getName();

        // extract the username from the "CN=username" string
        //String certUsername = certSubjectDN.substring(3); // add 3 to skip past "CN="

        // parse the Subject DN to extract the username
        Pattern pattern = Pattern.compile("(?<=CN=).*$");
        Matcher matcher = pattern.matcher(certSubjectDN);
        String certUsername = null;
        if (matcher.find()) {
            certUsername = matcher.group(0);
        }

        // compare the extracted username with the specified username
        return username.equals(certUsername);
    }

}

