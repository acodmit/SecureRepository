package app.security;

import app.AppConstants;
import app.AppPaths;
import app.service.CAContext;
import app.service.UserContext;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v2CRLBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.cert.*;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class CRLManager {
    public static X509CRL generateEmptyCRL()
            throws IOException, GeneralSecurityException, OperatorCreationException {
        X509v2CRLBuilder crlGen = new JcaX509v2CRLBuilder(CAContext.getInstance().getCertificate().getSubjectX500Principal(),
                new Date());
        crlGen.setNextUpdate(new Date(AppConstants.WEEK));
        // add extensions to CRL
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        crlGen.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(CAContext.getInstance().getCertificate()));
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .setProvider("BC").build(CAContext.getInstance().getPrivateKey());
        JcaX509CRLConverter converter = new JcaX509CRLConverter().setProvider("BC");
        //System.out.println("Number of revoked certificates: " + converter.getCRL(crlGen.build(signer)).getRevokedCertificates().size());
        return converter.getCRL(crlGen.build(signer));
    }

    public static X509CRL generateCRL(Set<? extends X509CRLEntry> revokedCertificates)
            throws IOException, GeneralSecurityException, OperatorCreationException {
        X509v2CRLBuilder crlGen = new JcaX509v2CRLBuilder(
                CAContext.getInstance().getCertificate().getSubjectX500Principal(), new Date());
        crlGen.setNextUpdate(new Date(AppConstants.WEEK));
        // add extensions to CRL
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        crlGen.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(CAContext.getInstance().getCertificate()));

        for (X509CRLEntry revokedCertificate : revokedCertificates) {
            crlGen.addCRLEntry(revokedCertificate.getSerialNumber(), revokedCertificate.getRevocationDate(),
                    CRLReason.PRIVILEGE_WITHDRAWN);
        }

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .setProvider("BC").build(CAContext.getInstance().getPrivateKey());
        JcaX509CRLConverter converter = new JcaX509CRLConverter().setProvider("BC");
        return converter.getCRL(crlGen.build(signer));
    }

    public static void saveAndUpdateCRL(X509CRL crl) {
        try {
            // save the new CRL to file
            OutputStream crlOutputStream = new FileOutputStream(AppPaths.CRL);
            crlOutputStream.write(crl.getEncoded());
            crlOutputStream.close();
        } catch (IOException | CRLException e) {
            e.printStackTrace();
        }
        CAContext.getInstance().setCRL(crl);
    }

    public static void addRevocationToCRL(X509Certificate certToRevoke)
            throws IOException, GeneralSecurityException, OperatorCreationException
    {
        X509v2CRLBuilder crlGen = new JcaX509v2CRLBuilder(CAContext.getInstance().getCRL());
        crlGen.setNextUpdate(new Date(AppConstants.WEEK));

        // add revocation
        ExtensionsGenerator extGen = new ExtensionsGenerator();
        CRLReason crlReason = CRLReason.lookup(CRLReason.unspecified);
        extGen.addExtension(Extension.reasonCode, false, crlReason);
        crlGen.addCRLEntry(certToRevoke.getSerialNumber(),
                new Date(), extGen.generate());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .setProvider("BC").build(CAContext.getInstance().getPrivateKey());
        JcaX509CRLConverter converter = new JcaX509CRLConverter().setProvider("BC");
        saveAndUpdateCRL(converter.getCRL(crlGen.build(signer)));
    }

    public static void removeRevocationFromCRL() throws IOException, CertificateException, CRLException {

        // get the set of revoked certificates from the CRL
        Set<? extends X509CRLEntry> revokedCertificates = CAContext.getInstance().getCRL().getRevokedCertificates();

        // create a new mutable set and add all the elements from the unmodifiable set
        Set<X509CRLEntry> mutableRevokedCertificates = new HashSet<>(revokedCertificates);

        // iterate over the set of revoked certificates to find the entry for the current user's certificate
        X509CRLEntry entryToRemove = null;
        for (X509CRLEntry entry : mutableRevokedCertificates) {
            if (entry.getSerialNumber().equals(UserContext.getInstance().getCertificate().getSerialNumber())) {
                entryToRemove = entry;
                break;
            }
        }

        // remove the entry for the current user's certificate from the set of revoked certificates
        mutableRevokedCertificates.remove(entryToRemove);

        // create a new unmodifiable set from the mutable set
        Set<? extends X509CRLEntry> updatedRevokedCertificates = Collections.unmodifiableSet(mutableRevokedCertificates);

        try {
            saveAndUpdateCRL(generateCRL(updatedRevokedCertificates));
        } catch (OperatorCreationException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public static boolean isCertificateRevoked(X509Certificate certificate) {
        // get the set of revoked certificates from the CRL
        Set<? extends X509CRLEntry> revokedCertificates = CAContext.getInstance().getCRL().getRevokedCertificates();

        // check if the certificate is revoked by serial number
        BigInteger serialNumber = certificate.getSerialNumber();
        if(revokedCertificates != null){
            for (X509CRLEntry entry : revokedCertificates) {
                if (entry.getSerialNumber().equals(serialNumber)) {
                    return true;
                }
            }
        }
        return false;
    }
}
