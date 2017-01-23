/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import prospector.connection.ConnectionController;
import prospector.data.DataController;
import prospector.mapping.MappingController;
import prospector.movement.MovementController;
import prospector.movement.Navigation;
import prospector.opsgui.MainWindowController;
import prospector.sensors.SensorController;

public class ProspectorMain extends Application {
	
	private static MappingController mappingController;
	private static MovementController movementController;
	private static SensorController visionController;
	private static MainWindowController mainWindowController;
	private static ConnectionController connectionController;
	private static Navigation navigationController;
	private static DataController dataController;
	
	//Do all the initialisation here
	public static void main(String[] args)
	{
		mappingController = new MappingController((int) MainWindowController.NUM_DIVISIONS, (int) MainWindowController.NUM_DIVISIONS);
		connectionController = new ConnectionController();
		movementController = new MovementController(connectionController);		
		visionController = new SensorController(connectionController);
		dataController = new DataController();
		//link navigationController and movementController
		navigationController = new Navigation(mappingController.getDataModel());
		navigationController.connect(movementController);
		movementController.connect(navigationController);
		movementController.connect(mappingController);
		mappingController.connect(movementController);
		visionController.addDistanceListener(mappingController);
		visionController.addDistanceListener(movementController);
		visionController.addAngleListener(movementController);
		visionController.addColourListener(mappingController);
		visionController.addColourListener(movementController);
		movementController.addLocationListener(mappingController);
		connectionController.addConnectionListener(movementController);
		connectionController.addConnectionListener(visionController);
		connectionController.addConnectionListener(mappingController);
		mappingController.addMapListener(movementController);
		mappingController.addMapListener(dataController);
		mappingController.setMapMode(true);
		connectionController.connect();
		visionController.start();
		movementController.start();
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
			@Override
			public void run()
			{
				closeAll();
			}
		}));
	 	FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/MainWindow.fxml"));
        Parent root = loader.load();
        mainWindowController = (MainWindowController) loader.getController();
        visionController.addDistanceListener(mainWindowController);
        visionController.addColourListener(mainWindowController);
        movementController.addLocationListener(mainWindowController);
        connectionController.addConnectionListener(mainWindowController);
        mainWindowController.init(movementController, connectionController, mappingController, dataController);
        mappingController.addMapListener(mainWindowController);
        primaryStage.setTitle("Prospector SFM Operations");
        Scene scene = new Scene(root, 1300, 800);
        primaryStage.setScene(scene);
        primaryStage.setMaxHeight(800);
        primaryStage.setMinHeight(800);
        primaryStage.setMaxWidth(1300);
        primaryStage.setMinWidth(1300);
        primaryStage.setOnCloseRequest(event->
        {
        	closeAll();
        	primaryStage.close();
        });
        primaryStage.show();
	}
	
	private void closeAll()
	{
		visionController.shutdown();
		mappingController.shutdown();
		connectionController.closeAll();
		dataController.shutdown();
		movementController.shutdown();
	}
}
