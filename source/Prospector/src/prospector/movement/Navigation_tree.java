/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.movement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import prospector.mapping.GridSquareEnum;
import prospector.mapping.MapDataModel;
import prospector.mapping.MappingController;

public class Navigation_tree {
	private Navigation_step root;
	private List<Navigation_step> closed = new ArrayList<Navigation_step>();
	private List<Navigation_step> open = new ArrayList<Navigation_step>();
	private MapDataModel n;
	private Integer[]end;
	
	public List<Navigation_step> getopen(){
		return open;
	}
	
	public List<Navigation_step> getclose(){
		return closed;
	}
	
	public void addclosed(Navigation_step s){
		closed.add(s);
	}
	
	public void addopen(Navigation_step s){
		open.add(s);
	}
	
	public void removefirst(){
		open.remove(0);
	}
	
	public Navigation_tree(MapDataModel n,Integer[] end){
		this.n = n;
		this.end = end;
		root = new Navigation_step(n, end);
	}
	
	public Navigation_step getroot(){
		return root;
	}
	
	public boolean inclosed(Navigation_step s){
		for(Navigation_step i:closed){
			if(i.getlocation()[0]==s.getlocation()[0] && i.getlocation()[1]==s.getlocation()[1]){
				return true;
			}
		}
		return false;
			
	}
	
	public void sortbymanhattan(){
		//Collections.sort(sorted, new Comparator<env>(){
		Collections.sort(open, new Comparator<Navigation_step>(){
			@Override
			public int compare(Navigation_step o1, Navigation_step o2) {
				// TODO Auto-generated method stub
				//sort width first then height
				return o1.getmanHattan()>o2.getmanHattan() ? 1
						:o1.getmanHattan()<o2.getmanHattan() ? -1
						:0;
			}
		});
		//open.sort(c);
	}

	private enum Direction
	{
		LEFT,RIGHT,UP,DOWN;
	}
	
	public void maketree(List<Navigation_step> open, Navigation_step step){		
		if (checkBetween(step, Direction.UP))
		{
			Navigation_step upnode = new Navigation_step();
			upnode.copy(step);
			//upnode.setlocation(step.getlocation());
			upnode.moveup();
			step.up = upnode;
			upnode.father = step;
			open.add(upnode);
		}

		if (checkBetween(step, Direction.DOWN))
		{
//			System.out.println("down");
//			System.out.println(step.getlocation()[0]+" "+step.getlocation()[1]);
			Navigation_step downnode = new Navigation_step();
			downnode.copy(step);
			//downnode.setlocation(step.getlocation());
			downnode.movedown();			
			step.down = downnode;
			downnode.father = step;
			open.add(downnode);
		}

		if (checkBetween(step, Direction.LEFT))
		{
//			System.out.println("left");
//			System.out.println(step.getlocation()[0]+" "+step.getlocation()[1]);
			Navigation_step leftnode = new Navigation_step();
			leftnode.copy(step);
			//leftnode.setlocation(step.getlocation());
			leftnode.moveleft();
			step.left = leftnode;
			leftnode.father = step;
			open.add(leftnode);
		}
		if (checkBetween(step, Direction.RIGHT))
		{
//			System.out.println("right");
//			System.out.println(step.getlocation()[0]+" "+step.getlocation()[1]);
			//System.out.println("before: " + step.getlocation()[0] + " " + step.getlocation()[1]);
			Navigation_step rightnode = new Navigation_step();
			rightnode.copy(step);
			//rightnode.setlocation(step.getlocation());
			rightnode.moveright();
			step.right = rightnode;
			rightnode.father = step;
			open.add(rightnode);
		}
		
	}

	private boolean checkBetween(Navigation_step step, Direction direction)
	{
		int[] start = step.getlocation();
		int griddistance = (int)(2*MappingController.GRID_SIZE);
		int distance = MappingController.ROBOT_WIDTH / griddistance;
		int[] tempLocation = new int[2];
		switch (direction){
			case UP:
				//for (int i = start[1] + 1; i < start[1] + Navigation_step.MOVE_SQUARES; i++)
				//{
				int i = start[1] + 1;
					for (int j = start[0] - MappingController.ROBOT_WIDTH / griddistance; j <= start[0] + MappingController.ROBOT_WIDTH / griddistance; j++)
					{
						tempLocation[0] = j;
						tempLocation[1] = i;
						if (!checkValid(step, tempLocation, j > n.getWidth()+distance 
								|| j<-distance || i>n.getHeight()+distance || i<-distance))
						{
							return false;
						}
					}
				//}
				return true;
			case DOWN:
				//for (int i = start[1] - 1; i > start[1] - Navigation_step.MOVE_SQUARES; i--)
				//{
					i = start[1] - 1;
					for (int j = start[0] - MappingController.ROBOT_WIDTH / griddistance; j <= start[0] + MappingController.ROBOT_WIDTH / griddistance; j++)
					{
						tempLocation[0] = j;
						tempLocation[1] = i;
						if (!checkValid(step, tempLocation, j > n.getWidth()+distance || j<-distance || i>n.getHeight()+distance || i<-distance))
						{
							return false;
						}
					}
				//}
				return true;
			case LEFT:
				//for (int i = start[0] - 1; i > start[0] - Navigation_step.MOVE_SQUARES; i--)
				//{
				i = start[0] - 1;
					for (int j = (int) (start[1] - MappingController.ROBOT_WIDTH / griddistance); j <= start[1] + MappingController.ROBOT_WIDTH /griddistance; j++)
					{
						tempLocation[0] = i;
						tempLocation[1] = j;
						//System.out.println("LEFT");
						//System.out.println(j > n.getHeight()+distance || j<-distance || i>n.getWidth()+distance || i<-distance);
						if (!checkValid(step, tempLocation, j > n.getHeight()+distance || j<-distance || i>n.getWidth()+distance || i<-distance))
						{
							return false;
						}
					}
				//}
				return true;

			case RIGHT:
				//for (int i = start[0] + 1; i < start[0] + Navigation_step.MOVE_SQUARES; i++)
				//{
				i = start[0] + 1;
					for (int j = (int) (start[1] - MappingController.ROBOT_WIDTH /griddistance); j <= start[1] + MappingController.ROBOT_WIDTH /griddistance; j++)
					{
						tempLocation[0] = i;
						tempLocation[1] = j;
						if (!checkValid(step, tempLocation, j > n.getHeight()+distance || j<-distance || i>n.getWidth()+distance || i<-distance))
						{
							return false;
						}
					}
				//}
				return true;
			default:
				return false;
		}
	}

	private boolean checkValid(Navigation_step step, int[] location, boolean checkExceedsBounds)
	{
		
//		System.out.println("grid value" + step.getMapDataModel().getGridSquare(location).getValue());
//		boolean v = step.getMapDataModel().getGridSquare(location).getValue()!=GridSquareEnum.OBSTACLE;
//		System.out.println("judgement: " + v);
		return (step.getMapDataModel().validPoint(location) || !checkExceedsBounds)&&
				 step.getMapDataModel().getGridSquare(location).getValue()!=GridSquareEnum.BOUNDARY
				&& step.getMapDataModel().getGridSquare(location).getValue()!=GridSquareEnum.NGZ
				&& step.getMapDataModel().getGridSquare(location).getValue()!=GridSquareEnum.OBSTACLE;
	}
	
}
