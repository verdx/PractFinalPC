package parte2;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;




public class BaseDeDatos {
	Map<String, String[]> files;
	Map<String, Pair<ObjectInputStream, ObjectOutputStream>> streams;
	Map<String, String> owners;
	
	public BaseDeDatos() {
		files = new HashMap<String, String[]>();
		streams = new HashMap<String, Pair<ObjectInputStream, ObjectOutputStream>>();
		owners = new HashMap<String, String>();
		
	}
	
	public void addUser(String username, String[] filenames, ObjectInputStream in, ObjectOutputStream out) {
		files.put(username, filenames);
		streams.put(username, new Pair<>(in, out));
		for(String s: filenames) {
			owners.put(s, username);
		}
	}
	
	public void removeUser(String username) {
		files.remove(username);
		streams.remove(username);
	}
	
	public String[] getUsers() {
		String[] users = (String[]) files.keySet().toArray();
		return users;
	}
	
	public ObjectOutputStream getUserFout(String user) {
		return streams.get(user).second();
	}
	
	public String getOwner(String filename) {
		return owners.get(filename);
	}
	
	
	public Map<String, String[]> getFiles() {
		return files;
	}

}
