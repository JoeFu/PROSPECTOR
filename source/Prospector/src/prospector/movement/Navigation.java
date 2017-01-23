/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.movement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import prospector.mapping.GridSquare;
import prospector.mapping.GridSquareEnum;
import prospector.mapping.MapDataModel;
import prospector.mapping.MapListener;

//based on Astar_manhattan
public class Navigation implements MapListener{
	private MapDataModel mapDataModel;
	private int end[];
	private MovementController m;
	public void connect(MovementController m){
		this.m = m;
	}
	@Override
	public void mapDataReceived(MapDataModel mapDataModel) {
		// TODO Auto-generated method stub
		this.mapDataModel = mapDataModel;
	}
	
	public MapDataModel getMapDataModel(){
		return mapDataModel;
	}
	
	public int[] getend(){
		return end;
	}
	
	public Navigation( MapDataModel m){
		mapDataModel = m;
		//this.end = end;
		//mapDataModel.setRobotPosition(start);
	}
	
	public List<String> nextmove(Navigation_step candidatestep){	
		List<String> movementseq = new LinkedList<String>();
		Navigation_step temp;
		boolean Surveyed = true;
		while(candidatestep.father!=null){
			//movementseq.add(candidatestep);
			//if there is one grid on the path which is not surveyed, 
			//then use Survey mode to finished the path
			if(candidatestep.getMapDataModel().getGridSquare(candidatestep.getlocation()).getValue()
					==GridSquareEnum.UNKNOWN ){ 
				Surveyed = false;
			}
			temp = candidatestep.father;
			if(temp.up == candidatestep){
				movementseq.add(0,"North");
			}
			if(temp.down == candidatestep){
				movementseq.add(0,"South");
			}
			if(temp.left == candidatestep){
				movementseq.add(0,"West");
			}
			if(temp.right == candidatestep){
				movementseq.add(0,"East");
			}
			candidatestep = temp;
		}	
//		for(String s:movementseq){
//			System.out.println(s);
//		}
		//get the root of the tree which is the first movement
		if(Surveyed){
			return movementseq;
		}else{
			List<String> tempmovementseq = new LinkedList<String>();
			tempmovementseq.add(movementseq.get(0));
			return tempmovementseq;
		}	
	}
	
	public List<String> run(Integer[] end){
		Navigation_tree tree = new Navigation_tree(mapDataModel, end);
		tree.addopen(tree.getroot());
		int runtime = 0;
		while(true){
			runtime++;
			if(tree.getopen().isEmpty()){
				System.out.println("No path");
				List<String> tempmovementseq = new LinkedList<String>();
				tempmovementseq.add("NoPath");
				return tempmovementseq;
			}
			Navigation_step candidatestep;
			candidatestep = tree.getopen().get(0);
			Navigation_step closedstep;
			closedstep = tree.getopen().get(0);
			tree.removefirst();
			if(candidatestep.getlocation()[0] == end[0] && candidatestep.getlocation()[1] == end[1]){
				//System.out.println("getPath" + runtime);
//				for(int i=0;i<mapDataModel.getWidth();i++){
//					for(int j=0;j<mapDataModel.getHeight();j++){
//						int[] temp = {i,j};
//						if(candidatestep.getMapDataModel().getGridSquare(temp).getValue().getValue()==-2){ //TODO: Why -2? this is not valid
//							System.out.print("*" + " ");
//						}else{
//							System.out.print(candidatestep.getMapDataModel().getGridSquare(temp).getValue() + " ");
//						}
//					}
//					System.out.println();
//				}
				return nextmove(candidatestep);
			}else{
				if(!tree.inclosed(candidatestep)){
					tree.addclosed(closedstep);
					tree.maketree(tree.getopen(), candidatestep);
					tree.sortbymanhattan();
				}
//				for(int i=0;i<mapDataModel.getWidth();i++){
//					for(int j=0;j<mapDataModel.getHeight();j++){
//						int[] temp = {i,j};
//						if(candidatestep.getMapDataModel().getGridSquare(temp).getValue().getValue()==-2){
//							System.out.print("*" + " ");
//						}else{
//							System.out.print(candidatestep.getMapDataModel().getGridSquare(temp).getValue() + " ");
//						}
//					}
//					System.out.println();
//				}
//				System.out.println("=========================");
			}		
		}
	}
}
