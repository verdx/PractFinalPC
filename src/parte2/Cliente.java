package parte2;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import mensajes.MensajeCerrarConexion;
import mensajes.MensajeConexion;
import mensajes.MensajeEmisorPreparadoCS;
import mensajes.MensajeEmitirFichero;
import mensajes.MensajeListaUsuarios;
import mensajes.MensajePedirFichero;

public class Cliente extends Thread {
	
	Socket s;
	ObjectOutputStream fout;
	ThreadOyServidor os;
	
	Scanner stdin;
	
	String host;
	int port;
	
	String username;
	List<File> files = new ArrayList<>();
	
	boolean exit;
	
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
		os = new ThreadOyServidor(s, username, this);
		os.start();
		
		// Mandamos el mensaje de conexion establecida con la info del cliente
		String[] filenames = new String[files.size()];
		for(File f: files) {
			filenames[files.indexOf(f)] = f.getPath();
		}
		try {
			fout.writeObject(new MensajeConexion(username, filenames));
			fout.flush();
		} catch (IOException e) {
			System.out.println("Fallo al mandar mensaje de conexion desde cliente " + username);
			e.printStackTrace();
		}
		
		// Entramos en el menu y pedimos comandos hasta que se salga
		exit = false;
		System.out.println("Type help for info on the commands.");
		while(!exit) {
			System.out.println(username + ">");
			String[] command = stdin.nextLine().split(" ");
			switch(command[0]) {
			case "help":
			case "h":
				help();
				break;
			case "users":
			case "u":
				users();
				break;
			case "files":
			case "f":
				files();
				break;
			case "download":
			case "d":
				download(command[1]);
				break;
			case "exit":
			case "e":
				exit();
				break;
			default:
				System.out.println("Command not recognized. Type help or h for help.");
			}
		}
	}
	
	private void help() {
		// Se imprime el mensaje de ayuda explicando los comandos
		System.out.println("[h]elp: This message.\n"
				+ "[u]sers: List the users in the system.\n"
				+ "[f]iles: List the available files in the system.\n"
				+ "[d]ownload FILENAME: Download FILENAME from the system.\n"
				+ "[e]xit: Exit the system.");
	}
	
	private void users() {
		// Mandamos un mensaje pidiendo la lista de usuarios que se escribirá desde el Oyente-Servidor cuando le llegue.
		try {
			fout.writeObject(new MensajeListaUsuarios());
			fout.flush();
		} catch (IOException e) {
			System.out.println("No se ha podido mandar el mensaje, inténtelo de nuevo");
			e.printStackTrace();
		}
	}
	
	private void files() {
		// Mandamos un mensaje pidiendo la lista de archivos con sus respectivos usuarios
		// La respuesta se escribirá desde el Oyente-Servidor cuando llegue.
		try {
			fout.writeObject(new MensajeListaUsuarios());
			fout.flush();
		} catch (IOException e) {
			System.out.println("No se ha podido mandar el mensaje, inténtelo de nuevo");
			e.printStackTrace();
		}
	}
	
	private void download(String filename) {
		try {
			fout.writeObject(new MensajePedirFichero(filename, username));
			fout.flush();
		} catch (IOException e) {
			System.out.println("No se ha podido mandar el mensaje, inténtelo de nuevo");
			e.printStackTrace();
		}
	}
	
	private void exit() {
		// We ask for confirmation
		System.out.println("Are you sure you want to leave?[y/N]\n>");
		String conf = stdin.nextLine();
		if(conf != "y" && conf != "Y") {
			exit = true;
			
			// Mandamos un mensaje para cerrar la sesión
			try {
				fout.writeObject(new MensajeCerrarConexion(username));
				fout.flush();
			} catch (IOException e) {
				System.out.println("No se ha podido cerrar la conexión, inténtelo de nuevo");
				e.printStackTrace();
			}
		}
	}
	
	public void emitirArchivo(String filename, String user_receptor) {
		File file = getFile(filename);
		Emisor emisor = new Emisor(port + 1, file);
		emisor.start();
		// Mandamos un mensaje para decir que estamos preparados y dar nuestra ip y puerto
		try {
			fout.writeObject(new MensajeEmisorPreparadoCS(host, port + 1, user_receptor));
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al mandar mensaje para conectarse p2p");
			e.printStackTrace();
		}
		
	}
	
	private File getFile(String filename) {
		for(File f: files) {
			if(f.getPath() == filename) {
				return f;
			}
		}
		return null;
	}


	public void recibirArchivo(String host, int port) {
		Receptor receptor = new Receptor(host, port);
		receptor.start();
	}
	
	
}
