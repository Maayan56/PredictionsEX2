package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static main.CommonResourcesPaths.APP_FXML_INCLUDE_RESOURCE;


import java.net.URL;

public class AppMain extends Application {



    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(APP_FXML_INCLUDE_RESOURCE);
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());


        //HeaderController headerController = fxmlLoader.getController();
       // headerController.setPrimaryStage(primaryStage);

        Scene scene = new Scene(root, 850, 680);
        scene.getStylesheets().add(getClass().getResource("/app/main/default.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        }

}