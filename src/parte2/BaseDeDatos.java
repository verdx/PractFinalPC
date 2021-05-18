package parte2;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BaseDeDatos {
	// Diccionario de usuarios y sus archivos y su monitor
	Map<String, List<String>> files;
	MonitorRW monfiles;
	
	// Diccionario de usuarios y sus streams y su monitor
	Map<String, Pair<ObjectInputStream, ObjectOutputStream>> streams;
	MonitorRW monstreams;
	
	// Diccionario de archivos y sus dueños y su monitor
	Map<String, String> owners;
	MonitorRW monowners;
	
	public BaseDeDatos() {
		files = new HashMap<String, List<String>>();
		monfiles = new MonitorRW();
		streams = new HashMap<String, Pair<ObjectInputStream, ObjectOutputStream>>();
		monstreams = new MonitorRW();
		owners = new HashMap<String, String>();
		monowners = new MonitorRW();
		
	}
	
	public boolean addUser(String username, List<String> filenames, ObjectInputStream in, ObjectOutputStream out) {
		monfiles.request_read();
		boolean ya_existe = files.containsKey(username);
		monfiles.release_read();
		
		if(ya_existe) {
			return false;
		} else {

			// Añadimos la info a files
			monfiles.request_write();
			files.put(username, filenames);
			monfiles.release_write();

			// Añadimos la info a streams
			monstreams.request_write();
			streams.put(username, new Pair<>(in, out));
			monstreams.release_write();

			// Añadimos la info a owners
			monowners.request_write();
			for(String s: filenames) {
				owners.put(s, username);
			}
			monowners.release_write();

			System.out.println("Se ha añadido correctamente el usuario: " + username);
			
			return true;
		}
	}
	
	public int addFiles(String username, List<String> filenames) {
		monfiles.request_read();
		boolean existe = files.containsKey(username);
		monfiles.release_read();
		
		if(!existe) {
			return 0;
		} else {
			
			int annadidas = 0; 
			for(String s: filenames) {
				monfiles.request_write();
				List<String> aux = new ArrayList<String>();
				aux.addAll(filenames);
				aux.addAll(files.get(username));
				files.put(username,aux);
				monfiles.release_write();
				
				monowners.request_write();
				owners.put(s, username);
				monowners.release_write();
				annadidas++;
			}

			System.out.println("Se ha[n] añadido " + annadidas + " archivo[s] al usuario " + username);
			return annadidas;
		}
	}
	
	
	
	public void removeUser(String username) {
		monfiles.request_write();
		files.remove(username);
		monfiles.release_write();
		
		monstreams.request_write();
		streams.remove(username);
		monstreams.release_write();
		
		monowners.request_write();
		Iterator<Entry<String, String>> it = owners.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, String> item = it.next();
			if(item.getValue() == username)
				it.remove();
		}
		monowners.release_write();

		System.out.println("Se ha borrado al usuario: " + username);
	}
	
	public String[] getUsers() {
		monfiles.request_read();
		String[] users = new String[files.size()];
		int i = 0;
		for(String s: files.keySet()) {
			users[i] = s;
			i++;
		}
		monfiles.release_read();
		
		return users;
	}
	
	public ObjectOutputStream getUserFout(String user) {
		monstreams.request_read();
		ObjectOutputStream aux = streams.get(user).second();
		monstreams.release_read();
		return aux;
	}
	
	public String getOwner(String filename) {
		monowners.request_read();
		String aux = owners.get(filename);
		monowners.release_read();
		return aux;
	}
	
	
	public Map<String, List<String>> getFiles() {
		monfiles.request_read();
		Map<String, List<String>> aux = new HashMap<String, List<String>>(files);
		monfiles.release_read();
		return aux;
	}
	
	class MonitorRW {
		
		private int nr, nw;
		private final Lock l;
		private final Condition oktowrite, oktoread;
		
		public MonitorRW() {
			nr = 0;
			nw = 0;
			l = new ReentrantLock();
			oktowrite = l.newCondition();
			oktoread = l.newCondition();
		}
		
		protected void request_read() {
			l.lock();
			while(nw > 0) 
				try {
					oktoread.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			nr++;
			l.unlock();
		}
		
		protected void release_read() {
			l.lock();
			nr--;
			if(nr == 0) oktowrite.signal();
			l.unlock();
		}
		
		protected void request_write() {
			l.lock();
			while(nr > 0 || nw > 0)
				try {
					oktowrite.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			nw++;
			l.unlock();
		}
		
		protected void release_write() {
			l.lock();
			nw--;
			oktowrite.signal();
			oktoread.signalAll();
			l.unlock();
		}	
	}
}
