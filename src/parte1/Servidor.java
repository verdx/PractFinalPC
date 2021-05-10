package parte1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor extends Thread {
	
	BaseDeDatos bd;
	ServerSocket listen;
	
	
	public Servidor(BaseDeDatos bd) {
		this.bd = bd;
		try {
			listen = new ServerSocket(500);
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al iniciar el servidor de escucha.");
			e.printStackTrace();
		}
	}

	public void run() {
		Socket s;
		while(true) {
			try {
				s = listen.accept();
			} catch (IOException e) {
				System.out.println("Ha habido algún fallo al empezar la escucha.");
				e.printStackTrace();
				break;
			}
			Thread conexion = new ThreadConexion(s, bd);
			conexion.run();
			
		}
	}
}
