package parte2;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Receptor extends Thread {
	

	ObjectInputStream fin;
	Socket s;
	
	int port;
	String host;
	
	public Receptor(String host, int port) {
		this.host = host;
		this.port = port;
		
		
	}
	
	public void run() {
		
		// Conectamos el socket al emisor
		try {
			s = new Socket(host, port);
		} catch (IOException e) {
			System.out.println("Problema en el receptor al conectarse al emisor.");
			e.printStackTrace();
			return;
		}
		
		
		// Iniciamos el stream de entrada
		try {
			fin = new ObjectInputStream(s.getInputStream());
		} catch (IOException e) {
			System.out.println("Problema al crear el stream de entrada del receptor.");
			e.printStackTrace();
		}
		
		// Mandamos el archivo por el stream
		FileContents fileabs = null;
		try {
			fileabs = (FileContents) fin.readObject();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Problema en el receptor al recibir el archivo");
			e.printStackTrace();
		}
		
		// Guardamos el archivo
		Path path = Paths.get(System.getProperty("user.dir") + fileabs.getFilename());
		try {
			Files.write(path, fileabs.getContents());
		} catch (IOException e) {
			System.out.println("Problem writing the file");
			e.printStackTrace();
		}
	}

}
