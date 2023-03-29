package app.controller;

import app.AppPaths;
import app.security.CRLManager;
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
import java.security.GeneralSecurityException;
import java.util.List;

public class CertRetrievalViewController {

    @FXML
    private Button btnRetrieve;

    @FXML
    private PasswordField pwfPassword;

    @FXML
    private TextField txfUsername;

    @FXML
    void handleRetrieve(ActionEvent event) {
        String username = txfUsername.getText();
        String password = pwfPassword.getText();

        // check if the username and password are correct
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

        if (!isLoginValid) {
            // show an error message indicating that the username or password is incorrect
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Login");
            alert.setHeaderText(null);
            alert.setContentText("The username or password you entered is incorrect.");
            alert.showAndWait();

            // set welcome scene after unsuccessful certificate retrieval
            try {
                Util.setScene(AppPaths.WELCOME_VIEW, event, "Welcome to Secure Repository Program");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // retrieve the certificate
        try {
            CRLManager.removeRevocationFromCRL();
            // show message about successful certificate retrieval
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Certificate Retrieval Successful!");
            alert.setHeaderText(null);
            alert.setContentText("The certificate retrieved successfully.");
            alert.showAndWait();

            // set repository scene after successful certificate retrieval
            Util.setScene(AppPaths.REPOSITORY_VIEW, event, "Repository");

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            // show an error message indicating that the certificate retrieval failed
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Certificate Retrieval Failed");
            alert.setHeaderText(null);
            alert.setContentText("The certificate retrieval failed. Please try again or contact the administrator for assistance.");
            alert.showAndWait();
        }
    }

}
