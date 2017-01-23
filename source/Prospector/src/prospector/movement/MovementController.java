/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.movement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lejos.remote.ev3.RMIRegulatedMotor;
import prospector.connection.ConnectionController;
import prospector.connection.ConnectionListener;
import prospector.mapping.GridSquareEnum;
import prospector.mapping.MapDataModel;
import prospector.mapping.MapListener;
import prospector.mapping.MappingController;
import prospector.sensors.ColourListener;
import prospector.sensors.DistanceListener;
import prospector.sensors.GyroListener;

import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MovementController implements DistanceListener, GyroListener,
		ConnectionListener, ColourListener, MapListener {

	private RMIRegulatedMotor motorA;
	private RMIRegulatedMotor motorB;
	private RMIRegulatedMotor motorD;

	private int turnright = 0;
	private int turnleft = 0;
	private float direction = 0;
	private int dispermove = 40;// 4cm per move 100
	private float locationAngle = 0; // positive counterclockwise
	private List<LocationListener> locationListeners = new ArrayList<>();
	private float distance;// distance from ultra reading
	private float angle; // Angle from gyro reading
	private float colourID;// colour from colour reading
	private Integer[] destination = new Integer[2];// the
	private ConnectionController connectionController;
	private static Navigation navigationController;
	private static MappingController mappingController;
    // Gird size =4.0
	public static final double MOVE_DISTANCE_CM = 2.0;
	private int UltraDirec = 4;

	// data for Gui
	private static final int NORTH = 1, EAST = 2, SOUTH = 3, WEST = 4;// the direction for
															// the robot
	private int measuredAngle = 1; // Actual angle (based on gyro and
										// estimate)
	private float angleOffset = 0; // Used to correct gyro error from user input
	private double x = 0; // positive east
	private double y = 0; // positive north
	private double north = 0;
	private double east = 0;
	private double northOffset = 0;
	private double eastOffset = 0;
	private int right = 1, left = -1, forward = 2, backward = -2;
	private int movedistance = 1;
	private boolean running = false;
	private boolean stopped;
	private MoveMode currentmode = MoveMode.MANUAL;
	private AUTOMode automode = AUTOMode.SURVEY;
	private MapDataModel mapDataModel;
	private List<Integer[]> destinationList = new ArrayList<>();
	private int currentDestinationIndex = 0;
	private ScheduledExecutorService MovementExecutor = Executors.newSingleThreadScheduledExecutor();
	private boolean mapRenewed = false;	
	private String text;
	public enum MoveMode
	{
		AUTO,
		MANUAL,
		MOVETOPOINT;
	}
	
	public void setMoveMode(MoveMode moveMode)
	{
		currentDestinationIndex = 0;
		this.currentmode = moveMode;
		switch(currentmode){
		case AUTO:
			automode = AUTOMode.SURVEY;
			text = "AUTO mode";
			break;
		case MANUAL:
			text = "MANUAL mode";
			break;
		case MOVETOPOINT:
			text = "MOVETOPOINT mode";
			break;
		}
		try {
			stop();
		} 
		catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startSurvey(Integer[] dest)
	{
		stopped = false;
		if (currentmode == MoveMode.MOVETOPOINT)
		{
			destination = dest;
			surveyStart();
		}
		else if (currentmode == MoveMode.AUTO)
		{
			createDestinationList();
			/************
			 * needn't to set currentDestinationIndex to 0
			 * otherwise if we stop survey and restart it, it will go to
			 * {0,0} again.
			 * 
			 * maybe we should add a reset() methods for currentDestinationIndex
			 * when we want to start survey from scratch?
			 */
			//currentDestinationIndex = 0;
			goToDestination();
		}
	}
	
	public enum AUTOMode
	{
		SURVEY,
		SURVEYREMAIN,
		SURVEYFAULTLINE;
	}
	
	private void goToDestination()
	{
		if (currentDestinationIndex < destinationList.size())
		{
			destination = destinationList.get(currentDestinationIndex);
			System.out.println("destination: " + destination[0]+ " " + destination[1]);
			mapRenewed = false;
			surveyStart();
		}
		else if(currentDestinationIndex == destinationList.size())
		{
			switch(automode){
			case SURVEY:
				text = "AUTO mode: Survey unexplored area.";
				automode = AUTOMode.SURVEYREMAIN;
				surveyUnexploredArea();
				break;
			case SURVEYREMAIN:
				text = "AUTO mode: Survey FaultLine ";
				automode = AUTOMode.SURVEYFAULTLINE;
				System.out.println("Survey FaultLine");
				surveyFaultLine();
				break;
			default:
				break;		
			}

			

		}else{
			switch(currentmode){
			case AUTO:
				text =  "AUTO mode: Finish Survey!";
			case MOVETOPOINT:
				text =  "MOVETOPOINT mode: Reach Destination!";
			default:
				break;
			}
			
			System.out.println("Finished!");
		}
	}
	
	private void GetUnexploredArea(){
		for(int i=0;i<mapDataModel.getHeight();i++){
			for(int j=0;j<mapDataModel.getWidth();j++){
				Integer[] location = {j,i};
				int[] location1 = {j,i};
				if(mapDataModel.getGridSquare(location1).getValue() == GridSquareEnum.UNKNOWN){
					destinationList.add(location);
				}
			}
		}
	}
	
	private void surveyUnexploredArea(){
		destinationList.clear();
		GetUnexploredArea();
		surveyStart();
	}
	
	private void getFaultLine(){
		int depth = mappingController.getDeepest();
		for(int i=0;i<mapDataModel.getHeight();i++){
			for(int j=0;j<mapDataModel.getWidth();j++){
				Integer[] location = {j,i};
				int[] location1 = {j,i};
				if(mapDataModel.getGridSquare(location1).getProperty() == depth){
					destinationList.add(location);
				}
			}
		}
	}
	
	private void surveyFaultLine()
	{
		destinationList.clear();
		getFaultLine();
		surveyStart();
	}
	
	private void destinationReached()
	{
		if(hasPath){
			text = "AUTO mode: reached! Move to next destination.";
		}else{
			switch(currentmode){
			case AUTO:
				text = "AUTO mode: Can't find out a path. Move to next destination.";
				break;
			case MOVETOPOINT:
				text = "MOVETOPOINT mode: Can't find out a path. Move to next destination.";
				break;
			default:
				break;
			}
			
		}
		System.out.println("reached! now index is: " + currentDestinationIndex);
		
		currentDestinationIndex++;
		goToDestination();
	}

	public void getDestinationList(int Width, int Height){
		int xstart = 0, ystart = 0;
		int xend = Width, yend = Height;
		Integer[] dest = {xstart,ystart};
		destinationList.add(dest);
		while(xstart<=xend && ystart<=yend){
			if(ystart==yend || xstart==xend){
				Integer[] dest1 = {xstart,yend};
				destinationList.add(dest1);
				break;
			}
			Integer[] dest1 = {xstart,yend};
			Integer[] dest2 = {xend,yend};
			Integer[] dest3 = {xend,ystart};
			destinationList.add(dest1);
			destinationList.add(dest2);
			destinationList.add(dest3);
			xstart++;
			if(xstart!=xend){
				Integer[] dest4 = {xstart,ystart};
				destinationList.add(dest4);
			}
			ystart++;
			yend--;
			xend--;
		}
	}
	
	
	public void reCalculateDestinationList(int Width, int Height){
		//Survey.interrupt();
		switch(currentmode){
		case AUTO:
			mapRenewed = true;
			destinationList.clear();
			getDestinationList(Width, Height);
			//currentDestinationIndex--;
		       try {
		            Thread.sleep(4000);
		        } catch (InterruptedException e) {
		            e.printStackTrace(); 
		        }
			//go back inside the boundary
			backward();
			backward();
			currentDestinationIndex++;
			//System.out.println("Get New Destinaition: " + destinationList.get(currentDestinationIndex)[0] + " " + destinationList.get(currentDestinationIndex)[1]);
			goToDestination();
			break;
		case MANUAL:
			//do nothing
			break;
		case MOVETOPOINT:
			//do nothing
			break;
		}
		
	}
	
	private void createDestinationList()
	{
		//initialize the start position as (0,0)
		//then each time we goto topleft->topright->bottomright->one grid right to bottomleft to scan the outermost grid
		//keep doing these steps until the xstart>xend || ystart>yend
		destinationList = new ArrayList<>();
		int xstart = 0, ystart = 0;
		int Width = mapDataModel.getWidth()-1, Height = mapDataModel.getHeight()-1;
		getDestinationList(Width,Height);
	}

	// connect movement controller with Navigation
	public void connect(Navigation n) {
		this.navigationController = n;
	}

	public void connect(MappingController n) {
		this.mappingController = n;
	}

	public void nextstate(int turn) {
		if (turn == left) {
			// System.out.println("left: " + this.measuredAngle);
			if (measuredAngle == NORTH) {
				measuredAngle = WEST;
			} else if (measuredAngle == WEST) {
				measuredAngle = SOUTH;
			} else if (measuredAngle == SOUTH) {
				measuredAngle = EAST;
			} else if (measuredAngle == EAST) {
				measuredAngle = NORTH;
			}
			// System.out.println("After left: " + this.measuredAngle);
		} else if (turn == right) {
			if (measuredAngle == NORTH) {
				measuredAngle = EAST;
			} else if (measuredAngle == EAST) {
				measuredAngle = SOUTH;
			} else if (measuredAngle == SOUTH) {
				measuredAngle = WEST;
			} else if (measuredAngle == WEST) {
				measuredAngle = NORTH;
			}
		} else if (turn == forward) {
			if (measuredAngle == NORTH) {
				y += movedistance;
			} else if (measuredAngle == EAST) {
				x += movedistance;
			} else if (measuredAngle == SOUTH) {
				y -= movedistance;
			} else if (measuredAngle == WEST) {
				x -= movedistance;
			}
		} else if (turn == backward) {
			if (measuredAngle == NORTH) {
				y -= movedistance;
			} else if (measuredAngle == EAST) {
				x -= movedistance;
			} else if (measuredAngle == SOUTH) {
				y += movedistance;
			} else if (measuredAngle == WEST) {
				x += movedistance;
			}
		}
		//broadcastLocationToListeners(0);
	}

	public MovementController(ConnectionController connectionController) {
		this.connectionController = connectionController;
		this.motorA = connectionController.getMotorA();
		this.motorB = connectionController.getMotorB();
		this.motorD = connectionController.getMotorD();
	}

	// to help the robot correct it's direction when there is a error more than
	// +-3 degrees
	// and help the robot to go back last step if there is any emergency stop
	public void recorrect() {
		if (!connected || stopped) {
			return;
		}

		try {
			// check the direction
			float correctedAngle = getCorrectedAngle();
			if (correctedAngle > (direction + 3)) {
				// turn right
				System.out.println("fix from " + angle + "to:" + direction);
				motorA.setSpeed((int) (motorA.getMaxSpeed() * 0.1));
				motorB.setSpeed((int) (motorB.getMaxSpeed() * 0.1));
				motorB.forward();
				motorA.backward();
				while (correctedAngle > direction && !stopped) {
					// TODO: fix bad blocking code
					try {
						Thread.sleep(5);
						correctedAngle = getCorrectedAngle();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // waiting for refresh distance value
				}
				motorB.stop(true);
				motorA.stop(true);
			} else if (correctedAngle < (direction - 3)) {
				System.out.println("fix from " + angle + "to:" + direction);
				motorA.setSpeed((int) (motorA.getMaxSpeed() * 0.07));
				motorB.setSpeed((int) (motorB.getMaxSpeed() * 0.07));
				motorA.forward();
				motorB.backward();
				while (correctedAngle < direction && !stopped) {
					// TODO: fix bad blocking code
					try {
						Thread.sleep(5);
						correctedAngle = getCorrectedAngle();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // waiting for refresh distance value
				}
				motorA.stop(true);
				motorB.stop(true);
			}
		} catch (RemoteException r) {
			r.printStackTrace();
		}
	}

	/*****************
	 * Survey: Step1: Only when the robot move forward and backward we need to
	 * scan around to update the map.
	 * 
	 * Step2: When the map is updated, Navigation wil generate a new path to
	 * tell movementController how to move by returning a List<String>.
	 * 
	 * Step3: movementController will use SurveyNextStep to move then go back to
	 * Step1.
	 */
	public void scan() {
		try {	
			//forward();
			motorD.rotate(90);// turn Ultrasonic Sensor to scan left
			UltraDirec = 1;
			Thread.sleep(300);// waiting for refresh distance value
			 // write map here (Ultrasonic Sensor)
			motorD.rotate(-90);// turn Ultrasonic Sensor to scan front
			UltraDirec = 0;
			Thread.sleep(300);// waiting for refresh distance value
			 // write map here (Ultrasonic Sensor)
			motorD.rotate(-90);// turn Ultrasonic Sensor to scan right
			UltraDirec = -1;
			Thread.sleep(300);
			 // write map here (Ultrasonic Sensor)
			UltraDirec = 3;
			motorD.rotate(90);// reset Ultrasonic Sensor to front
			
			 // write map here (color sensor)
			Thread.sleep(300);
			 // update map here one time every grid
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void commandReceived() {
		stopped = false;
	}

	public void forwardCommand() {
		switch(currentmode){
		case MANUAL:
			if(colourID == lejos.robotics.Color.BLACK){
				text = "MANUAL mode: Boundary!";
				return;
			}else{
					text = "MANUAL mode";
			}
			break;
		default:
			break;
		}
		commandReceived();
		if(!running){
			running = true;
			forward();
			running = false;
		}		
	}

	private double perc = 0.0;

	private void forward() {
		if (!connected || stopped) {
			return;
		}
		try {
			// check the direction
			
			recorrect();
			motorA.setSpeed((int) (motorA.getMaxSpeed() * 0.1));//0.1
			motorB.setSpeed((int) (motorB.getMaxSpeed() * 0.1));//0.1
			double tacho = motorA.getTachoCount();
			perc = 0.0;
			motorA.rotate(dispermove, true);
			motorB.rotate(dispermove, true);
			while (motorA.isMoving()) {
				double diff = motorA.getTachoCount()-tacho;
				perc = diff/dispermove;
				switch(currentmode){
				case MANUAL:
					if(colourID == lejos.robotics.Color.BLACK){
						text = "MANUAL mode: Boundary!";
						motorA.stop(true);
						motorB.stop(true);
					}
					break;
				default:
					break;
				}

			}
			motorA.waitComplete();
			motorB.waitComplete();
			perc = 0.0;
		} catch (RemoteException r) {
			r.printStackTrace();
		}
		nextstate(forward);
		scan();
	}

	public void backwardCommand() {
		switch(currentmode){
		case MANUAL:
			if(colourID == lejos.robotics.Color.BLACK){
				text = "MANUAL mode: Boundary!";
				return;
			}else{
				text = "MANUAL mode";
			}
			break;
		default:
			break;
		}
		commandReceived();
		if(!running){
			running = true;
			backward();
			running = false;
		}

	}

	private void backward() {
		if (!connected || stopped) {
			return;
		}
		try {
			motorA.setSpeed((int) (motorA.getMaxSpeed() * 0.1));
			motorB.setSpeed((int) (motorB.getMaxSpeed() * 0.1));
			perc = 0.0;
			double tacho = motorA.getTachoCount();
			motorB.rotate(-dispermove, true);
			motorA.rotate(-dispermove, true);
			while (motorA.isMoving()) {
				double diff = motorA.getTachoCount()-tacho;
				perc = diff/dispermove;
				switch(currentmode){
				case MANUAL:
					if(colourID == lejos.robotics.Color.BLACK){
						text = "MANUAL mode: Boundary!";
						motorA.stop(true);
						motorB.stop(true);
					}
					break;
				default:
					break;
				}
			}
			motorA.waitComplete();
			motorB.waitComplete();
			perc = 0.0;
			//scan();
		} catch (RemoteException r) {
			r.printStackTrace();
		}
		nextstate(backward);
	}

	public void leftCommand() {
		commandReceived();
		if(!running){
			running = true;
			left();
			running = false;
		}

	}

	private void left() {
		if (!connected || stopped) {
			return;
		}
		try {

			motorA.setSpeed((int) (motorA.getMaxSpeed() * 0.07));
			motorB.setSpeed((int) (motorB.getMaxSpeed() * 0.07));
			motorA.rotate(192, true);// test result
			motorB.rotate(-192, true);
			motorA.waitComplete();
			motorB.waitComplete();
			turnleft += 1;
			direction += 90;
		} catch (RemoteException r) {
			r.printStackTrace();
		}
		nextstate(left);
	}

	public void rightCommand() {
		commandReceived();
		if(!running){
			running = true;
			right();
			running = false;
		}

	}

	private void right() {
		if (!connected || stopped) {
			return;
		}
		try {
			motorA.setSpeed((int) (motorA.getMaxSpeed() * 0.07));
			motorB.setSpeed((int) (motorB.getMaxSpeed() * 0.07));
			motorB.rotate(192, true);// test result
			motorA.rotate(-192, true);
			motorA.waitComplete();
			motorB.waitComplete();
			turnright += 1;
			direction -= 90;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nextstate(right);
		//broadcastLocationToListeners(0);
	}

	public void stop() throws RemoteException {
		stopped = true;
		if (!connected) {
			return;
		}
		motorA.stop(true);
		motorB.stop(true);
	}

	public void addLocationListener(LocationListener locationListener) {
		locationListeners.add(locationListener);
	}

	public void broadcastLocationToListeners(double percent) {
		//System.out.println("CurrentLoc:" + x + "/" + y);
		east = x * MOVE_DISTANCE_CM - eastOffset;
		north = y * MOVE_DISTANCE_CM - northOffset;
		double east_ = east;
		double north_ = north;
		
		switch (measuredAngle)
		{
			case NORTH:
				north_ += MOVE_DISTANCE_CM*percent;
				break;
			case SOUTH:
				north_ -= MOVE_DISTANCE_CM*percent;
				break;
			case WEST:
				east_ -= MOVE_DISTANCE_CM*percent;
				break;
			case EAST:
				east_ += MOVE_DISTANCE_CM*percent;
				break;
		}

		for (LocationListener locationListener : locationListeners) {
			locationListener.locationReceived(east_, north_, angle, direction,UltraDirec, currentDestinationIndex,destination,text);// angle
		}
	}
	
	 public void start()
	 {
	 	startLocationThread();
	 }

	 private ScheduledExecutorService locationExecutor = Executors.newSingleThreadScheduledExecutor();

	 public void startLocationThread()
	 {
		 locationExecutor.scheduleAtFixedRate(()->
		 {
			broadcastLocationToListeners(perc);
		 }, 5000, 50, TimeUnit.MILLISECONDS);
	 }

	@Override
	public void distanceReceived(float distance) {
		this.distance = distance;

	}

	@Override
	public void colourDetected(float color) {
		// TODO Auto-generated method stub
		this.colourID = color;
		// System.out.println("thiscolour: " + this.colourID + " color: " +
		// color);
	}

	@Override
	public void angleReceived(float angle) {
		this.angle = angle;
	}

	public void setLocation(double x, double y, float newAngle) {
		this.northOffset += y;
		this.eastOffset += x;
		this.angleOffset = newAngle;
	}

	private float getCorrectedAngle() {
		return angle - angleOffset;
	}

	private boolean connected;


	@Override
	public void connectionStateChanged(boolean connected) {
		this.connected = connected;
		System.out.println("Movement:" + connected);
		if (connected) {
			this.motorA = connectionController.getMotorA();
			this.motorB = connectionController.getMotorB();
			this.motorD = connectionController.getMotorD();
			//startMovementThread();
			try {
				motorA.setSpeed((int) (motorA.getMaxSpeed() * 0.8));
				motorB.setSpeed((int) (motorB.getMaxSpeed() * 0.8));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				this.connected = false;
			}
		}

	}


	@Override
	public void mapDataReceived(MapDataModel mapDataModel) {
		this.mapDataModel = mapDataModel;
		navigationController.mapDataReceived(mapDataModel);
		newMapData = true;
	}

	/*****************************************/
	/************** NAVIGATION ***************/
	/*****************************************/

	//Indicates new map data has been received
	private volatile boolean newMapData = false;
	//Ensures all navigation happens on a single thread
	private ExecutorService surveyExecutor = Executors.newSingleThreadExecutor();

	/**
	 * Returns true if the current destination has been reached, false otherwise
	 * @return
	 */
	private boolean checkFinished()
	{
		return mapDataModel.getRobotPosition().getPosition()[0] == destination[0] && mapDataModel.getRobotPosition().getPosition()[1] == destination[1];
	}

	/**
	 * Invokes the navigation functions and performs the required movement
	 */
	private boolean hasPath;
	
	private void surveyStart() {
		hasPath = true;
		text = "MOVING";
		surveyExecutor.execute(()-> {
				while (!stopped && !mapRenewed)
				{
					if (newMapData)
					{
						if (checkFinished())
						{
							destinationReached();
							break;
						}
						List<String> result = new LinkedList<String>();
						result = navigationController.run(destination);
						// No Path Found
						if (result.isEmpty() || result.get(0).equals("NoPath")) {
							System.out.println("Can't find out a Path.");
							text = "AUTO mode: Can't find out a Path.";
							hasPath = false;
							switch(currentmode){
							case AUTO:
								destinationReached();
							default:
								break;							
							}							
							break;
						}
						// the whole path is not surveyed, moving without recalculating
						// the path
						else if (result.size() == 1 && mapRenewed) {
							if (result.get(0).equals("NORTH")) {
								if (measuredAngle == NORTH) {
									forward();
								} else if (measuredAngle == EAST) {
									left();
									forward();
								} else if (measuredAngle == SOUTH) {
									right();
									right();
									forward();
								} else {
									right();
									forward();
								}
							} else if (result.get(0).equals("EAST")) {
								if (measuredAngle == NORTH) {
									right();
									forward();
								} else if (measuredAngle == EAST) {
									forward();
								} else if (measuredAngle == SOUTH) {
									left();
									forward();
								} else {
									right();
									right();
									forward();
								}
							} else if (result.get(0).equals("SOUTH")) {
								if (measuredAngle == NORTH) {
									right();
									right();
									forward();
								} else if (measuredAngle == EAST) {
									right();
									forward();
								} else if (measuredAngle == SOUTH) {
									forward();
								} else {
									left();
									forward();
								}
							} else if (result.get(0).equals("WEST")) {
								if (measuredAngle == NORTH) {
									left();
								} else if (measuredAngle == EAST) {
									left();
									left();
									forward();
								} else if (measuredAngle == SOUTH) {
									right();
									forward();
								} else {
									forward();
								}
							}
						} else {
							// the whole path is surveyed, moving without recalculating
							// the path
							for (String s : result) {
								if(mapRenewed){
									System.out.println("Survey is interrupted");
									break;
								}
								if (s.equals("North")) {
									if (measuredAngle == NORTH) {
										forward();
									} else if (measuredAngle == EAST) {
										left();
										forward();
									} else if (measuredAngle == SOUTH) {
										right();
										right();
										forward();
									} else {
										right();
										forward();
									}
								} else if (s.equals("East")) {
									if (measuredAngle == NORTH) {
										right();
										forward();
									} else if (measuredAngle == EAST) {
										forward();
									} else if (measuredAngle == SOUTH) {
										left();
										forward();
									} else {
										right();
										right();
										forward();
									}
								} else if (s.equals("South")) {
									if (measuredAngle == NORTH) {
										right();
										right();
										forward();
									} else if (measuredAngle == EAST) {
										right();
										forward();
									} else if (measuredAngle == SOUTH) {
										forward();
									} else {
										left();
										forward();
									}
								} else if (s.equals("West")) {
									if (measuredAngle == NORTH) {
										left();
									} else if (measuredAngle == EAST) {
										left();
										left();
										forward();
									} else if (measuredAngle == SOUTH) {
										right();
										forward();
									} else {
										forward();
									}
								}
							}
						}
						newMapData = false;
					}
				}
			});
	}
	
	public void shutdown(){
		MovementExecutor.shutdown();
		locationExecutor.shutdown();
		surveyExecutor.shutdown();
	}
	
	public double getx(){
		return x;
	}
	
	public double gety(){
		return y;
	}
	
	public float getestimatedAngle(){
		return direction;
	}
	
	public List<Integer[]> gettheDestinationList(){
		return destinationList;
	}
}
