/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import javafx.scene.paint.Color;
import prospector.mapping.*;
import prospector.opsgui.MainWindowController;

public class Parser
{
	private static final String ROOT = "seafaultsmap";
	private static final String DATE = "Survey Date";
	private static final String MODEL = "Robot Model";
	private static final String BOUNDARY = "boundary";
	private static final String ROBOT_STATUS = "robot-status";
	private static final String HEADING = "heading";
	private static final String CONTAINER = "container";
	private static final String ZONE = "zone";
	private static final String EXPLORED = "explored";
	private static final String UNEXPLORED = "unexplored";
	private static final String STATE = "state";
	private static final String NGZ = "nogo";
	private static final String DEPTH = "depth-to-color";
	//Misspelling reflects a microcosm of the farcical nature of this entire course's organisation
	private static final String OBSTACLE = "obsticle";
	private static final String FAULTLINE = "fault-line";
	private static final String AREA = "area";
	private static final String CIRCLE = "circle";
	private static final String POINT = "point";
	private static final String ATTRIBUTE = "attribute";
	private static final String UNITS = "units";

	private static double scaleFactor = 1;

	private static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/YYYY");

	
	/**
	 * Parses a file indicated by a URL and returns a map data model object
	 * @param url The file URL
	 * @return The map data model
	 * @throws DocumentException
	 */
	public static MapDataModel parse(URL url, int h, int w) throws DocumentException, IllegalArgumentException
	{
		SAXReader reader = new SAXReader();
		Document document = reader.read(url);
		return documentToMap(document, h, w);
	}
	
	private static int h;
	private static int w;

	/**
	 * Converts a DOM document to a map data model object
	 * @param document
	 * @return
	 */
	private static MapDataModel documentToMap(Document document, int height, int width) throws IllegalArgumentException
	{
		Element root = document.getRootElement();
		if (!root.getName().equals(ROOT))
		{
			throw new IllegalArgumentException("Incorrect root element: " + root.getName());
		}
		Attribute unitAttr = root.attribute(UNITS);
		if (unitAttr == null)
		{
			throw new IllegalArgumentException("No unit attribute");
		}

		String units = unitAttr.getValue();
		//Calculate factor to scale to centimetres
		switch (units)
		{
			case "metres":
				scaleFactor = 100.0;
				break;
			case "centimetres":
				scaleFactor = 1.0;
				break;
			case "millimetres":
				scaleFactor = 0.1;
				break;
			default:
				throw new IllegalArgumentException("Invalid unit attribute:" + units);
		}

		h = 0;
		w = 0;
		List<GridSquare> squares = new ArrayList<>();
		RobotPosition robotPosition = new RobotPosition();

		for (Element element : root.elements())
		{
			switch (element.getName())
			{
				case DATE:
				case MODEL:
					//Not used
					break;
				case ROBOT_STATUS:
					robotPosition = getRobotPosition(element);
					break;
				case DEPTH:
					parseDepthElement(element);
					break;
				case BOUNDARY:
					addPointsToList(element.element(AREA), GridSquareEnum.BOUNDARY, squares, true, null);
					break;
				case ZONE:
					//Switch on zone type
					Attribute attr = element.attribute(STATE);
					if (attr != null)
					{
						switch (attr.getValue())
						{
							case NGZ:
								Element areaElement = element.element(AREA);
								if (areaElement != null)
								{
									addPointsToList(areaElement, GridSquareEnum.NGZ, squares, true, MappingController.getNextNgzId());
								}
								Element circleElement = element.element(CIRCLE);
								if (circleElement != null)
								{
									squares.addAll(getPolygonFromCircle(circleElement, GridSquareEnum.NGZ, MappingController.getNextNgzId()));
								}
								break;
							case EXPLORED:
								addPointsToList(element.element(AREA), GridSquareEnum.BLANK, squares, true, MappingController.getNextExploredId());
								break;
							case UNEXPLORED:
								addPointsToList(element.element(AREA), GridSquareEnum.UNKNOWN, squares, true, MappingController.getNextUnexploredId());
								break;
						}
					}
					break;
				case CONTAINER:
					addPointsToList(element, GridSquareEnum.EXTRACTION, squares, false, null);
					break;
				case OBSTACLE:
					addPointsToList(element, GridSquareEnum.OBSTACLE, squares, false, null);
					break;
				case FAULTLINE:
					addPointsToList(element, GridSquareEnum.FAULTLINE, squares, false, null);
					break;
			}
		}
		if (h >= height || w >= width)
		{
			throw new IllegalArgumentException("Map is larger than maximum supported size (" + width + " by " + height + " cm)");
		}

		MapDataModel model = new MapDataModel(width, height);
		model.importGridPoints(squares);
		model.setRobotPosition(robotPosition);
		return model;
	}

	private static RobotPosition getRobotPosition(Element element)
	{
		RobotPosition robotPosition = new RobotPosition();
		Element pointElement = element.element(POINT);
		try
		{
			int x = (int) Math.round(Double.parseDouble(pointElement.attribute("x").getValue()));
			int y = (int) Math.round(Double.parseDouble(pointElement.attribute("y").getValue()));
			int[] location = {x,y};
			robotPosition.setPosition(location);
			Element attr = element.element(ATTRIBUTE);
			Element key = attr.element("key");
			Element value = attr.element("value");
			if (key != null && value != null && key.getStringValue().equals(HEADING))
			{
				double heading = Double.parseDouble(value.getStringValue());
				robotPosition.setHeading(heading);
			}
			else
			{
				throw new IllegalArgumentException("Invalid robot heading attribute");
			}
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("Invalid robot heading attribute");
		}
		return robotPosition;
	}

	private static void parseDepthElement(Element element)
	{
		List <Element> attrs = element.elements(ATTRIBUTE);
		try
		{
			for (Element a : attrs)
			{
				Element key = a.element("key");
				Element value = a.element("value");
				ColourMap.defineDepth(value.getStringValue(), Integer.parseInt(key.getStringValue()));
			}
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("Invalid depth value defined in element: " + element.getName());
		}
	}
	
	/**
	 * Takes an element and returns a list of grid squares
	 * @param element
	 * @param type
	 * @param squares
	 */
	private static void addPointsToList(Element element, GridSquareEnum type, List<GridSquare> squares, boolean makeArea, Integer id) throws IllegalArgumentException
	{
		List<GridSquare> newSquares = new ArrayList<>();
		for (Element point : element.elements(POINT))
		{
			try
			{
				//Get point location
				int x = realToGrid(Double.parseDouble(point.attribute("x").getValue()));
				int y = realToGrid(Double.parseDouble(point.attribute("y").getValue()));
				//Update extents
				w = Math.max(x, w);
				h = Math.max(y, h);
				int[] location = {x, y};
				GridSquare square = new GridSquare(type);
				square.setLocation(location);
				if (id != null)
				{
					square.setProperty(id);
				}
				newSquares.add(square);
				//Extract depth for faultline
				if (type == GridSquareEnum.FAULTLINE)
				{
					Element attr = element.element(ATTRIBUTE);
					if (attr != null)
					{
						Element key = attr.element("key");
						Element val = attr.element("value");
						if (key != null && val != null)
						{
							square.setProperty(Integer.parseInt(val.getStringValue()));
						}
					}
				}
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException("Invalid point value in element:" + element.getName());
			}
		}
		newSquares = makeArea? createPolygon(newSquares):newSquares;
		if (type != GridSquareEnum.BOUNDARY)
		{
			newSquares = createPolygon(newSquares);
		}
		squares.addAll(newSquares);

	}

	/**
	 * Converts a map data model object to a DOM document
	 * @param mapDataModel
	 * @return
	 */
	private static Document mapToDocument(MapDataModel mapDataModel)
	{
		//Organise gridsquares by type
		Map<GridSquareEnum, List<GridSquare>> typeMap = getTypeMap(mapDataModel);
		 Document document = DocumentHelper.createDocument();
		 //Root element
        Element root = document.addElement(ROOT)
        		.addAttribute("units", "centimetres");
		Date date = new Date();
        //Info attributes
        addKeyValueAttribute(root, DATE, format.format(date));
        addKeyValueAttribute(root, MODEL, "Lego Mindstorm EV3");
        
        //Boundary area
        if (typeMap.containsKey(GridSquareEnum.BOUNDARY))
        {
        	Element boundary = root.addElement(BOUNDARY);
        	addAreaElement(boundary, typeMap.get(GridSquareEnum.BOUNDARY));
        }
        
        //Robot status
        Element robot = root.addElement(ROBOT_STATUS);
        RobotPosition robotPosition = mapDataModel.getRobotPosition();
        if (robotPosition.getPosition() != null)
        {
	        GridSquare robotSquare = new GridSquare(GridSquareEnum.ROBOT);
	        robotSquare.setLocation(robotPosition.getPosition());
	        addPointElement(robot, robotSquare, false);
	        addKeyValueAttribute(robot, HEADING, Double.toString(robotPosition.getHeading()));
        }
        
        //Container area/point
        if (typeMap.containsKey(GridSquareEnum.EXTRACTION))
        {
        	Element container = root.addElement(CONTAINER);
			typeMap.get(GridSquareEnum.EXTRACTION).forEach(c->addPointElement(container, c, true));
        }
        
        //Obstacle points
        if (typeMap.containsKey(GridSquareEnum.OBSTACLE))
        {
        	typeMap.get(GridSquareEnum.OBSTACLE).forEach(obs->
        	{
            	Element obstacle = root.addElement(OBSTACLE);
            	addPointElement(obstacle, obs, true);
        	});
        	
        }
        
        //Explored area
        if (typeMap.containsKey(GridSquareEnum.BLANK))
        {
        	Element explored = root.addElement(ZONE).addAttribute(STATE, EXPLORED);
        	addAreaElement(explored, typeMap.get(GridSquareEnum.BLANK));
        }
        
        //Unexplored area
   		if (typeMap.containsKey(GridSquareEnum.UNKNOWN))
        {
        	Element explored = root.addElement(ZONE).addAttribute(STATE, UNEXPLORED);
        	addAreaElement(explored, typeMap.get(GridSquareEnum.UNKNOWN));
        }
        
        //No-go
        if (typeMap.containsKey(GridSquareEnum.NGZ))
        {
			Map<Integer, List<GridSquare>> idMap = new HashMap<>();
			//Find individual ngz
			typeMap.get(GridSquareEnum.NGZ).forEach(t->
			{
				if (idMap.containsKey(t.getProperty()))
				{
					idMap.get(t.getProperty()).add(t);
				}
				else
				{
					List<GridSquare> newNgz = new ArrayList<>();
					newNgz.add(t);
					idMap.put(t.getProperty(), newNgz);
				}
			});
			//Add a zone for each NGZ
			for (Map.Entry<Integer, List<GridSquare>> entry : idMap.entrySet())
			{
				Element ngz = root.addElement(ZONE).addAttribute(STATE, NGZ);
				addAreaElement(ngz, entry.getValue());
			}
        }
        
        //Depth to colour
        Element depth = root.addElement(DEPTH);
        HashMap<Integer, Color> depthMap = ColourMap.getDepthMap();
        for (Map.Entry<Integer, Color> entry : depthMap.entrySet())
        {
			if (entry.getKey() > 0)
			{
				String key = Integer.toString(entry.getKey());
				String value = ColourMap.getHex(entry.getValue()).replace("#", "");
				//When importing, hex codes must be at least 3 long
				if (value.length() < 3)
				{
					value = "0" + value;
				}
				addKeyValueAttribute(depth, key, value);
			}
        }
        
        //Fault lines
        if (typeMap.containsKey(GridSquareEnum.FAULTLINE))
        {
        	typeMap.get(GridSquareEnum.FAULTLINE).forEach(flt->
        	{
            	Element faultline = root.addElement(FAULTLINE);
            	addPointElement(faultline, flt, true);
				addKeyValueAttribute(faultline, "depth", Integer.toString(flt.getProperty()));
        	});
        }
        
        return document;
	}
	
	private static void addKeyValueAttribute(Element element, String key, String value)
	{
		Element attr = element.addElement("attribute");
        Element keyElement = attr.addElement("key").addText(key);
        Element valElement = attr.addElement("value").addText(value);
	}
	
	private static void addAreaElement(Element element, List<GridSquare> squares)
	{
		Element area = element.addElement(AREA);
		List<GridSquare> polygon = getPolygonBounds(squares);
		for (GridSquare square : polygon)
		{
			addPointElement(area, square, true);
		}
	}

	private static List<GridSquare> getPolygonBounds(List<GridSquare> squares)
	{
		List<GridSquare> returnPoints = new ArrayList<>();
		if (squares.size() == 1)
		{
			return squares;
		}
		else if (squares.size() == 2)
		{
			getSquaresBetween(squares.get(0), squares.get(1));
		}
		else if (squares.size() > 2)
		{
			int minX = (int) MainWindowController.AREA_MAX_DIMENSION;
			int maxX = 0;
			int minY = (int) MainWindowController.AREA_MAX_DIMENSION;
			int maxY = 0;
			//Find the outermost limits of the polygon
			GridSquareEnum type = squares.get(0).getValue();
			for (GridSquare square : squares)
			{
				minX = Math.min(square.getLocation()[0], minX);
				maxX = Math.max(square.getLocation()[0], maxX);
				minY = Math.min(square.getLocation()[1], minY);
				maxY = Math.max(square.getLocation()[1], maxY);
			}
			int[][] locations = {{minX, minY}, {minX, maxY}, {maxX, maxY}, {maxX, minY}};
			for (int[] location : locations)
			{
				GridSquare square = new GridSquare(type);
				square.setLocation(location);
				returnPoints.add(square);
			}
		}

		return returnPoints;
	}

	/**
	 * Determines the colinearity of three points by determining if the area of the triangle they form is zero
	 * @param subject
	 * @param testSquares
	 * @return
	 */
	private static boolean checkColinear(GridSquare subject, Set<GridSquare> testSquares)
	{
		for (GridSquare test1 : testSquares)
		{
			for (GridSquare test2 : testSquares)
			{
				if (test1.getLocation() != test2.getLocation())
				{
					int a_x = test1.getLocation()[0];
					int a_y = test1.getLocation()[1];
					int b_x = test2.getLocation()[0];
					int b_y = test2.getLocation()[1];
					int c_x = subject.getLocation()[0];
					int c_y = subject.getLocation()[1];
					if (a_x * (b_y - c_y) + b_x * (c_y - a_y) + c_x * (a_y - b_y) == 0)
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns a polygon bounded by the provided points. Currently only supports rectangles
	 * @param squares
	 * @return
	 */
	private static List<GridSquare> createPolygon(List<GridSquare> squares)
	{
		//Use a set to avoid duplicates
		Set<GridSquare> squareSet = new HashSet<>();
		//Add original points
		squares.forEach(squareSet::add);
		int size = squares.size();
		for (int i = 0; i < size; i++)
		{
			GridSquare square = squares.get(i);
			for (int j = 0; j < size; j++)
			{
				if (i != j)
				{
					GridSquare otherSquare = squares.get(j);
					//If points are colinear parallel to an axis, get points in between
					if (square.getLocation()[0] == otherSquare.getLocation()[0] ||
							square.getLocation()[1] == otherSquare.getLocation()[1])
					{
						squareSet.addAll(getSquaresBetween(square, otherSquare));
					}
				}
			}
		}

		return new ArrayList<>(squareSet);
	}

	/**
	 * Takes a circle element and returns a list of grid squares which approximate a circle
	 * @param element
	 * @param type
	 * @return
	 * @throws IllegalArgumentException
	 */
	private static List<GridSquare> getPolygonFromCircle(Element element, GridSquareEnum type, Integer id) throws IllegalArgumentException
	{
		List<GridSquare> squares = new ArrayList<>();
		Attribute xAttr = element.attribute("x");
		Attribute yAttr = element.attribute("y");
		Attribute radiusAttr = element.attribute("radius");
		if (xAttr != null && yAttr != null && radiusAttr != null)
		{
			try
			{
				int x = realToGrid(Double.parseDouble(xAttr.getValue()));
				int y = realToGrid(Double.parseDouble(yAttr.getValue()));
				int r = realToGrid(Double.parseDouble(radiusAttr.getValue()));
				for (double i = -r; i <= r; i++)
				{
					for (double j = -r; j <= r; j++)
					{
						double radius = Math.sqrt(i*i+j*j);
						if (radius <= r)
						{
							int x_ = (int) (x+i);
							int y_ = (int) (y+j);
							int[] location = {x_, y_};
							GridSquare gridSquare = new GridSquare(type);
							gridSquare.setLocation(location);
							if (id != null)
							{
								gridSquare.setProperty(id);
							}
							squares.add(gridSquare);
						}
					}
				}
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException("Invalid circular area dimensions");
			}
		}
		else
		{
			throw new IllegalArgumentException("Invalid circular area");
		}
		return squares;
	}

	/**
	 * Gets grid squares between two points. Currently only supports rectangles
	 * @param start
	 * @param end
	 * @return
	 */
	public static List<GridSquare> getSquaresBetween(GridSquare start, GridSquare end)
	{
		List<GridSquare> squares = new ArrayList<>();
		//X values equal
		if (start.getLocation()[0] == end.getLocation()[0])
		{
			//Start and end y values
			int min = Math.min(start.getLocation()[1], end.getLocation()[1])+1;
			int max = Math.max(start.getLocation()[1], end.getLocation()[1] - 1);
			for (int i = min; i <= max; i++)
			{
				int[] location = {start.getLocation()[0], i};
				GridSquare gridSquare = new GridSquare(start.getValue());
				gridSquare.setLocation(location);
				gridSquare.setProperty(start.getProperty());
				squares.add(gridSquare);
			}
		}
		//Y values equal
		if (start.getLocation()[1] == end.getLocation()[1])
		{
			//Start and end y values
			int min = Math.min(start.getLocation()[0], end.getLocation()[0]) + 1;
			int max = Math.max(start.getLocation()[0], end.getLocation()[0] - 1);
			for (int i = min; i <= max; i++)
			{
				int[] location = {i, start.getLocation()[1]};
				GridSquare gridSquare = new GridSquare(start.getValue());
				gridSquare.setLocation(location);
				squares.add(gridSquare);
			}
		}
		return squares;
	}
	
	private static void addPointElement(Element element, GridSquare square, boolean scale)
	{
		int x = scale ? gridToReal(square.getLocation()[0]) : square.getLocation()[0];
		int	y = scale ? gridToReal(square.getLocation()[1]) : gridToReal(square.getLocation()[1]);

		element.addElement("point")
		.addAttribute("x", Integer.toString(x))
		.addAttribute("y", Integer.toString(y));
	}
	
	private static int gridToReal(int grid)
	{
		double val = grid*MappingController.GRID_SIZE;
		return (int) val;
	}
	
	private static int realToGrid(double real)
	{
		return (int) Math.round((real * scaleFactor) / MappingController.GRID_SIZE);
	}
	
	/**
	 * Organise grid squares by type
	 * @param mapDataModel
	 * @return
	 */
	private static Map<GridSquareEnum, List<GridSquare>> getTypeMap(MapDataModel mapDataModel)
	{
		//Organise gridsquares by type
		Map<GridSquareEnum, List<GridSquare>> typeMap = new HashMap<>();
		if (mapDataModel != null)
		{
			int w = mapDataModel.getWidth();
			int h = mapDataModel.getHeight();
			for (int i = 0; i < w; i++)
			{
				for (int j = 0; j < h; j++)
				{
					int[] location = {i,j};
					GridSquare square = mapDataModel.getGridSquare(location);
					if (typeMap.containsKey(square.getValue()))
					{
						typeMap.get(square.getValue()).add(square);
					}
					else
					{
						List<GridSquare> list = new ArrayList<>();
						list.add(square);
						typeMap.put(square.getValue(), list);
					}
				}
			}
		}
		return typeMap;

	}
	
	/**
	 * Saves a map data model object to a file
	 * @param mapDataModel The model object
	 * @param file The file to save to
	 * @throws IOException 
	 */
	public static void saveFile(MapDataModel mapDataModel, File file) throws IOException
	{
		Document doc = mapToDocument(mapDataModel);
		FileOutputStream fos = new FileOutputStream(file);
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(fos, format);
		writer.write(doc);
		writer.flush();
	}
}
