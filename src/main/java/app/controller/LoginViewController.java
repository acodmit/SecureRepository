package app.controller;

import app.AppConstants;
import app.AppPaths;
import app.security.CRLManager;
import app.security.CertManager;
import app.service.UserContext;
import app.util.Util;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class LoginViewController {

    @FXML
    private Button btnLogin;
    @FXML
    private PasswordField pwfPassword;
    @FXML
    private TextField txfUsername;
    private int loginAttempts = 0;
    private boolean isCertificateSuspended = false;

    @FXML
    void handleLogin(ActionEvent event) {
        String username = txfUsername.getText();
        String password = pwfPassword.getText();

        // read the users file to check if the username and password are correct
        boolean isLoginValid = false;
        try {
            List<String> users = Files.readAllLines(Paths.get(AppPaths.USERS));
            for (String user : users) {
                String[] parts = user.split(",");
                if (parts[0].equals(username)) {
                    if (parts[1].equals(app.security.Digest.hash(password))) {
                        isLoginValid = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isLoginValid) {

            // checks if certificate belongs to user
            if (CertManager.checkIfCertificateBelongsToUser(UserContext.getInstance().getCertificate(), username)) {
                // set currently logged user
                UserContext.getInstance().setUsername(username);

                // load rsa private key for currently logged user
                UserContext.getInstance().setPrivateKey(password);

                // load rsa public key for currently logged user
                UserContext.getInstance().setPublicKey();

                // generate user aes key using encrypted salt and password
                UserContext.getInstance().setAesKey(password);
                try {
                    // open the repository FXML page
                    Util.setScene(AppPaths.REPOSITORY_VIEW, event, "Repository");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // show a message indicating that the certificate does not belong to the user trying to access
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Certificate");
                alert.setHeaderText(null);
                alert.setContentText("The certificate does not belong to the user you are trying to access.");
                alert.showAndWait();
            }
        } else {
            // increment the login attempts counter and check if the certificate should be suspended
            loginAttempts++;
            if (loginAttempts == AppConstants.MAX_LOGIN_ATTEMPTS) {
                try {
                    CRLManager.addRevocationToCRL(UserContext.getInstance().getCertificate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isCertificateSuspended = true;
            }

            if (isCertificateSuspended) {
                // open the FXML page for certificate retrieval
                try {
                    Util.setScene(AppPaths.CERT_RETRIEVAL_VIEW, event, "Certificate Retrieval");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // show a message indicating that the login failed
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Login");
                alert.setHeaderText(null);
                alert.setContentText("The username or password you entered is incorrect.");
                alert.showAndWait();
            }
        }
    }

}
