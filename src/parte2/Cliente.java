package parte2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import mensajes.MensType;
import mensajes.MensajeConexion;

public class Cliente extends Thread {
	
	Socket s;
	ObjectOutputStream fout;
	ThreadOyServidor os;
	
	Scanner stdin;
	
	String host;
	int port;
	
	String username;
	List<File> files = new ArrayList<>();
	 
	
	
	
	public Cliente(String host, int port) {
		try(final DatagramSocket socket = new DatagramSocket()){
		  socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
		  host = socket.getLocalAddress().getHostAddress();
		} catch (UnknownHostException | SocketException e) {
			System.out.println("Fallo al conseguir la IP");
		}

	}

	
	public void run() {
		
		// Leer el nombre del usuario
		stdin = new Scanner(System.in);
		System.out.print("Username: ");
		username = stdin.nextLine();
		
		System.out.println("Iniciado el cliente " + username);
		
		// Creamos y activamos el socket y el stream de salida
		try {
			s = new Socket(host, port);
			fout = new ObjectOutputStream(s.getOutputStream());
		} catch (IOException e) {
			System.out.println("No se ha podido crear el Socket para el cliente " + username);
			e.printStackTrace();
			return;
		}
		System.out.println("Conectado el socket del cliente " + username);
		
		// Creamos y lanzamos el thread de escucha oyente-servidor
		os = new ThreadOyServidor(s, username);
		os.start();
		
		// Mandamos el mensaje de conexion establecida
		String[] filenames = new String[files.size()];
		for(File f: files) {
			filenames[files.indexOf(f)] = f.getPath();
		}
		try {
			fout.writeObject(new MensajeConexion(username, filenames));
		} catch (IOException e) {
			System.out.println("Fallo al mandar mensaje de conexion desde cliente " + username);
			e.printStackTrace();
		}
		
		
	}
	
	
}
