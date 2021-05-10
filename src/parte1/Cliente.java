package parte1;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;

public class Cliente extends Thread {
	
	String fn;
	Socket s;
	String host;
	int port;
	Writer writer;
	
	public Cliente(String fn, String host, int port) {
		this.fn = fn;
		this.host = host;
		this.port = port;
	}

	public Cliente(String fn, int port) {
		this.fn = fn;
		this.host = "127.0.0.1";
		this.port = port;
	}
	
	public void run() {
		System.out.println("Iniciado un cliente");
		
		//Creamos y activamos el socket
		try {
			s = new Socket(host, 1024);
		} catch (IOException e) {
			System.out.println("No se ha podido crear el Socket");
			e.printStackTrace();
		}
		
		// Creamos los canales de comunicacion con el cliente
		OutputStream fout;
		InputStream fin;
		try {
			fout = s.getOutputStream();
			fin = s.getInputStream();
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al conseguir los streams de input y output.");
			e.printStackTrace();
			return;
		}
		
		//Creamos un writer para mandar el string
		writer = new PrintWriter(fout);
		try {
			writer.write(fn);
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al mandar el string de filename");
			e.printStackTrace();
		}
		
		try {
			fout.close();
			fin.close();
	        s.close();
		} catch (IOException e) {
			System.out.println("Fallo al cerrar los streams o el socket");
			e.printStackTrace();
		}
		
		
	}
}
