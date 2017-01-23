/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import prospector.connection.ConnectionListener;
import prospector.data.Parser;
import prospector.movement.LocationListener;
import prospector.movement.MovementController;
import prospector.movement.MovementController.MoveMode;
import prospector.sensors.ColourListener;
import prospector.sensors.DistanceListener;

public class MappingController implements LocationListener, ColourListener, DistanceListener, ConnectionListener
{
	private double xLocation;
	private double yLocation;
	private float currentAngle;
	private float sensorDistance = Float.MAX_VALUE;
	private float currentColour = -1f;
	private MapDataModel mapDataModel;
	public static final double GRID_SIZE = 2.0; //cm
	private static final double SCALE_FACTOR = MovementController.MOVE_DISTANCE_CM/GRID_SIZE; // grids/movement
	private static final float COLOUR_SENSOR_OFFSET = 0.00f; // offset of colour sensor from centre of robot
	//Obstacles must be at least this close to be mapped
	private static final float OBSTACLE_MIN_RANGE = 0.1f, OBSTACLE_MAX_RANGE = 0.2f;
	public static final int ROBOT_WIDTH = 15; //cm
	public static final int ROBOT_X_OFFSET = 0; //Grid squares
	public static final int ROBOT_Y_OFFSET = 0;
	private List<MapListener> mapListeners = new ArrayList();
	private ScheduledExecutorService mapDispatcher = Executors.newSingleThreadScheduledExecutor();
	private ExecutorService mapUpdater = Executors.newSingleThreadExecutor();
	private Map<int[], GridSquare> savedSquares = new HashMap<>();
	private static double percentage = 0.0;
	private float estimatedAngle;
	private int UltraDirec, currentDestinationIndex;
	private boolean checkYbound = false, checkXbound = false;
	private MovementController movementController;
	private static Map<GridSquareEnum, AtomicInteger> idMap = new HashMap<>();
	private boolean mappingOn = false;
	private boolean connected = false;
	private final int width;
	private final int height;

	
	public void connect(MovementController movementController){
		this.movementController = movementController;
	}
	
	public MappingController(int width, int height)
	{
		this.width = width;
		this.height = height;
		initIdMap();
		clearMap();
		//Tell the listeners about the map
		mapDispatcher.scheduleAtFixedRate(()->
		{
			broadcastMapToListeners();
		}, 2000, 500, TimeUnit.MILLISECONDS);
	}

	private void initIdMap()
	{
		idMap.put(GridSquareEnum.NGZ, new AtomicInteger(0));
		idMap.put(GridSquareEnum.BLANK, new AtomicInteger(0));
		idMap.put(GridSquareEnum.UNKNOWN, new AtomicInteger(0));
	}
	
	public void addMapListener(MapListener mapListener)
	{
		mapListeners.add(mapListener);
	}
	
	private void broadcastMapToListeners()
	{
		mapListeners.forEach(listener->listener.mapDataReceived(mapDataModel));
	}
	
	public void setMapMode(boolean mapMode)
	{
		mappingOn = mapMode;
	}

	public boolean getMapMode()
	{
		return mappingOn;
	}
	/**
	 * Creates a no go zone from four corners
	 * @param squares
	 */
	public void createNgz(List<GridSquare> squares)
	{
		List<GridSquare> ngzSquares = new ArrayList<>(squares);
		for (GridSquare square :  squares)
		{
			for (GridSquare otherSquare :  squares)
			{
				//If the squares align, create the boundary
				if (square.getLocation()[0] == otherSquare.getLocation()[0] ||
						square.getLocation()[1] == otherSquare.getLocation()[1])
				{
					ngzSquares.addAll(Parser.getSquaresBetween(square, otherSquare));
				}
			}
		}
		int id = getNextNgzId();
		ngzSquares.forEach(n ->
		{
			n.setProperty(id);
			mapDataModel.setGridSquare(n.getLocation(), n);
		});
	}

	/**
	 * Returns the next id for a defined NGZ
	 * @return
	 */
	public static int getNextNgzId()
	{
		return idMap.get(GridSquareEnum.NGZ).incrementAndGet();
	}

	/**
	 * Returns the next id for a defined explored zone
	 * @return
	 */
	public static int getNextExploredId()
	{
		return idMap.get(GridSquareEnum.BLANK).incrementAndGet();
	}

	/**
	 * Returns the next id for a defined unexplored zone
	 * @return
	 */
	public static int getNextUnexploredId()
	{
		return idMap.get(GridSquareEnum.UNKNOWN).incrementAndGet();
	}

	private double ybound;
	private void updateMap()
	{
		mapUpdater.execute(()->
		{
			if (!mappingOn || !connected)
			{
				return;
			}
			//Remove robot from old location
			//Add robot to new location
			int[] robotLocation = getLocation(xLocation, yLocation);
			//////////////////////
			//Mark this area as explored (blank) if there is nothing there
			if (mapDataModel.getGridSquare(robotLocation).getValue() == GridSquareEnum.UNKNOWN)
			{
				mapDataModel.setGridSquare(robotLocation, new GridSquare(GridSquareEnum.BLANK));
			}
			RobotPosition robotPosition = new RobotPosition();
			robotPosition.setPosition(robotLocation);
			robotPosition.setHeading(currentAngle);
			mapDataModel.setRobotPosition(robotPosition);

			//If there is an obstacle close, calculate which grid square it is in
			if (sensorDistance < OBSTACLE_MAX_RANGE)
			{
				//Trigonometry
				//Assume the initial direction of robot is to North
				//East
				double radians = Math.toRadians(estimatedAngle);
				float obstacleX, obstacleY;

				if(Math.sin(radians)==-1){
					if(UltraDirec==1){
						obstacleX = (float) (xLocation + sensorDistance*100/GRID_SIZE);
						obstacleY = (float) (yLocation + sensorDistance*100/GRID_SIZE*4);
						System.out.println(obstacleX + "/" + obstacleY);
						int[] obstacleLocation = getLocation(obstacleX, obstacleY);
						mapDataModel.setGridSquare(obstacleLocation, new GridSquare(GridSquareEnum.OBSTACLE));
					}else if(UltraDirec==0 && sensorDistance < OBSTACLE_MIN_RANGE){
						obstacleX = (float) (xLocation + sensorDistance*100/GRID_SIZE);
						obstacleY = (float) (yLocation);
						int[] obstacleLocation = getLocation(obstacleX, obstacleY);
						mapDataModel.setGridSquare(obstacleLocation, new GridSquare(GridSquareEnum.OBSTACLE));
					}else if(UltraDirec==-1){ 
						obstacleX = (float) (xLocation + sensorDistance*100/GRID_SIZE);
						obstacleY = (float) (yLocation - sensorDistance*100/GRID_SIZE*4);
						int[] obstacleLocation = getLocation(obstacleX, obstacleY);
						mapDataModel.setGridSquare(obstacleLocation, new GridSquare(GridSquareEnum.OBSTACLE));
					}					
				}
				//West
				else if(Math.sin(radians)==1){
					if(UltraDirec==1){
						obstacleX = (float) (xLocation - sensorDistance*100/GRID_SIZE);
						obstacleY = (float) (yLocation - sensorDistance*100/GRID_SIZE*4);
						int[] obstacleLocation = getLocation(obstacleX, obstacleY);
						mapDataModel.setGridSquare(obstacleLocation, new GridSquare(GridSquareEnum.OBSTACLE));
					}else if(UltraDirec==0 && sensorDistance < OBSTACLE_MIN_RANGE){
						obstacleX = (float) (xLocation - sensorDistance*100/GRID_SIZE);
						obstacleY = (float) (yLocation);
						int[] obstacleLocation = getLocation(obstacleX, obstacleY);
						mapDataModel.setGridSquare(obstacleLocation, new GridSquare(GridSquareEnum.OBSTACLE));
					}else if(UltraDirec==-1){ 
						obstacleX = (float) (xLocation - sensorDistance*100/GRID_SIZE);
						obstacleY = (float) (yLocation + sensorDistance*100/GRID_SIZE*4);
						int[] obstacleLocation = getLocation(obstacleX, obstacleY);
						mapDataModel.setGridSquare(obstacleLocation, new GridSquare(GridSquareEnum.OBSTACLE));
					}	
				}
				//North
				else if(Math.cos(radians)==1){
					if(UltraDirec==1){
						obstacleX = (float) (xLocation - sensorDistance*100/GRID_SIZE*4);
						obstacleY = (float) (yLocation + sensorDistance*100/GRID_SIZE);
						int[] obstacleLocation = getLocation(obstacleX, obstacleY);
						mapDataModel.setGridSquare(obstacleLocation, new GridSquare(GridSquareEnum.OBSTACLE));
					}else if(UltraDirec==0 && sensorDistance < OBSTACLE_MIN_RANGE){
						obstacleX = (float) (xLocation);
						obstacleY = (float) (yLocation + sensorDistance*100/GRID_SIZE);
						int[] obstacleLocation = getLocation(obstacleX, obstacleY);
						mapDataModel.setGridSquare(obstacleLocation, new GridSquare(GridSquareEnum.OBSTACLE));
					}else if(UltraDirec==-1){ 
						obstacleX = (float) (xLocation + sensorDistance*100/GRID_SIZE*4);
						obstacleY = (float) (yLocation + sensorDistance*100/GRID_SIZE);
						int[] obstacleLocation = getLocation(obstacleX, obstacleY);
						mapDataModel.setGridSquare(obstacleLocation, new GridSquare(GridSquareEnum.OBSTACLE));
					}
				}
				//South
				else if(Math.cos(radians)==-1){
					if(UltraDirec==1){
						obstacleX = (float) (xLocation + sensorDistance*100/GRID_SIZE*4);
						obstacleY = (float) (yLocation - sensorDistance*100/GRID_SIZE);
						int[] obstacleLocation = getLocation(obstacleX, obstacleY);
						mapDataModel.setGridSquare(obstacleLocation, new GridSquare(GridSquareEnum.OBSTACLE));
					}else if(UltraDirec==0 && sensorDistance < OBSTACLE_MIN_RANGE){
						obstacleX = (float) (xLocation);
						obstacleY = (float) (yLocation - sensorDistance*100/GRID_SIZE);
						int[] obstacleLocation = getLocation(obstacleX, obstacleY);
						mapDataModel.setGridSquare(obstacleLocation, new GridSquare(GridSquareEnum.OBSTACLE));
					}else if(UltraDirec==-1){ 
						obstacleX = (float) (xLocation - sensorDistance*100/GRID_SIZE*4);
						obstacleY = (float) (yLocation - sensorDistance*100/GRID_SIZE);
						int[] obstacleLocation = getLocation(obstacleX, obstacleY);
						mapDataModel.setGridSquare(obstacleLocation, new GridSquare(GridSquareEnum.OBSTACLE));
					}		
				}

			}

			if (currentColour != lejos.robotics.Color.WHITE && currentColour != lejos.robotics.Color.NONE)
			{
				//Colour reading
				float colourX = (float) (xLocation + COLOUR_SENSOR_OFFSET * Math.cos(currentAngle));
				float colourY = (float) (yLocation + COLOUR_SENSOR_OFFSET * Math.sin(currentAngle));
				int[] colourLocation = getLocation(colourX, colourY);
				//Add faultline to grid
				GridSquare square;
				if (currentColour == lejos.robotics.Color.BLACK)
				{
					square = new GridSquare(GridSquareEnum.BOUNDARY);
					if(currentDestinationIndex == 1 && !checkXbound){
						boundary = true;
						System.out.println("Check X: " + colourLocation[0] + " " + colourLocation[1]);
						checkXbound = true;
						for(int i=0;i<mapDataModel.getWidth();i++){
							colourLocation[0] = i;
							mapDataModel.setGridSquare(colourLocation, square);
						}
						ybound = colourLocation[1]-1;

						movementController.reCalculateDestinationList(mapDataModel.getWidth()-1,colourLocation[1]-1);

					}
					//if ybound == ylocation, then the map size will renew both x and y when the robot hit boundry
					else if(currentDestinationIndex == 2 && !checkYbound && ybound!=yLocation && !boundary){
						System.out.println("Check Y" + colourLocation[0] + " " + colourLocation[1]);
						checkYbound = true;
						for(int i=0;i<mapDataModel.getHeight();i++){
							colourLocation[1] = i;
							mapDataModel.setGridSquare(colourLocation, square);
						}

						if(checkXbound){
							movementController.reCalculateDestinationList(colourLocation[0]-1,(int)ybound);
						}else{
							movementController.reCalculateDestinationList(colourLocation[0]-1,mapDataModel.getHeight()-1);
						}

					}else{
						mapDataModel.setGridSquare(colourLocation, square);
					}					
				} else
				{
					boundary = false;
					square = new GridSquare(GridSquareEnum.FAULTLINE);
					square.setProperty((int) ColourMap.getDepthForColor(ColourMap.getDrawColour((int) currentColour)));
					mapDataModel.setGridSquare(colourLocation, square);
				}
				
			}
			else
			{
				boundary = false;
			}
		});
	}
	
	private boolean boundary = false;
	
	private int[] getLocation(double x, double y)
	{
		int[] retval = new int[2];
		retval[0] = (int) Math.round(x/GRID_SIZE) + ROBOT_X_OFFSET;
		retval[1] = (int) Math.round(y/GRID_SIZE) + ROBOT_Y_OFFSET;
		return retval;
	}
	
	public MapDataModel getDataModel()
	{
		return mapDataModel;
	}

	@Override
	public void locationReceived(double x, double y, float angle, float estimatedAngle, int UltraDirec, int currentDestinationIndex,Integer[] dest, String text ) {
		this.xLocation = x;
		this.yLocation = y;
		this.currentAngle = angle;
		this.estimatedAngle = estimatedAngle;
		this.UltraDirec = UltraDirec;
		this.currentDestinationIndex = currentDestinationIndex;
		updateMap();
	}

	@Override
	public void distanceReceived(float distance) {
		sensorDistance = distance;
	}

	public void loadModel(MapDataModel model)
	{
		mapUpdater.execute(()->{
			this.mapDataModel = model;
		});
	}

	@Override
	public void colourDetected(float color) {
		currentColour = color;
	}

	public void clearMap()
	{
		mapDataModel = new MapDataModel(width, height);
		mapDataModel.clear();
		savedSquares.clear();
	}
	
	public void shutdown()
	{
		mapDispatcher.shutdown();
		mapUpdater.shutdown();
	}

	public int getDeepest()
	{
		return mapDataModel.getDeepest();
	}

	@Override
	public void connectionStateChanged(boolean connected)
	{
		this.connected = connected;
	}
}
