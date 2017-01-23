/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.connection;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RMISampleProvider;
import lejos.remote.ev3.RemoteEV3;

public class ConnectionController {
	private RemoteEV3 ev3;
	private	RMIRegulatedMotor motorA;
	private	RMIRegulatedMotor motorB;	  
	private RMISampleProvider gyroSample;
    private RMISampleProvider ultSample;
    private RMISampleProvider colourSample;
    private RMIRegulatedMotor motorD;
    private List<ConnectionListener> connectionListeners = new ArrayList<>();
    private boolean connected = false;
    private String currentAddress = null;
    
    public void setAddress(String address)
    {
    	this.currentAddress = address;
    }
    
	public void connect()
	{
		if (currentAddress == null)
		{
			connected = false;
			connectionListeners.forEach(c->
			{
				c.connectionStateChanged(connected);	
			});
			return;
		}
		try {
			ev3 = new RemoteEV3(currentAddress);
			
	        //accessing motors
			if(motorA==null){
				motorA = ev3.createRegulatedMotor("A", 'L');
			}
			if(motorB==null){
				motorB = ev3.createRegulatedMotor("B", 'L');
			}
			if(motorD==null){
				motorD = ev3.createRegulatedMotor("D", 'M');
			}
			if(ultSample==null){
				ultSample = ev3.createSampleProvider("S4", "lejos.hardware.sensor.EV3UltrasonicSensor", "Distance");
			}
			if(gyroSample==null){
				gyroSample = ev3.createSampleProvider("S1", "lejos.hardware.sensor.EV3GyroSensor", "Angle");
			}
			if(colourSample==null){
				colourSample = ev3.createSampleProvider("S2", "lejos.hardware.sensor.EV3ColorSensor", "ColorID");//or ColorID
			}
		} catch (RuntimeException | RemoteException | MalformedURLException | NotBoundException e)
		{
			e.printStackTrace();
			closeAll();
			connected = false;
			connectionListeners.forEach(c->
			{
				c.connectionStateChanged(connected);	
			});
			return;
		}
		connected = true;
		connectionListeners.forEach(c->c.connectionStateChanged(connected));
	}
	
	public boolean getConnected()
	{
		return connected;
	}
	
	public void addConnectionListener(ConnectionListener connectionListener)
	{
		connectionListeners.add(connectionListener);
	}
	
	public void closeAll()
	{
		System.out.println("Shutting down RMI ports");
		try {
			if(motorA!=null){
				motorA.close();
			}
			if(motorB!=null){
				motorB.close();
			}
			if(motorD!=null){
				motorD.close();
			}
			if(ultSample!=null){
				ultSample.close();
			}
			if(gyroSample!=null){
				gyroSample.close();
			}
			if(colourSample!=null){
				colourSample.close();
			}
		} catch (RemoteException re) {
			// TODO Auto-generated catch block
			re.printStackTrace();
		}	
	}
	
	public RMIRegulatedMotor getMotorA()
	{
		return motorA;
	}
	
	public RMIRegulatedMotor getMotorB()
	{
		return motorB;
	}
	
	public RMIRegulatedMotor getMotorD()
	{
		return motorD;
	}
	
	public RMISampleProvider getUltSample()
	{
		return ultSample;
	}
	
	public RMISampleProvider getGyroSample()
	{
		return gyroSample;
	}
	
	public RMISampleProvider getColourSample()
	{
		return colourSample;
	}

}
