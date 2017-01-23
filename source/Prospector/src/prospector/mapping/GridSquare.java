/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.mapping;

/**
 * Represents a single square of the map grid
 *
 */
public class GridSquare {
	
	private GridSquareEnum value;
	
	//Represents any relevant property of this gridsquare (i.e. faultline depth)
	private int property;
	private int[] location;
	
	public GridSquare(GridSquareEnum value)
	{
		this.value = value;
	}
	
	public void setLocation(int[] location)
	{
		this.location = location;
	}
	
	public int[] getLocation()
	{
		return location;
	}
	
	public void setProperty(int property)
	{
		this.property = property;
	}
	
	public GridSquareEnum getValue()
	{
		return value;
	}
	
	public int getProperty()
	{
		return property;
	}
}
