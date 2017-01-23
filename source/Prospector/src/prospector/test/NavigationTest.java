package prospector.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import prospector.mapping.MapDataModel;
import prospector.movement.Navigation_step;
import prospector.movement.Navigation_tree;

public class NavigationTest {

	private Integer[] end = {15,15};
	private Navigation_step navigation_step;
	private int[] location={5,5};
	private Navigation_tree navigation_tree;
		
	@Test
	public void testgetRoot(){
		MapDataModel expectedMap = new MapDataModel(10,10);
		navigation_tree = new Navigation_tree(expectedMap,end);
		int[] expectedlocation = {0,0};
		Assert.assertEquals(expectedMap, navigation_tree.getroot().getMapDataModel());		
		Assert.assertEquals(expectedlocation[0], navigation_tree.getroot().getlocation()[0]);		
		Assert.assertEquals(expectedlocation[1], navigation_tree.getroot().getlocation()[1]);		
	}
	
	@Test
	public void testInclosed(){
		navigation_tree = new Navigation_tree(new MapDataModel(10,10),end);
		Navigation_step s1 = new Navigation_step(new MapDataModel(10,10),end);
		Navigation_step s2 = new Navigation_step(new MapDataModel(10,10),end);
		Navigation_step s3 = new Navigation_step(new MapDataModel(10,10),end);
		int[]location1 = {1,1};
		s1.setlocation(location1);
		int[]location2 = {1,2};
		s2.setlocation(location2);
		int[]location3 = {1,3};
		s2.setlocation(location3);
		navigation_tree.addclosed(s1);
		navigation_tree.addclosed(s2);
		boolean expectedouput = false;
		Assert.assertEquals(expectedouput, navigation_tree.inclosed(s3));		
	}
	
	@Test
	public void testAddclosed(){
		navigation_tree = new Navigation_tree(new MapDataModel(10,10),end);
		Navigation_step s1 = new Navigation_step(new MapDataModel(10,10),end);
		Navigation_step s2 = new Navigation_step(new MapDataModel(10,10),end);
		Navigation_step s3 = new Navigation_step(new MapDataModel(10,10),end);
		int[]location1 = {1,1};
		s1.setlocation(location1);
		int[]location2 = {1,2};
		s2.setlocation(location2);
		int[]location3 = {1,3};
		s2.setlocation(location3);
		navigation_tree.addclosed(s1);
		navigation_tree.addclosed(s2);
		List<Navigation_step> expectedoutput = new ArrayList<Navigation_step>();
		expectedoutput.add(s1);
		expectedoutput.add(s2);
		Assert.assertEquals(expectedoutput, navigation_tree.getclose());		
	}
	
	@Test
	public void testInclosed1(){
		navigation_tree = new Navigation_tree(new MapDataModel(10,10),end);
		Navigation_step s1 = new Navigation_step(new MapDataModel(10,10),end);
		Navigation_step s2 = new Navigation_step(new MapDataModel(10,10),end);
		Navigation_step s3 = new Navigation_step(new MapDataModel(10,10),end);
		int[]location1 = {1,1};
		s1.setlocation(location1);
		int[]location2 = {1,2};
		s2.setlocation(location2);
		int[]location3 = {1,3};
		s2.setlocation(location3);
		navigation_tree.addclosed(s1);
		navigation_tree.addclosed(s2);
		navigation_tree.addclosed(s3);
		boolean expectedouput = true;
		Assert.assertEquals(expectedouput, navigation_tree.inclosed(s3));		
	}
	
	@Test
	public void testRemoveOpen1(){
		navigation_tree = new Navigation_tree(new MapDataModel(10,10),end);
		Navigation_step s1 = new Navigation_step(new MapDataModel(10,10),end);
		Navigation_step s2 = new Navigation_step(new MapDataModel(10,10),end);
		Navigation_step s3 = new Navigation_step(new MapDataModel(10,10),end);
		int[]location1 = {1,1};
		s1.setlocation(location1);
		int[]location2 = {1,2};
		s2.setlocation(location2);
		int[]location3 = {1,3};
		s2.setlocation(location3);
		navigation_tree.addopen(s1);
		navigation_tree.addopen(s2);
		navigation_tree.addopen(s3);
		navigation_tree.removefirst();
		List<Navigation_step> expectedoutput = new ArrayList<Navigation_step>();
		expectedoutput.add(s2);
		expectedoutput.add(s3);
		Assert.assertEquals(expectedoutput, navigation_tree.getopen());		
	}
	
	@Test
	public void testaddopen(){
		navigation_tree = new Navigation_tree(new MapDataModel(10,10),end);
		Navigation_step s1 = new Navigation_step(new MapDataModel(10,10),end);
		Navigation_step s2 = new Navigation_step(new MapDataModel(10,10),end);
		Navigation_step s3 = new Navigation_step(new MapDataModel(10,10),end);
		int[]location1 = {1,1};
		s1.setlocation(location1);
		int[]location2 = {1,2};
		s2.setlocation(location2);
		int[]location3 = {1,3};
		s2.setlocation(location3);
		navigation_tree.addopen(s1);
		navigation_tree.addopen(s2);
		navigation_tree.addopen(s3);
		List<Navigation_step> expectedoutput = new ArrayList<Navigation_step>();
		expectedoutput.add(s1);
		expectedoutput.add(s2);
		expectedoutput.add(s3);
		Assert.assertEquals(expectedoutput, navigation_tree.getopen());		
	}
	
	@Test
	public void testInit() {
		navigation_step = new Navigation_step(new MapDataModel(10,10),end);
		int[] result = navigation_step.getlocation();
		int[] expected = {0,0};
		Assert.assertEquals(result[0], expected[0]);
		Assert.assertEquals(result[1], expected[1]);
	}
	
	@Test
	public void testUp() {
		navigation_step = new Navigation_step(new MapDataModel(10,10),end);
		navigation_step.moveup();
		int[] result = navigation_step.getlocation();
		int[] expected = {0,1};
		Assert.assertEquals(result[0], expected[0]);
		Assert.assertEquals(result[1], expected[1]);
	}

	@Test
	public void testDown() {
		navigation_step = new Navigation_step(new MapDataModel(10,10),end);
		navigation_step.movedown();
		int[] result = navigation_step.getlocation();
		int[] expected = {0,-1};
		Assert.assertEquals(result[0], expected[0]);
		Assert.assertEquals(result[1], expected[1]);
	}

	@Test
	public void testLeft() {
		navigation_step = new Navigation_step(new MapDataModel(10,10),end);
		navigation_step.moveleft();
		int[] result = navigation_step.getlocation();
		int[] expected = {-1,0};
		Assert.assertEquals(result[0], expected[0]);
		Assert.assertEquals(result[1], expected[1]);
	}

	@Test
	public void testRight() {
		navigation_step = new Navigation_step(new MapDataModel(10,10),end);
		navigation_step.moveright();
		int[] result = navigation_step.getlocation();
		int[] expected = {1,0};
		Assert.assertEquals(result[0], expected[0]);
		Assert.assertEquals(result[1], expected[1]);
	}

}

