package parte1;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BaseDeDatos {
	Map<String, File> files;
	int MAX = 5;
	
	public BaseDeDatos(String[] fn) {
		files = new HashMap<String, File>();
		for(String str: fn) {
			files.put(str, new File(str));
		}
		
	}
	
	public synchronized File readFile(String filename) {
		if(files.containsKey(filename)) return files.get(filename);
		else {
			System.out.println("No se ha encontrado ningun archivo con ese nombre");
			return null;
		}
	}
}
