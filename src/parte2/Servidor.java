package parte2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor extends Thread {
	
	BaseDeDatos bd;
	ServerSocket listen;
	
	
	public Servidor(BaseDeDatos bd, int port) {
		this.bd = bd;
		try {
			listen = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al iniciar el servidor de escucha.");
			e.printStackTrace();
		}
	}

	public void run() {
		Socket s;
		for(int i = 0; i < 10;i++) {
			System.out.println("Vuelta " + i + " del servidor");
			try {
				s = listen.accept();
			} catch (IOException e) {
				System.out.println("Ha habido algún fallo al empezar la escucha.");
				e.printStackTrace();
				break;
			}
			ThreadOyCliente conexion = new ThreadOyCliente(s, bd);
			conexion.start();
		}
	}
}
