/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.mapping;

import java.util.List;

public class MapDataModel {
	private GridSquare[][] mapGrid;
	//private GridSquare[][] moveGrid;
	private RobotPosition robotPosition = new RobotPosition();
	
	public MapDataModel(int width, int height)
	{
		mapGrid = new GridSquare[width][height]; 
		//moveGrid = new GridSquare[width/2][height/2];
		clear();
	}
	
	/**
	 * Imports a list of grid squares to the data model
	 * @param squares
	 */
	public void importGridPoints(List<GridSquare> squares)
	{
		clear();
		for (GridSquare square : squares)
		{
			int[] location = square.getLocation();
			//Overwrite only if unknown
			if (getGridSquare(location).getValue() == GridSquareEnum.UNKNOWN || getGridSquare(location).getValue() == GridSquareEnum.BLANK)
			{
				setGridSquare(location, square);
			}
		}
	}

	/**
	 * Returns grid height
	 * @return
	 */
	public int getWidth()
	{
		return mapGrid.length;
	}
	
	/**
	 * Returns grid width
	 * @return
	 */
	public int getHeight()
	{
		return mapGrid[0].length;
	}
	
	/**
	 * Checks if a point is within the grid
	 * @param location
	 * @return
	 */
	public boolean validPoint(int[] location)
	{
		return location != null && location.length == 2 
				&& location[0] >= 0 && location[0] < mapGrid.length
				&& location[1] >= 0 && location[1] < mapGrid[0].length;				
	}
	
	/**
	 * Returns the grid square at the specified location
	 * @param location
	 * @return
	 */
	public GridSquare getGridSquare(int[] location)
	{
		if (validPoint(location))
		{
			return mapGrid[location[0]][location[1]];
		}  
		return new GridSquare(GridSquareEnum.UNKNOWN);
	}
	
	/**
	 * Sets the specified grid square at the location provided
	 * @param location
	 * @param value
	 */
	public void setGridSquare(int[] location, GridSquare value)
	{
		if (validPoint(location))
		{
			value.setLocation(location);
			mapGrid[location[0]][location[1]] = value;
		}  
	}
	
	/**
	 * Clears the data model, sets all grid squares to unknown
	 */
	public void clear()
	{
		for (int i = 0; i < mapGrid.length; i++)
		{
			for (int j = 0; j < mapGrid[0].length; j++)
			{
				int[] location = {i,j};
				setGridSquare(location, new GridSquare(GridSquareEnum.UNKNOWN));
			}
		}
	}
	
	/**
	 * Sets ths position of the robot
	 * @param position
	 */
	public void setRobotPosition(RobotPosition position)
	{
		this.robotPosition = position;
	}
	
	/**
	 * Returns the robot position
	 * @return
	 */
	public RobotPosition getRobotPosition()
	{
		return robotPosition;
	}

	public int getDeepest()
	{
		int deepest = -1;
		for (int i = 0; i < getWidth(); i++)
		{
			for (int j = 0; j < getHeight(); j++)
			{
				int []location = {i,j};
				GridSquare gridSquare = getGridSquare(location);
				if (gridSquare.getValue() == GridSquareEnum.FAULTLINE)
				{
					deepest = Math.max(deepest, gridSquare.getProperty());
				}
			}
		}
		return deepest;
	}
}
