package parte2;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Emisor extends Thread {
	
	ServerSocket server;
	ObjectOutputStream fout;
	Socket s;
	
	int port;
	
	File file;
	
	public Emisor(int port, File file) throws IOException {
		this.file = file;
		this.port = port;
		
		// Creamos el servidor en el puerto dado
		server = new ServerSocket(port);
	}
	
	public void run() {
		
		// Iniciamos la conexion con el receptor
		try {
			s = server.accept();
		} catch (IOException e) {
			System.out.println("Problema al recibir el socket en el emisor");
			e.printStackTrace();
		}
		
		// Iniciamos el stream de salida
		try {
			fout = new ObjectOutputStream(s.getOutputStream());
		} catch (IOException e) {
			System.out.println("Problema al crear el stream de salida del emisor");
			e.printStackTrace();
		}
		
		//Sacamos los contenidos del archivo
		FileContents fileabs = new FileContents(file);
		
		// Mandamos el archivo por el stream
		try {
			fout.writeObject(fileabs);
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al mandar el archivo por el stream de salida del emisor");
			e.printStackTrace();
		}
		return;
	}

}
