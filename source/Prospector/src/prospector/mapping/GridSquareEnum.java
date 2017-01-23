/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.mapping;

/**
 * Represents a single square of the map grid
 * @author Yann
 *
 */
public enum GridSquareEnum {
	UNKNOWN(0),
	BLANK(1),
	FAULTLINE(2),
	OBSTACLE(3),
	BOUNDARY(4),
	NGZ(5),
	EXTRACTION(6),
	ROBOT(7);
	
	private int value;
	
	GridSquareEnum(int value)
	{
		this.value = value;
	}
	
	
	public int getValue(){
		return value;
	}
}
