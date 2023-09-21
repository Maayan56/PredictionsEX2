package system.engine.world.grid.impl;

import system.engine.world.execution.instance.enitty.api.EntityInstance;
import system.engine.world.execution.instance.enitty.location.api.EntityLocationInWorld;
import system.engine.world.grid.api.WorldGrid;

public final class WorldGridImpl implements WorldGrid {
    private final Integer gridRows;
    private final Integer gridColumns;
    private final EntityInstance[][] worldGrid;
    private int numOfLocationsLeft;


    public WorldGridImpl(Integer gridRows, Integer gridColumns) {
        this.gridRows = gridRows;
        this.gridColumns = gridColumns;
        this.numOfLocationsLeft = gridRows * gridColumns;

        this.worldGrid = new EntityInstance[gridRows][gridColumns];
        for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridColumns; j++) {
                worldGrid[i][j] = null;
            }
        }
    }

    @Override
    public Integer getGridRows() {
        return gridRows;
    }
    @Override
    public Integer getGridColumns() {
        return gridColumns;
    }
    @Override
    public void setPosition(Integer row, Integer column, EntityInstance entityInstance){
        worldGrid[row][column] = entityInstance;
        if(entityInstance == null){
            numOfLocationsLeft++;
        } else {
            numOfLocationsLeft--;
        }
    }
    @Override
    public Boolean isPositionAvailable(Integer row, Integer column){
        return worldGrid[row][column] == null;
    }
    @Override
    public Boolean isPositionAvailableForMovingEntity(EntityLocationInWorld currentEntityLocationInWorld,
                                                      EntityLocationInWorld desirableEntityLocationInWorld){
        //column change
        if(currentEntityLocationInWorld.getRow().equals(desirableEntityLocationInWorld.getRow())){
            if(desirableEntityLocationInWorld.getColumn().equals(-1) ){
                if(isPositionAvailable(desirableEntityLocationInWorld.getRow(), gridColumns-1)){
                    desirableEntityLocationInWorld.setColumn(gridColumns-1);
                    return true;
                }
                return false;
            }
            else if (desirableEntityLocationInWorld.getColumn().equals(gridColumns)){
                if(isPositionAvailable(desirableEntityLocationInWorld.getRow(), 0)){
                    desirableEntityLocationInWorld.setColumn(0);
                    return true;
                }
                return false;
            }
            else{
                return isPositionAvailable(desirableEntityLocationInWorld.getRow(), desirableEntityLocationInWorld.getColumn());
            }
        }
        //row change
        else{
            if(desirableEntityLocationInWorld.getRow().equals(-1) ){
                if(isPositionAvailable(gridRows-1, desirableEntityLocationInWorld.getColumn())){
                    desirableEntityLocationInWorld.setRow(gridRows-1);
                    return true;
                }
                return false;
            }
            else if (desirableEntityLocationInWorld.getRow().equals(gridRows)){
                if(isPositionAvailable(0, desirableEntityLocationInWorld.getColumn())){
                    desirableEntityLocationInWorld.setRow(0);
                    return true;
                }
                return false;
            }
            else{
                return isPositionAvailable(desirableEntityLocationInWorld.getRow(), desirableEntityLocationInWorld.getColumn());
            }
        }
    }

    @Override
    public EntityInstance isThereSecondEntityCloseEnough(EntityInstance primaryEntityInstance, String targetEntityName, Float of){
        int centerX = primaryEntityInstance.getColumns();
        int centerY = primaryEntityInstance.getRow();
        Float radius = of;
        EntityInstance entityInstance =null;

        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridColumns; col++) {
                if(row == centerY & col == centerX){
                    continue;
                }
                // Calculate the distance from the current square to the center point.
                int distance = (int) Math.sqrt((row - centerY) * (row - centerY) + (col - centerX) * (col - centerX));

                // Check if the square is at the specified distance from the center point.
                if (Math.abs(distance - radius) < 0.5 || (isAtEdge(centerY, centerX) &&
                        isAtEdge(row, col) && isInCircle(centerY, centerX,row, col))) {
                    if(worldGrid[row][col] != null && worldGrid[row][col].getEntityDefinition().getUniqueName().equals(targetEntityName)){
                        entityInstance = worldGrid[row][col];
                        return entityInstance;
                    }
                }
            }
        }
        return entityInstance;
    }

    private boolean isInCircle(int centerY, int centerX, int row, int col){
        if(isOnCorner(centerY, centerX)){
            return ((centerY == row && isOnCorner(row, col)) || (centerX == col && isOnCorner(row, col)) ||  //same row and corner OR same col and corner
                    row-1 == centerY || row+1 == centerY || col-1 == centerX || col+1 == centerX);
        }
        else if(centerY == 0){
            return ((centerX == col && row == gridRows - 1) ||
                    col - 1 == centerX || col + 1 == centerX );
        }
        else if(centerY == gridRows-1){
            return ((centerX == col && row == 0) ||
                    col - 1 == centerX || col + 1 == centerX );
        }
        else if(centerX == 0){
            return ((centerY == row && col == gridColumns - 1) ||
                    row - 1 == centerY || row + 1 == centerY);
        }
        else if(centerX == gridColumns-1){
            return ((centerY == row && col == 0) ||
                    row - 1 == centerY || row + 1 == centerY );
        }
        return false;
    }

    private boolean isOnCorner(int row, int col){
        return ((row == 0 && col == 0) || (row == 0 && col == gridColumns - 1) ||
                (row == gridRows-1 && col == 0) || (row == gridRows-1 && col == gridColumns - 1));
    }

        private boolean isAtEdge(int row, int col) {
            return row == 0 || row == gridRows - 1 || col == 0 || col == gridColumns - 1;
        }

        @Override
    public int getNumOfLocationsLeft() {
        return numOfLocationsLeft;
    }
}
