/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.sensors;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lejos.remote.ev3.RMISampleProvider;
import prospector.connection.ConnectionController;
import prospector.connection.ConnectionListener;

public class SensorController implements SensorControllerInterface, ConnectionListener {
	
	private RMISampleProvider colourSample;
	private RMISampleProvider ultSample;
	private RMISampleProvider gyroSample;
	private float distance;
	private List<DistanceListener> distanceListeners = new ArrayList<>();
	private List<GyroListener> angleListeners = new ArrayList<>();
	private List<ColourListener> colourListeners = new ArrayList<>();
	private ConnectionController connectionController;
	private float colourID;
	private ScheduledExecutorService distanceExecutor = Executors.newSingleThreadScheduledExecutor();
	private ScheduledExecutorService gyroExecutor = Executors.newSingleThreadScheduledExecutor();
	private ScheduledExecutorService colourExecutor = Executors.newSingleThreadScheduledExecutor();
	
	public SensorController(ConnectionController connectionController)
	{
		this.connectionController = connectionController;
	}

	@Override
	public void addColourListener(ColourListener colourListener) {
		colourListeners.add(colourListener);
	}

	@Override
	public void addDistanceListener(DistanceListener distanceListener) {
		distanceListeners.add(distanceListener);
	}
	
	public void addAngleListener(GyroListener distanceListener) {
		angleListeners.add(distanceListener);
	}

	@Override
	public void broadcastColourToListeners() {
		colourListeners.forEach(d->d.colourDetected(colourID));
	}

	@Override
	public void broadcastDistanceToListeners() {
		distanceListeners.forEach(d->d.distanceReceived(distance));
	}
	
	public void broadcastAngleToListeners() {
		angleListeners.forEach(d->d.angleReceived(angle));
	}
	
	public void start()
	{
		startDistanceThread();
		startGyroThread();	
		startColourThread();	
	}
	
	private float getDistance() {
		//Create an array of floats to hold the sample returned by the sensor
		float[] distance;
		try {
			distance = ultSample.fetchSample();	
			return distance[0];
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
	}
		
	public void startDistanceThread()
	{
		distanceExecutor.scheduleAtFixedRate(()->
		{
			distance = getDistance();
			broadcastDistanceToListeners();
		}, 1000, 50, TimeUnit.MILLISECONDS);
	}
	
	public float returnDistance(){
		return distance;
	}

	private float getAngle() {
		//Create an array of floats to hold the sample returned by the sensor
		float[] ang;
		try {
			ang = gyroSample.fetchSample();
		
			return ang[0];
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	
	private float angle;
	
	public void startGyroThread()
	{
		gyroExecutor.scheduleAtFixedRate(()->
		{
			angle = getAngle();
			broadcastAngleToListeners();
		}, 1050, 50, TimeUnit.MILLISECONDS);
	}

	public float returnAngle(){
		return angle;
	}
	
	private float getColourID() {
		//Create an array of floats to hold the sample returned by the sensor
		float[] col;
		try {
			col = colourSample.fetchSample();
			//System.out.println("getColorID: " + col[0]);
			return col[0];
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
		
	public void startColourThread()
	{
		colourExecutor.scheduleAtFixedRate(()->
		{
			colourID = getColourID();
			broadcastColourToListeners();
		}, 1050, 50, TimeUnit.MILLISECONDS);
	}
	
	public float returnColor(){
		return colourID;
	}
	
	public void shutdown()
	{
		distanceExecutor.shutdown();
		gyroExecutor.shutdown();
		colourExecutor.shutdown();
	}

	@Override
	public void connectionStateChanged(boolean connected) {
		if (connected)
		{
			ultSample = connectionController.getUltSample();
			gyroSample = connectionController.getGyroSample();
			colourSample = connectionController.getColourSample();
			start();
		}
		
	}
	

}
