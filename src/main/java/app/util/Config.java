package app.util;

import app.AppPaths;
import app.security.CRLManager;
import app.security.CertManager;
import app.service.CAContext;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;

public class Config {

    public static void configure() {
        // add bouncy castle provider to security configuration
        Security.addProvider(new BouncyCastleProvider());

        boolean isNew = false; // flag to indicate if any new directories or files were created

        // check if required directories and files exist, create them if they don't
        File cnfDir = new File(AppPaths.CONFIGURATION_DIR);
        if (!cnfDir.exists()) {
            cnfDir.mkdirs();
            isNew = true;
        }

        File crlDir = new File(AppPaths.CRL_DIR);
        if (!crlDir.exists()) {
            crlDir.mkdirs();
            isNew = true;
        }

        File keystoreDir = new File(AppPaths.KEYSTORE_DIR);
        if (!keystoreDir.exists()) {
            keystoreDir.mkdirs();
            isNew = true;
        }

        File caDir = new File(AppPaths.CA_DIR);
        if (!caDir.exists()) {
            caDir.mkdirs();
            isNew = true;
        }

        File saltDir = new File(AppPaths.SALT_DIR);
        if (!saltDir.exists()) {
            saltDir.mkdirs();
            isNew = true;
        }

        File dgstDir = new File(AppPaths.DGST_DIR);
        if (!dgstDir.exists()) {
            dgstDir.mkdirs();
            isNew = true;
        }

        for (int i = 1; i <= 5; i++) {
            String folderName = "part_" + i;
            File folder = new File(AppPaths.REPOSITORY_DIR + File.separator + folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }
        }

        File usersFile = new File(AppPaths.USERS);
        if (!usersFile.exists()) {
            try {
                usersFile.createNewFile();
                isNew = true;
            } catch (IOException e) {
                System.err.println("Failed to create users file: " + e.getMessage());
                return;
            }
        }

        // generate CA certificate if it doesn't already exist
        File caJKSFile = new File(AppPaths.CA_KEYSTORE);
        if (!caJKSFile.exists()) {
            try {
                CertManager.generateSelfSignedCACert();
                isNew = true;
            } catch (Exception e) {
                System.err.println("Failed to generate CA certificate: " + e.getMessage());
                return;
            }
        }

        File crlListFile = new File(AppPaths.CRL);
        if (!crlListFile.exists()) {
            try {
                CRLManager.saveAndUpdateCRL(CRLManager.generateEmptyCRL());
                isNew = true;
            } catch (IOException | GeneralSecurityException | OperatorCreationException e) {
                System.err.println("Failed to create CRL list file: " + e.getMessage());
                return;
            }
        }
        CAContext.getInstance().loadCRL();

        // show success message if any new directories or files were created
        if (isNew) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Configuration Successful");
                alert.setHeaderText("Configuration is successful.");
                alert.showAndWait();
            });
        }
    }

}
