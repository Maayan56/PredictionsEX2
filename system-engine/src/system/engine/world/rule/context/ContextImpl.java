package system.engine.world.rule.context;

import system.engine.world.execution.instance.enitty.api.EntityInstance;
import system.engine.world.execution.instance.environment.api.EnvVariablesInstanceManager;
import system.engine.world.execution.instance.property.api.PropertyInstance;

import java.util.List;

public class ContextImpl implements Context {

    private final EntityInstance primaryEntityInstance;
   // private final EntityInstance secondEntityInstance;
    private final EnvVariablesInstanceManager envVariablesInstanceManager;
    private List<EntityInstance> entitiesToKill;

    public ContextImpl(EntityInstance primaryEntityInstance,
                       EnvVariablesInstanceManager envVariablesInstanceManager,
                       List<EntityInstance> entitiesToKill) {
        this.primaryEntityInstance = primaryEntityInstance;
        //this.secondEntityInstance = secondEntityInstance;
        this.envVariablesInstanceManager  =  envVariablesInstanceManager;
        this.entitiesToKill = entitiesToKill;
    }
    /*public ContextImpl(EntityInstance primaryEntityInstance, EntityInstance secondEntityInstance,
                       EnvVariablesInstanceManager envVariablesInstanceManager,
                       List<EntityInstance> entitiesToKill) {
        this.primaryEntityInstance = primaryEntityInstance;
        this.secondEntityInstance = secondEntityInstance;
        this.envVariablesInstanceManager  =  envVariablesInstanceManager;
        this.entitiesToKill = entitiesToKill;
    }*/

    @Override
    public EntityInstance getPrimaryEntityInstance() {
        return primaryEntityInstance;
    }

    @Override
    public void removeEntity(EntityInstance entityInstance) {
        entitiesToKill.add(entityInstance);
    }

    @Override
    public PropertyInstance getEnvironmentVariable(String name) {
        return envVariablesInstanceManager.getEnvVar(name);
    }

  /*  @Override
    public EntityInstance getSecondEntityInstance() {
        return secondEntityInstance;
    }*/
}