/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.dom4j.DocumentException;
import org.junit.After;
import prospector.data.Parser;
import prospector.mapping.GridSquare;
import prospector.mapping.GridSquareEnum;
import prospector.mapping.MapDataModel;

import org.junit.Before;
import org.junit.Test;
import prospector.mapping.MappingController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ParserTest {

	private File file;
	@Before
	public void setUp() throws Exception {
		//Initialise static bits
		MappingController mappingController = new MappingController(10, 10);
	}

	@After
	public void tearDown() throws Exception {
		if (file != null)
		{
			file.delete();
		}
	}

	/**
	 * Creates a map data model and checks that its integrity is preserved when written to file and read back in
	 * @throws IOException
	 * @throws DocumentException
	 */
	@Test
	public void testModelIntegrity() throws IOException, DocumentException
	{
		file = new File("test.xml");
		//Initialise tha map data model in pseudo-random fashion
		MapDataModel model = new MapDataModel(10, 10);
		int g = 0;
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				int[] location = {i,j};
				GridSquare square = new GridSquare(GridSquareEnum.values()[g]);
				square.setProperty(i);
				model.setGridSquare(location, square);
				if (g >= GridSquareEnum.values().length)
				{
					g = 0;
				}
			}
		}
		//Save the file and check it exists
		Parser.saveFile(model, file);
		assertTrue(file.exists());
		//Read in the file
		MapDataModel newModel = Parser.parse(file.toURI().toURL(), 10, 10);
		//Compare the old and new models
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				int[] location = {i,j};
				GridSquare gridSquareNew = newModel.getGridSquare(location);
				GridSquare gridSquareOld = model.getGridSquare(location);
				//Same type
				assertEquals(gridSquareNew.getValue(), gridSquareOld.getValue());
				switch (gridSquareOld.getValue())
				{
					case FAULTLINE: //Check faultline depth is preserved
						assertEquals(gridSquareNew.getProperty(), gridSquareOld.getProperty());
						break;
				}

			}
		}
	}
}
