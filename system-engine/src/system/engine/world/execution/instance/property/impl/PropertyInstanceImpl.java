package system.engine.world.execution.instance.property.impl;

import system.engine.world.definition.property.api.PropertyDefinition;
import system.engine.world.execution.instance.property.api.PropertyInstance;

import java.util.ArrayList;
import java.util.List;

public class PropertyInstanceImpl implements PropertyInstance {

    private PropertyDefinition propertyDefinition;
    private Object value;
    private Integer lastTickNumberOfValueUpdate = 0;
    private List<Integer> ticksListOfPropertyValueUpdate;

    public PropertyInstanceImpl(PropertyDefinition propertyDefinition, Object value) {
        this.propertyDefinition = propertyDefinition;
        this.value = value;
        this.ticksListOfPropertyValueUpdate=new ArrayList<>();
    }

    @Override
    public Float calculatePropertyAverage(){
        if(ticksListOfPropertyValueUpdate.isEmpty())
            return 0.0f;
        else if(ticksListOfPropertyValueUpdate.size()==1)
            return (float) (ticksListOfPropertyValueUpdate.get(0));
        else{
            int sumDifferences = 0;
            for (int i = 1; i < ticksListOfPropertyValueUpdate.size(); i++) {
                int difference = ticksListOfPropertyValueUpdate.get(i) - ticksListOfPropertyValueUpdate.get(i - 1);
                sumDifferences += difference;
            }
            Float averageDifference = (float) (sumDifferences / (ticksListOfPropertyValueUpdate.size() - 1));
            return averageDifference;
        }
    }

    @Override
    public PropertyDefinition getPropertyDefinition() {
        return propertyDefinition;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object val) {
        this.value = val;
    }
    @Override
    public void setLastTickNumberOfValueUpdate(Integer lastTickNumberOfValueUpdate, Object newValue) {
        if(! value.equals(newValue)){
            if(this.lastTickNumberOfValueUpdate!=lastTickNumberOfValueUpdate)
            {
                this.lastTickNumberOfValueUpdate = lastTickNumberOfValueUpdate;
                ticksListOfPropertyValueUpdate.add(lastTickNumberOfValueUpdate+1);
            }
        }

    }
    @Override
    public Integer getLastTickNumberOfValueUpdate(){
        return lastTickNumberOfValueUpdate;}
}
