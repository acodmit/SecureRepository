package app.controller;

import app.AppPaths;
import app.security.CertManager;
import app.security.Digest;
import app.util.Util;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.util.Optional;


public class RegistrationViewController {

    @FXML
    private PasswordField pwfPassword;

    @FXML
    private Button btnRegister;

    @FXML
    private TextField txfUsername;

    @FXML
    void handleRegister(ActionEvent event) {
        // get username and password from text fields
        String username = txfUsername.getText();
        String password = pwfPassword.getText();

        // check if username is at least 3 characters long
        if (username.length() < 3) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Username");
            alert.setHeaderText("Username must be at least 3 characters long.");
            alert.setContentText("Please try again with a valid username.");
            alert.showAndWait();
            return;
        }

        // check if username is already taken
        if (!Util.isUsernameAvailable(username)) {
            // display error message if username is not available
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Error");
            alert.setHeaderText(null);
            alert.setContentText("The username already exists. Please choose a different username.");
            alert.showAndWait();
            return;
        }

        // generate user certificate for given username and password
        File certificateFile = null;
        try {
            certificateFile = CertManager.generateUserCert(username, password);

        } catch (IOException | GeneralSecurityException | OperatorCreationException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Certificate Generation Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while generating the user certificate.");
            alert.showAndWait();
            return;
        }

        // ask user where to save certificate
        Alert successAlert = new Alert(Alert.AlertType.CONFIRMATION);
        successAlert.setTitle("Certificate Created Successfully");
        successAlert.setHeaderText(null);
        successAlert.setContentText("The user certificate has been created successfully. Where do you want to store it?");
        ButtonType chooseDirectoryButton = new ButtonType("Choose Directory", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        successAlert.getButtonTypes().setAll(chooseDirectoryButton, cancelButton);
        Optional<ButtonType> result = successAlert.showAndWait();


        // save certificate to selected directory
        if (result.get() == chooseDirectoryButton) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose Directory to Save Certificate");

            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            File selectedDirectory = directoryChooser.showDialog(stage);

            if (selectedDirectory != null) {
                try {
                    // copy certificate file to selected directory
                    Files.copy(certificateFile.toPath(), new File(selectedDirectory, certificateFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // save user to file
                    Digest.saveUser(username, password);

                    // display success message if certificate is saved successfully
                    Alert copySuccessAlert = new Alert(Alert.AlertType.INFORMATION);
                    copySuccessAlert.setTitle("Certificate Saved Successfully");
                    copySuccessAlert.setHeaderText(null);
                    copySuccessAlert.setContentText("The user certificate has been saved successfully.");

                    // add event handler to OK button
                    ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                    copySuccessAlert.getButtonTypes().setAll(okButton);
                    copySuccessAlert.setOnHidden(dialogEvent -> {
                        // load welcome FXML and set as root node of scene
                        try {
                            Util.setScene(AppPaths.WELCOME_VIEW, event, "Welcome to Secure Repository Program");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    copySuccessAlert.showAndWait();

                } catch (IOException e) {
                    // display error message if saving certificate fails
                    e.printStackTrace();
                    Alert copyErrorAlert = new Alert(Alert.AlertType.ERROR);
                    copyErrorAlert.setTitle("Certificate Save Error");
                    copyErrorAlert.setHeaderText(null);
                    copyErrorAlert.setContentText("An error occurred while saving the user certificate.");

                    // add event handler to Back button
                    ButtonType backButton = new ButtonType("BACK", ButtonBar.ButtonData.BACK_PREVIOUS);
                    copyErrorAlert.getButtonTypes().setAll(backButton);
                    copyErrorAlert.setOnHidden(dialogEvent -> {
                        // load welcome FXML and set as root node of scene
                        try {
                            Util.setScene(AppPaths.WELCOME_VIEW, event, "Welcome to Secure Repository Program");
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                    copyErrorAlert.showAndWait();
                }
            }
        }
    }

}
