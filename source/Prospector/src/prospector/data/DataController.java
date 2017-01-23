/**
 * Prospector SFM 
 * SEP group PG04
 * Semester 2, 2016
 */
package prospector.data;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.dom4j.DocumentException;

import prospector.mapping.MapDataModel;
import prospector.mapping.MapListener;

public class DataController implements MapListener {
	private MapDataModel mapDataModel;
	
	private static final String BACKUP_FILE_NAME = "backup.xml";
	private static final String TEMP_BACKUP_FILE_NAME = "_backup.xml";
	private ScheduledExecutorService backupExecutor = null;
	
	public synchronized void writeFileOut(MapDataModel mapDataModel, File file) throws IOException
	{
		Parser.saveFile(mapDataModel, file);
	}
	
	public synchronized MapDataModel readFileIn(URL url, int h, int w) throws MalformedURLException, DocumentException, IllegalArgumentException
	{
		return Parser.parse(url,h, w);
	}
	
	@Override
	public void mapDataReceived(MapDataModel mapDataModel) {
		this.mapDataModel = mapDataModel;
		if (backupExecutor == null)
		{
			initBackups();
		}
	}
	
	private void initBackups()
	{
		backupExecutor = Executors.newSingleThreadScheduledExecutor();
		backupExecutor.scheduleAtFixedRate(()->
		{
			doBackup();
		}, 0, 60000, TimeUnit.MILLISECONDS);
	}
	
	
	public void shutdown()
	{
		if (backupExecutor != null)
		{
			backupExecutor.shutdown();
		}
	}
	
	private void doBackup()
	{
		//Backup files
		File currentBackup = new File(BACKUP_FILE_NAME);
		File tmpBackup = new File(TEMP_BACKUP_FILE_NAME);
		//Write to temp to avoid corrupting existing backup
		try {
			writeFileOut(mapDataModel, tmpBackup);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		//Delete the current backup and overwrite with the temp
		if (currentBackup.exists())
		{
			currentBackup.delete();
		}
		tmpBackup.renameTo(currentBackup);
	}
}
