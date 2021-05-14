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
	
	public synchronized void addUser(String username, String[] filenames, ObjectInputStream in, ObjectOutputStream out) {
		files.put(username, filenames);
		for(String s: files.get(username)) {
			System.out.println("Añadiendo archivo: " + s);
		}
		streams.put(username, new Pair<>(in, out));
		for(String s: filenames) {
			owners.put(s, username);
		}
		System.out.println("Se ha añadido el usuario: " + username);
	}
	
	public synchronized void removeUser(String username) {
		files.remove(username);
		streams.remove(username);
		System.out.println("Se ha borrado al usuario: " + username);
	}
	
	public synchronized String[] getUsers() {
		System.out.println("Devolviendo lista de usuarios.");
		String[] users = new String[files.size()];
		int i = 0;
		for(String s: files.keySet()) {
			users[i] = s;
			i++;
		}
		
		return users;
	}
	
	public synchronized ObjectOutputStream getUserFout(String user) {
		return streams.get(user).second();
	}
	
	public synchronized String getOwner(String filename) {
		return owners.get(filename);
	}
	
	
	public synchronized Map<String, String[]> getFiles() {
		return new HashMap<String, String[]>(files);
	}

}
