package parte1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Cliente extends Thread {
	
	String fn;
	Socket s;
	String host;
	int port;
	int id;
	OutputStream fout;
	InputStream fin;
	DataOutputStream strout;
	
	public Cliente(String fn, String host, int port, int id) {
		this.fn = fn;
		this.host = host;
		this.port = port;
		this.id = id;
	}

	public Cliente(String fn, int port, int id) {
		this.fn = fn;
		this.host = "127.0.0.1";
		this.port = port;
		this.id = id;
	}
	
	public void run() {
		System.out.println("Iniciado el cliente " + id);
		
		//Creamos y activamos el socket
		try {
			s = new Socket(host, port);
		} catch (IOException e) {
			System.out.println("No se ha podido crear el Socket para el cliente " + id);
			e.printStackTrace();
			closeAll();
			return;
		}
		System.out.println("Conectado el socket del cliente " + id);
		
		// Creamos los canales de comunicacion con el cliente
		try {
			fout = s.getOutputStream();
			fin = s.getInputStream();
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al conseguir los streams de input y output del cliente " + id);
			e.printStackTrace();
			closeAll();
			return;
		}
		System.out.println("Se han creado los canales del cliente " + id);
		
		//Mandamos un string
		strout = new DataOutputStream(fout);
		System.out.println("Mandando filename: " + fn + " desde el cliente " + id);
		try {
			strout.writeUTF(fn);
			strout.flush();
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al mandar el filename del cliente " + id);
			e.printStackTrace();
			closeAll();
			return;
		}
		System.out.println("Filename " + fn + " mandado correctamente desde cliente " + id);
		
		//Creamos un stream para recibir el fichero
		ObjectInputStream objin;
		try {
			objin = new ObjectInputStream(fin);
		} catch (IOException e) {
			System.out.println("Fallo al crear el stream de objetos en el cliente " + id);
			e.printStackTrace();
			closeAll();
			return;
		}
		
		//Recibimos el fichero
		File recibido;
		try {
			recibido = (File) objin.readObject();
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("Fallo al recibir el objeto en el cliente " +  id);
			e.printStackTrace();
			closeAll();
			return;
		}
		
		//Leemos el fichero
		System.out.println("Fichero " + fn + " recibido correctamente en el cliente " + id);
		try (BufferedReader br = new BufferedReader(new FileReader(recibido))) {
			   String line;
			   while ((line = br.readLine()) != null) {
			       System.out.println(line);
			   }
		} catch (IOException e) {
			System.out.println("Fallo al leer el archivo " + fn);
			e.printStackTrace();
			closeAll();
			return;
		}
		
		//Cerramos todas las conexiones
		closeAll();
	}
	
	private void closeAll() {
		try {
			fout.close();
			fin.close();
	        s.close();
			strout.close();
		} catch (IOException e) {
			System.out.println("Fallo al cerrar los streams o el socket en el cliente " + id);
			e.printStackTrace();
		}
		
	}
}
