/*****************************************************************
 * Prospector SFM
 * Software Engineering and Project
 * Semester 2, 2016
 * PG Group 4
 *****************************************************************/
package prospector.opsgui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.*;
import prospector.connection.ConnectionController;
import prospector.connection.ConnectionListener;
import prospector.data.DataController;
import prospector.mapping.*;
import prospector.movement.LocationListener;
import prospector.movement.MovementController;
import prospector.movement.MovementController.MoveMode;
import prospector.sensors.ColourListener;
import prospector.sensors.DistanceListener;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.dom4j.DocumentException;

/**
 * JavaFX controller for the main Operations GUI window
 */
public class MainWindowController implements DistanceListener, ColourListener, LocationListener, Initializable, ConnectionListener, MapListener
{
	/**
	 * Injected
	 */
	@FXML
	private Button upButton;

	@FXML
	private Button downButton;

	@FXML
	private Button leftButton;

	@FXML
	private Button rightButton;

	@FXML
	private Button stopButton;

	@FXML
	private Button connectButton;

	@FXML
	private TextArea addressText;

	@FXML
	private ComboBox<String> modeComboBox;

	@FXML
	private Button saveButton;

	@FXML
	private Button loadButton;

	@FXML
	private Button startButton;

	@FXML
	private Button locationButton;

	@FXML
	private TextArea debugText;

	@FXML
	private Canvas mapCanvas;

	@FXML
	private TextField xField;

	@FXML
	private TextField yField;

	@FXML
	private TextField angleField;

	@FXML
	private Canvas boundaryLegend;

	@FXML
	private Canvas ngzLegend;

	@FXML
	private Canvas obstacleLegend;

	@FXML
	private Canvas robotLegend;

	@FXML
	private Canvas faultlineLegend;

	@FXML
	private Canvas containerLegend;

	@FXML
	private VBox legendVbox;

	@FXML
	private TextArea xDest;

	@FXML
	private TextArea yDest;

	@FXML
	private CheckBox mapCheckbox;

	@FXML
	private GridPane utilPane;

	@FXML
	private GridPane navPane;

	@FXML
	private HBox selectorHBox;

	@FXML
	private HBox startHBox;

	@FXML
	private HBox locationHBox;

	private Scene scene;

	private ContextMenu contextMenu = new ContextMenu();

	private MovementController movementcontroller;

	private ConnectionController connectionController;

	private MappingController mappingController;

	private DataController dataController;

	private boolean connected = false;

	private Integer[] dest = new Integer[2];

	private boolean legendDrawn = false;

	private float distance;

	private double eventX;
	private double eventY;

	private double xLocation;
	private double yLocation;
	private float currentAngle;
	private float estimatedAngle;
	private float angleOffset = 0;
	private double mapScaleFactor;
	private MapDataModel mapDataModel;
	private static final double ROBOT_IMAGE_HEIGHT = 15.0; //in px
	private static final double ROBOT_IMAGE_WIDTH = 15.0; // in px
	public static final double AREA_MAX_DIMENSION = 100.0; //in cmt
	//private static final double AREA_MAX_DIMENSION = 20.0; //in cm easy for test
	public static final double NUM_DIVISIONS = Math.round(AREA_MAX_DIMENSION / MappingController.GRID_SIZE);
	private double gridsize;
	private static final double MAP_START_POS_X = MappingController.ROBOT_X_OFFSET*MappingController.GRID_SIZE;
	private static final double MAP_START_POS_Y = MappingController.ROBOT_Y_OFFSET*MappingController.GRID_SIZE;
	private float currentColour;
	private static final Pattern IP_ADDRESS = Pattern.compile(
			"^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	private double dragStartX;
	private double dragStartY;
	private double dragEndX;
	private double dragEndY;
	private boolean dragging = false;
	private String text;

	/**
	 * Called my JavaFX framework on startup
	 */
	public void init(MovementController movementcontroller, ConnectionController connectionController, MappingController mappingController, DataController dataController)
	{
		this.connectionController = connectionController;
		this.movementcontroller = movementcontroller;
		this.mappingController = mappingController;
		this.dataController = dataController;
		ObservableList<String> modeList = FXCollections.observableArrayList("Manual", "To Point", "Automatic");
		modeComboBox.setItems(modeList);
		modeComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue)
			{
				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						switch (newValue)
						{
							case "Manual":
								startButton.setDisable(true);
								upButton.setDisable(false);
								downButton.setDisable(false);
								leftButton.setDisable(false);
								rightButton.setDisable(false);
								xDest.setDisable(true);
								yDest.setDisable(true);
								movementcontroller.setMoveMode(MoveMode.MANUAL);
								break;
							case "Automatic":
								startButton.setDisable(false);
								upButton.setDisable(true);
								downButton.setDisable(true);
								leftButton.setDisable(true);
								rightButton.setDisable(true);
								xDest.setDisable(true);
								yDest.setDisable(true);
								movementcontroller.setMoveMode(MoveMode.AUTO);
								break;
							case "To Point":
								startButton.setDisable(false);
								upButton.setDisable(true);
								downButton.setDisable(true);
								leftButton.setDisable(true);
								rightButton.setDisable(true);
								xDest.setDisable(false);
								yDest.setDisable(false);
								movementcontroller.setMoveMode(MoveMode.MOVETOPOINT);
						}
					}
				});
			}
		});
		modeComboBox.getSelectionModel().select(0);
		stopButton.setGraphic(new ImageView(getClass().getResource("/resources/ic_close_black_24dp_2x.png").toString()));
		downButton.setGraphic(new ImageView(getClass().getResource("/resources/ic_keyboard_arrow_down_black_24dp_2x.png").toString()));
		leftButton.setGraphic(new ImageView(getClass().getResource("/resources/ic_keyboard_arrow_left_black_24dp_2x.png").toString()));
		rightButton.setGraphic(new ImageView(getClass().getResource("/resources/ic_keyboard_arrow_right_black_24dp_2x.png").toString()));
		upButton.setGraphic(new ImageView(getClass().getResource("/resources/ic_keyboard_arrow_up_black_24dp_2x.png").toString()));

		MenuItem setRobotMenu = new MenuItem("Set robot position");

		contextMenu.getItems().add(setRobotMenu);

		setRobotMenu.setOnAction(event -> setRobotPosition());

		mapCanvas.setOnMouseClicked(event ->
		{
			//Right click context menu
			if (connected && event.getButton().equals(MouseButton.SECONDARY))
			{
				eventY = ((mapCanvas.getHeight() - (event.getSceneY() - mapCanvas.getLayoutY())) / mapScaleFactor);
				eventX = event.getSceneX() / mapScaleFactor;
				contextMenu.show(mapCanvas, event.getScreenX() - mapCanvas.getTranslateX(), event.getScreenY() - mapCanvas.getTranslateY());
			}
			else
			{
				contextMenu.hide();
			}
		});

		mapCanvas.setOnMousePressed(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY)) //Left click and drag for NGZ
			{
				dragging = true;
				dragStartX = event.getSceneX() - mapCanvas.getLayoutX();
				dragStartY = event.getSceneY() - mapCanvas.getLayoutY();
			}
		});

		//mark as dragging
		mapCanvas.setOnMouseDragged(event ->
		{
			dragEndX = event.getSceneX() - mapCanvas.getLayoutX();
			dragEndY = event.getSceneY() - mapCanvas.getLayoutY();
			dragging = true;
			regenerateMap();
		});

		//Define NGZ
		mapCanvas.setOnMouseReleased(event ->
		{
			if (dragging)
			{
				//Show user confirmation dialog
				Alert dialog = new Alert(AlertType.CONFIRMATION);
				dialog.setTitle("NGZ");
				dialog.setHeaderText("No-go zone definition");
				dialog.setContentText("Create no-go zone here?");
				Optional<ButtonType> result = dialog.showAndWait();
				if (result.get() == ButtonType.OK)
				{
					//Get the four corners of the bounding square in grid coordinates
					int y0 = (int) Math.round((mapCanvas.getHeight() - dragStartY) / (mapScaleFactor*MappingController.GRID_SIZE))-1;
					int x0 = (int) Math.round(dragStartX / (mapScaleFactor*MappingController.GRID_SIZE))-1;
					int y1 = (int) Math.round((mapCanvas.getHeight() - dragEndY) / (mapScaleFactor*MappingController.GRID_SIZE))-1;
					int x1 = (int) Math.round(dragEndX / (mapScaleFactor*MappingController.GRID_SIZE))-1;
					int[] location1 = {x0, y0};
					int[] location2 = {x0, y1};
					int[] location3 = {x1, y1};
					int[] location4 = {x1, y0};
					int[][] locations = {location1, location2, location3, location4};
					List<GridSquare> squares = new ArrayList<GridSquare>();
					for (int[] location : locations)
					{
						GridSquare square = new GridSquare(GridSquareEnum.NGZ);
						square.setLocation(location);
						squares.add(square);
					}
					//Invoke the mapping controller
					Thread t = new Thread(() -> {
						mappingController.createNgz(squares);
					});
					t.start();
				}
				dragging = false;
			}
		});

		mapCheckbox.setSelected(mappingController.getMapMode());

		mapCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>()
		{

			@Override
			public void changed(ObservableValue arg0, Boolean arg1, Boolean arg2)
			{
				mappingController.setMapMode(arg2);
				regenerateDebugText();
			}

		});

		//Enter press initiates connection
		addressText.setOnKeyPressed(event ->
		{
			if(event.getCode().equals(KeyCode.ENTER))
			{
				addressText.commitValue();
				onConnect();
				event.consume();
			}
		});

		//Enter commits value
		xDest.setOnKeyPressed(event ->
		{
			if (event.getCode().equals(KeyCode.ENTER))
			{
				xDest.commitValue();
				event.consume();
			}
		});

		yDest.setOnKeyPressed(event ->
		{
			if (event.getCode().equals(KeyCode.ENTER))
			{
				yDest.commitValue();
				event.consume();
			}
		});

		disableControls(true);
	}

	/**
	 * Sets the robot position to the right-click location
	 */
	private void setRobotPosition()
	{
		//Show user confirmation dialog
		Alert dialog = new Alert(AlertType.CONFIRMATION);
		dialog.setTitle("Manual position correction");
		dialog.setHeaderText("Please confirm new robot position:");
		dialog.setContentText(Math.round(eventX) + ", " + Math.round(eventY));

		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get() == ButtonType.OK)
		{
			double eastOffset = xLocation - eventX;
			double northOffset = yLocation - eventY;
			System.out.println(eastOffset + "/" + northOffset);
			//Perform the action off the UI thread
			Thread t = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					movementcontroller.setLocation(eastOffset, northOffset, angleOffset);
				}
			});
			t.start();
		}
	}

	@FXML
	private void up()
	{
		System.out.println("Up!");

		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				movementcontroller.forwardCommand();
			}
		});
		t.start();
	}

	@FXML
	private void down()
	{
		System.out.println("Down!");
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				movementcontroller.backwardCommand();
			}
		});
		t.start();
	}

	@FXML
	private void left()
	{
		System.out.println("Left!");
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				movementcontroller.leftCommand();
			}
		});
		t.start();
	}

	@FXML
	private void right()
	{
		System.out.println("Right!");
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				movementcontroller.rightCommand();
			}
		});
		t.start();
	}

	@FXML
	private void stop()
	{
		System.out.println("Stop!");
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					movementcontroller.stop();
				} catch (RemoteException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	@FXML
	private void save()
	{
		//File chooser with extension filter
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Map File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Map Files", "*.xml"));
		File file = fileChooser.showSaveDialog(new Stage());
		if (file != null)
		{
			Thread t = new Thread(() ->
			{
				try
				{
					dataController.writeFileOut(mapDataModel, file);
				} catch (IOException e)
				{
					//Inform user of error
					Platform.runLater(() ->
					{
						//Show user alert dialog
						Alert dialog = new Alert(AlertType.ERROR);
						dialog.setTitle("Error saving document");
						dialog.setHeaderText("There was an error while trying to save the document");
						dialog.setContentText(e.getMessage());
						dialog.showAndWait();
					});
				}
			});
			t.start();
		}
	}

	@FXML
	private void load()
	{
		//Let the user choose a file
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Map File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Map Files", "*.xml"));
		File file = fileChooser.showOpenDialog(new Stage());
		if (file != null)
		{
			Thread t = new Thread(() ->
			{
				try
				{
					MapDataModel mapDataModel = dataController.readFileIn(file.toURI().toURL(),
							mappingController.getDataModel().getWidth(), mappingController.getDataModel().getHeight());
					mappingController.loadModel(mapDataModel);
					RobotPosition robotPosition = mapDataModel.getRobotPosition();
					movementcontroller.setLocation(-robotPosition.getPosition()[0], -robotPosition.getPosition()[1], (float) robotPosition.getHeading());
				} catch (MalformedURLException | DocumentException | IllegalArgumentException e)
				{
					//Inform user of error
					Platform.runLater(() ->
					{
						//Show user alert dialog
						Alert dialog = new Alert(AlertType.ERROR);
						dialog.setTitle("Error loading document");
						dialog.setHeaderText("There was an error while trying to load the document");
						dialog.setContentText(e.getMessage());
						dialog.showAndWait();
					});
				}
			});
			t.start();
		}
	}

	@FXML
	private void clear()
	{
		//Show user confirmation dialog
		Alert dialog = new Alert(AlertType.CONFIRMATION);
		dialog.setTitle("Clear map");
		dialog.setHeaderText("Please confirm map clear");
		dialog.setContentText("Are you sure you want to clear the map?\nUnsaved changes will be lost.");
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get() == ButtonType.OK)
		{
			Thread t = new Thread(() ->
			{
				mappingController.clearMap();
			});
			t.start();
		}
	}

	@FXML
	private void start()
	{
		try
		{
			if (modeComboBox.getSelectionModel().getSelectedItem().equals("Automatic"))
			{
				dest = null;
			} else
			{
				dest[0] = (int) Math.round(Double.parseDouble(xDest.getText())/MappingController.GRID_SIZE);
				dest[1] = (int) Math.round(Double.parseDouble(yDest.getText())/MappingController.GRID_SIZE);
			}
			movementcontroller.startSurvey(dest);
		} catch (NumberFormatException e)
		{
			xDest.clear();
			yDest.clear();
		}
	}

	@FXML
	private void onConnect()
	{
		if (!connected)
		{
			String address = addressText.getText();
			connectionController.setAddress(address);
			if (!IP_ADDRESS.matcher(address).matches())
			{
				addressText.clear();
				return;
			}
			connectButton.setDisable(true);
			Thread t = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					connectionController.connect();
				}
			});
			t.start();
		}
	}

	@Override
	public void distanceReceived(float distance)
	{
		this.distance = distance;
		regenerateDebugText();
	}

	private void regenerateDebugText()
	{
		Platform.runLater(() ->
		{
			StringBuilder sb = new StringBuilder(connected ? "Connected" : "Disconnected");
			sb.append("\n");
			sb.append("status: " + text);
			sb.append("\n");
			sb.append("Map mode: " + (mapCheckbox.isSelected() ? "On" : "Off"));
			sb.append("\n");
			sb.append("Distance: " + distance);
			sb.append("\n");
			sb.append("Angle: " + currentAngle);

			sb.append("\n");
			sb.append("Location: " + xLocation + ", " + yLocation);
			sb.append("\n");
			sb.append("Estimated angle: " + estimatedAngle);
			sb.append("\n");
			sb.append("Angular error: " + Double.toString(currentAngle - estimatedAngle));
			sb.append("\n");
			sb.append("Colour value: " + Float.toString(currentColour));
			sb.append("\n");
			sb.append("Target destination:  " + dest[0] + ", " + dest[1]);


			debugText.setText(sb.toString());
		});
	}

	/**
	 * Regenerates the map graphic
	 */
	private void regenerateMap()
	{
		Platform.runLater(() ->
		{
			mapScaleFactor = mapCanvas.getWidth() / AREA_MAX_DIMENSION;
			gridsize = MappingController.GRID_SIZE * mapScaleFactor;
//			MAP_START_POS_X = gridsize / 2; //mapCanvas.getWidth()/2;
//			MAP_START_POS_Y = gridsize / 2;//mapCanvas.getHeight()/2;
			GraphicsContext graphicsContext = mapCanvas.getGraphicsContext2D();
			graphicsContext.beginPath();
			graphicsContext.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
			graphicsContext.closePath();
			graphicsContext.beginPath();
			graphicsContext.setFill(Color.LIGHTGRAY);
			graphicsContext.fillRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
			graphicsContext.closePath();
			graphicsContext.beginPath();
			graphicsContext.setFill(Color.GREEN);
			graphicsContext.closePath();
			drawGrid(graphicsContext);
			drawMapData(graphicsContext);
			if (dragging)
			{
				drawDragBox(graphicsContext);
			}
			if (connected)
			{
				drawRobotIcon(graphicsContext);
			}
			if (!legendDrawn)
			{
				drawLegend();
			}
		});
	}

	private void drawDragBox(GraphicsContext graphicsContext)
	{
		//Draw the prospective NGZ box
		graphicsContext.setStroke(Color.FUCHSIA);
		graphicsContext.setLineWidth(5);
		graphicsContext.beginPath();
		double minX = Math.min(dragStartX, dragEndX);
		double maxX = Math.max(dragStartX, dragEndX);
		double minY = Math.min(dragStartY, dragEndY);
		double maxY = Math.max(dragStartY, dragEndY);
		graphicsContext.strokeRect(minX, minY, maxX-minX, maxY-minY);
		graphicsContext.stroke();
		graphicsContext.closePath();
	}
	
	private void drawLegend()
	{
		legendDrawn = true;
		legendVbox.setStyle("-fx-padding: 10;" +
			 "-fx-border-style: solid inside;" +
			 "-fx-border-width: 2;" +
			 "-fx-border-insets: 5;" +
			 "-fx-border-radius: 5;" +
			 "-fx-border-color: black;");
		GraphicsContext ngzContext = ngzLegend.getGraphicsContext2D();
		ngzContext.setFill(Color.FUCHSIA);
		ngzContext.beginPath();
		ngzContext.fillRect(0, 0, ngzLegend.getWidth(), ngzLegend.getHeight());
		ngzContext.closePath();

		GraphicsContext obstacleContext = obstacleLegend.getGraphicsContext2D();
		obstacleContext.setFill(Color.DARKSLATEGREY);
		obstacleContext.beginPath();
		obstacleContext.fillRect(0, 0, obstacleLegend.getWidth(), obstacleLegend.getHeight());
		obstacleContext.closePath();

		GraphicsContext boundaryContext = boundaryLegend.getGraphicsContext2D();
		boundaryContext.setFill(Color.BLACK);
		boundaryContext.beginPath();
		boundaryContext.fillRoundRect(0, 0, boundaryLegend.getWidth(), boundaryLegend.getHeight(),
				boundaryLegend.getWidth()/4, boundaryLegend.getHeight()/4);
		boundaryContext.closePath();

		GraphicsContext robotContext = robotLegend.getGraphicsContext2D();
		Image image = new Image(getClass().getResourceAsStream("/resources/ic_send_black_24dp_2x.png"));
		robotContext.drawImage(image, 0, 2, ROBOT_IMAGE_HEIGHT, ROBOT_IMAGE_WIDTH);
		 
		GraphicsContext containerContext = containerLegend.getGraphicsContext2D();
		containerContext.setFill(Color.BLUEVIOLET);
		containerContext.beginPath();
		containerContext.fillRect(0, 0, containerLegend.getWidth(), containerLegend.getHeight());
		containerContext.closePath();
		 
		GraphicsContext faultlineContext = faultlineLegend.getGraphicsContext2D();
		faultlineContext.setFill(Color.RED);
		faultlineContext.beginPath();
		faultlineContext.fillOval(0, 0, 3*faultlineLegend.getWidth()/4, 3*faultlineLegend.getHeight()/4);
		faultlineContext.closePath();
	}
	
	/**
	 * Draws the grid lines
	 * @param graphicsContext
	 */
	private void drawGrid(GraphicsContext graphicsContext)
	{
		graphicsContext.setStroke(Color.DARKGREY);
		graphicsContext.setLineWidth(1);
		//Horizontal lines
		graphicsContext.moveTo(0, 0);
		graphicsContext.beginPath();
		for (int i = 0; i < NUM_DIVISIONS; i++)
		{
			graphicsContext.moveTo(0, i*gridsize);
			graphicsContext.lineTo(mapCanvas.getWidth(), i*gridsize);
		}
		graphicsContext.stroke();
		graphicsContext.closePath();
		
		//vertical lines
		graphicsContext.moveTo(0, 0);
		graphicsContext.beginPath();
		for (int i = 0; i < NUM_DIVISIONS; i++)
		{
			graphicsContext.moveTo(i*MappingController.GRID_SIZE*mapScaleFactor, 0);
			graphicsContext.lineTo(i*MappingController.GRID_SIZE*mapScaleFactor, mapCanvas.getHeight());
		}
		graphicsContext.stroke();
		graphicsContext.closePath();	
	}
	
	/**
	 * Paints the map data
	 * @param graphicsContext
	 */
	private void drawMapData(GraphicsContext graphicsContext)
	{
		if (mapDataModel != null)
		{
			int width = mapDataModel.getWidth();
			int height = mapDataModel.getHeight();
			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					int[] location = {i,j};
					GridSquare square = mapDataModel.getGridSquare(location);
					double x = i*gridsize;
					double y = mapCanvas.getHeight()-j*gridsize;
					drawGridSquare(graphicsContext,square, x, y);
				}
			}
		}
	}
	
	/**
	 * Draws a single gridsquare on the provided graphics context (canvas)
	 * @param graphicsContext
	 * @param gridSquare
	 * @param x
	 * @param y
	 */
	private void drawGridSquare(GraphicsContext graphicsContext, GridSquare gridSquare, double x, double y)
	{
		if (gridSquare != null)
		{
			switch(gridSquare.getValue())
			{
				case BLANK:
					graphicsContext.setFill(Color.WHITE);
					graphicsContext.fillRect(x, y-gridsize, gridsize, gridsize);
					break;
				case ROBOT: //Only used for legend
					Image image = new Image(getClass().getResourceAsStream("/resources/ic_send_black_24dp_2x.png"));
					graphicsContext.drawImage(image, x, y-gridsize, ROBOT_IMAGE_HEIGHT, ROBOT_IMAGE_WIDTH);
					break;
				case OBSTACLE:
					graphicsContext.setFill(Color.DARKSLATEGREY);
					graphicsContext.fillRoundRect(x, y-gridsize, gridsize, gridsize, gridsize/4, gridsize/4);
					break;
				case BOUNDARY:
					graphicsContext.setFill(Color.BLACK);
					graphicsContext.fillRect(x, y-gridsize, gridsize, gridsize);
					break;
				case NGZ:
					graphicsContext.setFill(Color.FUCHSIA);
					graphicsContext.fillRect(x, y-gridsize, gridsize, gridsize);
					break;
				case EXTRACTION:
					graphicsContext.setFill(Color.BLUEVIOLET);
					graphicsContext.fillRect(x, y-gridsize, gridsize, gridsize);
					break;
				case FAULTLINE:
					Color fillColour = ColourMap.getColorForDepth(gridSquare.getProperty());
					graphicsContext.setFill(fillColour);
					graphicsContext.fillOval(x, y-gridsize, 4*gridsize/5, 4*gridsize/5);
					break;
			}
		}
	}
	
	/**
	 * Draws the robot on the map canvas
	 * @param graphicsContext
	 */
	private void drawRobotIcon(GraphicsContext graphicsContext)
	{
		double mapLocationX = (MAP_START_POS_X +xLocation+MappingController.GRID_SIZE)*mapScaleFactor;
		double mapLocationY = mapCanvas.getHeight() - (yLocation+MappingController.GRID_SIZE + MAP_START_POS_Y)*mapScaleFactor;
		//Image location defined as top left corner
		double drawX = mapLocationX - ROBOT_IMAGE_WIDTH;
		double drawY = mapLocationY;// - ROBOT_IMAGE_HEIGHT;
		Image image = new Image(getClass().getResourceAsStream("/resources/ic_send_black_24dp_2x.png"));
		graphicsContext.save();
		double rotateX = xLocation*mapScaleFactor+ROBOT_IMAGE_WIDTH/2;
		double rotateY = mapCanvas.getHeight()-yLocation*mapScaleFactor;
		Rotate rotate = new Rotate(-getCorrectedAngle(), mapLocationX-ROBOT_IMAGE_WIDTH/2, mapLocationY+ROBOT_IMAGE_WIDTH/2);
		graphicsContext.setTransform(rotate.getMxx(), rotate.getMyx(), rotate.getMxy(), rotate.getMyy(), rotate.getTx(), rotate.getTy());
		graphicsContext.drawImage(image, drawX, drawY, ROBOT_IMAGE_HEIGHT, ROBOT_IMAGE_WIDTH);
		graphicsContext.restore();
	}
	
	private float getCorrectedAngle()
	{
		return currentAngle - angleOffset;
	}

	@Override
	public void locationReceived(double x, double y, float angle, float estimatedAngle, int UltraDirec, int currentDestinationIndex,
			Integer[] dest, String text) {
		this.xLocation = x;
		this.yLocation = y;
		this.estimatedAngle = estimatedAngle;
		this.currentAngle = angle;
		this.text = text;
		this.dest = dest;
		regenerateDebugText();
		regenerateMap();
	}
	
	@FXML
	public void setLocation()
	{
		try
		{
			//Only use fields with data
			double x = xField.getText().isEmpty() ? xLocation : Integer.parseInt(xField.getText());
			double y = yField.getText().isEmpty() ? yLocation : Integer.parseInt(yField.getText());
			angleOffset = angleField.getText().isEmpty() ? 0 : currentAngle + Integer.parseInt(angleField.getText());
			double eastOffset =  xLocation - x;
			double northOffset = yLocation - y;
			System.out.println(eastOffset + "/" + northOffset);
			Thread t = new Thread(new Runnable()
			{
	    		@Override public void run()
	    		{
					movementcontroller.setLocation(eastOffset, northOffset, angleOffset);
	    		}
			});
	    	t.start();
		} catch (NumberFormatException nfe)
		{
			xField.clear();
			yField.clear();
			angleField.clear();
		}
	}
	
	@Override
    public void initialize(URL url, ResourceBundle rb) {
		regenerateDebugText();
        regenerateMap();
    }

	@Override
	public void connectionStateChanged(boolean connected) {
		//TODO: disable controls if disconnected
		this.connected = connected;
		System.out.println("Connected:" + connected);
		Platform.runLater(()->connectButton.setDisable(connected));
		disableControls(!connected);
		regenerateDebugText();
		regenerateMap();
	}

	@Override
	public void colourDetected(float color) {
		this.currentColour = color;
	}

	@Override
	public void mapDataReceived(MapDataModel mapDataModel) {
		this.mapDataModel = mapDataModel;
		regenerateMap();
	}
	
	private void disableControls(boolean disable)
	{
		Platform.runLater(()->
		{
			navPane.setDisable(disable);
			selectorHBox.setDisable(disable);
			startHBox.setDisable(disable);
			locationHBox.setDisable(disable);
			locationButton.setDisable(disable);
		});
	}

}
