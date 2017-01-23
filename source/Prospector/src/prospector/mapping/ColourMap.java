/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.mapping;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;

/**
 * Maps LeJOS colours to javafx colours for drawing
 *
 */
public enum ColourMap {
	INSTANCE; //Enum to enforce singleton behaviour
	
	private HashMap<Integer, Color> map = new HashMap<>();
	
	private HashMap<Integer, Color> depthMap = new HashMap<>();
	
	private HashMap<Color, Integer> reverseDepthMap = new HashMap<>();
	
	private static final Color DEFAULT = Color.TRANSPARENT;
	
	ColourMap()
	{
		//Instantiate the map
		map.put(lejos.robotics.Color.BLACK, Color.BLACK);
		map.put(lejos.robotics.Color.BLUE, Color.BLUE);
		map.put(lejos.robotics.Color.BROWN, Color.BROWN);
		map.put(lejos.robotics.Color.CYAN, Color.CYAN);
		map.put(lejos.robotics.Color.DARK_GRAY, Color.DARKGRAY);
		map.put(lejos.robotics.Color.GRAY, Color.GRAY);
		map.put(lejos.robotics.Color.GREEN, Color.GREEN);
		map.put(lejos.robotics.Color.LIGHT_GRAY, Color.LIGHTGRAY);
		map.put(lejos.robotics.Color.MAGENTA, Color.MAGENTA);
		map.put(lejos.robotics.Color.NONE, Color.TRANSPARENT);
		map.put(lejos.robotics.Color.ORANGE, Color.ORANGE);
		map.put(lejos.robotics.Color.PINK, Color.PINK);
		map.put(lejos.robotics.Color.RED, Color.RED);
		map.put(lejos.robotics.Color.WHITE, Color.WHITE);
		map.put(lejos.robotics.Color.YELLOW, Color.YELLOW);
		
		//Unless told otherwise, assume default depth values
		depthMap.put(lejos.robotics.Color.BLACK, Color.BLACK);
		depthMap.put(lejos.robotics.Color.BLUE, Color.BLUE);
		depthMap.put(lejos.robotics.Color.BROWN, Color.BROWN);
		depthMap.put(lejos.robotics.Color.CYAN, Color.CYAN);
		depthMap.put(lejos.robotics.Color.DARK_GRAY, Color.DARKGRAY);
		depthMap.put(lejos.robotics.Color.GRAY, Color.GRAY);
		depthMap.put(lejos.robotics.Color.GREEN, Color.GREEN);
		depthMap.put(lejos.robotics.Color.LIGHT_GRAY, Color.LIGHTGRAY);
		depthMap.put(lejos.robotics.Color.MAGENTA, Color.MAGENTA);
		depthMap.put(lejos.robotics.Color.NONE, Color.TRANSPARENT);
		depthMap.put(lejos.robotics.Color.ORANGE, Color.ORANGE);
		depthMap.put(lejos.robotics.Color.PINK, Color.PINK);
		depthMap.put(lejos.robotics.Color.RED, Color.RED);
		depthMap.put(lejos.robotics.Color.WHITE, Color.WHITE);
		depthMap.put(lejos.robotics.Color.YELLOW, Color.YELLOW);
		
		populateReverseMap();
	}
	
	private void populateReverseMap()
	{
		for (Map.Entry<Integer, Color> entry : depthMap.entrySet())
		{
			reverseDepthMap.put(entry.getValue(), entry.getKey());
		}
	}
	
	public static Color getDrawColour(int lejosColour)
	{
		Color drawColour = INSTANCE.map.get(lejosColour);
		return drawColour != null ? drawColour : DEFAULT;
	}
	
	public static void defineDepth(String hex, int depth)
	{
		INSTANCE.depthMap.put(depth, Color.valueOf(hex));
		INSTANCE.reverseDepthMap.put(Color.valueOf(hex), depth);
	}
	
	public static Color getColorForDepth(int depth)
	{
		if (INSTANCE.depthMap.containsKey(depth))
		{
			return INSTANCE.depthMap.get(depth);
		}
		return null;
	}
	
	public static int getDepthForColor(Color color)
	{
		if (INSTANCE.reverseDepthMap.containsKey(color))
		{
			return INSTANCE.reverseDepthMap.get(color);
		}
		return -1;
	}
	
	public static HashMap<Integer, Color> getDepthMap()
	{
		return INSTANCE.depthMap;
	}
	
	public static String getHex(Color color)
	{
		return String.format( "#%02X%02X%02X", (int)( color.getRed() * 255 ),(int)( color.getGreen() * 255 ),(int)( color.getBlue() * 255 ) );
	}
}
