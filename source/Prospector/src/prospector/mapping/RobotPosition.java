/**
 * Prospector SFM
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.mapping;

/**
 * Represents robot position and heading
 */
public class RobotPosition
{
    int[] position = new int[2];
    double heading;

    public int[] getPosition()
    {
        return position;
    }

    public void setPosition(int[] position)
    {
        this.position = position;
    }

    public double getHeading()
    {
        return heading;
    }

    public void setHeading(double heading)
    {
        this.heading = heading;
    }
}
