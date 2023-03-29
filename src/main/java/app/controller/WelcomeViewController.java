package app.controller;

import java.io.*;
import java.security.cert.CertificateException;

import app.AppPaths;
import app.security.CRLManager;
import app.security.CertManager;
import app.service.UserContext;
import app.util.Util;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;



public class WelcomeViewController {

    @FXML
    private Button btnBrowse;

    @FXML
    private Button btnRegister;

    @FXML
    void handleBrowse(ActionEvent event) {
        // create a new FileChooser object
        FileChooser fileChooser = new FileChooser();

        // set the title of the FileChooser dialog box
        fileChooser.setTitle("Choose your Certificate");

        // show the dialog box and wait for the user to choose a file
        File selectedFile = fileChooser.showOpenDialog(null);

        // if a file was chosen, verify the certificate
        if (selectedFile != null) {
            // flag that indicates if certificate is revoked or not
            boolean isCertificateRevoked = false;
            try {
                isCertificateRevoked = CRLManager.isCertificateRevoked(CertManager.getUserCertificate(selectedFile));
            } catch (IOException | CertificateException e) {
                e.printStackTrace();
            }
            if(!isCertificateRevoked){
                // flag that indicates if certificate is valid or not
                boolean isCertificateValid = CertManager.verifyCertificate(selectedFile);
                if (isCertificateValid) {
                    try {
                        // set currently loaded certificate
                        UserContext.getInstance().setCertificate(CertManager.getUserCertificate(selectedFile));

                        // show a message indicating that the certificate was accepted
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Certificate Accepted");
                        alert.setHeaderText(null);
                        alert.setContentText("The selected certificate has been accepted.");
                        alert.showAndWait();

                        // load the login view and display it in the main window
                        Util.setScene(AppPaths.LOGIN_VIEW, event, "Login");

                    } catch (IOException | CertificateException e) {
                        e.printStackTrace();
                    }
                } else {
                    // show a message indicating that the selected file is not a valid certificate
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Certificate");
                    alert.setHeaderText(null);
                    alert.setContentText("The selected file is not a valid X.509 certificate.");
                    alert.showAndWait();
                }
            } else {
                // show a message indicating that the certificate is revoked
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Revoked Certificate");
                alert.setHeaderText(null);
                alert.setContentText("The certificate is revoked and cannot be used to log in.");
                alert.showAndWait();
            }
        }

    }

    @FXML
    void handleRegister(ActionEvent event) {
        // load the registration view and display it in the main window
        try {
            Util.setScene(AppPaths.REGISTRATION_VIEW, event, "Registration");
        } catch (IOException e) {
            // if an error occurs, print the stack trace
            e.printStackTrace();
        }
    }

}
