package parte2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Servidor extends Thread {
	
	BaseDeDatos bd;
	ServerSocket listen;
	
	int port;
	
	
	public Servidor(BaseDeDatos bd) {
		this.bd = bd;
	}

	public void run() {
		Socket s;
		
		System.out.print("Puerto: ");
		Scanner stdin = new Scanner(System.in);
		port = Integer.parseInt(stdin.nextLine());
		stdin.close();
		try {
			listen = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al iniciar el servidor de escucha.");
			e.printStackTrace();
		}
		
		for(int i = 0; i < 10;i++) {
			System.out.println("Vuelta " + i + " del servidor");
			try {
				s = listen.accept();
			} catch (IOException e) {
				System.out.println("Ha habido algún fallo al empezar la escucha.");
				e.printStackTrace();
				break;
			}
			System.out.println("Comenzando thread oyente");
			ThreadOyCliente conexion = new ThreadOyCliente(s, bd);
			conexion.start();
		}
	}
}
