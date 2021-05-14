package parte2;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Servidor extends Thread {
	
	BaseDeDatos bd;
	ServerSocket listen;
	
	int port;
	String myip;
	
	
	public Servidor(BaseDeDatos bd) {
		this.bd = bd;
		
		// Conseguimos nuestra IP
		try(final DatagramSocket socket = new DatagramSocket()){
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			myip = socket.getLocalAddress().getHostAddress();
			System.out.println("IP: " + myip);
		} catch (UnknownHostException | SocketException e) {
			System.out.println("Fallo al conseguir la IP");
		}
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
			
			ThreadOyCliente conexion = new ThreadOyCliente(s, bd);
			conexion.start();
		}
	}
}
