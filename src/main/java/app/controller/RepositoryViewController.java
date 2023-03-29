package app.controller;

import app.AppConstants;
import app.AppPaths;
import app.security.Digest;
import app.security.Encryption;
import app.service.FileHandler;
import app.service.UserContext;
import app.util.Util;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RepositoryViewController implements Initializable {

    @FXML
    private Button btnAddFile;
    @FXML
    private ListView<String> lvRepoFiles;
    @FXML
    private Button btnDeleteFile;
    @FXML
    private Button btnLogout;

    private ObservableList<String> repositories;
    private ObservableList<String> filteredRepositories;

    public void initialize(URL location, ResourceBundle resourceBundle) {
        lvRepoFiles.setEditable(true);

        // load all files from directory
        File folder = new File(AppPaths.PART_DIR + "1");
        File[] files = folder.listFiles();

        // create an observable list of filenames
        List<String> filenames = new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && file.getName().startsWith(UserContext.getInstance().getUsername())) {
                String filename = file.getName().substring(UserContext.getInstance().getUsername().length() + 1, file.getName().length() - 6);
                filenames.add(filename);
            }
        }
        ObservableList<String> filenamesList = FXCollections.observableArrayList(filenames);

        // clear the items in the list view
        lvRepoFiles.getItems().clear();

        // set the items property of the ListView to the observable list of filenames
        lvRepoFiles.getItems().addAll(filenamesList);

        // set the double-click event handler to open the selected file
        lvRepoFiles.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedItem = lvRepoFiles.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    handleOpenFile( selectedItem);
                }
            }
        });
    }

    private void handleOpenFile(String filename) {
        byte[][] encryptedByteParts = new byte[5][];
        for (int i = 0; i < 5; i++) {
            String path = AppPaths.PART_DIR + (i + 1) + File.separator + UserContext.getInstance().getUsername() + "_" +
                    filename + ".part" + (i + 1); // path to match file's location
            File file = new File(path);
            try {
                encryptedByteParts[i] = FileHandler.fileToBytes(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (encryptedByteParts == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to join file parts.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        byte[][] decryptedByteParts;
        byte[] joinedBytes;
        try {
            decryptedByteParts = Encryption.decryptParts(encryptedByteParts);
            joinedBytes = FileHandler.joinByteArrays(decryptedByteParts);
        } catch (Exception ex){
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open file.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        boolean isCorrupted = true;
        try {
            isCorrupted = Digest.isFileCorrupted(joinedBytes,filename);
        }catch ( IOException e){
            e.printStackTrace();
        }
        try {
            if (isCorrupted) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "The file is corrupted.", ButtonType.OK);
                alert.showAndWait();
            } else {
                FileHandler.openByteArray(joinedBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open file.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    void handleAddFile(ActionEvent event) throws Exception{
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Encrypt");
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // generate digest for the selected file and saves it to filesystem
            FileHandler.bytesToFile(Digest.digest(selectedFile),
                    AppPaths.DGST_DIR + File.separator + selectedFile.getName() + "." + AppConstants.DGST_ALGORITHM.toLowerCase());

            byte[][] parts = null;
            try {
                // divide the bytes into parts
                 parts = FileHandler.divideBytesIntoParts(FileHandler.fileToBytes(selectedFile));
            }catch (IOException e){
                e.printStackTrace();
            }

            // encrypt the parts
            byte[][] encryptedParts = null;
            if(parts != null)
                encryptedParts = Encryption.encryptParts(parts);
            else{
                throw new Exception ( "Division unsuccessful.");
            }

            // save encrypted byte arrays as files
            for (int i = 0; i < encryptedParts.length; i++) {
                byte[] part = encryptedParts[i];
                String filePath = AppPaths.PART_DIR + (i + 1) + File.separator
                        + UserContext.getInstance().getUsername() + "_" +selectedFile.getName() + ".part" + (i + 1);
                FileHandler.bytesToFile(part, filePath);
            }

            // refresh the list view to show the new file
            initialize(null, null);

            // display a success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("File added");
            alert.setContentText("The file has been successfully added to the repository.");
            alert.showAndWait();
        }
    }

    @FXML
    void handleDeleteFile(ActionEvent event) {
        // get the selected item from the ListView
        String filename = lvRepoFiles.getSelectionModel().getSelectedItem();

        // call the deleteFile method to delete the file
        boolean success = FileHandler.deleteFile(filename);

        // show a message indicating whether the file was successfully deleted
        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("File Deleted");
            alert.setHeaderText(null);
            alert.setContentText("The file " + filename + " was successfully deleted.");
            alert.showAndWait();

            // refresh the ListView by calling the initialize method
            initialize(null, null);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("There was an error deleting the file " + filename + ".");
            alert.showAndWait();
        }

        // refresh the list view
        initialize(null, null);
    }


    @FXML
    void handleLogout(ActionEvent event) {
        // set currently logged UserContext fields to null
        UserContext.getInstance().clear();

        try {
            // open the welcome FXML page
            Util.setScene(AppPaths.WELCOME_VIEW, event, "Welcome to Secure Repository Program!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}