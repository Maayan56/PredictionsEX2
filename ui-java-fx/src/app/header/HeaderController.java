package app.header;

import app.main.AppController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Path;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import system.engine.api.SystemEngineAccess;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static main.CommonResourcesPaths.APP_FXML_INCLUDE_RESOURCE;
import static main.CommonResourcesPaths.BODY_FXML_RESOURCE;

public class HeaderController {


    @FXML
    private GridPane headerComponent;

    @FXML
    private Button loadFileButton;


    @FXML
    private Label waitingLabel;
    @FXML
    private Label overLabel;
    @FXML
    private Label currentlyExecutingLabel;
    @FXML
    private Label valueCurrentlyExecutingLabel;
    @FXML
    private Label valueWaitingLabel;
    @FXML
    private GridPane queueManagementGridPane;
    @FXML
    private Label valueOverLabel;
    @FXML
    private Button detailsButton;
    @FXML
    private Button newExecutionButton;
    @FXML
    private Button resultButtonClick;
    @FXML
    private Label selectedFileNameLabel;
    @FXML
    private Rectangle detailsRec;
    @FXML
    private Rectangle executionRec;
    @FXML
    private Rectangle resultsRec;

    @FXML
    private Button skin1Button;

    @FXML
    private Button skin2Button;

    @FXML
    private Button defaultSkinButton;

    @FXML
    private Button animationsButton;

    private SystemEngineAccess systemEngine;
    private SimpleStringProperty selectedFileProperty;
    private SimpleBooleanProperty isFileSelected;
    private AppController mainController;
    private Stage primaryStage;
    private boolean activeAnimations = true;
    private Animations animations;


    public void setSystemEngine(SystemEngineAccess systemEngineAccess){
        this.systemEngine = systemEngineAccess;
    }
    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    public HeaderController() {
        selectedFileProperty = new SimpleStringProperty();
        isFileSelected = new SimpleBooleanProperty(false);
        this.animations = new Animations();
    }

    @FXML
    private void initialize() {
        selectedFileNameLabel.textProperty().bind(selectedFileProperty);
        detailsButton.disableProperty().bind(isFileSelected.not());
        newExecutionButton.disableProperty().bind(isFileSelected.not());
        resultButtonClick.disableProperty().bind(isFileSelected.not());
    }

    @FXML
    void onLoadFileButtonClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile == null) {
            return;
        }
        boolean exceptionOccurred = false;
        String absolutePath = selectedFile.getAbsolutePath();
        try {
            systemEngine.getXMLFromUser(absolutePath);
        } catch(RuntimeException | JAXBException | FileNotFoundException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("Invalid XML file - Please load other file");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            exceptionOccurred=true;
        }
        if(!exceptionOccurred){
            selectedFileProperty.set(absolutePath);
            isFileSelected.set(true);
            mainController.onLoadFileButtonClick();
        }


    }

    @FXML
    void onDetailsButtonClick(ActionEvent event) {
        mainController.onDetailsButtonClick();
        if(activeAnimations){
            animations.startFillTransition(detailsRec);
        }

    }


    @FXML
    void onNewExecutionButtonClick(ActionEvent event) {
        mainController.onNewExecutionButtonClick();
        if(activeAnimations){
            animations.startFadeTransition(executionRec, false);
        }
    }

    public void setCurrentlyExecutingLabel(String currentlyExecutingLabel) {
        this.valueCurrentlyExecutingLabel.setText(currentlyExecutingLabel);
    }

    public void setWaitingLabel(String waitingLabel) {
        this.valueWaitingLabel.setText(waitingLabel);
    }

    public void setOverLabel(String overLabel) {
        this.valueOverLabel.setText(overLabel);
    }

    @FXML
    void onResultButtonClick(ActionEvent event) {
        mainController.onResultButtonClick();
        if(activeAnimations){
            animations.startPathTransition(valueWaitingLabel, valueCurrentlyExecutingLabel, valueOverLabel);
            Insets labelMargins = new Insets(20, 0, 0, 0);
            GridPane.setMargin(valueWaitingLabel, labelMargins);
            GridPane.setMargin(valueCurrentlyExecutingLabel, labelMargins);
            GridPane.setMargin(valueOverLabel, labelMargins);
        }
    }


    @FXML
    void onAnimationsClick(MouseEvent event) {
        activeAnimations = !activeAnimations;
        if(!activeAnimations)
            animationsButton.setText("Enable animations");
        else
            animationsButton.setText("Disable animations");
    }


    @FXML
    void onDefaultSkinClick(ActionEvent event) {
        Scene scene = headerComponent.getScene();
        ObservableList<String> stylesheets = scene.getStylesheets();
        if (!stylesheets.isEmpty()) {
            stylesheets.remove(stylesheets.size() - 1);
        }
        // Add the "new.css" stylesheet to the scene
        scene.getStylesheets().add(getClass().getResource("/app/main/default.css").toExternalForm());
    }


    @FXML
    void onSkin1Click(MouseEvent event) {

        Scene scene = headerComponent.getScene();
        ObservableList<String> stylesheets = scene.getStylesheets();
        if (!stylesheets.isEmpty()) {
            stylesheets.remove(stylesheets.size() - 1);
        }

        // Add the "new.css" stylesheet to the scene
        scene.getStylesheets().add(getClass().getResource("/app/main/appGlobal1.css").toExternalForm());
    }


    @FXML
    void onSkin2Click(MouseEvent event) {
        Scene scene = headerComponent.getScene();
        ObservableList<String> stylesheets = scene.getStylesheets();
        if (!stylesheets.isEmpty()) {
            stylesheets.remove(stylesheets.size() - 1);
        }
        // Add the "new.css" stylesheet to the scene
        scene.getStylesheets().add(getClass().getResource("/app/main/appGlobal2.css").toExternalForm());
    }
}
