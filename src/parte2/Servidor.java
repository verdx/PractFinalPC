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
			System.out.println("Fallo al conseguir la IP: " + e.getLocalizedMessage());
		}
	}

	public void run() {
		Socket s;
		
		// Pedimos el puerto
		System.out.print("Puerto: ");
		Scanner stdin = new Scanner(System.in);
		boolean puertocorr = false;
		while(!puertocorr) try {
			port = Integer.parseInt(stdin.nextLine());
			puertocorr = true;
		} catch (Exception e) {
			System.out.println("Puerto incorrecto, pruebe de nuevo");
		}
		stdin.close();
		

		// Abrimos el socket servidor
		try {
			listen = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Problema al iniciar el socket de escucha del servidor: " + e.getLocalizedMessage());
		}
		
		// Iniciamos el bucle de espera para clientes
		while(true) {
			System.out.println("Esperando a un cliente");
			try {
				s = listen.accept();
				System.out.println("Ha llegado un cliente.");
			} catch (IOException e) {
				System.out.println("Problema al empezar la escucha: " + e.getLocalizedMessage());
				break;
			}
			
			OyenteServidor conexion = new OyenteServidor(s, bd);
			conexion.start();
		}
	}
}
