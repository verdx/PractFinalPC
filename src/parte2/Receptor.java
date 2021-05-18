package parte2;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.locks.Lock;

import mensajes.MensajeSubirArchivos;

public class Receptor extends Thread {
	

	ObjectInputStream fin;
	List<File> files;
	Socket s;
	
	int port;
	String host;
	
	Cliente cliente;
	
	public Receptor(String host, int port, Cliente cliente) {
		this.host = host;
		this.port = port;
		this.cliente = cliente;
	}
	
	public void run() {
		
		// Conectamos el socket al emisor
		try {
			s = new Socket(host, port);
		} catch (IOException e) {
			System.out.println("Problema en el receptor al conectarse al emisor: " + e.getLocalizedMessage());
			return;
		}
		
		
		// Iniciamos el stream de entrada
		try {
			fin = new ObjectInputStream(s.getInputStream());
		} catch (IOException e) {
			System.out.println("Problema al crear el stream de entrada del receptor: " + e.getLocalizedMessage());
		}
		
		// Recibimos el archivo por el stream
		FileContents fileabs = null;
		try {
			fileabs = (FileContents) fin.readObject();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Problema en el receptor al recibir el archivo: " + e.getLocalizedMessage());
		}
		
		// Guardamos el archivo
		Path path = Paths.get(System.getProperty("user.dir") + "/" + fileabs.getFilename());
		try {
			Files.write(path, fileabs.getContents());
		} catch (IOException e) {
			System.out.println("Problema escribiendo el archivo: " + e.getLocalizedMessage());
		}
		
		cliente.addFile(path.toFile());
		return;
	}

}
