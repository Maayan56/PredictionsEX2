package app.body.screen3.main;

import app.body.screen3.list.view.update.UpdateListView;
import app.body.screen3.result.ResultsController;
import app.body.screen3.simulation.progress.SimulationProgressController;
import app.body.screen3.simulation.progress.task.UpdateUiTask;
import app.main.AppController;
import dto.api.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import system.engine.api.SystemEngineAccess;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Body3Controller {
    @FXML
    private ListView<String> simulationsList;
    @FXML
    private Button stopButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Button resumeButton;

    @FXML
    private Button rerunButton;
    @FXML
    private ScrollPane simulationProgressScrollPane;

    @FXML
    private ScrollPane simulationResultScrollPane;

    @FXML private VBox simulationProgressComponent;
    @FXML private SimulationProgressController simulationProgressComponentController;
    private SystemEngineAccess systemEngine;
    private Map<Integer, HBox> simulationResultsNodesMap;
    private Map<Integer, ResultsController> simulationResultControllersMap;
    private Thread oldUpdateUiThreadThread = null;
    private AppController mainController;


    public Body3Controller() {
        simulationResultScrollPane=new ScrollPane();
        simulationResultsNodesMap = new HashMap<>();
        simulationResultControllersMap = new HashMap<>();
    }


    public void primaryInitialize() {
        simulationsList.getItems().clear();
        simulationResultsNodesMap.clear();
        simulationResultControllersMap.clear();
        clearSimulationProgressScreen();
        simulationResultScrollPane.setContent(null);
        simulationsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleSimulationListItemSelection(newValue);
            }
        });
        simulationProgressComponentController.setBody3ComponentController(this);
        UpdateListView updateListView = new UpdateListView(simulationsList, systemEngine);
        new Thread(updateListView).start();
    }
    private void clearSimulationProgressScreen(){
        if(oldUpdateUiThreadThread !=null && oldUpdateUiThreadThread.isAlive()){
            oldUpdateUiThreadThread.interrupt();
        }
        simulationProgressComponentController.clearMyLabels();
    }
    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }


    public void addItemToSimulationListView(int idNum){
        simulationsList.getItems().add("Simulation ID: " + idNum);
    }

        private void handleSimulationListItemSelection(String selectedItem) {
            simulationResultScrollPane.setContent(null);
            if(oldUpdateUiThreadThread !=null && oldUpdateUiThreadThread.isAlive()){
                oldUpdateUiThreadThread.interrupt();
            }
            String[] words = selectedItem.split("\\s+");
            Integer simulationID = 0;

            if(words.length == 3){
                simulationID = (Integer.parseInt(words[words.length - 1]));
            }
            else if(words.length == 4) {
                simulationID = (Integer.parseInt(words[words.length - 2]));
            }
            else{
                simulationID = (Integer.parseInt(words[words.length - 3]));
            }
            //simulationProgressScrollPane.setContent(simulationProgressNodesList.get(simulationID-1));
            UpdateUiTask updateUiTask = new UpdateUiTask(simulationProgressComponentController, systemEngine, simulationID);

            simulationProgressComponentController.setSystemEngine(systemEngine);
            simulationProgressComponentController.setSimulationIdLabel(simulationID.toString());
            simulationProgressComponentController.setSimulationID(simulationID);
            simulationProgressComponentController.setTotalSeconds(systemEngine.getTotalSecondsNumber());
            simulationProgressComponentController.bindUiTaskToUiUpLevelComponents(updateUiTask);
            simulationProgressComponentController.bindUiTaskToUiDownLevelComponents(updateUiTask);


            oldUpdateUiThreadThread  = new Thread(updateUiTask);
            oldUpdateUiThreadThread.start();
            if(simulationResultsNodesMap.get(simulationID) != null &&
                    ! systemEngine.getAllSimulationsStatus().get(simulationID-1).equals(("terminated because of an error!"))){
                simulationResultScrollPane.setContent(simulationResultsNodesMap.get(simulationID));
                ResultsController resultsController = simulationResultControllersMap.get(simulationID);
                resultsController.handleSimulationSelection(simulationID,systemEngine);
            }
        }

        public void setButtonsDisableIfThereIsNoSimulations(){
            if(simulationsList.getItems().isEmpty() || simulationProgressComponentController.isSimulationWasChosen()){
                simulationProgressComponentController.setButtonsDisableBeforeSimulationWasChosen();
            }
        }

    public void setSystemEngine(SystemEngineAccess systemEngineAccess){
        this.systemEngine = systemEngineAccess;
        //this.resultsComponentController.setSystemEngine(systemEngineAccess);
    }


    public void createAndAddNewSimulationResultToList(DTOSimulationEndingForUi dtoSimulationEndingForUi) {

        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/body/screen3/result/results.fxml"));
            HBox simulationResultNode = loader.load();
            ResultsController simulationResultController = loader.getController();
            simulationResultController.setBody3Controller(this);
            simulationResultController.setSystemEngine(systemEngine);
            simulationResultController.primaryInitialize();
            Integer simulationID = dtoSimulationEndingForUi.getSimulationID();
            Integer simulationEndReason = dtoSimulationEndingForUi.getTerminationReason()[2];
            createEndSimulationWindow(simulationID, simulationEndReason);
            simulationResultsNodesMap.put(simulationID, simulationResultNode);
            simulationResultControllersMap.put(simulationID, simulationResultController);

            Object selectedItem = simulationsList.getSelectionModel().getSelectedItem();
            String selectedString = selectedItem != null ? selectedItem.toString() : "";
            if(selectedString.equals("Simulation ID: " + simulationID)){
                simulationsList.getSelectionModel().clearSelection();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    private String createEndReasonString(Integer simulationEndReason){
        String massage = null;
        switch (simulationEndReason){
            case 0:
                massage = "last tick";
                break;
            case 1:
                massage = "last second";
                break;
            case 2:
                massage = "user";
                break;
            case 3:
                massage = "an errors";
                break;
        }
        return massage;
    }
    public void createEndSimulationWindow(Integer simulationID, Integer simulationEndReason){
        Stage primaryStage = new Stage();
        String message = "Simulation #" + simulationID + " is over because of " + createEndReasonString(simulationEndReason)+ "\n\n\n";

        Label label = new Label(message);
        Font font = new Font(16);
        label.setFont(font);
        Image image = new Image(getClass().getResource("/resource/finishImage.jpg").toExternalForm());
        ImageView imageView = new ImageView(image);
        VBox vbox = new VBox(label, imageView);
        vbox.setAlignment(Pos.CENTER);
        StackPane root = new StackPane(vbox);
        Scene scene = new Scene(root, 350, 250);

        primaryStage.setScene(scene);
        primaryStage.setTitle("End simulation");
        primaryStage.show();
    }

    public void updateQueueManagementInAppMain(){
        mainController.onQueueManagementButtonUpdate();
    }

    public void onRerunClick(int simulationID) {
        mainController.onRerunClick( simulationID);
    }
}


