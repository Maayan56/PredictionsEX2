package app.body.screen3.simulation.progress;
import app.body.screen3.main.Body3Controller;
import app.body.screen3.simulation.progress.task.UpdateUiTask;
import dto.api.DTOEntitiesAfterSimulationByQuantityForUi;
import dto.api.DTOSimulationEndingForUi;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import system.engine.api.SystemEngineAccess;

import java.util.List;
import java.util.Map;


public class SimulationProgressController {

    @FXML
    private ProgressBar simulationProgressBar;
    @FXML
    private Label PercentLabel;
    @FXML
    private Label simulationIdLabel;
    @FXML
    private Label progressMassageLabel;
    @FXML
    private Label secondsLabel;
    @FXML
    private Label entitiesLeftLabel;
    @FXML
    private Label ticksLabel;
    @FXML
    private Button pauseButton;
    @FXML
    private Button resumeButton;
    @FXML
    private Button stopButton;
    @FXML
    private GridPane entitiesLeftGridPane;

    private int simulationID;
    private Body3Controller body3ComponentController;
    private SystemEngineAccess systemEngine;

    public void setSimulationID(int simulationID) {
        this.simulationID = simulationID;
    }

    public void setSystemEngine(SystemEngineAccess systemEngine) {
        this.systemEngine = systemEngine;
    }

    public void setBody3ComponentController(Body3Controller body3ComponentController) {
        this.body3ComponentController = body3ComponentController;
    }
    public void setSimulationIdLabel(String simulationIdLabel) {
        this.simulationIdLabel.setText(simulationIdLabel);
    }

    public void bindUiTaskToUiDownLevelComponents(UpdateUiTask uiTask) {
        secondsLabel.textProperty().bind(Bindings.format("%,d", uiTask.getSecondsPastProperty()));
        entitiesLeftLabel.textProperty().bind(Bindings.format("%,d", uiTask.getEntitiesLeftProperty()));
        ticksLabel.textProperty().bind(Bindings.format("%,d", uiTask.getTicksPastProperty()));
        if(progressMassageLabel.getText().equals("Done!")){
            toggleTaskButtons(false);
        }
        else if(progressMassageLabel.getText().equals("Running!")){
            toggleTaskButtons(true);
        }
    }
    public void bindUiTaskToUiUpLevelComponents(Task<Boolean> uiTask) {
        // task message
        progressMassageLabel.textProperty().bind(uiTask.messageProperty());

        // task progress bar
        simulationProgressBar.progressProperty().bind(uiTask.progressProperty());

        // task percent label
        PercentLabel.textProperty().bind(
                Bindings.concat(
                        Bindings.format(
                                "%.0f",
                                Bindings.multiply(
                                        uiTask.progressProperty(),
                                        100)),
                        " %"));

        // task cleanup upon finish
        /*aTask.valueProperty().addListener((observable, oldValue, newValue) -> {
            onTaskFinished(Optional.ofNullable(onFinish));
        });*/
    }

    public void onTaskFinished() {
        this.progressMassageLabel.textProperty().unbind();
        this.PercentLabel.textProperty().unbind();
        this.simulationProgressBar.progressProperty().unbind();
    }

    public void toggleTaskButtons(boolean isActive) {
        stopButton.setDisable(!isActive);
        pauseButton.setDisable(!isActive);
        resumeButton.setDisable(!isActive);
    }

    public void addNewSimulationResultToBody3Controller(DTOSimulationEndingForUi dtoSimulationEndingForUi){
        body3ComponentController.createAndAddNewSimulationResultToList(dtoSimulationEndingForUi);
    }
    @FXML
    synchronized void onPauseClick(MouseEvent event) {
        systemEngine.pauseSimulation(simulationID);
    }

    @FXML
    synchronized void onResumeClick(MouseEvent event) {
        systemEngine.resumeSimulation(simulationID);
    }

    @FXML
    void onStopClick(MouseEvent event) {
       systemEngine.cancelSimulation(simulationID);
    }

    public void updateEntitiesLeftGridPane(Map<String, Integer> entitiesPopulationAfterSimulationRunning) {
        for(String key: entitiesPopulationAfterSimulationRunning.keySet()){
            for (int row = 0; row < entitiesPopulationAfterSimulationRunning.size(); row++) {
                Label entityName = new Label(key);
                entitiesLeftGridPane.add(entityName, 0, row);
                Label population = new Label(entitiesPopulationAfterSimulationRunning.get(key).toString());
                entitiesLeftGridPane.add(population, 1, row);
            }
        }
    }
}
