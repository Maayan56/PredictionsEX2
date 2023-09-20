package app.body.screen3.list.view.update;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import system.engine.api.SystemEngineAccess;

import java.util.List;

import static java.lang.Thread.sleep;

public class UpdateListView implements Runnable{
    private ListView<String> simulationsList;
    private SystemEngineAccess systemEngine;


    public UpdateListView(ListView<String> simulationsList, SystemEngineAccess systemEngine) {
        this.simulationsList = simulationsList;
        this.systemEngine = systemEngine;
    }

    @Override
    public void run() {
        while(Thread.currentThread().isAlive()) {
            List<String> simulationsStatuses = systemEngine.getAllSimulationsStatus();
            Integer simulationID = 1;

            for (String simulation : simulationsStatuses) {
                Integer finalSimulationID = simulationID;
                Platform.runLater(() -> {
                    changeStatusOfSimulationsListItem(finalSimulationID, simulation);
                });
                simulationID++;
            }
            try {
                sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void changeStatusOfSimulationsListItem(Integer simulationID, String simulationStatus) {
        ObservableList<String> items = simulationsList.getItems();
        if(simulationStatus.equals("terminated because of an error!")){
            items.set(simulationID -1, "Simulation ID: " + simulationID + " (error)");

        }
        else {
            items.set(simulationID - 1, "Simulation ID: " + simulationID + " (" + simulationStatus + ")");
        }
    }
}
