package parte2;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;




public class BaseDeDatos {
	Map<String, String[]> files;
	Map<String, Pair<ObjectInputStream, ObjectOutputStream>> streams;
	
	public BaseDeDatos() {
		files = new HashMap<String, String[]>();
		streams = new HashMap<String, Pair<ObjectInputStream, ObjectOutputStream>>();
		
	}
	
	public void addUser(String username, String[] filenames, ObjectInputStream in, ObjectOutputStream out) {
		files.put(username, filenames);
		streams.put(username, new Pair(in, out));
	}
}
