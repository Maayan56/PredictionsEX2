package system.engine.world.execution.instance.enitty.manager.impl;

import system.engine.world.definition.entity.api.EntityDefinition;
import system.engine.world.definition.entity.manager.api.EntityDefinitionManager;
import system.engine.world.definition.property.api.PropertyDefinition;
import system.engine.world.execution.instance.enitty.api.EntityInstance;
import system.engine.world.execution.instance.enitty.impl.EntityInstanceImpl;
import system.engine.world.execution.instance.enitty.manager.api.EntityInstanceManager;
import system.engine.world.execution.instance.property.api.PropertyInstance;
import system.engine.world.execution.instance.property.impl.PropertyInstanceImpl;
import system.engine.world.grid.api.WorldGrid;


import java.util.*;
import java.util.stream.Collectors;

public class EntityInstanceManagerImpl implements EntityInstanceManager {
    private int count;   //all instances of all entities
    private final List<EntityInstance> instances;
    private final List<EntityInstance> instancesBeforeKill;
    private List<Float> entitiesConsistencyList;
    private final Map<String, Integer> entitiesPopulationAfterSimulationRunning;
    private Map<String,List<EntityInstance>> entityInstanceByEntityDef;
    private Map<String,List<EntityInstance>> StartEntityInstanceByEntityDef;
    private final WorldGrid worldGrid;
    private final EntityDefinitionManager entityDefinitionManager;
  
    private final Map<Integer,Integer> NumOfEntitiesLeftByTicks;



    public EntityInstanceManagerImpl(EntityDefinitionManager entityDefinitionManager, WorldGrid worldGrid) {
        this.entityDefinitionManager = entityDefinitionManager;
        count = 0;
        instances = new ArrayList<>();
        instancesBeforeKill = new ArrayList<>();
        this.entitiesConsistencyList=new ArrayList<>();
        entitiesPopulationAfterSimulationRunning = new HashMap<>();
        entityInstanceByEntityDef=new HashMap<>();
        NumOfEntitiesLeftByTicks=new HashMap<>();
        StartEntityInstanceByEntityDef=new HashMap<>();
        this.worldGrid=worldGrid;
        for (EntityDefinition entityDefinition: entityDefinitionManager.getDefinitions()){
            for(int i = 0; i<entityDefinition.getPopulation(); i++) {
                create(entityDefinition, this.worldGrid);
            }
            Integer population = new Integer(entityDefinition.getPopulation());
            entitiesPopulationAfterSimulationRunning.put(entityDefinition.getUniqueName(),
                    population);
        }
        instancesBeforeKill.addAll(instances);
        entityInstanceByEntityDef = instances.stream()
                .collect(Collectors.groupingBy(entityInstance -> entityInstance.getEntityDefinition().getUniqueName()));

        NumOfEntitiesLeftByTicks.put(0,instances.size());

        StartEntityInstanceByEntityDef=instances.stream()
                .collect(Collectors.groupingBy(entityInstance -> entityInstance.getEntityDefinition().getUniqueName()));
    }
    @Override
    public Float getConsistencyByEntityAndPropertyName(String entityName,String propertyName){
        float sum=0.0f;
        List<EntityInstance> entityInstanceList=StartEntityInstanceByEntityDef.get(entityName);
        if(entityInstanceList==null)
            return 0.0f;
        for(EntityInstance entityInstance:entityInstanceList){
            if(entityInstance!=null)
                sum+=entityInstance.getPropertyConsistencyByName(propertyName);
        }
        if(entityInstanceList.size()!=0)
            return sum/entityInstanceList.size();
        return 0.0f;
    }

    @Override
    public void setNumOfEntitiesLestByTicks(Integer currentTick, Integer numOfInstances){
        NumOfEntitiesLeftByTicks.put(currentTick,numOfInstances);
    }
    @Override
    public int getWorldPopulation(){
        int sum=0;
        for(int populationOfEntityDef:entitiesPopulationAfterSimulationRunning.values())
            sum+=populationOfEntityDef;
        return sum;
    }

    @Override
    public void createEntityInstanceFromScratch(EntityDefinition entityDefinitionToCreate,EntityInstance entityInstanceToKill) {
        EntityInstance newEntityInstance=create(entityDefinitionToCreate, this.worldGrid);
        newEntityInstance.getEntityDefinition().setPopulation(entitiesPopulationAfterSimulationRunning.get(newEntityInstance.getEntityDefinition().getUniqueName()) + 1);
        updateMembersAfterNewEntity(entityDefinitionToCreate);
        changeGridByKillEntity(newEntityInstance,entityInstanceToKill);
    }

    @Override
    public void createEntityInstanceFromDerived(EntityDefinition entityDefinitionToCreate, EntityInstance derivedEntityInstance) {
        count++;
        EntityInstance newEntityInstance = new EntityInstanceImpl(entityDefinitionToCreate, count, worldGrid);
        newEntityInstance.getEntityDefinition().setPopulation(entitiesPopulationAfterSimulationRunning.get(newEntityInstance.getEntityDefinition().getUniqueName()) + 1);
        changeGridByKillEntity(newEntityInstance,derivedEntityInstance);
        instances.add(newEntityInstance);

        boolean foundMatchProperty;
        for (PropertyDefinition propertyDefinition : entityDefinitionToCreate.getProps()) {
            foundMatchProperty=false;
            for(PropertyDefinition derivedPropertyDefinition:derivedEntityInstance.getEntityDefinition().getProps()){
                if(derivedPropertyDefinition.getUniqueName().equals(propertyDefinition.getUniqueName()) && derivedPropertyDefinition.getType().equals(propertyDefinition.getType())){
                    foundMatchProperty=true;
                    Object value=derivedEntityInstance.getPropertyByName(derivedPropertyDefinition.getUniqueName()).getValue();
                    PropertyInstance newPropertyInstance = new PropertyInstanceImpl(propertyDefinition, value);
                    newEntityInstance.addPropertyInstance(newPropertyInstance);
                }
            }
            if(!foundMatchProperty){
                Object value = propertyDefinition.generateValue();
                PropertyInstance newPropertyInstance = new PropertyInstanceImpl(propertyDefinition, value);
                newEntityInstance.addPropertyInstance(newPropertyInstance);
            }
        }
        updateMembersAfterNewEntity(entityDefinitionToCreate);

    }
    @Override
    public void updateMembersAfterNewEntity(EntityDefinition entityDefinitionToCreate){
        //entityDefinitionToCreate.setPopulation(entityDefinitionToCreate.getPopulation()+1);
        entitiesPopulationAfterSimulationRunning.put(entityDefinitionToCreate.getUniqueName(),
                entityDefinitionToCreate.getPopulation());
        instancesBeforeKill.add(instances.get(instances.size()-1));

        if (!entityInstanceByEntityDef.containsKey(entityDefinitionToCreate.getUniqueName())) {
            entityInstanceByEntityDef.put(entityDefinitionToCreate.getUniqueName(), new ArrayList<>());
        }
        List<EntityInstance> entityInstancesList = entityInstanceByEntityDef.get(entityDefinitionToCreate.getUniqueName());

        if (!StartEntityInstanceByEntityDef.containsKey(entityDefinitionToCreate.getUniqueName())) {
            StartEntityInstanceByEntityDef.put(entityDefinitionToCreate.getUniqueName(), new ArrayList<>());
        }
        List<EntityInstance> StartEntityInstancesList = StartEntityInstanceByEntityDef.get(entityDefinitionToCreate.getUniqueName());

        entityInstancesList.add(instances.get(instances.size()-1));
    }
    @Override
    public void changeGridByKillEntity(EntityInstance newEntityInstance, EntityInstance killEntityInstance){
        if(newEntityInstance.getRow() != null) {
            worldGrid.setPosition(newEntityInstance.getRow(), newEntityInstance.getColumns(), null);
        }
        worldGrid.setPosition(killEntityInstance.getRow(),killEntityInstance.getColumns(),newEntityInstance);
        newEntityInstance.setRow(killEntityInstance.getRow());
        newEntityInstance.setColumns(killEntityInstance.getColumns());
        killEntityInstance.setRow(null);
        killEntityInstance.setColumns(null);
    }
    @Override
    public int getEntityPopulationAfterRunning(String entityDefinitionName){
        return entitiesPopulationAfterSimulationRunning.get(entityDefinitionName);
    }

    @Override
    public Map<Integer, Integer> getNumOfEntitiesLeftByTicks() {
        return NumOfEntitiesLeftByTicks;
    }

    @Override
    public EntityInstance create(EntityDefinition entityDefinition, WorldGrid worldGrid) {

        count++;
        EntityInstance newEntityInstance = new EntityInstanceImpl(entityDefinition, count, worldGrid);
        instances.add(newEntityInstance);

        for (PropertyDefinition propertyDefinition : entityDefinition.getProps()) {
            Object value = propertyDefinition.generateValue();
            PropertyInstance newPropertyInstance = new PropertyInstanceImpl(propertyDefinition, value);
            newEntityInstance.addPropertyInstance(newPropertyInstance);
        }
        return newEntityInstance;
    }



    @Override
    public List<EntityInstance> getInstances() {
        return instances;
    }
    @Override
    public Map<String, List<EntityInstance>> getEntityInstanceByEntityDef() {
        return entityInstanceByEntityDef;
    }
    @Override
    public Map<String, List<EntityInstance>> getStartEntityInstanceByEntityDef() {
        return StartEntityInstanceByEntityDef;
    }

    @Override
    public List<EntityInstance> getInstancesBeforeKill() {
        for(EntityInstance entityInstance : instances){
            if(entityInstance != null){
                int id = entityInstance.getId();
                instancesBeforeKill.set(id -1, instances.get(id-1));
            }
        }
        return instancesBeforeKill;
    }

    @Override
    public void killEntity(int id) {
        String entityDefinitionName = instances.get(id - 1).getEntityDefinition().getUniqueName();
        int oldPopulation = entitiesPopulationAfterSimulationRunning.get(entityDefinitionName);

        //instances.get(id - 1).getEntityDefinition().setPopulation(oldPopulation-1);

        List<EntityInstance> entitiesInstanceByNameList=entityInstanceByEntityDef.get(entityDefinitionName);
        if(entitiesInstanceByNameList!=null)
            if (instances.get(id - 1).getRow() != null) {
                worldGrid.setPosition(instances.get(id - 1).getRow(),instances.get(id - 1).getColumns(),null);
            }
            entitiesInstanceByNameList.remove(instances.get(id-1));

        entitiesPopulationAfterSimulationRunning.put(entityDefinitionName, oldPopulation - 1);
        instancesBeforeKill.set(id -1, instances.get(id-1));
        instances.set(id-1, null);
    }


    @Override
    public Map<String, Integer> getEntitiesPopulationAfterSimulationRunning() {
        return entitiesPopulationAfterSimulationRunning;
    }

    @Override
    public EntityDefinitionManager getEntityDefinitionManager() {
        return entityDefinitionManager;
    }

    @Override
    public WorldGrid getWorldGrid() {
        return worldGrid;
    }


}
