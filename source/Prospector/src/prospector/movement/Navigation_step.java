/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.movement;

import prospector.mapping.*;

public class Navigation_step{
	private MapDataModel n = null;
	private Integer[] end;
	private int cost;
	private int mancost;
	private int location[];
	public static final int MOVE_SQUARES = (int) Math.round(MovementController.MOVE_DISTANCE_CM/ MappingController.GRID_SIZE);
	public Navigation_step(MapDataModel n,Integer[] end){
		this.n = n;
		//int start[] = {0,0};
		location = n.getRobotPosition().getPosition();
		//System.out.println("start position: " + n.getRobotPosition().getPosition()[0] + " "+ n.getRobotPosition().getPosition()[1]);

		this.end = end;
		cost = 0;
		//n.setGridSquare(location, new GridSquare(GridSquareEnum.EXPLORED));
		this.father = null;
	}
	
	Navigation_step(){
		
	}
	
	Navigation_step up;
	Navigation_step down;
	Navigation_step left;
	Navigation_step right;
	Navigation_step father;
	
	public MapDataModel getMapDataModel(){
		return n;
	}
	public int getmanHattan(){
		return mancost;
	}
	
	public void setlocation(int[] location){
		this.location = location;
	}
	
	
	public int[] getlocation(){
		return location;
	}
	
	public void setManhattan(){
		//System.out.println(location[0] + " " +location[1]);
		//System.out.println(end[0] + " " +end[1]);

		mancost = Math.abs(end[0]-location[0]) + Math.abs(end[1]-location[1]) + cost;
	}
	
	public void moveleft(){
		location[0] -= MOVE_SQUARES;
		//n.setGridSquare(location, new GridSquare(GridSquareEnum.EXPLORED));
		cost++;
		setManhattan();
	}
	
	public void moveright(){
		location[0] += MOVE_SQUARES;
		//n.setGridSquare(location, new GridSquare(GridSquareEnum.EXPLORED));
		cost++;
		setManhattan();
	}
	
	public void moveup(){
		location[1] += MOVE_SQUARES;
		//n.setGridSquare(location, new GridSquare(GridSquareEnum.EXPLORED));
		cost++;
		setManhattan();
	}
	
	public void movedown(){
		location[1] -= MOVE_SQUARES;
		//n.setGridSquare(location, new GridSquare(GridSquareEnum.EXPLORED));
		cost++;
		setManhattan();
	}
	
	public void copy(Navigation_step s){
		this.n = new MapDataModel(s.getMapDataModel().getWidth(),s.getMapDataModel().getHeight());
		for(int i=0;i<s.getMapDataModel().getWidth();i++){
			for(int j=0;j<s.getMapDataModel().getHeight();j++){
				int[] location = {i,j};
				this.n.setGridSquare(location, s.getMapDataModel().getGridSquare(location));
			}
		}
		
		this.cost = s.cost;
		this.down = s.down;
		this.up = s.up;
		this.right = s.right;
		this.left = s.left;
		this.father = s.father;
		this.location = new int[2];
		this.location[0] = s.location[0];
		this.location[1] = s.location[1];
		this.mancost = s.mancost;
		this.end = s.end;
		
	}
	
}
