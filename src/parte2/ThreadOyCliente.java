package parte2;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import mensajes.Mensaje;
import mensajes.MensajeConexion;
import mensajes.MensajeConfConexion;

public class ThreadOyCliente extends Thread {
	
	Socket s;
	BaseDeDatos bd;
	File file;
	ObjectInputStream fin;
	ObjectOutputStream fout;
	
	public ThreadOyCliente(Socket s, BaseDeDatos bd) {
		this.s = s;
		this.bd = bd;
		
		// Creamos los canales de comunicacion con el cliente
		try {
			fin = new ObjectInputStream(s.getInputStream());
			fout = new ObjectOutputStream(s.getOutputStream());
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al conseguir los streams de input y output.");
			e.printStackTrace();
			closeAll();
			return;
		}
	}

	
	public void run() {
		while(true) {
			Mensaje m = null;
			try {
				m = (Mensaje) fin.readObject();
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Problema recibiendo el mensaje en un oc");
				e.printStackTrace();
				closeAll();
			}
			
			switch(m.getTipo()) {
			case MENSAJE_CONEXION:
				bd.addUser(((MensajeConexion) m).getUsername(), ((MensajeConexion) m).getFiles(), fin, fout);
				try {
					fout.writeObject(new MensajeConfConexion());
				} catch (IOException e) {
					System.out.println("Fallo al envias mensaje de confirmacion de conexion");
					e.printStackTrace();
				}
				
				
			}
		}
        
	}
	
	private void closeAll() {
		try {
			fin.close();
	        s.close();
		} catch (IOException e) {
			System.out.println("Fallo al cerrar los streams o el socket");
			e.printStackTrace();
		}   
	}
}
