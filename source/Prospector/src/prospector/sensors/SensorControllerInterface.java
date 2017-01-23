/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.sensors;

public interface SensorControllerInterface {
	/**
	 * Adds a ColourListener to the list
	 * @param colourListener The listener to add
	 */
	void addColourListener(ColourListener colourListener);
	
	/**
	 * Adds a DistanceListener to the list
	 * @param distanceListener The listener to add
	 */
	void addDistanceListener(DistanceListener distanceListener);
	
	/**
	 * Informs all colour listeners of the current colour reading
	 * Can be called any time, but on a timer is recommended
	 */
    void broadcastColourToListeners();
    
    /**
	 * Informs all distance listeners of the current distance reading
	 * Can be called any time, but on a timer is recommended
	 */
    void broadcastDistanceToListeners();

}
