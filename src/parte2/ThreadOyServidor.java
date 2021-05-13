package parte2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import mensajes.MensType;
import mensajes.Mensaje;

public class ThreadOyServidor extends Thread {

	String username;
	
	Socket s;
	ObjectInputStream fin;
	
	public ThreadOyServidor(Socket s, String username) {
		this.s = s;
		this.username = username;
		
		// Creamos el canal de escucha para el servidor
		try {
			fin = new ObjectInputStream(s.getInputStream());
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al conseguir los streams de input y output del cliente " + username);
			e.printStackTrace();
			closeAll();
			return;
		}
		System.out.println("Se han creado los canales del cliente " + username);

	}

	public void run()  {
		
		while(true) {
			Mensaje m = null;
			try {
				m = (Mensaje) fin.readObject();
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Problema recibiendo el mensaje en el os del cliente " + username);
				e.printStackTrace();
			}
			
			switch(m.getTipo()) {
			
			}
		}
	}
	
	

	private void closeAll() {
		try {
			fin.close();
			s.close();
		} catch (IOException e) {
			System.out.println("Fallo al cerrar los streams o el socket en el cliente " + username);
			e.printStackTrace();
		}

	}

}
