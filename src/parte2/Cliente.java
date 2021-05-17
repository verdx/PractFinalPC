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
import java.util.concurrent.Semaphore;

import mensajes.MensajeCerrarConexion;
import mensajes.MensajeConexion;
import mensajes.MensajeEmisorPreparadoCS;
import mensajes.MensajeListaArchivos;
import mensajes.MensajeListaUsuarios;
import mensajes.MensajePedirFichero;

public class Cliente extends Thread {
	
	Socket s;
	ObjectOutputStream fout;
	ThreadOyServidor os;
	
	Scanner stdin;
	
	String myhost;
	String serverhost;
	int port;
	int portout;
	
	String username;
	List<File> files = new ArrayList<>();
	
	Semaphore keyboard_sem;
	
	boolean exit;
	
	public Cliente(String[] filenames) {
		// Conseguimos nuestra IP
		conseguirIP();
		
		// Abrimos los archivos que nos pasen 
		initializeFiles(filenames);
	}
	
	public Cliente() {
		//Conseguimos nuestra IP
		conseguirIP();
		
	}
	
	private void conseguirIP() {
		try(final DatagramSocket socket = new DatagramSocket()){
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			myhost = socket.getLocalAddress().getHostAddress();
		} catch (UnknownHostException | SocketException e) {
			System.out.println("Fallo al conseguir la IP");
		}
	}
	
	private void initializeFiles(String[] filenames) {
		for(String s: filenames) {
			files.add(new File(s));
		}
	}
	
	private void pedirArchivos() {
		System.out.print("Introduzca una lista con los archivos que desee subir: ");
		stdin = new Scanner(System.in);
		String[] filenames = stdin.nextLine().split(" ");
		initializeFiles(filenames);
	}

	
	public void run() {
		
		// Leer el nombre del usuario y el puerto e ip al que conectarnos
		System.out.print("Username: ");
		stdin = new Scanner(System.in);
		username = stdin.nextLine();
		
		System.out.print("Host: ");
		stdin = new Scanner(System.in);
		serverhost = stdin.nextLine();
		
		System.out.print("Port: ");
		stdin = new Scanner(System.in);
		port = Integer.parseInt(stdin.nextLine());
		portout = port +1;
		
		pedirArchivos();
		
		// Creamos y activamos el socket y el stream de salida
		try {
			s = new Socket(serverhost, port);
			fout = new ObjectOutputStream(s.getOutputStream());
		} catch (IOException e) {
			System.out.println("No se ha podido conectar con el servidor.");
			e.printStackTrace();
			return;
		}
		
		// Creamos y lanzamos el thread de escucha oyente-servidor
		os = new ThreadOyServidor(s, username, this);
		os.start();
		
		// Mandamos el mensaje de conexion establecida con la info del cliente
		String[] filenames = new String[files.size()];
		for(File f: files) {
			filenames[files.indexOf(f)] = f.getName();
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
			System.out.print(username + "> ");
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
		return;
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
			fout.writeObject(new MensajeListaArchivos());
			fout.flush();
		} catch (IOException e) {
			System.out.println("No se ha podido mandar el mensaje, inténtelo de nuevo");
			e.printStackTrace();
		}
	}
	
	private void download(String filename) {
		// Mandamos un mensaje comenzando el proceso de conexion para descargar 
		// un archivo de otro peer
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
		System.out.print("Are you sure you want to leave?[y/N]> ");
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
			try {
				os.join();
			} catch (InterruptedException e) {
				System.out.println("Problema al cerrar el oyente");
				e.printStackTrace();
			}
		}
	}
	
	public void emitirArchivo(String filename, String user_receptor) {
		File file = getFile(filename);
		if(file == null) {
			System.out.println("El fichero no parece existir");
		};
		Emisor emisor = new Emisor(portout, file);
		emisor.start();
		// Mandamos un mensaje para decir que estamos preparados y dar nuestra ip y puerto
		try {
			fout.writeObject(new MensajeEmisorPreparadoCS(myhost, portout, user_receptor));
			portout += 1;
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al mandar mensaje para conectarse p2p");
			e.printStackTrace();
		}
		
	}
	
	private File getFile(String filename) {
		for(File f: files) {
			if(f.getName().equals(filename)) {
				return f;
			}
		}
		System.out.println("getFile no ha encontrado el archivo.");
		return null;
	}


	public void recibirArchivo(String myhost, int port) {
		Receptor receptor = new Receptor(myhost, port);
		receptor.start();
	}
	
	
}
