package prospector.test;

import prospector.mapping.*;
import prospector.movement.MovementController.MoveMode;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MappingControllerTest {
	private MappingController mappingController;

	@Before
	public void setUp() throws Exception {
		 mappingController = new MappingController(100, 100);
	}

	@After
	public void tearDown() throws Exception {
		mappingController.clearMap();
	}

	@Test
	public void testZeroAngle() {
		mappingController.setMapMode(true);
		mappingController.connectionStateChanged(true);
		mappingController.colourDetected(2.0f);
		mappingController.distanceReceived(0.09f);
		Integer[] dest = {0,0};
		mappingController.locationReceived(10, 10, 0, 0, 0, 0, dest,"");
		//Wait for map dispatcher to run
		try
		{
			Thread.sleep(200);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		//Check faultline and obstacle created
		MapDataModel model = mappingController.getDataModel();
		int[] location1 = {5,5};
		//Obstacle to the north
		int[] location2 = {5,7};
		Assert.assertEquals(GridSquareEnum.FAULTLINE, model.getGridSquare(location1).getValue());
		Assert.assertEquals(GridSquareEnum.OBSTACLE, model.getGridSquare(location2).getValue());
	}
	
	@Test
	public void testNonZeroAngle01() {
		mappingController.setMapMode(true);
		mappingController.connectionStateChanged(true);
		mappingController.colourDetected(2.0f);
		mappingController.distanceReceived(0.09f);
		Integer[] dest = {0,0};
		mappingController.locationReceived(10, 10, 0, 90, 0, 0, dest,"");
		//Wait for map dispatcher to run
		try
		{
			Thread.sleep(200);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		//Check faultline and obstacle created
		MapDataModel model = mappingController.getDataModel();
		int[] location1 = {5,5};
		//Obstacle to the west
		int[] location2 = {3,5};
		Assert.assertEquals(GridSquareEnum.FAULTLINE, model.getGridSquare(location1).getValue());
		Assert.assertEquals(GridSquareEnum.OBSTACLE, model.getGridSquare(location2).getValue());
	}
	
	@Test
	public void testNonZeroAngle02() {
		mappingController.setMapMode(true);
		mappingController.connectionStateChanged(true);
		mappingController.colourDetected(2.0f);
		mappingController.distanceReceived(0.09f);
		Integer[] dest = {0,0};
		mappingController.locationReceived(10, 10, 0, 450, 0, 0, dest,"");
		//Wait for map dispatcher to run
		try
		{
			Thread.sleep(200);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		//Check faultline and obstacle created
		MapDataModel model = mappingController.getDataModel();
		int[] location1 = {5,5};
		//Obstacle to the west
		int[] location2 = {3,5};
		Assert.assertEquals(GridSquareEnum.FAULTLINE, model.getGridSquare(location1).getValue());
		Assert.assertEquals(GridSquareEnum.OBSTACLE, model.getGridSquare(location2).getValue());
	}
	
	@Test
	public void testNonZeroAngle03() {
		mappingController.setMapMode(true);
		mappingController.connectionStateChanged(true);
		mappingController.colourDetected(2.0f);
		mappingController.distanceReceived(0.09f);
		Integer[] dest = {0,0};
		mappingController.locationReceived(10, 10, 0, -90, 0, 0, dest,"");
		//Wait for map dispatcher to run
		try
		{
			Thread.sleep(200);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		//Check faultline and obstacle created
		MapDataModel model = mappingController.getDataModel();
		int[] location1 = {5,5};
		//Obstacle to the west
		int[] location2 = {7,5};
		Assert.assertEquals(GridSquareEnum.FAULTLINE, model.getGridSquare(location1).getValue());
		Assert.assertEquals(GridSquareEnum.OBSTACLE, model.getGridSquare(location2).getValue());
	}
	
	@Test
	public void testNonZeroAngle04() {
		mappingController.setMapMode(true);
		mappingController.connectionStateChanged(true);
		mappingController.colourDetected(2.0f);
		mappingController.distanceReceived(0.09f);
		Integer[] dest = {0,0};
		mappingController.locationReceived(10, 10, 0, 180, 0, 0, dest,"");
		//Wait for map dispatcher to run
		try
		{
			Thread.sleep(200);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		//Check faultline and obstacle created
		MapDataModel model = mappingController.getDataModel();
		int[] location1 = {5,5};
		//Obstacle to the west
		int[] location2 = {5,3};
		Assert.assertEquals(GridSquareEnum.FAULTLINE, model.getGridSquare(location1).getValue());
		Assert.assertEquals(GridSquareEnum.OBSTACLE, model.getGridSquare(location2).getValue());
	}
	
	@Test
	public void testNonZeroAngle05() {
		mappingController.setMapMode(true);
		mappingController.connectionStateChanged(true);
		mappingController.colourDetected(2.0f);
		mappingController.distanceReceived(0.09f);
		Integer[] dest = {0,0};
		mappingController.locationReceived(10, 10, 0, 270, 0, 0, dest,"");
		//Wait for map dispatcher to run
		try
		{
			Thread.sleep(200);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		//Check faultline and obstacle created
		MapDataModel model = mappingController.getDataModel();
		int[] location1 = {5,5};
		//Obstacle to the west
		int[] location2 = {7,5};
		Assert.assertEquals(GridSquareEnum.FAULTLINE, model.getGridSquare(location1).getValue());
		Assert.assertEquals(GridSquareEnum.OBSTACLE, model.getGridSquare(location2).getValue());
	}
	

}