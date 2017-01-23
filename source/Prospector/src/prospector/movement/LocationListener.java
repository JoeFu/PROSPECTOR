/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.movement;

public interface LocationListener {
	/**
	 * Informs the listener of the current location
	 * @param x Distance from the west boundary
	 * @param y Distance from the south boundary
	 * @param angle Angle +CCW from x axis
	 */
	void locationReceived(double x, double y, float angle, float estimatedAngle, int UltraDirec, int currentDestinationIndex,
			Integer[] dest, String text);
}
