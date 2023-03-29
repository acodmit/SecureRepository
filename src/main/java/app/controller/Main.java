package app.controller;

import app.AppPaths;
import app.util.Config;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(AppPaths.WELCOME_VIEW));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Secure Repository Program");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Config.configure();
        launch();
    }
}