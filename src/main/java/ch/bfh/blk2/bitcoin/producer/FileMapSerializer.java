package ch.bfh.blk2.bitcoin.producer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The fileMap is a map containing the .blk files and the height of the highest
 * block that is to be found inside this file. With this information it is much
 * quicker to to continue creating the list of blocks. The fileMap gets created
 * by the BlockSorter. This class can serialize it to a file, so that it can 
 * be used for the next run.
 * 
 * @author niklaus
 *
 */
public class FileMapSerializer {
	private static final Logger logger = LogManager.getLogger("FileMapSerializer");

	static void write(Map<String, Integer> fileMap) {
		File file = new File("fileMap.serial");

		try {
			if (file.exists())
				file.delete();
			file.createNewFile();

			OutputStream strem = new FileOutputStream(file);
			OutputStream bufstrem = new BufferedOutputStream(strem);
			ObjectOutput output = new ObjectOutputStream(bufstrem);

			output.writeObject(fileMap);

			output.close();
			bufstrem.close();
			strem.close();
		} catch (IOException e) {
			logger.warn("Unable to serialize the fileMap into a file", e);
		}
	}

	/**
	 * Reads the fileMap from it's serialized form on the disk.
	 * 
	 * @return the fileMap
	 */
	public static Map<String, Integer> read() {
		File file = new File("fileMap.serial");
		if (!file.exists())
			return null;

		InputStream strem;
		try {
			strem = new FileInputStream(file);
			InputStream bufstrem = new BufferedInputStream(strem);
			ObjectInput input = new ObjectInputStream(bufstrem);

			Map<String, Integer> fileMap = (Map<String, Integer>) input.readObject();

			return fileMap;
		} catch (IOException e) {
			logger.warn("Unable to deserialize the fileMap", e);
		} catch (ClassNotFoundException e) {
			logger.warn("Something went wrong when trying to deserialize the fileMap. Corrupt file?", e);
		}

		return null;
	}

}
