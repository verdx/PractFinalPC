package parte2;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mensajes.MensType;
import mensajes.Mensaje;
import mensajes.MensajeCerrarConexion;
import mensajes.MensajeConexion;
import mensajes.MensajeListaArchivos;
import mensajes.MensajeListaUsuarios;
import mensajes.MensajePedirFichero;
import mensajes.MensajeSubirArchivos;

public class Cliente extends Thread {
	
	private Socket s;
	private ObjectOutputStream fout;
	private ObjectInputStream fin;
	private OyenteCliente os;
	
	private Scanner stdin;
	
	private String myhost;
	private String serverhost;
	private int port;
	
	private String username;
	private List<File> files;
	
	private Lock files_lock;
	
	public Cliente() {
		//Conseguimos nuestra IP
		conseguirIP();
		
		files = new ArrayList<File>();

		stdin = new Scanner(System.in);	

		//input_sem = new Semaphore(1);
		
		files_lock = new ReentrantLock();
	}
	
	public void run() {
		
		// Leemos el puerto e ip al que conectarnos
		System.out.print("Host: ");
		serverhost = stdin.nextLine();
		
		System.out.print("Port: ");
		port = Integer.parseInt(stdin.nextLine());
		
		introducirArchivos();
		
		// Creamos y activamos el socket y el stream de salida
		try {
			s = new Socket(serverhost, port);
			fout = new ObjectOutputStream(s.getOutputStream());
			fin = new ObjectInputStream(s.getInputStream());
		} catch (IOException e) {
			System.out.println("No se ha podido conectar con el servidor: " + e.getLocalizedMessage());
			return;
		}

		// Pedimos usuarios hasta que uno no este cogido
		registrarUsuario();
		
		// Creamos y lanzamos el thread de escucha oyente-servidor
		try {
			os = new OyenteCliente(fin, fout, username, port + 1, myhost, files, files_lock);
			os.start();
		} catch(Exception e) {
			System.out.println("Fallo de conexion con el servidor: " + e.getLocalizedMessage());
			return;
		}
		
		// Entramos en el menu y pedimos comandos hasta que se salga
		menu();
		
		return;
	}
	
	private void registrarUsuario() {
		boolean usuario_registrado = false;

		while(!usuario_registrado) {
			System.out.print("Username: ");
			username = stdin.nextLine();

			// Mandamos el mensaje de conexion establecida con la info del cliente
			List<String> filenames = new ArrayList<String>();
			files_lock.lock();
			for(File f: files) {
				filenames.add(f.getName());
			}
			files_lock.unlock();
			try {
				fout.writeObject(new MensajeConexion(username, filenames));
				fout.flush();
			} catch (IOException e) {
				System.out.println("Fallo al mandar mensaje de conexion: " + e.getLocalizedMessage());
			}

			// Recibimos la respuesta del servidor
			Mensaje m = null;
			try {
				m = (Mensaje) fin.readObject();
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Problema al recibir el mensaje :" + e.getLocalizedMessage());
			}
			if(m == null ) {
				System.out.println("No se ha recibido el mensaje de confirmacion de conexion, pruebe de nuevo");
			} else if (m.getTipo() == MensType.MENSAJE_USUARIO_COGIDO) {
				System.out.println("Ya hay otro usuario con ese nombre, prueba con otro.");
			} else {
				usuario_registrado = true;
				System.out.println("Usuario registrado.");
			}
		}
	}
	
	private void menu() {
		boolean exit = false;
		boolean prompt = true;
		System.out.println("Type help for info on the commands.");
		while(!exit) {
			if(prompt) System.out.print(username + "> ");
			String[] command = stdin.nextLine().split(" ");
			switch(command[0]) {
			case "help":
			case "h":
				help();
				prompt = true;
				break;
			case "users":
			case "u":
				users();
				prompt = false;
				break;
			case "files":
			case "f":
				files();
				prompt = false;
				break;
			case "download":
			case "d":
				if(command.length < 2) {
					System.out.println("Falta el argumento FILE");
				} else {
					download(command[1]);
				}
				prompt = false;
				break;
			case "exit":
			case "e":
				System.out.print("Are you sure you want to leave?[y/N]> ");
				String conf = stdin.nextLine();
				if(conf != "y" && conf != "Y") {
					exit = true;
					prompt = false;
					exit();
				} else {
					prompt = false;
				}
				break;
			case "upload":
			case "up":
				upload(command);
				prompt = false;
				break;
			default:
				System.out.println("Command not recognized. Type help or h for help.");
				prompt = true;
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
				+ "[up]load FILENAME: Download FILENAME from the system if given or ask for files to upload.\n"
				+ "[e]xit: Exit the system.");
		}
	
	private void users() {
		// Mandamos un mensaje pidiendo la lista de usuarios que se escribirá desde el Oyente-Servidor cuando le llegue.
		try {
			fout.writeObject(new MensajeListaUsuarios());
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar el mensaje: " + e.getLocalizedMessage());
		}
	}
	
	private void files() {
		// Mandamos un mensaje pidiendo la lista de archivos con sus respectivos usuarios
		// La respuesta se escribirá desde el Oyente-Servidor cuando llegue.
		try {
			fout.writeObject(new MensajeListaArchivos());
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar el mensaje: " + e.getLocalizedMessage());
		}	
	}
	
	private void download(String filename) {
		// Mandamos un mensaje comenzando el proceso de conexion para descargar 
		// un archivo de otro peer
		try {
			fout.writeObject(new MensajePedirFichero(filename, username));
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar el mensaje: " + e.getLocalizedMessage());
		}
	}
	
	private void upload(String[] command) {
		List<File> filesout;
		File aux;
		if(command.length == 1) {
			filesout = introducirArchivos();
		} else {
			filesout = new ArrayList<File>();
			for(int i = 1; i < command.length; i++) {
				aux = new File(command[i]);
				if(aux.canRead()) {
					filesout.add(aux);
				}
			}
		} 
		addFiles(filesout);
	}
	
	private void addFiles(List<File> filesin) {	
		List<String> in = new ArrayList<String>();
		
		files_lock.lock();
		for(File f: filesin) {
			files.add(f);
			in.add(f.getName());
		}
		files_lock.unlock();
		
		try {
			
			fout.writeObject(new MensajeSubirArchivos(in));
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar el mensaje: " + e.getLocalizedMessage());
		}
		
	}

	private void exit() {
		// Mandamos un mensaje para cerrar la sesión
		try {
			fout.writeObject(new MensajeCerrarConexion(username));
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar el mensaje: " + e.getLocalizedMessage());
		}
		try {
			os.join();
		} catch (InterruptedException e) {
			System.out.println("Problema al cerrar el oyente: " + e.getLocalizedMessage());
		}

		stdin.close();
		try {
			fout.close();
		} catch (IOException e) {
			System.out.println("Problema al cerrar el canal de salida: " + e.getLocalizedMessage());
		}
		try {
			fin.close();
		} catch (IOException e) {
			System.out.println("Problema al cerrar el canal de entrada: " + e.getLocalizedMessage());
		}
		try {
			s.close();
		} catch (IOException e) {
			System.out.println("Problema al cerrar el socket: " + e.getLocalizedMessage());
		}
	}


	private void conseguirIP() {
		try(final DatagramSocket socket = new DatagramSocket()){
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			myhost = socket.getLocalAddress().getHostAddress();
			socket.close();
		} catch (UnknownHostException | SocketException e) {
			System.out.println("Fallo al conseguir la IP: " + e.getLocalizedMessage());
		}
	}

	private List<File> introducirArchivos() {
		// Aparte de añadir los archivos a files devuelve sus nombres para añadirlos
		// por si la llamamos después de la inicialización
		List<File> filesout = new ArrayList<File>();
		System.out.println("Introduzca los archivos de uno en uno, termine con ENTER: ");
		String in;
		File aux;
		in = stdin.nextLine();
		while(!in.equals("")) {
			aux = new File(in);
			if(aux.canRead()) {
				filesout.add(aux);
			} else {
				System.out.println("No se ha podido encontrar el archivo: " + aux.getName());
			}
			in = stdin.nextLine();
		}
		return filesout;
	}

	
}
