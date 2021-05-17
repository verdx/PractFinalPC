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
import java.util.concurrent.Semaphore;

import mensajes.MensType;
import mensajes.Mensaje;
import mensajes.MensajeCerrarConexion;
import mensajes.MensajeConexion;
import mensajes.MensajeEmisorPreparadoCS;
import mensajes.MensajeListaArchivos;
import mensajes.MensajeListaUsuarios;
import mensajes.MensajePedirFichero;

public class Cliente extends Thread {
	
	private Socket s;
	private ObjectOutputStream fout;
	private ObjectInputStream fin;
	private OyenteCliente os;
	
	private Scanner stdin;
	
	private String myhost;
	private String serverhost;
	private int port;
	private int portout;
	
	private String username;
	private List<File> files = new ArrayList<>();
	
	private Semaphore input_sem;
	boolean exit;
	
	public Cliente(String[] filenames) {
		this();
		
		// Abrimos los archivos que nos pasen 
		initializeFiles(filenames);
	}
	
	public Cliente() {
		//Conseguimos nuestra IP
		conseguirIP();

		stdin = new Scanner(System.in);	

		input_sem = new Semaphore(1);
	}
	

	
	public void run() {
		
		// Leemos el puerto e ip al que conectarnos
		System.out.print("Host: ");
		serverhost = stdin.nextLine();
		
		System.out.print("Port: ");
		port = Integer.parseInt(stdin.nextLine());
		portout = port +1;
		
		pedirArchivos();
		
		// Creamos y activamos el socket y el stream de salida
		try {
			s = new Socket(serverhost, port);
			fout = new ObjectOutputStream(s.getOutputStream());
			fin = new ObjectInputStream(s.getInputStream());
		} catch (IOException e) {
			System.out.println("No se ha podido conectar con el servidor: " + e.getLocalizedMessage());
			return;
		}

		boolean usuario_registrado = false;

		while(!usuario_registrado) {
			System.out.print("Username: ");
			username = stdin.nextLine();

			// Mandamos el mensaje de conexion establecida con la info del cliente
			String[] filenames = new String[files.size()];
			for(File f: files) {
				filenames[files.indexOf(f)] = f.getName();
			}
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
		
		// Creamos y lanzamos el thread de escucha oyente-servidor
		try {
			os = new OyenteCliente(fin, username, this, input_sem);
			os.start();
		} catch(Exception e) {
			System.out.println("Fallo de conexion con el servidor: " + e.getLocalizedMessage());
			return;
		}
		
		
		
		
		// Entramos en el menu y pedimos comandos hasta que se salga
		exit = false;
		System.out.println("Type help for info on the commands.");
		while(!exit) {
			try {
				input_sem.acquire();
			} catch (InterruptedException e) {
				System.out.println("Fallo al coger el semaforo de input: " + e.getLocalizedMessage());
			}
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
			input_sem.release();
		}
		return;
	}
	
	private void help() {
		// Se imprime el mensaje de ayuda explicando los comandos
		try {
			input_sem.acquire();
		} catch (InterruptedException e) {
			System.out.println("Fallo al coger el semaforo de input: " + e.getLocalizedMessage());
		}
		System.out.println("[h]elp: This message.\n"
				+ "[u]sers: List the users in the system.\n"
				+ "[f]iles: List the available files in the system.\n"
				+ "[d]ownload FILENAME: Download FILENAME from the system.\n"
				+ "[e]xit: Exit the system.");
		input_sem.release();
	}
	
	private void users() {
		// Mandamos un mensaje pidiendo la lista de usuarios que se escribirá desde el Oyente-Servidor cuando le llegue.
		try {
			fout.writeObject(new MensajeListaUsuarios());
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar el mensaje: " + e.getLocalizedMessage());
		}
		try {
			input_sem.acquire();
		} catch (InterruptedException e) {
			System.out.println("Fallo al coger el semaforo de input: " + e.getLocalizedMessage());
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
		try {
			input_sem.acquire();
		} catch (InterruptedException e) {
			System.out.println("Fallo al coger el semaforo de input: " + e.getLocalizedMessage());
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
		try {
			input_sem.acquire();
		} catch (InterruptedException e) {
			System.out.println("Fallo al coger el semaforo de input: " + e.getLocalizedMessage());
		}
	}
	
	private void exit() {
		// We ask for confirmation
		System.out.print("Are you sure you want to leave?[y/N]> ");
		String conf = stdin.nextLine();
		input_sem.release();
		if(conf != "y" && conf != "Y") {
			exit = true;
			
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
	}
	
	public void emitirArchivo(String filename, String user_receptor) {
		File file = getFile(filename);
		if(file == null) {
			System.out.println("El fichero no parece existir");
		}
		
		boolean puerto_correcto = false;
		Emisor emisor = null;
		
		while(!puerto_correcto) {
			puerto_correcto = true;
			try {
				emisor = new Emisor(portout, file);
				emisor.start();
			} catch(Exception e) {
				puerto_correcto = false;
			}
			portout += 1;
		}
		
		// Mandamos un mensaje para decir que estamos preparados y dar nuestra ip y puerto
		try {
			fout.writeObject(new MensajeEmisorPreparadoCS(myhost, portout - 1, user_receptor));
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar el mensaje: " + e.getLocalizedMessage());
		}
		try {
			emisor.join();
		} catch (InterruptedException e) {
			System.out.println("Problema al cerrar el emisor: " + e.getLocalizedMessage());
		}
		
	}
	
	private File getFile(String filename) {
		for(File f: files) {
			if(f.getName().equals(filename)) {
				return f;
			}
		}
		System.out.println("No se ha encontrado el archivo.");
		return null;
	}


	public void recibirArchivo(String myhost, int port) {
		Receptor receptor = new Receptor(myhost, port);
		receptor.start();
		try {
			receptor.join();
		} catch (InterruptedException e) {
			System.out.println("Problema al cerrar el emisor: " + e.getLocalizedMessage());
		}
		System.out.println("Archivo recibido.");
		input_sem.release();
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
	
	private void initializeFiles(String[] filenames) {
		//Añadimos los archivos
		for(String s: filenames) {
			files.add(new File(s));
		}
		for(File f:files) {
			if(!f.canRead()) {
				System.out.println("No se ha podido encontrar el archivo: " + f.getName());
			}
		}
	}
	
	private void pedirArchivos() {
		System.out.println("Introduzca los archivos de uno en uno, termine con ENTER: ");
		String in;
		File aux;
		in = stdin.nextLine();
		while(!in.equals("")) {
			aux = new File(in);
			if(aux.canRead()) {
				files.add(aux);
			} else {
				System.out.println("No se ha podido encontrar el archivo: " + aux.getName());
			}
			in = stdin.nextLine();
		}
	}
	
	
}
