package prospector.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import prospector.connection.ConnectionController;
import prospector.mapping.MapDataModel;
import prospector.movement.LocationListener;
import prospector.movement.MovementController;
import prospector.movement.Navigation_step;

public class MovementControllerTest{
	MovementController movementcontroller;
	ConnectionController connetioncontroller = new ConnectionController();
	@Test
	public void testforward() {
		movementcontroller = new MovementController(connetioncontroller);
		movementcontroller.nextstate(2);
		movementcontroller.broadcastLocationToListeners(0);
		double x = movementcontroller.getx();
		double y = movementcontroller.gety();
		double estimatedAngele = movementcontroller.getestimatedAngle();
		double expectedx = 0;
		double expectedy = 1;
		double expectedAng = 0;
		Assert.assertEquals(expectedx, x,0.001);
		Assert.assertEquals(expectedy, y,0.001);
		Assert.assertEquals(expectedAng, estimatedAngele,0.001);
	}
	
	@Test
	public void testright() {
		movementcontroller = new MovementController(connetioncontroller);
		movementcontroller.nextstate(1);
		movementcontroller.broadcastLocationToListeners(0);
		double x = movementcontroller.getx();
		double y = movementcontroller.gety();
		double estimatedAngele = movementcontroller.getestimatedAngle();
		double expectedx = 0;
		double expectedy = 0;
		double expectedAng = 0;
		Assert.assertEquals(expectedx, x,0.001);
		Assert.assertEquals(expectedy, y,0.001);
		Assert.assertEquals(expectedAng, estimatedAngele,0.001);

	}
	
	@Test
	public void testrightandforward() {
		movementcontroller = new MovementController(connetioncontroller);
		movementcontroller.nextstate(1);
		movementcontroller.nextstate(2);
		movementcontroller.broadcastLocationToListeners(0);
		double x = movementcontroller.getx();
		double y = movementcontroller.gety();
		double estimatedAngele = movementcontroller.getestimatedAngle();
		double expectedx = 1;
		double expectedy = 0;
		double expectedAng = 0;
		Assert.assertEquals(expectedx, x,0.001);
		Assert.assertEquals(expectedy, y,0.001);
		Assert.assertEquals(expectedAng, estimatedAngele,0.001);

	}
	
	@Test
	public void testleft() {
		movementcontroller = new MovementController(connetioncontroller);
		movementcontroller.nextstate(-1);
		movementcontroller.broadcastLocationToListeners(0);
		double x = movementcontroller.getx();
		double y = movementcontroller.gety();
		double estimatedAngele = movementcontroller.getestimatedAngle();
		double expectedx = 0;
		double expectedy = 0;
		double expectedAng = 0;
		Assert.assertEquals(expectedx, x,0.001);
		Assert.assertEquals(expectedy, y,0.001);
		Assert.assertEquals(expectedAng, estimatedAngele,0.001);

	}
	
	@Test
	public void testleftandforward() {
		movementcontroller = new MovementController(connetioncontroller);
		movementcontroller.nextstate(-1);
		movementcontroller.nextstate(2);
		movementcontroller.broadcastLocationToListeners(0);
		double x = movementcontroller.getx();
		double y = movementcontroller.gety();
		double estimatedAngele = movementcontroller.getestimatedAngle();
		double expectedx = -1;
		double expectedy = 0;
		double expectedAng = 0;
		Assert.assertEquals(expectedx, x,0.001);
		Assert.assertEquals(expectedy, y,0.001);
		Assert.assertEquals(expectedAng, estimatedAngele,0.001);

	}
	
	@Test
	public void testback() {
		movementcontroller = new MovementController(connetioncontroller);
		movementcontroller.nextstate(-2);
		movementcontroller.broadcastLocationToListeners(0);
		double x = movementcontroller.getx();
		double y = movementcontroller.gety();
		double estimatedAngele = movementcontroller.getestimatedAngle();
		double expectedx = 0;
		double expectedy = -1;
		double expectedAng = 0;
		Assert.assertEquals(expectedx, x,0.001);
		Assert.assertEquals(expectedy, y,0.001);
		Assert.assertEquals(expectedAng, estimatedAngele,0.001);

	}
	
	@Test
	public void testDestinationList(){
		movementcontroller = new MovementController(connetioncontroller);
		movementcontroller.getDestinationList(3, 3);
		List<Integer[]> List = movementcontroller.gettheDestinationList();
		List<Integer[]> expectedList = new ArrayList<Integer[]>();
		Integer[] t1 = {0,0};
		Integer[] t2 = {0,3};
		Integer[] t3 = {3,3};
		Integer[] t4 = {3,0};
		Integer[] t5 = {1,0};
		Integer[] t6 = {1,2};
		Integer[] t7 = {2,2};
		Integer[] t8 = {2,1};
		expectedList.add(t1);
		expectedList.add(t2);
		expectedList.add(t3);
		expectedList.add(t4);
		expectedList.add(t5);
		expectedList.add(t6);
		expectedList.add(t7);
		expectedList.add(t8);
		for(int i=0;i<List.size();i++){
			Assert.assertEquals(expectedList.get(i)[0],List.get(i)[0]);
			Assert.assertEquals(expectedList.get(i)[1],List.get(i)[1]);
		}
		
		
	}
	

}
