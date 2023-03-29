package app.util;

import app.AppPaths;
import app.controller.Main;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;

public final class Util {
    private Util(){
        // private constructor to avoid initialization
    }
    public static void setScene(String fxmlPath, Event event, String title) throws IOException {
        // load the FXML file and set the scene
        Parent root = FXMLLoader.load(Main.class.getResource(fxmlPath));
        Scene scene = new Scene(root);

        Stage stage = null;

        if (event.getSource() instanceof Node) {
            // set the window title
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            // create a new stage
            stage = new Stage();
        }

        // set the window title
        stage.setTitle(title);

        // show the scene
        stage.setScene(scene);
        stage.show();
    }

    public static boolean isUsernameAvailable(String username) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(AppPaths.USERS));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(username)) {
                    reader.close();
                    return false;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
