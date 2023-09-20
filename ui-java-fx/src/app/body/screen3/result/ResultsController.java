package app.body.screen3.result;

import app.body.screen3.main.Body3Controller;
import dto.api.DTODefinitionsForUi;
import dto.api.DTOEntitiesAfterSimulationByQuantityForUi;
import dto.api.DTOPropertyHistogramForUi;
import dto.api.DTOSimulationEndingForUi;
import dto.definition.entity.api.EntityDefinitionDTO;
import dto.definition.property.definition.api.PropertyDefinitionDTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import system.engine.api.SystemEngineAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResultsController {


    @FXML
    private ScrollPane entityTimeGraphPane;

    @FXML
    private TreeView<String> entityPropTreeView;

    @FXML
    private ComboBox<String> viewComboBox;

    @FXML
    private Label ConsistencyValueLabel;

    @FXML
    private Label PropertyAverageValueLabel;

    @FXML
    private ScrollPane histogramGraphPane;

    @FXML
    private Label ConsistencyLabel;

    @FXML
    private Label PropertyAverageLabel;

    private Body3Controller body3Controller;
    private SystemEngineAccess systemEngine;

    public void setBody3Controller(Body3Controller body3Controller) {
        this.body3Controller = body3Controller;
    }

    @FXML
    public void initialize() {

    }

    public TreeView<String> getEntityPropTreeView() {
        return entityPropTreeView;
    }

    public void primaryInitialize() {

            viewComboBox.getItems().addAll("Histogram", "Consistency", "Property Average");
            viewComboBox.setVisible(false);
            ConsistencyValueLabel.setVisible(false);
            PropertyAverageValueLabel.setVisible(false);
            ConsistencyLabel.setVisible(false);
            PropertyAverageLabel.setVisible(false);
            histogramGraphPane.setVisible(false);
            entityPropTreeView.setVisible(false);
            entityTimeGraphPane.setVisible(false);
            viewComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                ConsistencyValueLabel.setVisible(false);
                ConsistencyLabel.setVisible(false);
                PropertyAverageValueLabel.setVisible(false);
                PropertyAverageLabel.setVisible(false);
                histogramGraphPane.setVisible(false);

                if (newValue != null) {
                    switch (newValue) {
                        case "Histogram":
                            histogramGraphPane.setVisible(true);
                            break;
                        case "Consistency":
                            ConsistencyValueLabel.setVisible(true);
                            ConsistencyLabel.setVisible(true);
                            break;
                        case "Property Average":
                            PropertyAverageValueLabel.setVisible(true);
                            PropertyAverageLabel.setVisible(true);
                            break;

                    }
                }
            });
    }

    public void setSystemEngine(SystemEngineAccess systemEngineAccess){
        this.systemEngine = systemEngineAccess;
    }

    public BarChart<String, Number> createHistogram(TreeItem<String> selectedItem,SystemEngineAccess systemEngine,int simulationID) {

        // Create the X and Y axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        yAxis.setTickUnit(1.0);

        // Create the bar chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        xAxis.setLabel("property value");
        yAxis.setLabel("quantity of entities");

        // Prepare data for the chart
        XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();

        DTOPropertyHistogramForUi dtoPropertyHistogramForUi = systemEngine.getPropertyDataAfterSimulationRunningByHistogramByNames(simulationID,selectedItem.getParent().getValue(),selectedItem.getValue());
        Map< Object, Long> propertyHistogramMap = dtoPropertyHistogramForUi.getPropertyHistogram();

        for (Map.Entry<Object, Long> entry : propertyHistogramMap.entrySet()) {
            dataSeries.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
        }

        barChart.getData().add(dataSeries);
        return barChart;
    }

    public LineChart<Number, Number> createEntitiesByTickGraph(SystemEngineAccess systemEngine,int simulationID) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setTickUnit(1.0);
        yAxis.setTickUnit(1.0);

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        Map<Integer, Integer> entitiesData = systemEngine.getEntitiesDataAfterSimulationRunningByQuantity(simulationID).getEntitiesLeftByTicks();

        if (entitiesData.size() > 0) {
            if (entitiesData.size() > 1000) {
                int jump = 1000;
                for (int i = 0; i < entitiesData.size(); i += jump) {
                    int key = i;
                    int value = entitiesData.get(key);
                    series.getData().add(new XYChart.Data<>(key, value));
                }
            } else {
                for (Map.Entry<Integer, Integer> entry : entitiesData.entrySet()) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
            }
        }

        lineChart.getData().add(series);
        return lineChart;

        /*for (Map.Entry<Integer, Integer> entry : systemEngine.getEntitiesDataAfterSimulationRunningByQuantity(simulationID).getEntitiesLeftByTicks().entrySet())
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));*/
    }

    public float calculatePropertyAverage(Integer simulationID,String entityName,String propertyName){
        DTOPropertyHistogramForUi dtoPropertyHistogramForUi;
        float sumOfProducts = 0.0f;
        float sumOfValues = 0.0f;
            dtoPropertyHistogramForUi=systemEngine.getPropertyDataAfterSimulationRunningByHistogramByNames(simulationID,entityName,propertyName);
            for (Map.Entry<Object, Long> entry : dtoPropertyHistogramForUi.getPropertyHistogram().entrySet()) {
                float key = Float.parseFloat(entry.getKey().toString());
                float value = Float.parseFloat(entry.getValue().toString());
                float product = key * value;

                sumOfProducts += product;
                sumOfValues += value;
            }

            return (sumOfProducts / sumOfValues);

    }


    public void handleSimulationSelection(int simulationID,SystemEngineAccess systemEngine) {
        entityTimeGraphPane.setContent(createEntitiesByTickGraph(systemEngine,simulationID));
        entityTimeGraphPane.setVisible(true);

        TreeItem<String> rootItem = createEntitiesSubTree(simulationID, systemEngine);
        entityPropTreeView.setRoot(rootItem);
        entityPropTreeView.setShowRoot(false);
        entityPropTreeView.setVisible(true);

        entityPropTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!=null && newValue.isLeaf())
                handleSelectedProperty(newValue,systemEngine,simulationID);
        });
    }



    public void handleSelectedProperty(TreeItem<String> selectedItem,SystemEngineAccess systemEngine,int simulationID){
        viewComboBox.setVisible(true);
        viewComboBox.setPromptText("select option");
        if(systemEngine.getDtoSimulationProgressForUi(simulationID).getEntitiesLeft().get(selectedItem.getParent().getValue())>0){
            if(systemEngine.getDefinitionsDataFromSE().getPropertyDefinitionByName(selectedItem.getParent().getValue(),selectedItem.getValue()).getType().toLowerCase().equals("float"))
                PropertyAverageValueLabel.setText(String.valueOf(calculatePropertyAverage(simulationID,selectedItem.getParent().getValue(),selectedItem.getValue())));
            else{
                PropertyAverageValueLabel.setText("Property's type is not numeric");
                PropertyAverageValueLabel.setWrapText(true);
            }

            BarChart<String, Number> histogram = createHistogram(selectedItem,systemEngine,simulationID);
            histogramGraphPane.setContent(histogram);
        }
        else{

            PropertyAverageValueLabel.setText("Entity's population is 0");
            PropertyAverageValueLabel.setWrapText(true);
            Label zeroPopulation=new Label("Entity's population is 0 - no data to show");
            zeroPopulation.setWrapText(true);
            histogramGraphPane.setContent(zeroPopulation);
        }
        ConsistencyValueLabel.setText(String.valueOf(systemEngine.getConsistencyDTOByEntityPropertyName(simulationID,selectedItem.getParent().getValue(),selectedItem.getValue()).getConsistency()));
    }



        public TreeItem<String> createEntitiesSubTree(int simulationID, SystemEngineAccess systemEngine){
            DTOEntitiesAfterSimulationByQuantityForUi entitiesAfterSimulationForUi= systemEngine.getEntitiesDataAfterSimulationRunningByQuantity(simulationID);
            List<String> entitiesNames = entitiesAfterSimulationForUi.getEntitiesNames();

            List<TreeItem<String>> entitiesBrunches = new ArrayList<>();
            for(String entityName : entitiesNames){
                entitiesBrunches.add(createSingleEntitySubTree(entityName,systemEngine));
            }

            TreeItem<String> entitiesBranch = new TreeItem<>("Entities");
            entitiesBranch.getChildren().addAll(entitiesBrunches);
            return entitiesBranch;
        }

    public TreeItem<String> createSingleEntitySubTree(String entityName,SystemEngineAccess systemEngine) {

        TreeItem<String> entityBranch = new TreeItem<>(entityName);
        DTODefinitionsForUi dtoDefinitionsForUi = systemEngine.getDefinitionsDataFromSE();
        List<EntityDefinitionDTO> entities = dtoDefinitionsForUi.getEntitiesDTO();
        for (EntityDefinitionDTO entityDefinitionDTO : entities) {
            if (entityDefinitionDTO.getUniqueName().equals(entityName)) {
                for (PropertyDefinitionDTO propertyDefinitionDTO : entityDefinitionDTO.getProps()) {
                    entityBranch.getChildren().add(new TreeItem<>(propertyDefinitionDTO.getUniqueName()));
                }
            }
        }
        return entityBranch;
    }

}
