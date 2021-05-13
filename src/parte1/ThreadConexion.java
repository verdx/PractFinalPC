package parte1;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ThreadConexion extends Thread {
	
	Socket s;
	BaseDeDatos bd;
	File file;
	OutputStream fout;
	InputStream fin;
	DataInputStream strin;
	
	public ThreadConexion(Socket s, BaseDeDatos bd) {
		this.s = s;
		this.bd = bd;
	}

	
	public void run() {
		System.out.println("Iniciado un thread de escucha");
		
		// Creamos los canales de comunicacion con el cliente
		try {
			fout = s.getOutputStream();
			fin = s.getInputStream();
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al conseguir los streams de input y output.");
			e.printStackTrace();
			closeAll();
			return;
		}
		
		// Recibimos string 
		strin = new DataInputStream(fin);
		String fn;
		try {
			fn = strin.readUTF();
		} catch (IOException e1) {
			System.out.println("Problemas al recibir el filename");
			e1.printStackTrace();
			closeAll();
			return;
		}
		System.out.println("Se ha recibido el string " + fn);
		
		//Creamos un stream para mandar el fichero
		ObjectOutputStream objout;
		try {
			objout = new ObjectOutputStream(fout);
		} catch (IOException e) {
			System.out.println("Fallo al crear el stream de objetos");
			e.printStackTrace();
			closeAll();
			return;
		}
		
		//Mandamos el archivo
		try {
			objout.writeObject(bd.readFile(fn));
			objout.flush();
		} catch (IOException e) {
			System.out.println("Fallo al mandar el fichero por el stream de objetos");
			e.printStackTrace();
			closeAll();
			return;
		}

		closeAll();
        
	}
	
	private void closeAll() {
		try {
			fout.close();
			fin.close();
	        s.close();
	        strin.close();
		} catch (IOException e) {
			System.out.println("Fallo al cerrar los streams o el socket");
			e.printStackTrace();
		}   
	}
}
